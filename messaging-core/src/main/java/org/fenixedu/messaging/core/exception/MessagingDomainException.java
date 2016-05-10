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
package org.fenixedu.messaging.core.exception;

import javax.ws.rs.core.Response;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;

public class MessagingDomainException extends DomainException {

    private static final long serialVersionUID = -8622024813103819898L;
    protected static final String BUNDLE = "MessagingResources";

    protected MessagingDomainException(String bundle, String key, String... args) {
        super(bundle, key, args);
    }

    protected MessagingDomainException(Response.Status status, String bundle, String key, String... args) {
        super(status, bundle, key, args);
    }

    protected MessagingDomainException(Throwable cause, String bundle, String key, String... args) {
        super(cause, bundle, key, args);
    }

    protected MessagingDomainException(Throwable cause, Response.Status status, String bundle, String key, String... args) {
        super(cause, status, bundle, key, args);
    }

    public static MessagingDomainException malformedTemplate(Exception e, String key) {
        return new MessagingDomainException(e, BUNDLE, "error.template.malformed", key);
    }

    public static MessagingDomainException nullSender() {
        return new MessagingDomainException(BUNDLE, "error.message.null.sender");
    }

    public static MessagingDomainException invalidAddress() {
        return new MessagingDomainException(BUNDLE, "error.sender.invalid.address");
    }

    public static MessagingDomainException forbidden() {
        return new MessagingDomainException(Response.Status.FORBIDDEN, BUNDLE, "error.not.authorized");
    }

    public static MessagingDomainException missingTemplate(String key) {
        return new MessagingDomainException(Response.Status.NOT_FOUND, BUNDLE, "error.template.missing", key);
    }

}
