package org.fenixedu.messaging.domain;

import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.messaging.MessagingSystemConfiguration;

@Task(englishTitle = "Message Task", readOnly = true)
public class MessageTask extends CronTask {
    @Override
    public void runTask() {
        int deleted = MessagingSystem.deleteOldMessages();
        taskLog("Deleted %d old messages sent %d days after send\n", deleted, MessagingSystemConfiguration.getConfiguration()
                .daysToKeepSentMessages());
        int dispatched = 0;
        for (final Message message : MessagingSystem.getInstance().getMessagePendingDispatchSet()) {
            long start = System.currentTimeMillis();
            MessageDispatchReport report = MessagingSystem.dispatch(message);
            getLogger().info("Dispatched message: {} in {}ms for {} emails", message.getExternalId(),
                    System.currentTimeMillis() - start, report.getTotalCount());
            dispatched++;
        }
        taskLog("Dispatched %d messages\n", dispatched);
    }
}
