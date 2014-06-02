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

import pt.ist.bennu.core.domain.MyOrg;
import pt.ist.fenixframework.Atomic;

/**
 * 
 * @author Luis Cruz
 * 
 */
public class MessagingSystem extends MessagingSystem_Base {

    private MessagingSystem() {
        super();
        setMyOrg(MyOrg.getInstance());
    }

    public static MessagingSystem getInstance() {
        final MyOrg myOrg = MyOrg.getInstance();
        return myOrg.getMessagingSystem() != null ? myOrg.getMessagingSystem() : createMessagingSystem(myOrg);
    }

    @Atomic
    private static MessagingSystem createMessagingSystem(final MyOrg myOrg) {
        return myOrg.getMessagingSystem() != null ? myOrg.getMessagingSystem() : new MessagingSystem();
    }

    @Deprecated
    public java.util.Set<org.fenixedu.messaging.domain.Sender> getSender() {
        return getSenderSet();
    }

    @Deprecated
    public java.util.Set<org.fenixedu.messaging.domain.Message> getMessage() {
        return getMessageSet();
    }

    @Deprecated
    public java.util.Set<org.fenixedu.messaging.domain.Message> getMessagePendingDispatch() {
        return getMessagePendingDispatchSet();
    }

}
