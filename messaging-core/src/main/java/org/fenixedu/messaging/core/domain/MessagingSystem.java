/*
 * @(#)MessagingSystem.java
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

import static org.fenixedu.messaging.core.bootstrap.MessagingSystemBootstrap.defaultSystemSenderAddress;
import static org.fenixedu.messaging.core.bootstrap.MessagingSystemBootstrap.defaultSystemSenderMembers;
import static org.fenixedu.messaging.core.bootstrap.MessagingSystemBootstrap.defaultSystemSenderName;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.UserProfile;
import org.fenixedu.bennu.core.domain.groups.PersistentGroup;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.messaging.core.dispatch.MessageDispatcher;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

/**
 * @author Luis Cruz
 */
public class MessagingSystem extends MessagingSystem_Base {
    private static MessagingSystem instance = null;
    private static MessageDispatcher dispatcher = null;

    private MessagingSystem() {
        super();
        setBennu(Bennu.getInstance());
    }

    @Atomic(mode = TxMode.WRITE)
    private static void initialize() {
        instance = Bennu.getInstance().getMessagingSystem();
        if (instance == null) {
            instance = new MessagingSystem();
            Sender sender = Sender.from(defaultSystemSenderAddress).as(defaultSystemSenderName)
                    .members(Group.parse(defaultSystemSenderMembers)).recipients(Group.anyone()).build();
            instance.setSystemSender(sender);
        }

        MessageTemplate.reifyDeclarations();
    }

    public static MessagingSystem getInstance() {
        if (instance == null) {
            initialize();
        }
        return instance;
    }

    @Atomic(mode = TxMode.WRITE)
    public static MessageDispatchReport dispatch(Message message) {
        MessageDispatchReport report = null;
        if (dispatcher != null) {
            report = dispatcher.dispatch(message);
            if (report != null) {
                message.setMessagingSystemFromPendingDispatch(null);
                message.setDispatchReport(report);
            }
        }
        return report;
    }

    public static MessageDispatcher getMessageDispatcher(MessageDispatcher dispatcher) {
        return MessagingSystem.dispatcher;
    }

    public static void setMessageDispatcher(MessageDispatcher dispatcher) {
        MessagingSystem.dispatcher = dispatcher;
    }

    public static Set<Message> getPendingMessages() {
        // FIXME remove when the framework supports read-only properties
        return getInstance().getMessagePendingDispatchSet();
    }

    public static Sender systemSender() {
        return getInstance().getSystemSender();
    }

    public boolean isOptOutAvailable(User user){
        return getOptOutAvailable().isMember(user);
    }

    public boolean isOptedOut(User user){
        return getOptedOutGroup().isMember(user);
    }

    public boolean isOptedIn(User user){
        return !isOptedOut(user);
    }

    private Group getOptSomethingGroup(PersistentGroup persistentGroup) {
        if (persistentGroup == null){
            return Group.nobody();
        }
        return persistentGroup.toGroup();
    }

    public Group getOptedOutGroup() {
        return getOptSomethingGroup(getPersistentOptedOutGroup());
    }

    public Group getOptOutAvailable() {
        return getOptSomethingGroup(getPersistentOptOutAvailableGroup());
    }

    public void setOptOutAvailable(Group group){
        setPersistentOptOutAvailableGroup(group == null ? null : group.toPersistentGroup());
    }

    public void setOptedOutGroup(Group group){
        setPersistentOptedOutGroup(group == null ? null : group.toPersistentGroup());
    }

    public void optOut(User user){
        if (user != null){
            setOptedOutGroup(getOptedOutGroup().grant(user));
        }
    }

    public void optIn(User user){
        if (user != null){
            setOptedOutGroup(getOptedOutGroup().revoke(user));
        }
    }

    public static final class Util {

        private static final String MAIL_LIST_SEPARATOR = "\\s*,\\s*";
        private static final Joiner MAIL_LIST_JOINER = Joiner.on(",").skipNulls();

        public static boolean isValidEmail(final String email) {
            if (email == null) {
                return false;
            }
            try {
                new InternetAddress(email).validate();
                return true;
            } catch (AddressException ex) {
                return false;
            }
        }

        public static Set<String> toEmailSet(Collection<PersistentGroup> groups) {
            return groups.stream().flatMap(g -> g.getMembers()).map(User::getProfile).filter(Objects::nonNull)
                    .map(UserProfile::getEmail).filter(e -> !Strings.isNullOrEmpty(e)).collect(Collectors.toSet());
        }

        public static Set<String> toEmailSet(String emails) {
            return Strings.isNullOrEmpty(emails) ? Sets.newHashSet() : Stream.of(emails.split(MAIL_LIST_SEPARATOR))
                    .filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        }

        public static String toEmailListString(Collection<String> emails) {
            return emails == null ? "" : MAIL_LIST_JOINER
                    .join(emails.stream().filter(e -> !Strings.isNullOrEmpty(e)).collect(Collectors.toSet()));
        }

        protected static <T> void builderSetAdd(Stream<T> stream, Predicate<T> filter, Set<T> set) {
            stream.filter(filter).forEach(set::add);
        }

        protected static <T> void builderSetAdd(T[] array, Predicate<T> filter, Set<T> set) {
            builderSetAdd(Arrays.stream(array), filter, set);
        }

        protected static <T> void builderSetCopy(Collection<T> collection, Predicate<T> filter, Set<T> set) {
            set.clear();
            builderSetAdd(collection.stream(), filter, set);
        }
    }
}
