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

import java.util.Arrays;
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
import org.fenixedu.messaging.core.exception.MessagingDomainException;
import org.joda.time.Period;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

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
        private MessageDeletionPolicy policy = MessageDeletionPolicy.unlimited();
        private Set<Group> recipients = new HashSet<>();

        protected SenderBuilder(String address) {
            if (!MessagingSystem.Util.isValidEmail(address)) {
                throw MessagingDomainException.invalidAddress();
            }
            this.address = address;
        }

        public SenderBuilder as(String name) {
            this.name = name;
            return this;
        }

        public SenderBuilder replyTo(String replyTo) {
            this.replyTo = MessagingSystem.Util.isValidEmail(replyTo) ? replyTo : null;
            return this;
        }

        public SenderBuilder htmlEnabled(boolean htmlEnabled) {
            this.htmlEnabled = htmlEnabled;
            return this;
        }

        public SenderBuilder members(Group members) {
            this.members = members != null ? members : Group.nobody();
            return this;
        }

        public SenderBuilder members(PersistentGroup members) {
            this.members = members != null ? members.toGroup() : Group.nobody();
            return this;
        }

        public SenderBuilder deletionPolicy(MessageDeletionPolicy policy) {
            this.policy = policy != null ? policy : MessageDeletionPolicy.unlimited();
            return this;
        }

        public SenderBuilder keepMessages(int amount) {
            return deletionPolicy(MessageDeletionPolicy.keepAmount(amount));
        }

        public SenderBuilder keepMessages(Period period) {
            return deletionPolicy(MessageDeletionPolicy.keepForDuration(period));
        }

        public SenderBuilder keepMessages(int amount, Period period) {
            return deletionPolicy(MessageDeletionPolicy.keepAmountForDuration(amount, period));
        }

        public SenderBuilder recipients(Group... recipients) {
            filteredAddRecipients(Arrays.stream(recipients));
            return this;
        }

        public SenderBuilder recipients(Collection<Group> recipients) {
            if (recipients != null) {
                filteredAddRecipients(recipients.stream());
            }
            return this;
        }

        public SenderBuilder recipients(Stream<Group> recipients) {
            if (recipients != null) {
                filteredAddRecipients(recipients);
            }
            return this;
        }

        private void filteredAddRecipients(Stream<Group> recipients) {
            recipients.filter(Objects::nonNull).forEach(this.recipients::add);
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

    public void delete() {
        getMessageSet().forEach(Message::delete);
        setMemberGroup(null);
        getRecipientSet().clear();
        setMessagingSystem(null);
        deleteDomainObject();
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

    protected void pruneMessages() {
        getPolicy().pruneMessages(this);
    }

    public static Set<Sender> available() {
        final User user = Authenticate.getUser();
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
