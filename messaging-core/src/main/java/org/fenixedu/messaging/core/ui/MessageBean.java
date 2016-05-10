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
 *   You should have received a copy of the GNU Leneral Public License
 *   along with the Messaging Module. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.fenixedu.messaging.core.ui;

import java.util.Base64;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.domain.Message.MessageBuilder;
import org.fenixedu.messaging.core.domain.Message.TemplateMessageBuilder;
import org.fenixedu.messaging.core.domain.MessagingSystem;
import org.fenixedu.messaging.core.domain.Sender;
import org.fenixedu.messaging.core.template.DeclareMessageTemplate;
import org.fenixedu.messaging.core.template.TemplateParameter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@DeclareMessageTemplate(id = "org.fenixedu.messaging.message.wrapper",
        description = "message.template.message.wrapper.description", subject = "message.template.message.wrapper.subject",
        text = "message.template.message.wrapper.text", html = "message.template.message.wrapper.html", parameters = {
        @TemplateParameter(id = "subjectContent",
                description = "message.template.message.wrapper.parameter.subjectContent"),
        @TemplateParameter(id = "textContent", description = "message.template.message.wrapper.parameter.textContent"),
        @TemplateParameter(id = "htmlContent", description = "message.template.message.wrapper.parameter.htmlContent"),
        @TemplateParameter(id = "sender", description = "message.template.message.wrapper.parameter.sender"),
        @TemplateParameter(id = "recipients", description = "message.template.message.wrapper.parameter.recipients") },
        bundle = "MessagingResources")
public class MessageBean extends MessageContentBean {

    private static final long serialVersionUID = 336571169494160668L;

    private Sender sender;
    private String replyTo;
    private Set<String> singleRecipients, recipients;
    private Locale preferredLocale = I18N.getLocale();

    public Sender getSender() {
        return sender;
    }

    public void setSender(final Sender sender) {
        this.sender = sender;
    }

    public Locale getPreferredLocale() {
        return preferredLocale;
    }

    public void setPreferredLocale(Locale preferredLocale) {
        this.preferredLocale = preferredLocale;
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
            this.recipients = recipients.stream().map(g -> encoder.encodeToString(g.getExpression().getBytes()))
                    .collect(Collectors.toSet());
        } else {
            this.recipients = null;
        }
    }

    public String getSingleRecipients() {
        return MessagingSystem.Util.toEmailListString(singleRecipients);
    }

    public void setSingleRecipients(String singleRecipients) {
        this.singleRecipients = MessagingSystem.Util.toEmailSet(singleRecipients);
    }

    public Set<String> getSingleRecipientsSet() {
        return singleRecipients;
    }

    public void setSingleRecipientsSet(Set<String> singleRecipients) {
        this.singleRecipients = singleRecipients;
    }

    @Override
    public Collection<String> validate() {
        Collection<String> errors = Lists.newArrayList();
        if (getSender() == null) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.sender.empty"));
        }

        errors.addAll(super.validate());

        String singleRecipients = getSingleRecipients();
        Set<String> recipients = getRecipients();
        if ((recipients == null || recipients.isEmpty()) && Strings.isNullOrEmpty(singleRecipients)) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.recipients.empty"));
        }

        if (recipients != null && !recipients.isEmpty()) {
            try {
                Base64.Decoder decoder = Base64.getDecoder();
                recipients.forEach(e -> Group.parse(new String(decoder.decode(e))));
            } catch (DomainException e) {
                String[] args = e.getArgs();
                errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.recipient.erroneous",
                        args != null && args.length > 0 ? args[0] : ""));
            }
        }

        if (!Strings.isNullOrEmpty(singleRecipients)) {
            String[] emails = singleRecipients.split(",");
            for (String emailString : emails) {
                final String email = emailString.trim();
                if (!MessagingSystem.Util.isValidEmail(email)) {
                    errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.recipient.single.invalid", email));
                }
            }
        }

        String replyTo = getReplyTo();
        if (!(Strings.isNullOrEmpty(replyTo) || MessagingSystem.Util.isValidEmail(replyTo))) {
            errors.add(BundleUtil.getString(BUNDLE, "error.sender.validation.replyTo.invalid", replyTo));
        }

        if (getHtmlBody() != null && !getHtmlBody().isEmpty() && !sender.getHtmlEnabled()) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.html.forbidden"));
        }

        setErrors(errors);
        return errors;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    Message send() {
        Collection<String> errors = validate();
        if (errors.isEmpty()) {
            Sender sender = getSender();
            Set<Group> recipients = getRecipientGroups();
            MessageBuilder messageBuilder = Message.from(sender).preferredLocale(preferredLocale);
            TemplateMessageBuilder templateBuilder =
                    messageBuilder.template("org.fenixedu.messaging.message.wrapper").parameter("sender", sender.getName());
            if (recipients != null && !recipients.isEmpty()) {
                templateBuilder.parameter("recipients",
                        recipients.stream().map(r -> r.getPresentationName()).sorted().collect(Collectors.toList()));
                messageBuilder.bcc(recipients);
            }
            if (getSubject() != null && !getSubject().isEmpty()) {
                templateBuilder.parameter("subjectContent", getSubject());
            }
            if (getTextBody() != null && !getTextBody().isEmpty()) {
                templateBuilder.parameter("textContent", getTextBody());
            }
            if (getHtmlBody() != null && !getHtmlBody().isEmpty()) {
                templateBuilder.parameter("htmlContent", getHtmlBody());
            }
            messageBuilder = templateBuilder.and();
            if (singleRecipients != null) {
                messageBuilder.singleBcc(singleRecipients);
            }
            if (!Strings.isNullOrEmpty(replyTo)) {
                messageBuilder.replyTo(replyTo);
            }
            return messageBuilder.send();
        }
        return null;
    }
}
