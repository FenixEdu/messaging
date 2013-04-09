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
package pt.ist.messaging.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
import pt.ist.bennu.core.domain.RoleType;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.VirtualHost;
import pt.ist.bennu.core.domain.groups.PersistentGroup;
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

    public Sender() {
        super();
        setMessagingSystem(MessagingSystem.getInstance());
        setVirtualHost(VirtualHost.getVirtualHostForThread());
    }

    public Sender(final String fromName, final String fromAddress, final PersistentGroup members) {
        this();
        setFromName(fromName);
        setFromAddress(fromAddress);
        setMembers(members);
    }

    public void delete() {
        for (final Message message : getMessageSet()) {
            message.delete();
        }
        removeMembers();
        getRecipientsSet().clear();
        removeMessagingSystem();
        deleteDomainObject();
    }

    public static SortedSet<Sender> getAvailableSenders() {
        final User user = UserView.getCurrentUser();

        final SortedSet<Sender> senders = new TreeSet<Sender>(Sender.COMPARATOR_BY_FROM_NAME);
        for (final Sender sender : MessagingSystem.getInstance().getSenderSet()) {
            if (sender.getVirtualHost() == VirtualHost.getVirtualHostForThread() && sender.isMember(user)) {
                senders.add(sender);
            }
        }

        return senders;
    }

    public boolean isMember(final User user) {
        final PersistentGroup persistentGroup = getMembers();
        return (hasMembers() && persistentGroup.isMember(user)) || (user != null && user.hasRoleType(RoleType.MANAGER));
    }

    public static boolean userHasRecipients() {
        final User user = UserView.getCurrentUser();
        for (final Sender sender : MessagingSystem.getInstance().getSenderSet()) {
            if (sender.getVirtualHost() == VirtualHost.getVirtualHostForThread() && sender.isMember(user)
                    && sender.hasAnyRecipients()) {
                return true;
            }
        }
        return false;
    }

    @Atomic
    public List<ReplyTo> getConcreteReplyTos() {
        List<ReplyTo> replyTos = new ArrayList<ReplyTo>();
        for (ReplyTo replyTo : getReplyToSet()) {
            if (replyTo instanceof CurrentUserReplyTo) {
                final User user = UserView.getCurrentUser();
                final UserReplyTo userReplyTo = user.hasUserReplyTo() ? user.getUserReplyTo() : UserReplyTo.createFor(user);
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

    public void deleteOldMessages() {
        final SortedSet<Message> messages = new TreeSet<Message>(Message.COMPARATOR_BY_CREATED_DATE_OLDER_LAST);
        messages.addAll(getMessageSet());
        int sentCounter = 0;
        for (final Message message : messages) {
            if (message.getSent() != null) {
                ++sentCounter;
                if (sentCounter > Message.NUMBER_OF_SENT_EMAILS_TO_STAY) {
                    message.delete();
                }
            }
        }
    }

}
