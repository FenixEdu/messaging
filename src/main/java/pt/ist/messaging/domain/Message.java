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
package pt.ist.messaging.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import org.joda.time.DateTime;

import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.VirtualHost;
import pt.ist.bennu.core.domain.groups.PersistentGroup;
import pt.ist.emailNotifier.domain.Email;
import pt.ist.emailNotifier.util.EmailAddressList;
import pt.ist.fenixframework.Atomic;
import pt.utl.ist.fenix.tools.util.i18n.Language;

/**
 * 
 * @author Luis Cruz
 * 
 */
public class Message extends Message_Base {

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

    public static final int NUMBER_OF_SENT_EMAILS_TO_STAY = 500;

    public Message() {
        super();
        final MessagingSystem messagingSystem = MessagingSystem.getInstance();
        setMessagingSystem(messagingSystem);
        setMessagingSystemFromPendingDispatch(messagingSystem);
    }

    public Message(final Sender sender, final Collection<? extends ReplyTo> replyTos, final Collection<PersistentGroup> tos,
            final Collection<PersistentGroup> ccs, final Collection<PersistentGroup> bccs, final String bccString,
            final String subject, final String body, final String htmlBody) {
        this();
        setSender(sender);
        if (replyTos != null) {
            getReplyToSet().addAll(replyTos);
        }
        if (tos != null) {
            getToSet().addAll(tos);
        }
        if (ccs != null) {
            getCcSet().addAll(ccs);
        }
        if (bccs != null) {
            getBccSet().addAll(bccs);
        }
        setBccString(bccString);
        setUser(UserView.getCurrentUser());
        setCreated(new DateTime());
        setSubject(subject);
        setBody(body);
        setHtmlBody(htmlBody);
    }

    public void safeDelete() {
        if (getSent() == null) {
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
        for (final Email email : getEmailSet()) {
            email.setMessage(null);
            email.delete();
        }

        setSender(null);
        setUser(null);
        setMessagingSystemFromPendingDispatch(null);
        setMessagingSystem(null);
        deleteDomainObject();
    }

    public String getRecipientsAsText() {
        final StringBuilder stringBuilder = new StringBuilder();
        recipients2Text(stringBuilder, getBccSet());
        if (getBccString() != null && !getBccString().isEmpty()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("\n");
            }
            stringBuilder.append(getBccString());
        }
        return stringBuilder.toString();
    }

    public String getRecipientsAsToText() {
        return recipients2Text(getToSet());
    }

    public String getRecipientsAsCcText() {
        return recipients2Text(getCcSet());
    }

    protected static String recipients2Text(final Set<PersistentGroup> recipients) {
        final StringBuilder stringBuilder = new StringBuilder();
        recipients2Text(stringBuilder, recipients);
        return stringBuilder.toString();
    }

