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
package org.fenixedu.messaging.domain;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.groups.PersistentGroup;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.exception.MessagingDomainException;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

/**
 *
 * @author Luis Cruz
 *
 */
public final class Message extends Message_Base implements Comparable<Message> {

    public static class TemplateMessageBuilder {

        private MessageBuilder messageBuilder;
        private String key;
        private final Map<String, Object> params = new HashMap<>();

        public TemplateMessageBuilder(String key, MessageBuilder messageBuilder) {
            this.key = key;
            this.messageBuilder = messageBuilder;
        }

        public TemplateMessageBuilder parameter(String s, Object e) {
            params.put(s, e);
            return this;
        }

        public TemplateMessageBuilder parameters(Map<String, Object> params) {
            params.putAll(params);
            return this;
        }

        public MessageBuilder and() {
            MessageTemplate template = MessagingSystem.getTemplate(key);
            messageBuilder.subject(template.getCompiledSubject(params));
            messageBuilder.body(template.getCompiledTextBody(params));
            messageBuilder.htmlBody(template.getCompiledHtmlBody(params));
            return messageBuilder;
        }
    }

    public static final class MessageBuilder implements Serializable {
        private static final long serialVersionUID = 525424959825814582L;
        private Sender sender;
        private LocalizedString subject = new LocalizedString(), body = new LocalizedString(), htmlBody = new LocalizedString();
        private String replyTo;
        private Locale extraBccLocale = I18N.getLocale();
        private Set<Group> to = new HashSet<>(), cc = new HashSet<>(), bcc = new HashSet<>();
        private Set<String> extraBcc = new HashSet<>();

        private MessageBuilder(Sender sender) {
            this.sender = sender;
        }

        public MessageBuilder subject(LocalizedString subject) {
            this.subject = subject;
            return this;
        }

        public MessageBuilder subject(String subject, Locale locale) {
            this.subject = this.subject.with(locale, subject);
            return this;
        }

        public MessageBuilder subject(String subject) {
            this.subject = this.subject.with(I18N.getLocale(), subject);
            return this;
        }

        public MessageBuilder body(LocalizedString body) {
            this.body = body;
            return this;
        }

        public MessageBuilder body(String body, Locale locale) {
            this.body = this.body.with(locale, body);
            return this;
        }

        public MessageBuilder body(String body) {
            this.body = this.body.with(I18N.getLocale(), body);
            return this;
        }

        public MessageBuilder htmlBody(LocalizedString htmlBody) {
            this.htmlBody = htmlBody;
            return this;
        }

        public MessageBuilder htmlBody(String htmlBody, Locale locale) {
            this.htmlBody = this.htmlBody.with(locale, htmlBody);
            return this;
        }

        public MessageBuilder htmlBody(String htmlBody) {
            this.htmlBody = this.htmlBody.with(I18N.getLocale(), htmlBody);
            return this;
        }

        public TemplateMessageBuilder template(String key) {
            return new TemplateMessageBuilder(key, this);
        }

        public MessageBuilder template(String key, Map<String, Object> parameters) {
            return new TemplateMessageBuilder(key, this).parameters(parameters).and();
        }

        public MessageBuilder extraBccLocale(Locale extraBccsLocale) {
            this.extraBccLocale = extraBccsLocale;
            return this;
        }

        public MessageBuilder content(String subject, String body, String htmlBody, Locale locale) {
            this.subject = this.subject.with(locale, subject);
            this.body = this.body.with(locale, body);
            this.htmlBody = this.htmlBody.with(locale, htmlBody);
            return this;
        }

        public MessageBuilder content(String subject, String body, String htmlBody) {
            return content(subject, body, htmlBody, I18N.getLocale());
        }

        public MessageBuilder content(LocalizedString subject, LocalizedString body, LocalizedString htmlBody) {
            this.subject = subject;
            this.body = body;
            this.htmlBody = htmlBody;
            return this;
        }

        public MessageBuilder to(Set<Group> to) {
            if (to != null) {
                this.to.addAll(to);
            }
            return this;
        }

        public MessageBuilder to(Group... to) {
            for (Group group : to) {
                this.to.add(group);
            }
            return this;
        }

        public MessageBuilder cc(Set<Group> cc) {
            if (cc != null) {
                this.cc.addAll(cc);
            }
            return this;
        }

        public MessageBuilder cc(Group... cc) {
            for (Group group : cc) {
                this.cc.add(group);
            }
            return this;
        }

        public MessageBuilder bcc(Set<Group> bcc) {
            if (bcc != null) {
                this.bcc.addAll(bcc);
            }
            return this;
        }

