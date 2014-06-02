/*
 * @(#)ReplyTo.java
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

import pt.ist.bennu.core.domain.User;

/**
 * 
 * @author Luis Cruz
 * 
 */
public abstract class ReplyTo extends ReplyTo_Base {

    protected ReplyTo() {
        super();
    }

    public void delete() {
        getMessageSet().clear();
        getSenderSet().clear();
        deleteDomainObject();
    }

    public abstract String getReplyToAddress(final User user);

    public abstract String getReplyToAddress();

    public Collection<? extends ReplyTo> asCollection() {
        return Collections.singletonList(this);
    }

    @Deprecated
    public java.util.Set<pt.ist.messaging.domain.Sender> getSender() {
        return getSenderSet();
    }

    @Deprecated
    public java.util.Set<pt.ist.messaging.domain.Message> getMessage() {
        return getMessageSet();
    }

}