    protected static void recipients2Text(final StringBuilder stringBuilder, final Set<PersistentGroup> recipients) {
        for (final PersistentGroup recipient : recipients) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("\n");
            }
            stringBuilder.append(recipient.getPresentationName());
        }
    }

    private static class Worker extends Thread {

        private final Set<PersistentGroup> recipients;

        private final Set<String> emailAddresses = new HashSet<String>();
        private final String virtualHostName;

        private Worker(final Set<PersistentGroup> recipients, final String virtualHostName) {
            this.recipients = recipients;
            this.virtualHostName = virtualHostName;
        }

        @Atomic
        @Override
        public void run() {
            try {
                VirtualHost.setVirtualHostForThread(virtualHostName);
                for (final PersistentGroup recipient : recipients) {
                    addDestinationEmailAddresses(recipient, emailAddresses);
                }
            } finally {
                VirtualHost.releaseVirtualHostFromThread();
            }
        }

    }

    protected static Set<String> getRecipientAddresses(Set<PersistentGroup> recipients) {
        final Set<String> emailAddresses = new HashSet<String>();
        for (final PersistentGroup recipient : recipients) {
            addDestinationEmailAddresses(recipient, emailAddresses);
        }
        return emailAddresses;
    }

    public static void addDestinationEmailAddresses(final PersistentGroup persistentGroup, final Set<String> emailAddresses) {
        for (final User user : persistentGroup.getMembers()) {
            final String value = user.getEmail();
            if (value != null && !value.isEmpty()) {
                emailAddresses.add(value);
            }
        }
    }

    protected Set<String> getDestinationBccs() {
        final Set<String> emailAddresses = new HashSet<String>();
        if (getBccString() != null && !getBccString().isEmpty()) {
            for (final String emailAddress : getBccString().replace(',', ' ').replace(';', ' ').split(" ")) {
                final String trimmed = emailAddress.trim();
                if (!trimmed.isEmpty()) {
                    emailAddresses.add(emailAddress);
                }
            }
        }
        final Worker worker = new Worker(getBccSet(), VirtualHost.getVirtualHostForThread().getHostname());
        worker.start();
        try {
            worker.join();
        } catch (final InterruptedException e) {
            throw new Error(e);
        }
        emailAddresses.addAll(worker.emailAddresses);
        return emailAddresses;
    }

    protected String[] getReplyToAddresses(final User user) {
        final String[] replyToAddresses = new String[getReplyToSet().size()];
        int i = 0;
        for (final ReplyTo replyTo : getReplyToSet()) {
            replyToAddresses[i++] = replyTo.getReplyToAddress(user);
        }
        return replyToAddresses;
    }

    public void dispatch() {
        try {
            final Sender sender = getSender();
            VirtualHost.setVirtualHostForThread(sender.getVirtualHost());
            final User user = getUser();
            final Set<String> destinationBccs = getDestinationBccs();
            for (final Set<String> bccs : split(destinationBccs)) {
                if (!bccs.isEmpty()) {
                    final Email email =
                            new Email(sender.getFromName(user), sender.getFromAddress(), getReplyToAddresses(user),
                                    Collections.EMPTY_SET, Collections.EMPTY_SET, bccs, getSubject(), getBody(), getHtmlBody());
                    email.setMessage(this);
                }
            }
            final Set<String> tos = getRecipientAddresses(getToSet());
            final Set<String> ccs = getRecipientAddresses(getCcSet());
            if (!tos.isEmpty() || !ccs.isEmpty()) {
                final Email email =
                        new Email(sender.getFromName(user), sender.getFromAddress(), getReplyToAddresses(user), tos, ccs,
                                Collections.EMPTY_SET, getSubject(), getBody(), getHtmlBody());
                email.setMessage(this);
            }
            setMessagingSystemFromPendingDispatch(null);
            setSent(new DateTime());
        } finally {
            VirtualHost.releaseVirtualHostFromThread();
        }
    }

    private Set<Set<String>> split(final Set<String> destinations) {
        final Set<Set<String>> result = new HashSet<Set<String>>();
        int i = 0;
        Set<String> subSet = new HashSet<String>();
        for (final String destination : destinations) {
            if (i++ == 50) {
                result.add(subSet);
                subSet = new HashSet<String>();
                i = 1;
            }
            subSet.add(destination);
        }
        result.add(subSet);
        return result;
    }

    public int getPossibleRecipientsCount() {
        int count = 0;
        for (final PersistentGroup recipient : getBccSet()) {
            count += recipient.getMembers().size();
        }
        return count;
    }

    public int getRecipientsWithEmailCount() {
        int count = 0;
        for (final PersistentGroup recipient : getBccSet()) {
            final Set<User> elements = recipient.getMembers();
            for (final User user : elements) {
                if (user.getEmail() != null) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getSentMailsCount() {
        int count = 0;
        for (final Email email : getEmailSet()) {
            final EmailAddressList confirmedAddresses = email.getConfirmedAddresses();
            if (confirmedAddresses != null && !confirmedAddresses.isEmpty()) {
                count += confirmedAddresses.toCollection().size();
            }
        }
        return count;
    }

    public int getFailedMailsCount() {
        int count = 0;
        for (final Email email : getEmailSet()) {
            EmailAddressList failedAddresses = email.getFailedAddresses();

            if (failedAddresses != null && !failedAddresses.isEmpty()) {
                count += failedAddresses.size();
            }
        }
        return count;
    }

    @Override
    public void setBody(final String body) {
        if (body != null) {
            final ResourceBundle resourceBundle = ResourceBundle.getBundle("resources.MessagingResources", Language.getLocale());

            final StringBuilder message = new StringBuilder();
            if (!body.trim().isEmpty()) {
                message.append(body);
                message.append("\n\n---\n");
                message.append(resourceBundle.getString("message.email.footer.prefix"));
                message.append(" ");
                message.append(getSender().getFromName());
                message.append(resourceBundle.getString("message.email.footer.prefix.suffix"));
                concatRecipients(message, getToSet());
                concatRecipients(message, getCcSet());
                concatRecipients(message, getBccSet());
                message.append("\n");
            }
            super.setBody(message.toString());
        } else {
            super.setBody(body);
        }
    }

    private void concatRecipients(final StringBuilder message, final Set<PersistentGroup> set) {
        for (final PersistentGroup recipient : set) {
            message.append("\n\t");
            message.append(recipient.getPresentationName());
        }
    }

    @Deprecated
    public java.util.Set<pt.ist.bennu.core.domain.groups.PersistentGroup> getCc() {
        return getCcSet();
    }

    @Deprecated
    public java.util.Set<pt.ist.bennu.core.domain.groups.PersistentGroup> getBcc() {
        return getBccSet();
    }

    @Deprecated
    public java.util.Set<pt.ist.bennu.core.domain.groups.PersistentGroup> getTo() {
        return getToSet();
    }

    @Deprecated
    public java.util.Set<pt.ist.emailNotifier.domain.Email> getEmail() {
        return getEmailSet();
    }

    @Deprecated
    public java.util.Set<pt.ist.messaging.domain.ReplyTo> getReplyTo() {
        return getReplyToSet();
    }

}
