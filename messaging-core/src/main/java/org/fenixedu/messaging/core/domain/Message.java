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

import com.google.common.base.Strings;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.groups.PersistentGroup;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.core.exception.MessagingDomainException;
import org.joda.time.DateTime;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Luis Cruz
 *
 */
public final class Message extends Message_Base implements Comparable<Message> {

    public static final class TemplateMessageBuilder {

        private MessageBuilder messageBuilder;
        private String key;
        private final Map<String, Object> params = new HashMap<>();

        protected TemplateMessageBuilder(String key, MessageBuilder messageBuilder) {
            if (MessageTemplate.get(key) == null) {
                throw MessagingDomainException.missingTemplate(key);
            }
            this.key = key;
            this.messageBuilder = messageBuilder;
        }

        public TemplateMessageBuilder parameter(String s, Object e) {
            if (s != null && e != null) {
                this.params.put(s, e);
            }
            return this;
        }

        public TemplateMessageBuilder parameters(Map<String, Object> params) {
            if (params != null) {
                params.entrySet().stream().filter(e -> e.getKey() != null && e.getValue() != null)
                        .forEach(e -> this.params.put(e.getKey(), e.getValue()));
            }
            return this;
        }

        public MessageBuilder and() {
            MessageTemplate template = MessageTemplate.get(key);
            messageBuilder.subject(template.getCompiledSubject(params));
            messageBuilder.textBody(template.getCompiledTextBody(params));
            messageBuilder.htmlBody(template.getCompiledHtmlBody(params));
            return messageBuilder;
        }
    }

    public static final class MessageBuilder implements Serializable {
        private static final long serialVersionUID = 525424959825814582L;
        private Sender sender;
        private LocalizedString subject = new LocalizedString(), textBody = new LocalizedString(),
                htmlBody = new LocalizedString();
        private String replyTo = null;
        private Locale preferredLocale = I18N.getLocale();
        private Set<Group> tos = new HashSet<>(), ccs = new HashSet<>(), bccs = new HashSet<>();
        private Set<String> singleBccs = new HashSet<>();

        protected MessageBuilder(Sender sender) {
            if (sender == null) {
                throw MessagingDomainException.nullSender();
            }
            this.sender = sender;
        }

        public MessageBuilder subject(LocalizedString subject) {
            this.subject = subject != null ? subject : new LocalizedString();
            return this;
        }

        public MessageBuilder subject(String subject, Locale locale) {
            if (locale != null) {
                this.subject = this.subject.with(locale, subject != null ? subject : "");
            }
            return this;
        }

        public MessageBuilder subject(String subject) {
            this.subject = this.subject.with(I18N.getLocale(), subject != null ? subject : "");
            return this;
        }

        public MessageBuilder textBody(LocalizedString textBody) {
            this.textBody = textBody != null ? textBody : new LocalizedString();
            return this;
        }

        public MessageBuilder textBody(String textBody, Locale locale) {
            if (locale != null) {
                this.textBody = this.textBody.with(locale, textBody != null ? textBody : "");
            }
            return this;
        }

        public MessageBuilder textBody(String textBody) {
            this.textBody = this.textBody.with(I18N.getLocale(), textBody != null ? textBody : "");
            return this;
        }

        public MessageBuilder htmlBody(LocalizedString htmlBody) {
            this.htmlBody = htmlBody != null ? htmlBody : new LocalizedString();
            return this;
        }

        public MessageBuilder htmlBody(String htmlBody, Locale locale) {
            if (locale != null) {
                this.htmlBody = this.htmlBody.with(locale, htmlBody != null ? htmlBody : "");
            }
            return this;
        }

        public MessageBuilder htmlBody(String htmlBody) {
            this.htmlBody = this.htmlBody.with(I18N.getLocale(), htmlBody != null ? htmlBody : "");
            return this;
        }

        public TemplateMessageBuilder template(String key) {
            return new TemplateMessageBuilder(key, this);
        }

        public MessageBuilder template(String key, Map<String, Object> parameters) {
            return new TemplateMessageBuilder(key, this).parameters(parameters).and();
        }