        public MessageBuilder bcc(Group... bcc) {
            for (Group group : bcc) {
                this.bcc.add(group);
            }
            return this;
        }

        public MessageBuilder bcc(String... bcc) {
            for (String group : bcc) {
                this.extraBcc.add(group);
            }
            return this;
        }

        public MessageBuilder replyToSender() {
            this.replyTo = sender.getReplyTo();
            return this;
        }

        public MessageBuilder replyTo(String replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        @Atomic(mode = TxMode.WRITE)
        public Message send() {
            return new Message(sender, subject, body, htmlBody, to, cc, bcc, extraBcc, replyTo, extraBccLocale);
        }
    }

    public static MessageBuilder from(Sender sender) {
        if (sender == null) {
            throw MessagingDomainException.nullSender();
        }
        return new MessageBuilder(sender);
    }

    public static MessageBuilder fromSystem() {
        return new MessageBuilder(MessagingSystem.systemSender());
    }

    static final public Comparator<Message> COMPARATOR_BY_CREATED_DATE_OLDER_FIRST = new Comparator<Message>() {
        @Override
        public int compare(Message o1, Message o2) {
            return o1.getCreated().compareTo(o2.getCreated());
        }
    };

    static final public Comparator<Message> COMPARATOR_BY_CREATED_DATE_OLDER_LAST = new Comparator<Message>() {
        @Override
        public int compare(Message o1, Message o2) {
            return o2.getCreated().compareTo(o1.getCreated());
        }
    };

    protected Message() {
        super();
        final MessagingSystem messagingSystem = MessagingSystem.getInstance();
        setMessagingSystem(messagingSystem);
        setMessagingSystemFromPendingDispatch(messagingSystem);
        setCreated(new DateTime());
        setUser(Authenticate.getUser());
        setExtraBccsLocale(I18N.getLocale());
    }

    Message(Sender sender, LocalizedString subject, LocalizedString body, LocalizedString htmlBody, Set<Group> to, Set<Group> cc,
            Set<Group> bcc, Set<String> extraBccs, String replyTo, Locale extraBccsLocale) {
        this();
        setSender(sender);
        if (to != null) {
            to.stream().map(Group::toPersistentGroup).forEach(this::addTo);
        }
        if (cc != null) {
            cc.stream().map(Group::toPersistentGroup).forEach(this::addCc);
        }
        if (bcc != null) {
            bcc.stream().map(Group::toPersistentGroup).forEach(this::addBcc);
        }
        setReplyTo(replyTo);
        setExtraBccs(extraBccs != null ? MessagingSystem.MAIL_LIST_JOINER.join(extraBccs) : "");
        setSubject(subject != null ? subject : new LocalizedString());
        setBody(body != null ? body : new LocalizedString());
        setHtmlBody(htmlBody != null ? htmlBody : new LocalizedString());
        setExtraBccsLocale(extraBccsLocale);
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

    public void safeDelete() {
        if (getDispatchReport() == null) {
            delete();
        }
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

    public Set<Group> getToGroup() {
        return getToSet().stream().map(g -> g.toGroup()).collect(Collectors.toSet());
    }

    public Set<String> getTos() {
        return recipientsToEmails(getToSet());
    }

    public Set<Group> getCcGroup() {
        return getCcSet().stream().map(g -> g.toGroup()).collect(Collectors.toSet());
    }

    public Set<String> getCcs() {
        return recipientsToEmails(getCcSet());
    }

    public Set<Group> getBccGroup() {
        return getBccSet().stream().map(g -> g.toGroup()).collect(Collectors.toSet());
    }

    public Set<String> getBccs() {
        Set<String> base = recipientsToEmails(getBccSet());
        base.addAll(getExtraBccsSet());
        return base;
    }

    public Set<String> getExtraBccsSet() {
        String extraBccs = getExtraBccs();
        if (!Strings.isNullOrEmpty(extraBccs)) {
            return Sets.newHashSet(getExtraBccs().replace(',', ' ').replace(';', ' ').split("\\s+"));
        } else {
            return Sets.newHashSet();
        }
    }

    private Set<String> recipientsToEmails(Set<PersistentGroup> recipients) {
        return recipients.stream().map(g -> g.toGroup()).flatMap(g -> g.getMembers().stream()).distinct()
                .map(user -> user.getProfile().getEmail()).filter(Strings::isNullOrEmpty).collect(Collectors.toSet());
    }

    public boolean isLoggedUserCreator() {
        return getUser().equals(Authenticate.getUser());
    }

    @Override
    public int compareTo(Message o) {
        int date = -getCreated().compareTo(o.getCreated());
        return date != 0 ? date : getExternalId().compareTo(o.getExternalId());
    }
}
