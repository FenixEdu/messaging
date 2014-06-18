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
package org.fenixedu.messaging.domain;

import org.fenixedu.bennu.core.domain.Bennu;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

/**
 *
 * @author Luis Cruz
 *
 */
public class MessagingSystem extends MessagingSystem_Base {
    private MessagingSystem() {
        super();
        setBennu(Bennu.getInstance());
        setPersistentSenderGroup(new PersistentSenderGroup());
    }

    public static MessagingSystem getInstance() {
        if (Bennu.getInstance().getMessagingSystem() == null) {
            return createMessagingSystem();
        }
        return Bennu.getInstance().getMessagingSystem();
    }

    @Atomic(mode = TxMode.WRITE)
    private static MessagingSystem createMessagingSystem() {
        if (Bennu.getInstance().getMessagingSystem() == null) {
            return new MessagingSystem();
        }
        return Bennu.getInstance().getMessagingSystem();
    }

    private static MessageDispatcher dispatcher = null;

    public static void setMessageDispatcher(MessageDispatcher dispatcher) {
        MessagingSystem.dispatcher = dispatcher;
    }

    @Atomic(mode = TxMode.WRITE)
    public static void deleteOldMessages() {
        for (Sender sender : MessagingSystem.getInstance().getSenderSet()) {
            sender.pruneOldMessages();
        }
    }

    @Atomic(mode = TxMode.WRITE)
    public static MessageDispatchReport dispatch(Message message) {
        MessageDispatchReport report = dispatcher.dispatch(message);
        message.setMessagingSystemFromPendingDispatch(null);
        message.setDispatchReport(report);
        return report;
    }

    public static Sender systemSender() {
        return getInstance().getSystemSender();
    }
}
