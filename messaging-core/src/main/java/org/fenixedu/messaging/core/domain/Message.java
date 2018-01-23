/*
 * @(#)Message.java
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
package org.fenixedu.messaging.core.domain;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.groups.PersistentGroup;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.core.domain.MessagingSystem.Util;
import org.fenixedu.messaging.core.template.DeclareMessageTemplate;
import org.fenixedu.messaging.core.template.TemplateParameter;
import org.joda.time.DateTime;

import com.google.common.base.Strings;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.fenixedu.messaging.core.domain.MessagingSystem.Util.builderSetAdd;
import static org.fenixedu.messaging.core.domain.MessagingSystem.Util.builderSetCopy;
import static org.fenixedu.messaging.core.domain.MessagingSystem.Util.toEmailSet;

/**
 * @author Luis Cruz
 */
public final class Message extends Message_Base implements Comparable<Message> {

    public static final class TemplateMessageBuilder {

        private MessageBuilder messageBuilder;
        private MessageTemplate template;
        private final Map<String, Object> params = new HashMap<>();

        protected TemplateMessageBuilder(String key, MessageBuilder messageBuilder) {
            this.template = MessageTemplate.get(key);
            if (this.template == null) {
                throw new IllegalArgumentException("Unknown template key.");
            }
            this.messageBuilder = requireNonNull(messageBuilder);
        }

        public TemplateMessageBuilder parameter(String s, Object e) {
            this.params.put(requireNonNull(s), e);
            return this;
        }

        public TemplateMessageBuilder parameters(Map<String, Object> params) {
            params.entrySet().stream().filter(e -> e.getKey() != null && e.getValue() != null)
                    .forEach(e -> this.params.put(e.getKey(), e.getValue()));
            return this;
        }

        public MessageBuilder and() {
            messageBuilder.subject(template.getCompiledSubject(params));
            messageBuilder.textBody(template.getCompiledTextBody(params));
            messageBuilder.htmlBody(template.getCompiledHtmlBody(params));
            return messageBuilder;
        }
    }

    @DeclareMessageTemplate(id = "org.fenixedu.messaging.message.wrapper",
            description = "message.template.message.wrapper.description", subject = "message.template.message.wrapper.subject",
            text = "message.template.message.wrapper.text", html = "message.template.message.wrapper.html", parameters = {
            @TemplateParameter(id = "sender", description = "message.template.message.wrapper.parameter.sender"),
            @TemplateParameter(id = "creator", description = "message.template.message.wrapper.parameter.creator"),
            @TemplateParameter(id = "replyTo", description = "message.template.message.wrapper.parameter.replyTo"),
            @TemplateParameter(id = "preferredLocale", description = "message.template.message.wrapper.parameter.preferredLocale"),
            @TemplateParameter(id = "subject", description = "message.template.message.wrapper.parameter.subject"),
            @TemplateParameter(id = "textBody", description = "message.template.message.wrapper.parameter.textBody"),
            @TemplateParameter(id = "htmlBody", description = "message.template.message.wrapper.parameter.htmlBody"),
            @TemplateParameter(id = "tos", description = "message.template.message.wrapper.parameter.tos"),
            @TemplateParameter(id = "ccs", description = "message.template.message.wrapper.parameter.ccs"),
            @TemplateParameter(id = "bccs", description = "message.template.message.wrapper.parameter.bccs"),
            @TemplateParameter(id = "singleBccs", description = "message.template.message.wrapper.parameter.singleBccs") },
            bundle = "MessagingResources")
    public static final class MessageBuilder implements Serializable {
        private boolean wrapped = false;
        private static final long serialVersionUID = 525424959825814582L;
        private Sender sender;
        private LocalizedString subject = new LocalizedString(), textBody = new LocalizedString(), htmlBody =
                new LocalizedString();
        private Set<String> replyTo = null;
        private Locale preferredLocale = I18N.getLocale();
        private Set<Group> tos = new HashSet<>(), ccs = new HashSet<>(), bccs = new HashSet<>();
        private Set<String> singleBccs = new HashSet<>();

        protected MessageBuilder(Sender sender) {
            from(sender);
        }

        public MessageBuilder from(Sender sender) {
            this.sender = requireNonNull(sender);
            return this;
        }

        public MessageBuilder wrapped() {
            wrapped = true;
            return this;
        }

        public MessageBuilder unwrapped() {
            wrapped = false;
            return this;
        }

        public MessageBuilder subject(LocalizedString subject) {
            this.subject = requireNonNull(subject);
            return this;
        }

        public MessageBuilder subject(String subject, Locale locale) {
            requireNonNull(locale);
            this.subject = Strings.isNullOrEmpty(subject) ? this.subject.without(locale) : this.subject.with(locale, subject);
            return this;
        }

        public MessageBuilder subject(String subject) {
            return subject(subject, I18N.getLocale());
        }

        public MessageBuilder textBody(LocalizedString textBody) {
            this.textBody = requireNonNull(textBody);
            return this;
        }

