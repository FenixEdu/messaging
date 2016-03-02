package org.fenixedu.messaging.core.dispatch;

import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.domain.MessageDispatchReport;

public interface MessageDispatcher {
    public MessageDispatchReport dispatch(Message message);
}
