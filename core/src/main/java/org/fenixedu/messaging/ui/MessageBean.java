/*
 * @(#)MessageBean.java
 *
 * Copyright 2012 Instituto Superior Tecnico
 * Founding Authors: Luis Cruz
 *
 *      https://fenix-ashes.ist.utl.pt/
 *
 *   This file is part of the Messaging Module.
 *
 *   The Messaging Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version
 *   3 of the License, or (at your option) any later version.
 *
 *   The Messaging Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the Messaging Module. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.fenixedu.messaging.ui;

import static pt.ist.fenixframework.FenixFramework.atomic;

import java.io.Serializable;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.domain.Message;
import org.fenixedu.messaging.domain.Message.MessageBuilder;
import org.fenixedu.messaging.domain.ReplyTo;
import org.fenixedu.messaging.domain.Sender;

import com.google.common.base.Strings;

public class MessageBean implements Serializable {
    private static final long serialVersionUID = -2004655177098978589L;
    private static final String BUNDLE = "MessagingResources";
    private static final String FOOTER = "message.footer";

    private boolean automaticFooter = true;
    private Sender sender;
    private String bccs;
    private LocalizedString subject, body, htmlBody;
    private Locale extraBccsLocale = I18N.getLocale();

    private Set<String> errors, recipients, replyTos;

    public boolean isAutomaticFooter() {
        return automaticFooter;
    }

    public void setAutomaticFooter(boolean automaticFooter) {
        this.automaticFooter = automaticFooter;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(final Sender sender) {
        this.sender = sender;
    }

    public Locale getExtraBccsLocale() {
        return extraBccsLocale;
    }

    public void setExtraBccsLocale(Locale extraBccsLocale) {
        this.extraBccsLocale = extraBccsLocale;
    }

    public Set<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(Set<String> recipients) {
        this.recipients = recipients;
    }

    public Set<Group> getRecipientGroups() {
        Set<String> recipientExpressions = getRecipients();
        if (recipientExpressions != null) {
            Base64.Decoder decoder = Base64.getDecoder();
            return recipientExpressions.stream().map(e -> Group.parse(new String(decoder.decode(e)))).collect(Collectors.toSet());
        }
        return null;
    }

    public void setRecipientGroups(Set<Group> recipients) {
        if (recipients != null) {
            Base64.Encoder encoder = Base64.getEncoder();
            this.recipients =
                    recipients.stream().map(g -> encoder.encodeToString(g.getExpression().getBytes()))
                            .collect(Collectors.toSet());
        } else {
            this.recipients = null;
        }
    }

    public Set<ReplyTo> getReplyToObjects() {
        if (replyTos != null) {
            return replyTos.stream().map(rt -> ReplyTo.parse(rt)).collect(Collectors.toSet());
        }
        return null;
    }

    public void setReplyToObjects(Set<ReplyTo> replyTos) {
        if (replyTos != null) {
            this.replyTos = replyTos.stream().map(rt -> rt.serialize()).collect(Collectors.toSet());
        } else {
            this.replyTos = null;
        }
    }

    public Set<String> getReplyTos() {
        return replyTos;
    }

    public void setReplyTos(Set<String> replyTos) {
        this.replyTos = replyTos;
    }

    public String getBccs() {
        return bccs;
    }

    public void setBccs(String bccs) {
        this.bccs = bccs;
    }

    public LocalizedString getSubject() {
        return subject;
    }

    public void setSubject(LocalizedString subject) {
        this.subject = subject;
    }

    public LocalizedString getBody() {
        return body;
    }

    public void setBody(final LocalizedString body) {
        this.body = body;
    }

    public LocalizedString getHtmlBody() {
        return htmlBody;
    }

    public void setHtmlBody(final LocalizedString htmlBody) {
        this.htmlBody = htmlBody;
    }

    public Set<String> getErrors() {
        return errors;
    }

    public void setErrors(Set<String> errors) {
        this.errors = errors;
    }

    public Set<String> validate() {
        Set<String> errors = new HashSet<String>();
        if (getSender() == null) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.sender.empty"));
        }

        String bccs = getBccs();
        Set<String> recipients = getRecipients();
        if ((recipients == null || recipients.isEmpty()) && Strings.isNullOrEmpty(bccs)) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.recipients.empty"));
        }

        if (recipients != null && !recipients.isEmpty()) {
            try {
                Base64.Decoder decoder = Base64.getDecoder();
                recipients.forEach(e -> Group.parse(new String(decoder.decode(e))));
            } catch (DomainException e) {
                String[] args = e.getArgs();
                errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.recipient.erroneous", args != null
                        && args.length > 0 ? args[0] : ""));
            }
        }

        if (!Strings.isNullOrEmpty(bccs)) {
            String[] emails = bccs.split(",");
            for (String emailString : emails) {
                final String email = emailString.trim();
                if (!isValidEmailAddress(email)) {
                    errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.bcc.invalid", email));
                }
            }
        }

        LocalizedString subject = getSubject(), body = getBody(), htmlBody = getHtmlBody();
        if (subject == null || subject.isEmpty()) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.subject.empty"));
        }

        if ((body == null || body.isEmpty()) && (htmlBody == null || htmlBody.isEmpty())) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.message.empty"));
        }

        if (htmlBody != null && !htmlBody.isEmpty() && !sender.getHtmlSender()) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.html.forbidden"));
        }

        this.errors = errors;
        return errors;
    }

    public Message send() throws Exception {
        Set<String> validate = validate();
        if (validate.isEmpty()) {
            Sender sender = getSender();
            Set<Group> recipients = getRecipientGroups();
            LocalizedString body = getBody();
            if (body != null && !body.isEmpty() && isAutomaticFooter() && recipients != null && !recipients.isEmpty()) {
                LocalizedString footer =
                        BundleUtil.getLocalizedString(BUNDLE, FOOTER, sender.getFromName(), presentRecients(recipients));
                LocalizedString union = new LocalizedString();
                Set<Locale> locales = new HashSet<>();
                locales.addAll(footer.getLocales());
                locales.addAll(body.getLocales());
                for (Locale l : locales) {
                    union = union.with(l, getContent(body, l).concat(getContent(footer, l)));
                }
                body = union;
            }
            final MessageBuilder builder = new MessageBuilder(sender, getSubject(), body);
            LocalizedString htmlBody = getHtmlBody();
            if (htmlBody != null && !htmlBody.isEmpty()) {
                builder.htmlBody(htmlBody);
            }
            for (Group recipient : recipients) {
                builder.bcc(recipient);
            }
            String bccs = getBccs();
            if (!Strings.isNullOrEmpty(bccs)) {
                for (String bcc : bccs.split(",")) {
                    if (!Strings.isNullOrEmpty(bcc.trim())) {
                        builder.bcc(bcc.trim());
                    }
                }
            }
            builder.bccLocale(getExtraBccsLocale());
            Set<String> replyTos = getReplyTos();
            if (replyTos != null) {
                for (String replyTo : replyTos) {
                    builder.replyTo(replyTo);
                }
            }
            return atomic(() -> builder.send());
        }
        return null;
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

    private String presentRecients(Collection<Group> recipients) {
        return recipients.stream().map(r -> r.getPresentationName()).collect(Collectors.joining("\n\t"));
    }

    private static boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

}
