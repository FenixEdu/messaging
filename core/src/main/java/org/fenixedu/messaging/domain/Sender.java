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
package org.fenixedu.messaging.domain;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.groups.PersistentGroup;
import org.fenixedu.bennu.core.groups.DynamicGroup;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.security.Authenticate;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;

/**
 *
 * @author Luis Cruz
 *
 */
public class Sender extends Sender_Base {

    public static Comparator<Sender> COMPARATOR_BY_FROM_NAME = new Comparator<Sender>() {

        @Override
        public int compare(final Sender sender1, final Sender sender2) {
            final int c = sender1.getFromName().compareTo(sender2.getFromName());
            return c == 0 ? sender1.getExternalId().compareTo(sender2.getExternalId()) : c;
        }

    };

    protected Sender() {
        super();
        setMessagingSystem(MessagingSystem.getInstance());
    }

    public Sender(final String fromName, final String fromAddress, final Group members, MessageDeletionPolicy policy) {
        this();
        setFromName(fromName);
        setFromAddress(fromAddress);
        setMembers(members);
        setPolicy(policy);
    }

    @Override
    public Set<Message> getMessageSet() {
        // TODO remove when framework supports read-only relations
        return Collections.unmodifiableSet(super.getMessageSet());
    }

    public void delete() {
        for (final Message message : getMessageSet()) {
            message.delete();
        }
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

    public boolean isMember(final User user) {
        return getMembers().isMember(user) || DynamicGroup.get("managers").isMember(user);
    }

    public boolean isMember(User user, DateTime when) {
        return getMembers().isMember(user, when) || DynamicGroup.get("managers").isMember(user, when);
    }

    public Set<Group> getRecipients() {
        return getRecipientSet().stream().map(g -> g.toGroup()).collect(Collectors.toSet());
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

    @Atomic
    public Set<ReplyTo> getConcreteReplyTos() {
        Set<ReplyTo> replyTos = new HashSet<ReplyTo>();
        for (ReplyTo replyTo : getReplyToSet()) {
            if (replyTo instanceof CurrentUserReplyTo) {
                final User user = Authenticate.getUser();
                final UserReplyTo userReplyTo =
                        user.getUserReplyTo() != null ? user.getUserReplyTo() : UserReplyTo.createFor(user);
                replyTos.add(userReplyTo);
            } else {
                replyTos.add(replyTo);
            }
        }
        return replyTos;
    }

    public String getFromName(final User user) {
        return getFromName();
    }

    public void pruneOldMessages() {
        getPolicy().pruneSender(this);
    }

    public Message send(String subject, String body, String htmlBody, Set<Group> to, Set<Group> cc, Set<Group> bcc,
            Set<String> extraBccs) {
        return new Message(this, subject, body, htmlBody, to, cc, bcc, extraBccs, getConcreteReplyTos());
    }

    public Message send(String subject, String body, String htmlBody, Set<Group> to, Set<Group> cc, Set<Group> bcc,
            Set<String> extraBccs, Set<ReplyTo> replyTos) {
        return new Message(this, subject, body, htmlBody, to, cc, bcc, extraBccs, replyTos);
    }

    public static boolean userHasRecipients() {
        final User user = Authenticate.getUser();
        for (final Sender sender : MessagingSystem.getInstance().getSenderSet()) {
            if (sender.isMember(user) && !sender.getRecipientSet().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static SortedSet<Sender> getAvailableSenders() {
        final User user = Authenticate.getUser();

        final SortedSet<Sender> senders = new TreeSet<Sender>(Sender.COMPARATOR_BY_FROM_NAME);
        for (final Sender sender : MessagingSystem.getInstance().getSenderSet()) {
            if (sender.isMember(user)) {
                senders.add(sender);
            }
        }

        return senders;
    }

    public static Stream<Sender> all() {
        return MessagingSystem.getInstance().getSenderSet().stream();
    }
}
