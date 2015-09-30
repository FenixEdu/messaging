/*
 * @(#)MessagingDomainException.java
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
package org.fenixedu.messaging.exception;

import javax.ws.rs.core.Response;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;

public class MessagingDomainException extends DomainException {

    private static final long serialVersionUID = -8622024813103819898L;
    protected static final String BUNDLE = "MessagingResources";

    protected MessagingDomainException(Response.Status status, String bundle, String key, String... args) {
        super(status, bundle, key, args);
    }

    public static MessagingDomainException forbidden() {
        return new MessagingDomainException(Response.Status.FORBIDDEN, BUNDLE, "error.not.authorized");
    }

    public static MessagingDomainException missingTemplate(String id) {
        return new MessagingDomainException(Response.Status.NOT_FOUND, BUNDLE, "error.template.missing", id);
    }

}
