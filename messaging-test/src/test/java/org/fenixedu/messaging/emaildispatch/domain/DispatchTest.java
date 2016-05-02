package org.fenixedu.messaging.emaildispatch.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import pt.ist.fenixframework.test.core.FenixFrameworkRunner;

@RunWith(FenixFrameworkRunner.class)
public class DispatchTest {

    @Test
    public void deletion() {
        //FIXME XXX object deletion cannot be tested with current backend;
        /*
            MessagingSystem sys = MessagingSystem.getInstance();
            Sender s = SenderTest.newEmptySender();
            Message m = MessageTest.newEmptyMessage(s);
            MessagingSystem.dispatch(m);
            MessageDispatchReport report = m.getDispatchReport();
            m.delete();
            assertNull(m.getDispatchReport());
            assertFalse(sys.getUnfinishedReportsSet().contains(report));
            assertTrue(((LocalEmailMessageDispatchReport)report).getHandlerSet().isEmpty());
        */
    }
}
