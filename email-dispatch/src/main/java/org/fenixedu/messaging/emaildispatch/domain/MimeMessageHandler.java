package org.fenixedu.messaging.emaildispatch.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.messaging.emaildispatch.EmailDispatchConfiguration;
import org.fenixedu.messaging.emaildispatch.EmailDispatchConfiguration.ConfigurationProperties;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public final class MimeMessageHandler extends MimeMessageHandler_Base {
    private static final int MAX_RECIPIENTS = EmailDispatchConfiguration.getConfiguration().mailSenderMaxRecipients();

    private static Session SESSION = null;

    private static synchronized Session session() {
        final Properties properties = new Properties();
        ConfigurationProperties conf = EmailDispatchConfiguration.getConfiguration();
        properties.put("mail.smtp.host", conf.mailSmtpHost());
        properties.put("mail.smtp.name", conf.mailSmtpName());
        properties.put("mail.smtp.port", conf.mailSmtpPort());
        properties.put("mailSender.max.recipients", conf.mailSenderMaxRecipients());
        properties.put("mailingList.host.name", conf.mailingListHostName());
        properties.put("mail.debug", "true");
        SESSION = Session.getDefaultInstance(properties, null);
        return SESSION;
    }

    protected MimeMessageHandler(List<String> tos, List<String> ccs, List<String> bccs) {
        super();
        if (tos != null) {
            setToAddresses(Joiner.on(", ").join(tos));
        }
        if (ccs != null) {
            setCcAddresses(Joiner.on(", ").join(ccs));
        }
        if (bccs != null) {
            setBccAddresses(Joiner.on(", ").join(bccs));
        }
    }

    public String getFrom() {
        String name = getReport().getMessage().getSender().getFromName();
        String from = getReport().getMessage().getSender().getFromAddress();
        return StringUtils.isNotEmpty(name) ? name.replace(',', ' ').trim() + " <" + from + ">" : from;
    }

    public Set<String> getTos() {
        return getToAddresses() != null ? Sets.newHashSet(getToAddresses().trim().split("\\s*,\\s*")) : Collections.emptySet();
    }

    public Set<String> getCcs() {
        return getCcAddresses() != null ? Sets.newHashSet(getCcAddresses().trim().split("\\s*,\\s*")) : Collections.emptySet();
    }

    public Set<String> getBccs() {
        return getBccAddresses() != null ? Sets.newHashSet(getBccAddresses().trim().split("\\s*,\\s*")) : Collections.emptySet();
    }

    public MimeMessage mimeMessage() throws AddressException, MessagingException {
        MimeMessage message = new MimeMessage(session()) {
            private String fenixMessageId = null;

            @Override
            public String getMessageID() throws MessagingException {
                if (fenixMessageId == null) {
                    fenixMessageId = getExternalId() + "." + new DateTime().getMillis() + "@fenix";
                }
                return fenixMessageId;
            }

            @Override
            protected void updateMessageID() throws MessagingException {
                setHeader("Message-ID", getMessageID());
            }
        };
        message.setFrom(getFrom());
        message.setSubject(getReport().getMessage().getSubject());

        List<Address> replyTos = new ArrayList<>();
        for (String replyTo : getReport().getMessage().getReplyToAddresses()) {
            replyTos.add(new InternetAddress(replyTo));
        }
        message.setReplyTo(replyTos.toArray(new Address[0]));

        final MimeMultipart mimeMultipart = new MimeMultipart();

        final String htmlBody = getReport().getMessage().getBody();
        if (htmlBody != null && !htmlBody.trim().isEmpty()) {
            final BodyPart bodyPart = new MimeBodyPart();
//            bodyPart.setContent(htmlBody, "text/html; charset='utf-8'");
            bodyPart.setContent(htmlBody, "text/html");
            mimeMultipart.addBodyPart(bodyPart);
        }

        final String body = getReport().getMessage().getBody();
        if (body != null && !body.trim().isEmpty()) {
            final BodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(body);
            mimeMultipart.addBodyPart(bodyPart);
        }

        message.setContent(mimeMultipart);

        for (String email : getTos()) {
            message.addRecipient(RecipientType.TO, new InternetAddress(email));
        }
        for (String email : getCcs()) {
            message.addRecipient(RecipientType.CC, new InternetAddress(email));
        }
        for (String email : getBccs()) {
            message.addRecipient(RecipientType.BCC, new InternetAddress(email));
        }
        return message;
    }

    public static Set<MimeMessageHandler> create(List<String> tos, List<String> ccs, List<String> bccs) {
        Set<MimeMessageHandler> handlers = new HashSet<>();
        for (List<String> toChunk : Lists.partition(tos, MAX_RECIPIENTS)) {
            handlers.add(new MimeMessageHandler(toChunk, null, null));
        }
        for (List<String> ccChunk : Lists.partition(ccs, MAX_RECIPIENTS)) {
            handlers.add(new MimeMessageHandler(null, ccChunk, null));
        }
        for (List<String> bccChunk : Lists.partition(bccs, MAX_RECIPIENTS)) {
            handlers.add(new MimeMessageHandler(null, null, bccChunk));
        }
        return handlers;
    }

    @Atomic(mode = TxMode.WRITE)
    public void deliver() throws MessagingException {
        try {
            MimeMessage message = mimeMessage();
            Transport.send(message);
            getReport().setDeliveredCount(getReport().getDeliveredCount() + message.getAllRecipients().length);
        } catch (SendFailedException e) {
            if (e.getValidSentAddresses() != null) {
                getReport().setDeliveredCount(getReport().getDeliveredCount() + e.getValidSentAddresses().length);
            }
            if (e.getInvalidAddresses() != null) {
                getReport().setFailedCount(getReport().getFailedCount() + e.getInvalidAddresses().length);
                for (Address failed : e.getInvalidAddresses()) {
                    EmailBlacklist.getInstance().addFailedAddress(failed.toString());
                }
            }
            if (e.getValidUnsentAddresses() != null) {
                resend(e.getValidUnsentAddresses());
            }
        }
        delete();
    }

    private void resend(Address[] validUnsentAddresses) {
        Set<String> currentTos = getTos();
        Set<String> currentCcs = getCcs();
        Set<String> currentBccs = getBccs();
        List<String> tos = new ArrayList<>();
        List<String> ccs = new ArrayList<>();
        List<String> bccs = new ArrayList<>();
        for (Address email : validUnsentAddresses) {
            if (currentTos.contains(email.toString())) {
                tos.add(email.toString());
            }
            if (currentCcs.contains(email.toString())) {
                ccs.add(email.toString());
            }
            if (currentBccs.contains(email.toString())) {
                bccs.add(email.toString());
            }
        }
        for (MimeMessageHandler handler : MimeMessageHandler.create(tos, ccs, bccs)) {
            getReport().addHandler(handler);
        }
    }

    public void delete() {
        setReport(null);
        super.deleteDomainObject();
    }
}
