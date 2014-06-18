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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.groups.PersistentGroup;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.security.Authenticate;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;

/**
 *
 * @author Luis Cruz
 *
 */
public final class Message extends Message_Base implements Comparable<Message> {
    public static final class MessageBuilder implements Serializable {
        private static final long serialVersionUID = 525424959825814582L;

        private Sender sender;

        private String subject;

        private String body;

        private String htmlBody;

        private Set<Group> to = new HashSet<>();

        private Set<Group> cc = new HashSet<>();

        private Set<Group> bcc = new HashSet<>();

        private Set<String> extraBcc = new HashSet<>();

        private Set<ReplyTo> replyTo = new HashSet<>();

        public MessageBuilder(Sender sender, String subject, String body) {
            this.sender = sender;
            this.subject = subject;
            this.body = body;
        }

        public MessageBuilder htmlBody(String htmlBody) {
            this.htmlBody = htmlBody;
            return this;
        }

        public MessageBuilder to(Group... to) {
            for (Group group : to) {
                this.to.add(group);
            }
            return this;
        }

        public MessageBuilder cc(Group... cc) {
            for (Group group : cc) {
                this.cc.add(group);
            }
            return this;
        }

        public MessageBuilder bcc(Group... bcc) {
            for (Group group : bcc) {
                this.bcc.add(group);
            }
            return this;
        }

        public MessageBuilder bcc(Set<String> bcc) {
            for (String group : bcc) {
                this.extraBcc.add(group);
            }
            return this;
        }

        public MessageBuilder bcc(String... bcc) {
            for (String group : bcc) {
                this.extraBcc.add(group);
            }
            return this;
        }

        public MessageBuilder replyToSystem() {
            this.replyTo.addAll(sender.getConcreteReplyTos());
            return this;
        }

        public MessageBuilder replyTo(Set<? extends ReplyTo> replyTo) {
            this.replyTo.addAll(replyTo);
            return this;
        }

        public MessageBuilder replyTo(ReplyTo... replyTo) {
            for (ReplyTo reply : replyTo) {
                this.replyTo.add(reply);
            }
            return this;
        }

        public MessageBuilder replyTo(String... emails) {
            for (String email : emails) {
                this.replyTo.add(new ConcreteReplyTo(email));
            }
            return this;
        }

        public Message send() {
            return new Message(sender, subject, body, htmlBody, to, cc, bcc, extraBcc, replyTo);
        }
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
    }

    Message(Sender sender, String subject, String body, String htmlBody, Set<Group> to, Set<Group> cc, Set<Group> bcc,
            Set<String> extraBccs, Set<ReplyTo> replyTos) {
        this();
        setSender(sender);
        if (to != null) {
            for (Group group : to) {
                addTo(group.toPersistentGroup());
            }
        }
        if (cc != null) {
            for (Group group : cc) {
                addCc(group.toPersistentGroup());
            }
        }
        if (bcc != null) {
            for (Group group : bcc) {
                addBcc(group.toPersistentGroup());
            }
        }
        if (replyTos != null) {
            for (ReplyTo replyTo : replyTos) {
                addReplyTo(replyTo);
            }
        }
        setExtraBccs(Joiner.on(", ").join(extraBccs));
        setSubject(subject);
        setBody(body);
        setHtmlBody(htmlBody);
    }

    @Override
    public User getUser() {
        // TODO remove when the framework supports read-only properties
        return super.getUser();
    }

    @Override
    public MessageDispatchReport getDispatchReport() {
        // TODO remove when the framework supports read-only properties
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
        for (final ReplyTo replyTo : getReplyToSet()) {
            removeReplyTo(replyTo);
            if (replyTo.getSenderSet().isEmpty()) {
                replyTo.delete();
            }
        }
        if (getDispatchReport() != null) {
            getDispatchReport().delete();
        }
        setSender(null);
        setUser(null);
        setMessagingSystemFromPendingDispatch(null);
        setMessagingSystem(null);
        deleteDomainObject();
    }

    @Override
    public void setBody(final String body) {
        if (body != null) {
            final StringBuilder message = new StringBuilder();
            if (!body.trim().isEmpty()) {
                message.append(body);
                message.append("\n\n---\n");
                message.append(BundleUtil.getString("resources.MessagingResources", "message.email.footer.prefix"));
                message.append(" ");
                message.append(getSender().getFromName());
                message.append(BundleUtil.getString("resources.MessagingResources", "message.email.footer.prefix.suffix"));
                message.append(Joiner.on("\n\t").join(recipientsToName(getToSet())));
                message.append(Joiner.on("\n\t").join(recipientsToName(getCcSet())));
                message.append(Joiner.on("\n\t").join(recipientsToName(getBccSet())));
                message.append("\n");
            }
            super.setBody(message.toString());
        } else {
            super.setBody(body);
        }
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
        if (getExtraBccs() != null && !getExtraBccs().isEmpty()) {
            Set<String> extended = Sets.newHashSet(getExtraBccs().replace(',', ' ').replace(';', ' ').split("\\s+"));
            extended.addAll(base);
            return extended;
        }
        return base;
    }

    public List<String> getReplyToAddresses() {
        List<String> replyTos = new ArrayList<>();
        for (final ReplyTo replyTo : getReplyToSet()) {
            replyTos.add(replyTo.getReplyToAddress(getUser()));
        }
        return replyTos;
    }

    private Set<String> recipientsToEmails(Set<PersistentGroup> recipients) {
        return recipients.stream().map(g -> g.toGroup()).flatMap(g -> g.getMembers().stream()).distinct()
                .filter(user -> !Strings.isNullOrEmpty(user.getEmail())).map(user -> user.getEmail()).collect(Collectors.toSet());
    }

    private Iterable<String> recipientsToName(final Set<PersistentGroup> recipients) {
        return FluentIterable.from(recipients).transform(PersistentGroup.persistentGroupToGroup)
                .transform(Group.groupToGroupName);
    }

    @Override
    public int compareTo(Message o) {
        int date = -getCreated().compareTo(o.getCreated());
        return date != 0 ? date : getExternalId().compareTo(o.getExternalId());
    }
}