        public MessageBuilder preferredLocale(Locale preferredLocale) {
            this.preferredLocale = preferredLocale != null ? preferredLocale : I18N.getLocale();
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

        private static <T> void filteredAdd(Stream<T> items, Collection<T> collection) {
            items.filter(Objects::nonNull).forEach(collection::add);
        }

        public MessageBuilder to(Collection<Group> tos) {
            if (tos != null) {
                filteredAdd(tos.stream(), this.tos);
            }
            return this;
        }

        public MessageBuilder to(Stream<Group> tos) {
            if (tos != null) {
                filteredAdd(tos, this.tos);
            }
            return this;
        }

        public MessageBuilder to(Group... tos) {
            filteredAdd(Arrays.stream(tos), this.tos);
            return this;
        }

        public MessageBuilder cc(Collection<Group> ccs) {
            if (ccs != null) {
                filteredAdd(ccs.stream(), this.ccs);
            }
            return this;
        }

        public MessageBuilder cc(Stream<Group> ccs) {
            if (ccs != null) {
                filteredAdd(ccs, this.ccs);
            }
            return this;
        }

        public MessageBuilder cc(Group... ccs) {
            filteredAdd(Arrays.stream(ccs), this.ccs);
            return this;
        }

        public MessageBuilder bcc(Collection<Group> bccs) {
            if (bccs != null) {
                filteredAdd(bccs.stream(), this.bccs);
            }
            return this;
        }

        public MessageBuilder bcc(Stream<Group> bccs) {
            if (bccs != null) {
                filteredAdd(bccs, this.bccs);
            }
            return this;
        }

        public MessageBuilder bcc(Group... bccs) {
            filteredAdd(Arrays.stream(bccs), this.bccs);
            return this;
        }

        public MessageBuilder singleBcc(Collection<String> bccs) {
            if (bccs != null) {
                filteredAdd(bccs.stream(), this.singleBccs);
            }
            return this;
        }

        public MessageBuilder singleBcc(Stream<String> bccs) {
            if (bccs != null) {
                filteredAdd(bccs, this.singleBccs);
            }
            return this;
        }

        public MessageBuilder singleBcc(String... bccs) {
            filteredAdd(Arrays.stream(bccs), this.singleBccs);
            return this;
        }

        public MessageBuilder replyToSender() {
            return replyTo(sender.getReplyTo());
        }

        public MessageBuilder replyTo(String replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        @Atomic(mode = TxMode.WRITE)
        public Message send() {
            Message message = new Message();
            message.setSender(sender);
            message.setReplyTo(replyTo);
            this.tos.stream().map(Group::toPersistentGroup).forEach(message::addTo);
            this.ccs.stream().map(Group::toPersistentGroup).forEach(message::addCc);
            this.bccs.stream().map(Group::toPersistentGroup).forEach(message::addBcc);
            message.setSingleBccs(Strings.emptyToNull(MessagingSystem.Util.toEmailListString(singleBccs)));
            message.setSubject(subject);
            message.setTextBody(textBody);
            message.setHtmlBody(htmlBody);
            message.setPreferredLocale(preferredLocale);
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
        setUser(Authenticate.getUser());
    }

    @Override
    public User getUser() {
        // FIXME remove when the framework supports read-only properties
        return super.getUser();
    }

    @Override
    public MessageDispatchReport getDispatchReport() {
        // FIXME remove when the framework supports read-only properties
        return super.getDispatchReport();
    }

    public DateTime getSent() {
        return getDispatchReport() != null ? getDispatchReport().getFinishedDelivery() : null;
    }

    @Atomic
    public void delete() {
        getToSet().clear();
        getCcSet().clear();
        getBccSet().clear();
        if (getDispatchReport() != null) {
            getDispatchReport().delete();
        }
        setSender(null);
        setUser(null);
        setMessagingSystemFromPendingDispatch(null);
        setMessagingSystem(null);
        deleteDomainObject();
    }

    public Set<Group> getToGroups() {
        return getToSet().stream().map(PersistentGroup::toGroup).collect(Collectors.toSet());
    }

    public Set<String> getTos() {
        return MessagingSystem.Util.toEmailSet(getToSet());
    }

    public Set<Group> getCcGroups() {
        return getCcSet().stream().map(PersistentGroup::toGroup).collect(Collectors.toSet());
    }

    public Set<String> getCcs() {
        return MessagingSystem.Util.toEmailSet(getCcSet());
    }

    public Set<Group> getBccGroups() {
        return getBccSet().stream().map(PersistentGroup::toGroup).collect(Collectors.toSet());
    }

    public Set<String> getBccs() {
        Set<String> bccs = MessagingSystem.Util.toEmailSet(getBccSet());
        bccs.addAll(getSingleBccsSet());
        return bccs;
    }

    public Set<Locale> getContentLocales() {
        return Stream.of(getSubject(), getTextBody(), getHtmlBody()).filter(Objects::nonNull)
                .flatMap(c -> c.getLocales().stream()).collect(Collectors.toSet());
    }

    public Set<String> getSingleBccsSet() {
        return MessagingSystem.Util.toEmailSet(getSingleBccs());
    }

    @Override
    public int compareTo(Message message) {
        int c = -getCreated().compareTo(message.getCreated());
        return c != 0 ? c : getExternalId().compareTo(message.getExternalId());
    }
}
