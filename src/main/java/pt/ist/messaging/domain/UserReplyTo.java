/*
 * @(#)UserReplyTo.java
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

import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.exceptions.DomainException;
import pt.ist.fenixframework.Atomic;

/**
 * 
 * @author Luis Cruz
 * 
 */
public class UserReplyTo extends UserReplyTo_Base {

    public UserReplyTo(final User user) {
        super();
        if (user.getUserReplyTo() != null) {
            throw new DomainException("error.person.already.has.reply.to");
        }
        setUser(user);
    }

    @Atomic
    public static UserReplyTo createFor(final User user) {
        return user.getUserReplyTo() != null ? user.getUserReplyTo() : new UserReplyTo(user);
    }

    @Override
    public String getReplyToAddress(final User user) {
        return user.getEmail();
    }

    @Override
    public String getReplyToAddress() {
        return getUser().getEmail();
    }

    @Override
    public void delete() {
        setUser(null);
        super.delete();
    }

}
