package org.fenixedu.messaging.exception;

import java.util.Set;

import org.springframework.util.StringUtils;

public class InvalidMessageException extends Exception {

    private static final long serialVersionUID = 3285386864762895722L;

    public InvalidMessageException(Set<String> errors) {
        super(System.lineSeparator() + StringUtils.collectionToDelimitedString(errors, System.lineSeparator()));
    }

}
