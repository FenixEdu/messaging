package org.fenixedu.messaging.core.task;

import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.domain.MessageDispatchReport;
import org.fenixedu.messaging.core.domain.MessagingSystem;

@Task(englishTitle = "Message Task", readOnly = true)
public class MessageTask extends CronTask {
    @Override
    public void runTask() {
        final long dispatched = MessagingSystem.getPendingMessages().stream()
                .mapToLong(this::dispatch)
                .count();
        if (dispatched > 0) {
            taskLog("Dispatched %d messages\n", dispatched);
        }
    }

    private long dispatch(final Message message) {
        final long start = System.currentTimeMillis();
        final MessageDispatchReport report = MessagingSystem.dispatch(message);
        if (report != null) {
            getLogger().info("Dispatched message: {} in {}ms for {} addresses", message.getExternalId(), System
                    .currentTimeMillis() - start, report.getTotalCount());
            return 1;
        }
        return 0;
    }
}
