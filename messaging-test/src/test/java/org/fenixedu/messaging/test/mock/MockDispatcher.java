package org.fenixedu.messaging.test.mock;

import com.google.common.collect.Sets;
import org.fenixedu.messaging.core.dispatch.MessageDispatcher;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.domain.MessageDispatchReport;
import org.fenixedu.messaging.test.domain.MockAsyncEmailMessageDispatchReport;
import org.fenixedu.messaging.test.domain.MockEmailMessageDispatchReport;

public class MockDispatcher implements MessageDispatcher {
    private boolean async;

    public MockDispatcher() {
        async = false;
    }

    public MockDispatcher(boolean async) {
        this.async = async;
    }

    @Override
    public MessageDispatchReport dispatch(Message message) {
        int mailCount = Sets.union(Sets.union(message.getTos(),message.getCcs()), message.getBccs()).size();
        return async ? new MockAsyncEmailMessageDispatchReport(mailCount) : new MockEmailMessageDispatchReport(mailCount);
    }
}
