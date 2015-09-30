package org.fenixedu.messaging.emaildispatch.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.domain.Message;
import org.fenixedu.messaging.domain.Sender;
import org.fenixedu.messaging.emaildispatch.EmailDispatchConfiguration;
import org.fenixedu.messaging.emaildispatch.EmailDispatchConfiguration.ConfigurationProperties;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
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

    protected MimeMessageHandler(Locale locale, List<String> tos, List<String> ccs, List<String> bccs) {
        super();
        setLocale(locale);
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
        Sender sender = getReport().getMessage().getSender();
        String name = sender.getFromName();
        String from = sender.getFromAddress();
        return Strings.isNullOrEmpty(name) ? from : name.replace(',', ' ').trim() + " <" + from + ">";
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
        Message message = getReport().getMessage();
        Locale locale = getLocale();
        MimeMessage mimeMessage = new MimeMessage(session()) {
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
                setHeader("Date", message.getCreated().toString());
            }
        };

        InternetAddress fromAddr = new InternetAddress(getFrom());
        mimeMessage.setFrom(fromAddr);
        mimeMessage.setSubject(getContent(message.getSubject(), locale));

        List<Address> replyTos = new ArrayList<>();
        for (String replyTo : message.getReplyToAddresses()) {
            replyTos.add(new InternetAddress(replyTo));
        }
        mimeMessage.setReplyTo(replyTos.toArray(new Address[0]));

        final MimeMultipart mimeMultipart = new MimeMultipart();

        final String htmlBody = getContent(message.getHtmlBody(), locale);
        if (htmlBody != null && !htmlBody.trim().isEmpty()) {
            final BodyPart bodyPart = new MimeBodyPart();
//            bodyPart.setContent(htmlBody, "text/html; charset='utf-8'");
            bodyPart.setContent(htmlBody, "text/html");
            mimeMultipart.addBodyPart(bodyPart);
        }

        final String body = getContent(message.getBody(), locale);
        if (body != null && !body.trim().isEmpty()) {
            final BodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(body);
            mimeMultipart.addBodyPart(bodyPart);
        }

        mimeMessage.setContent(mimeMultipart);

        for (String email : getTos()) {
            mimeMessage.addRecipient(RecipientType.TO, new InternetAddress(email));
        }
        for (String email : getCcs()) {
            mimeMessage.addRecipient(RecipientType.CC, new InternetAddress(email));
        }
        for (String email : getBccs()) {
            mimeMessage.addRecipient(RecipientType.BCC, new InternetAddress(email));
        }
        return mimeMessage;
    }

    private String getContent(LocalizedString ls, Locale l) {
        if (ls != null) {
            String s = ls.getContent(l);
            if (s == null) {
                return ls.getContent();
            }
            return s;
        }
        return null;
    }

    public static Set<MimeMessageHandler> create(Map<Locale, Set<String>> tos, Map<Locale, Set<String>> ccs,
            Map<Locale, Set<String>> bccs) {
        Set<MimeMessageHandler> handlers = new HashSet<>();
        for (Map.Entry<Locale, Set<String>> entry : tos.entrySet()) {
            for (List<String> toChunk : Lists.partition(new ArrayList<String>(entry.getValue()), MAX_RECIPIENTS)) {
                handlers.add(new MimeMessageHandler(entry.getKey(), toChunk, null, null));
            }
        }
        for (Map.Entry<Locale, Set<String>> entry : ccs.entrySet()) {
            for (List<String> ccChunk : Lists.partition(new ArrayList<String>(entry.getValue()), MAX_RECIPIENTS)) {
                handlers.add(new MimeMessageHandler(entry.getKey(), null, ccChunk, null));
            }
        }
        for (Map.Entry<Locale, Set<String>> entry : bccs.entrySet()) {
            for (List<String> bccChunk : Lists.partition(new ArrayList<String>(entry.getValue()), MAX_RECIPIENTS)) {
                handlers.add(new MimeMessageHandler(entry.getKey(), null, null, bccChunk));
            }
        }
        return handlers;
    }

    public static Set<MimeMessageHandler> create(Locale locale, List<String> tos, List<String> ccs, List<String> bccs) {
        Set<MimeMessageHandler> handlers = new HashSet<>();
        for (List<String> toChunk : Lists.partition(tos, MAX_RECIPIENTS)) {
            handlers.add(new MimeMessageHandler(locale, toChunk, null, null));
        }
        for (List<String> ccChunk : Lists.partition(ccs, MAX_RECIPIENTS)) {
            handlers.add(new MimeMessageHandler(locale, null, ccChunk, null));
        }
        for (List<String> bccChunk : Lists.partition(bccs, MAX_RECIPIENTS)) {
            handlers.add(new MimeMessageHandler(locale, null, null, bccChunk));
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
        for (MimeMessageHandler handler : MimeMessageHandler.create(getLocale(), tos, ccs, bccs)) {
            getReport().addHandler(handler);
        }
    }

    public void delete() {
        setReport(null);
        super.deleteDomainObject();
    }
}
