package org.fenixedu.messaging.core.domain;

import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;

@Task(englishTitle = "Message Task", readOnly = true)
public class MessageTask extends CronTask {
    @Override
    public void runTask() {
        MessagingSystem.pruneMessages();
        int dispatched = 0;
        for (final Message message : MessagingSystem.getInstance().getMessagePendingDispatchSet()) {
            long start = System.currentTimeMillis();
            MessageDispatchReport report = MessagingSystem.dispatch(message);
            if (report != null) {
                getLogger().info("Dispatched message: {} in {}ms for {} emails", message.getExternalId(),
                        System.currentTimeMillis() - start, report.getTotalCount());
                dispatched++;
            }
        }
        taskLog("Dispatched %d messages\n", dispatched);
    }
}
