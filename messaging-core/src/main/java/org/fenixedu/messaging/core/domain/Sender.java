/*
 * @(#)Sender.java
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.groups.PersistentGroup;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.security.Authenticate;
import org.joda.time.Period;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import static java.util.Objects.requireNonNull;
import static org.fenixedu.messaging.core.domain.MessagingSystem.Util.builderSetAdd;
import static org.fenixedu.messaging.core.domain.MessagingSystem.Util.builderSetCopy;
import static org.fenixedu.messaging.core.domain.MessagingSystem.Util.isValidEmail;

/**
 * @author Luis Cruz
 */
public class Sender extends Sender_Base implements Comparable<Sender> {

    protected Sender() {
        super();
        setMessagingSystem(MessagingSystem.getInstance());
    }

    public static final class SenderBuilder {
        private String address, name = null, replyTo = null;
        private boolean htmlEnabled = false;
        private Group members = Group.nobody();
        private MessageStoragePolicy policy = MessageStoragePolicy.keepAll();
        private Set<Group> recipients = new HashSet<>();

        protected SenderBuilder(String address) {
            from(address);
        }

        public SenderBuilder from(String address) {
            if (!isValidEmail(requireNonNull(address))) {
                throw new IllegalArgumentException("Invalid sender address.");
            }
            this.address = address;
            return this;
        }

        public SenderBuilder as(String name) {
            this.name = requireNonNull(name);
            return this;
        }

        public SenderBuilder replyTo(String replyTo) {
            this.replyTo = isValidEmail(replyTo) ? replyTo : null;
            return this;
        }

        public SenderBuilder htmlEnabled(boolean htmlEnabled) {
            this.htmlEnabled = htmlEnabled;
            return this;
        }

        public SenderBuilder members(Group members) {
            this.members = requireNonNull(members);
            return this;
        }

        public SenderBuilder storagePolicy(MessageStoragePolicy policy) {
            this.policy = requireNonNull(policy);
            return this;
        }

        public SenderBuilder keepMessages(int amount) {
            this.policy = MessageStoragePolicy.keep(amount);
            return this;
        }

        public SenderBuilder keepMessages(Period period) {
            this.policy = MessageStoragePolicy.keep(period);
            return this;
        }

        public SenderBuilder keepMessages(int amount, Period period) {
            this.policy = MessageStoragePolicy.keep(amount, period);
            return this;
        }

        public SenderBuilder keepAllMessages() {
            this.policy = MessageStoragePolicy.keepAll();
            return this;
        }

        public SenderBuilder keepNoMessages() {
            this.policy = MessageStoragePolicy.keepAll();
            return this;
        }

        public SenderBuilder recipients(Group... recipients) {
            builderSetAdd(requireNonNull(recipients), Objects::nonNull, this.recipients);
            return this;
        }

        public SenderBuilder recipients(Collection<Group> recipients) {
            builderSetCopy(requireNonNull(recipients), Objects::nonNull, this.recipients);
            return this;
        }

        public SenderBuilder recipients(Stream<Group> recipients) {
            builderSetAdd(requireNonNull(recipients), Objects::nonNull, this.recipients);
            return this;
        }

        @Atomic(mode = TxMode.WRITE)
        public Sender build() {
            Sender sender = new Sender();
            sender.setAddress(address);
            sender.setName(Strings.nullToEmpty(name));
            sender.setReplyTo(replyTo);
            sender.setHtmlEnabled(htmlEnabled);
            sender.setMembers(members);
            sender.setPolicy(policy);
            sender.setRecipients(recipients);
            return sender;
        }
    }

    public static SenderBuilder from(String address) {
        return new SenderBuilder(address);
    }

    @Override
    public Set<Message> getMessageSet() {
        // FIXME remove when framework supports read-only relations
        return super.getMessageSet();
    }

    @Override
    public void setAddress(String address) {
        if (!isValidEmail(requireNonNull(address))) {
            throw new IllegalArgumentException("Invalid sender address.");
        }
        super.setAddress(address);
    }

    @Override
    public void setName(String name) {
        super.setName(requireNonNull(name));
    }

    @Override
    public void setReplyTo(String replyTo) {
        super.setReplyTo(isValidEmail(replyTo) ? replyTo : null);
    }

    @Override
    public void setPolicy(MessageStoragePolicy policy) {
        super.setPolicy(requireNonNull(policy));
    }

    public Group getMembers() {
        return getMemberGroup().toGroup();
    }

    public void setMembers(Group members) {
        super.setMemberGroup(members.toPersistentGroup());
    }

    public Set<Group> getRecipients() {
        return getRecipientSet().stream().map(PersistentGroup::toGroup).collect(Collectors.toSet());
    }

    public void setRecipients(Collection<Group> recipients) {
        getRecipientSet().clear();
        recipients.stream().distinct().forEach(this::addRecipient);
    }

    public void addRecipient(Group recipient) {
        PersistentGroup group = recipient.toPersistentGroup();
        if (!getRecipientSet().contains(group)) {
            super.addRecipient(group);
        }
    }

    public void removeRecipient(Group recipient) {
        PersistentGroup group = recipient.toPersistentGroup();
        if (getRecipientSet().contains(group)) {
            super.removeRecipient(group);
        }
    }

    public String getName(final User user) {
        return getName();
    }

    public void pruneMessages() {
        getPolicy().pruneMessages(this);
    }

    @Atomic(mode = TxMode.WRITE)
    public void delete() {
        getMessageSet().forEach(Message::delete);
        setMemberGroup(null);
        getRecipientSet().clear();
        setMessagingSystem(null);
        deleteDomainObject();
    }

    public static Set<Sender> available() {
        return available(Authenticate.getUser());
    }

    public static Set<Sender> available(User user) {
        return MessagingSystem.getInstance().getSenderSet().stream().filter(sender -> sender.getMembers().isMember(user))
                .collect(Collectors.toSet());
    }

    public static Set<Sender> all() {
        return Sets.newHashSet(MessagingSystem.getInstance().getSenderSet());
    }

    @Override
    public int compareTo(Sender sender) {
        int c = getName().compareTo(sender.getName());
        return c == 0 ? sender.getExternalId().compareTo(sender.getExternalId()) : c;
    }
}