        public MessageBuilder textBody(String textBody, Locale locale) {
            requireNonNull(locale);
            this.textBody =
                    Strings.isNullOrEmpty(textBody) ? this.textBody.without(locale) : this.textBody.with(locale, textBody);
            return this;
        }

        public MessageBuilder textBody(String textBody) {
            return textBody(textBody, I18N.getLocale());
        }

        public MessageBuilder htmlBody(LocalizedString htmlBody) {
            this.htmlBody = requireNonNull(htmlBody);
            return this;
        }

        public MessageBuilder htmlBody(String htmlBody, Locale locale) {
            requireNonNull(locale);
            this.htmlBody =
                    Strings.isNullOrEmpty(htmlBody) ? this.htmlBody.without(locale) : this.htmlBody.with(locale, htmlBody);
            return this;
        }

        public MessageBuilder htmlBody(String htmlBody) {
            return htmlBody(htmlBody, I18N.getLocale());
        }

        public TemplateMessageBuilder template(String key) {
            return new TemplateMessageBuilder(requireNonNull(key), this);
        }

        public MessageBuilder template(String key, Map<String, Object> parameters) {
            return new TemplateMessageBuilder(requireNonNull(key), this).parameters(requireNonNull(parameters)).and();
        }

        public MessageBuilder preferredLocale(Locale preferredLocale) {
            this.preferredLocale = requireNonNull(preferredLocale);
            return this;
        }

        public MessageBuilder content(String subject, String textBody, String htmlBody, Locale locale) {
            subject(subject, locale);
            textBody(textBody, locale);
            htmlBody(htmlBody, locale);
            return this;
        }

        public MessageBuilder content(String subject, String textBody, String htmlBody) {
            return content(subject, textBody, htmlBody, I18N.getLocale());
        }

        public MessageBuilder content(LocalizedString subject, LocalizedString textBody, LocalizedString htmlBody) {
            subject(subject);
            textBody(textBody);
            htmlBody(htmlBody);
            return this;
        }

        public MessageBuilder to(Collection<Group> tos) {
            builderSetCopy(requireNonNull(tos), Objects::nonNull, this.tos);
            return this;
        }

        public MessageBuilder to(Stream<Group> tos) {
            builderSetAdd(requireNonNull(tos), Objects::nonNull, this.tos);
            return this;
        }

        public MessageBuilder to(Group... tos) {
            builderSetAdd(requireNonNull(tos), Objects::nonNull, this.tos);
            return this;
        }

        public MessageBuilder cc(Collection<Group> ccs) {
            builderSetCopy(requireNonNull(ccs), Objects::nonNull, this.ccs);
            return this;
        }

        public MessageBuilder cc(Stream<Group> ccs) {
            builderSetAdd(requireNonNull(ccs), Objects::nonNull, this.ccs);
            return this;
        }

        public MessageBuilder cc(Group... ccs) {
            builderSetAdd(requireNonNull(ccs), Objects::nonNull, this.ccs);
            return this;
        }

        public MessageBuilder bcc(Collection<Group> bccs) {
            builderSetCopy(requireNonNull(bccs), Objects::nonNull, this.bccs);
            return this;
        }

        public MessageBuilder bcc(Stream<Group> bccs) {
            builderSetAdd(requireNonNull(bccs), Objects::nonNull, this.bccs);
            return this;
        }

        public MessageBuilder bcc(Group... bccs) {
            builderSetAdd(requireNonNull(bccs), Objects::nonNull, this.bccs);
            return this;
        }

        public MessageBuilder singleBcc(Collection<String> bccs) {
            builderSetCopy(requireNonNull(bccs), Util::isValidEmail, this.singleBccs);
            return this;
        }

        public MessageBuilder singleBcc(Stream<String> bccs) {
            builderSetAdd(requireNonNull(bccs), Util::isValidEmail, this.singleBccs);
            return this;
        }

        public MessageBuilder singleBcc(String... bccs) {
            builderSetAdd(requireNonNull(bccs), Util::isValidEmail, this.singleBccs);
            return this;
        }

        public MessageBuilder replyTo(Collection<String> replyTos) {
            builderSetCopy(requireNonNull(replyTos), Util::isValidEmail, this.replyTo);
            return this;
        }

        public MessageBuilder replyTo(Stream<String> replyTos) {
            builderSetAdd(requireNonNull(replyTos), Util::isValidEmail, this.replyTo);
            return this;
        }

        public MessageBuilder replyTo(String... replyTos) {
            builderSetAdd(requireNonNull(replyTos), Util::isValidEmail, this.replyTo);
            return this;
        }

        public MessageBuilder replyToSender() {
            return replyTo(sender.getReplyTo());
        }

