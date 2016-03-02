package org.fenixedu.messaging.dispatch;

import org.fenixedu.messaging.domain.Message;
import org.fenixedu.messaging.domain.MessageDispatchReport;

public interface MessageDispatcher {
    public MessageDispatchReport dispatch(Message message);
}
