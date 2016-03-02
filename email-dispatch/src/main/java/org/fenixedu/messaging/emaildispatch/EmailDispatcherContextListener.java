package org.fenixedu.messaging.emaildispatch;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.fenixedu.messaging.dispatch.MessageDispatcher;
import org.fenixedu.messaging.domain.Message;
import org.fenixedu.messaging.domain.MessageDispatchReport;
import org.fenixedu.messaging.domain.MessagingSystem;
import org.fenixedu.messaging.emaildispatch.domain.LocalEmailMessageDispatchReport;

@WebListener
public class EmailDispatcherContextListener implements ServletContextListener {
    public static class LocalEmailMessageDispatcher implements MessageDispatcher {
        @Override
        public MessageDispatchReport dispatch(Message message) {
            return LocalEmailMessageDispatchReport.dispatch(message);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        MessagingSystem.setMessageDispatcher(new LocalEmailMessageDispatcher());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        MessagingSystem.setMessageDispatcher(null);
    }
}