        @Atomic(mode = TxMode.WRITE)
        public Message send() {
            Message message = new Message();
            message.setSender(sender);
            message.setReplyTo(Strings.emptyToNull(Util.toEmailListString(replyTo)));
            message.setPreferredLocale(preferredLocale);
            tos.stream().map(Group::toPersistentGroup).forEach(message::addTo);
            ccs.stream().map(Group::toPersistentGroup).forEach(message::addCc);
            bccs.stream().map(Group::toPersistentGroup).forEach(message::addBcc);
            message.setSingleBccs(Strings.emptyToNull(Util.toEmailListString(singleBccs)));
            if (wrapped) {
                template("org.fenixedu.messaging.message.wrapper").parameter("sender", sender)
                        .parameter("creator", Authenticate.getUser()).parameter("replyTo", replyTo)
                        .parameter("preferredLocale", preferredLocale).parameter("subject", subject)
                        .parameter("textBody", textBody).parameter("htmlBody", htmlBody).parameter("tos", newArrayList(tos))
                        .parameter("ccs", newArrayList(ccs)).parameter("bccs", newArrayList(bccs))
                        .parameter("singleBccs", newArrayList(singleBccs)).and();
            }
            message.setSubject(subject);
            message.setTextBody(textBody);
            message.setHtmlBody(htmlBody);
            return message;
        }
    }

    public static MessageBuilder from(Sender sender) {
        return new MessageBuilder(sender);
    }

    public static MessageBuilder fromSystem() {
        return new MessageBuilder(MessagingSystem.systemSender());
    }

    protected Message() {
        super();
        final MessagingSystem messagingSystem = MessagingSystem.getInstance();
        setMessagingSystem(messagingSystem);
        setMessagingSystemFromPendingDispatch(messagingSystem);
        setCreated(new DateTime());
        setCreator(Authenticate.getUser());
    }

    @Override
    public User getCreator() {
        // FIXME remove when the framework supports read-only properties
        return super.getCreator();
    }

    @Override
    public MessageDispatchReport getDispatchReport() {
        // FIXME remove when the framework supports read-only properties
        return super.getDispatchReport();
    }

    @Override
    public DateTime getCreated() {
        // FIXME remove when the framework supports read-only properties
        return super.getCreated();
    }

    @Override
    public String getReplyTo() {
        // FIXME remove when the framework supports read-only properties
        return super.getReplyTo();
    }

    @Override
    public Sender getSender() {
        // FIXME remove when the framework supports read-only properties
        return super.getSender();
    }

    @Override
    public LocalizedString getSubject() {
        // FIXME remove when the framework supports read-only properties
        return super.getSubject();
    }

    @Override
    public LocalizedString getTextBody() {
        // FIXME remove when the framework supports read-only properties
        return super.getTextBody();
    }

    @Override
    public LocalizedString getHtmlBody() {
        // FIXME remove when the framework supports read-only properties
        return super.getHtmlBody();
    }

    @Override
    public Locale getPreferredLocale() {
        // FIXME remove when the framework supports read-only properties
        return super.getPreferredLocale();
    }

    public Set<Group> getToGroups() {
        return getToSet().stream().map(PersistentGroup::toGroup).collect(Collectors.toSet());
    }

    public Set<String> getTos() {
        return toEmailSet(getToSet());
    }

    public Set<Group> getCcGroups() {
        return getCcSet().stream().map(PersistentGroup::toGroup).collect(Collectors.toSet());
    }

    public Set<String> getCcs() {
        return toEmailSet(getCcSet());
    }

    public Set<Group> getBccGroups() {
        return getBccSet().stream().map(PersistentGroup::toGroup).collect(Collectors.toSet());
    }

    public Set<String> getBccs() {
        Set<String> bccs = toEmailSet(getBccSet());
        bccs.addAll(getSingleBccsSet());
        return bccs;
    }

    public Set<String> getSingleBccsSet() {
        return toEmailSet(getSingleBccs());
    }

    public Set<String> getReplyTosSet() {
        return toEmailSet(getReplyTo());
    }

    public Set<Locale> getContentLocales() {
        return Stream.of(getSubject(), getTextBody(), getHtmlBody()).filter(Objects::nonNull)
                .flatMap(c -> c.getLocales().stream()).collect(Collectors.toSet());
    }

    public DateTime getSent() {
        return getDispatchReport() != null ? getDispatchReport().getFinishedDelivery() : null;
    }

    @Atomic(mode = TxMode.WRITE)
    protected void delete() {
        getToSet().clear();
        getCcSet().clear();
        getBccSet().clear();
        if (getDispatchReport() != null) {
            getDispatchReport().delete();
        }
        setSender(null);
        setCreator(null);
        setMessagingSystemFromPendingDispatch(null);
        setMessagingSystem(null);
        deleteDomainObject();
    }

    public void safeDelete() {
        if (isDeletable()) {
            delete();
        } else {
            throw new IllegalStateException("Message is not deletable by current user at this time.");
        }
    }

    public boolean isDeletable() {
        return getCreator().equals(Authenticate.getUser()) && getDispatchReport() == null;
    }

    @Override
    public int compareTo(Message message) {
        int c = -getCreated().compareTo(message.getCreated());
        return c != 0 ? c : getExternalId().compareTo(message.getExternalId());
    }
}
