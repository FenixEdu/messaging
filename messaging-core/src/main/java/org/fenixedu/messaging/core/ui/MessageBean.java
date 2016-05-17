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
    private String replyTo, singleRecipients;
    private Set<String> recipients;
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
            return recipientExpressions.stream().map(Group::parse).collect(Collectors.toSet());
        }
        return null;
    }

    public void setRecipientGroups(Set<Group> recipients) {
        if (recipients != null) {
            this.recipients = recipients.stream().map(Group::getExpression).collect(Collectors.toSet());
        } else {
            this.recipients = null;
        }
    }

    public String getSingleRecipients() {
        return singleRecipients;
    }

    public void setSingleRecipients(String singleRecipients) {
        this.singleRecipients = singleRecipients;
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
            String bccs = getSingleRecipients();
            MessageBuilder messageBuilder = Message.from(sender).preferredLocale(preferredLocale).replyTo(replyTo);
            TemplateMessageBuilder templateBuilder =
                    messageBuilder.template("org.fenixedu.messaging.message.wrapper").parameter("sender", sender.getName())
                            .parameter("subjectContent", getSubject()).parameter("textContent", getTextBody())
                            .parameter("htmlContent", getHtmlBody());
            if (recipients != null) {
                templateBuilder.parameter("recipients",
                        recipients.stream().map(Group::getPresentationName).sorted().collect(Collectors.toList()));
                messageBuilder.bcc(recipients);
            }
            if (bccs != null) {
                messageBuilder.singleBcc(bccs);
            }
            return templateBuilder.and().send();
        }
        return null;
    }

    @Override
    public Collection<String> validate() {
        Collection<String> errors = Lists.newArrayList();
        Sender sender = getSender();
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
            Set<Group> allowedRecipients = sender != null ? sender.getRecipients() : null;
            recipients.stream().forEach(expression -> {
                try {
                    Group recipient = Group.parse(expression);
                    if (sender != null && !allowedRecipients.contains(recipient)) {
                        errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.recipient.forbidden",
                                recipient.getPresentationName()));
                    }
                } catch (DomainException e) {
                    String[] args = e.getArgs();
                    errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.recipient.erroneous",
                            args != null && args.length > 0 ? args[0] : ""));
                }
            });
        }

        if (!Strings.isNullOrEmpty(singleRecipients)) {
            Set<String> emails = MessagingSystem.Util.toEmailSet(singleRecipients);
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
}
