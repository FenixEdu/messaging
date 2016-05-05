package org.fenixedu.messaging.emaildispatch.domain;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.domain.MessagingSystem;
import org.fenixedu.messaging.core.domain.Sender;
import org.fenixedu.messaging.emaildispatch.EmailDispatchConfiguration;
import org.fenixedu.messaging.emaildispatch.EmailDispatchConfiguration.ConfigurationProperties;
import org.joda.time.DateTime;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

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

    protected MimeMessageHandler(Locale locale, Collection<String> tos, Collection<String> ccs, Collection<String> bccs) {
        super();
        setLocale(locale);
        if (tos != null) {
            setToAddresses(MessagingSystem.Util.toEmailListString(tos));
        }
        if (ccs != null) {
            setCcAddresses(MessagingSystem.Util.toEmailListString(ccs));
        }
        if (bccs != null) {
            setBccAddresses(MessagingSystem.Util.toEmailListString(bccs));
        }
    }

    protected String getFrom() {
        Sender sender = getReport().getMessage().getSender();
        String name = sender.getName();
        String from = sender.getAddress();
        return Strings.isNullOrEmpty(name) ? from : name.replace(',', ' ').trim() + " <" + from + ">";
    }

    protected MimeMessage mimeMessage() throws AddressException, MessagingException {
        Message message = getReport().getMessage();
        Locale locale = getLocale();
        String[] languages = { locale.toLanguageTag() };
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

        mimeMessage.setContentLanguage(languages);

        mimeMessage.setSubject(getContent(message.getSubject(), locale));

        String replyTo = message.getReplyTo();
        if (!Strings.isNullOrEmpty(replyTo)) {
            Address[] replyTos = { new InternetAddress(replyTo) };
            mimeMessage.setReplyTo(replyTos);
        }

        final MimeMultipart mimeMultipart = new MimeMultipart();

        final String htmlBody = getContent(message.getHtmlBody(), locale);
        if (htmlBody != null && !htmlBody.trim().isEmpty()) {
            final BodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(htmlBody, "text/html");
            mimeMultipart.addBodyPart(bodyPart);
        }

        final String textBody = getContent(message.getTextBody(), locale);
        if (textBody != null && !textBody.trim().isEmpty()) {
            final BodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(textBody);
            mimeMultipart.addBodyPart(bodyPart);
        }

        mimeMessage.setContent(mimeMultipart);

        String addresses = getToAddresses();
        if (addresses != null) {
            mimeMessage.addRecipients(RecipientType.TO, addresses);
        }
        addresses = getCcAddresses();
        if (addresses != null) {
            mimeMessage.addRecipients(RecipientType.CC, addresses);
        }
        addresses = getBccAddresses();
        if (addresses != null) {
            mimeMessage.addRecipients(RecipientType.BCC, addresses);
        }

        return mimeMessage;
    }

    private static String getContent(LocalizedString ls, Locale l) {
        if (ls != null) {
            String s = ls.getContent(l);
            if (s == null) {
                return ls.getContent();
            }
            return s;
        }
        return null;
    }

    public static Collection<MimeMessageHandler> create(Map<Locale, Set<String>> tos, Map<Locale, Set<String>> ccs,
            Map<Locale, Set<String>> bccs) {
        return Stream.of(tos, ccs, bccs).flatMap(m -> m.keySet().stream()).distinct()
                .flatMap(locale -> bestEffortCreate(locale, tos.get(locale), ccs.get(locale), bccs.get(locale)).stream())
                .collect(Collectors.toSet());
    }

    /*XXX Best effort minimizes number of mime messages using a moving window. This approach also allows to group Tos and Ccs so
     * that, in the most common case where there is an overflow of Bccs, at least the Tos and Ccs will be visible to each other.
     * Note however that this intent is somewhat wasted when there are multiple preferred locales among Tos and Ccs due to the
     * locale separation */
    private static Collection<MimeMessageHandler> bestEffortCreate(Locale locale, Collection<String> tos, Collection<String> ccs,
            Collection<String> bccs) {
        Collection<MimeMessageHandler> handlers = new ArrayList<>();
        List<String> all =
                Stream.of(tos, ccs, bccs).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toList()),
                partial;
        List<List<String>> split = Lists.partition(all, MAX_RECIPIENTS);
        MimeMessageHandler handler;

        int nHandlers = split.size(), nRecipients = all.size(), nTos = tos != null ? tos.size() : 0, nCcs =
                ccs != null ? ccs.size() : 0, nVisible = nTos + nCcs;
        int ccStart, bccStart, mixedTos = nTos % MAX_RECIPIENTS, mixedVisible = nVisible % MAX_RECIPIENTS;
        if (nTos == nRecipients && mixedTos != 0) {
            ccStart = bccStart = nHandlers;
        } else if (nVisible == nRecipients && mixedVisible != 0) {
            ccStart = nTos / MAX_RECIPIENTS;
            bccStart = nHandlers;
        } else {
            ccStart = nTos / MAX_RECIPIENTS;
            bccStart = nVisible / MAX_RECIPIENTS;
        }

        int i;
        for (i = 0; i < ccStart; i++) { // Tos only
            handlers.add(new MimeMessageHandler(locale, split.get(i), null, null));
        }
        if (i < nHandlers && mixedTos != 0) {
            partial = split.get(i);
            if (nCcs == 0) { //Tos along with Bccs
                handler = new MimeMessageHandler(locale, partial.subList(0, mixedTos), null,
                        partial.subList(mixedTos, partial.size()));
            } else if (ccStart == bccStart) {// Tos along with Ccs and Bccs
                handler = new MimeMessageHandler(locale, partial.subList(0, mixedTos), partial.subList(mixedTos, mixedVisible),
                        partial.subList(mixedVisible, partial.size()));
            } else { // Tos along with Ccs
                handler = new MimeMessageHandler(locale, partial.subList(0, mixedTos), partial.subList(mixedTos, partial.size()),
                        null);
            }
            handlers.add(handler);
            i++;
        }
        for (; i < bccStart; i++) { // Ccs only
            handlers.add(new MimeMessageHandler(locale, null, split.get(i), null));
        }
        if (i < nHandlers && mixedVisible != 0 && ccStart != bccStart) { // Ccs along with Bccs
            partial = split.get(i);
            handlers.add(new MimeMessageHandler(locale, null, partial.subList(0, mixedVisible),
                    partial.subList(mixedVisible, partial.size())));
            i++;
        }
        for (; i < nHandlers; i++) { // Bccs only
            handlers.add(new MimeMessageHandler(locale, null, null, split.get(i)));
        }
        return handlers;
    }

    @Atomic(mode = TxMode.WRITE)
    public void deliver() throws MessagingException {
        LocalEmailMessageDispatchReport report = getReport();
        try {
            MimeMessage message = mimeMessage();
            Transport.send(message);
            report.setDeliveredCount(report.getDeliveredCount() + message.getAllRecipients().length);
        } catch (SendFailedException e) {
            if (e.getValidSentAddresses() != null) {
                report.setDeliveredCount(report.getDeliveredCount() + e.getValidSentAddresses().length);
            }
            if (e.getInvalidAddresses() != null) {
                report.setFailedCount(report.getFailedCount() + e.getInvalidAddresses().length);
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
        Set<String> currentTos = MessagingSystem.Util.toEmailSet(getToAddresses());
        Set<String> currentCcs = MessagingSystem.Util.toEmailSet(getCcAddresses());
        Map<RecipientType, List<String>> unsent =
                Stream.of(validUnsentAddresses).map(Address::toString).collect(Collectors.groupingBy(e -> {
                    if (currentTos.contains(e)) {
                        return RecipientType.TO;
                    } else if (currentCcs.contains(e)) {
                        return RecipientType.CC;
                    } else {
                        return RecipientType.BCC;
                    }
                }));
        getReport().addHandler(new MimeMessageHandler(getLocale(), unsent.get(RecipientType.TO), unsent.get(RecipientType.CC),
                unsent.get(RecipientType.BCC)));
    }

    public void delete() {
        setReport(null);
        super.deleteDomainObject();
    }
}
