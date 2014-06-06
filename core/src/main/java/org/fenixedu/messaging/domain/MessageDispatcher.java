package org.fenixedu.messaging.domain;

public interface MessageDispatcher {
    public MessageDispatchReport dispatch(Message message);
}
