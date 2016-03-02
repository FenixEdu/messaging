package org.fenixedu.messaging.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.domain.MessagingSystem;
import org.fenixedu.messaging.domain.Sender;

import com.google.common.base.Strings;

public class MessageSendingUrlBuilder {
    private final UriBuilder builder = UriBuilder.fromPath(CoreConfiguration.getConfiguration().applicationUrl()
            + "/messaging/message");

    public MessageSendingUrlBuilder sender(Sender sender) {
        builder.replaceQueryParam("sender", sender.getExternalId());
        return this;
    }

    public MessageSendingUrlBuilder bcc(Group bcc) {
        builder.queryParam("recipients", encode(bcc));
        return this;
    }

    public MessageSendingUrlBuilder bcc(String bcc) {
        builder.queryParam("bccs", encode(bcc));
        return this;
    }

    public MessageSendingUrlBuilder bccs(Set<Group> bccs) {
        builder.queryParam("recipients", bccs.stream().map(MessageSendingUrlBuilder::encode).toArray());

        return this;
    }

    public MessageSendingUrlBuilder bccs(String bccs) {
        builder.queryParam("bccs", MessagingSystem.Util.toEmailSet(bccs).stream().map(MessageSendingUrlBuilder::encode).toArray());
        return this;
    }

    public MessageSendingUrlBuilder textBody(LocalizedString text) {
        builder.replaceQueryParam("textBody", encode(text));
        return this;
    }

    public MessageSendingUrlBuilder textBody(Locale locale, String text) {
        return textBody(new LocalizedString(locale, text));
    }

    public MessageSendingUrlBuilder textBody(String text) {
        return textBody(I18N.getLocale(), text);
    }

    public MessageSendingUrlBuilder htmlBody(LocalizedString html) {
        builder.replaceQueryParam("htmlBody", encode(html));
        return this;
    }

    public MessageSendingUrlBuilder htmlBody(Locale locale, String html) {
        return htmlBody(new LocalizedString(locale, html));
    }

    public MessageSendingUrlBuilder htmlBody(String html) {
        return htmlBody(I18N.getLocale(), html);
    }

    public MessageSendingUrlBuilder subject(LocalizedString subject) {
        builder.replaceQueryParam("subject", encode(subject));
        return this;
    }

    public MessageSendingUrlBuilder subject(Locale locale, String subject) {
        return subject(new LocalizedString(locale, subject));
    }

    public MessageSendingUrlBuilder subject(String subject) {
        return subject(I18N.getLocale(), subject);
    }

    public MessageSendingUrlBuilder replyTo(String replyTo) {
        if (!Strings.isNullOrEmpty(replyTo)) {
            builder.queryParam("replyTo", encode(replyTo));
        }
        return this;
    }

    public MessageSendingUrlBuilder preferredLocale(Locale preferredLocale) {
        builder.replaceQueryParam("preferredLocale", preferredLocale);
        return this;
    }

    public String build() {
        return builder.build().toString();
    }

    private static String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String encode(Group g) {
        return encode(Base64.getEncoder().encodeToString(g.getExpression().getBytes()));
    }

    private static String encode(LocalizedString ls) {
        return encode(ls.json().toString());
    }

}