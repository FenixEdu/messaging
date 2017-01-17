package org.fenixedu.messaging.core.task;

import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.domain.MessageDispatchReport;
import org.fenixedu.messaging.core.domain.MessagingSystem;
import org.fenixedu.messaging.core.domain.Sender;

@Task(englishTitle = "Message Task", readOnly = true)
public class MessageTask extends CronTask {
    @Override
    public void runTask() {
        int dispatched = 0;
        for (final Message message : MessagingSystem.getPendingMessages()) {
            long start = System.currentTimeMillis();
            MessageDispatchReport report = MessagingSystem.dispatch(message);
            if (report != null) {
                getLogger().info("Dispatched message: {} in {}ms for {} addresses", message.getExternalId(), System
                        .currentTimeMillis() - start, report.getTotalCount());
                dispatched++;
            }
        }
        if (dispatched > 0) {
            taskLog("Dispatched %d messages\n", dispatched);
        }

        int pruned = 0;
        for (Sender sender : Sender.all()) {
            int diff = sender.getMessageSet().size();
            sender.pruneMessages();
            diff = diff - sender.getMessageSet().size();
            if (diff > 0) {
                getLogger().info("Pruned sender: {} for {} messages", sender.getExternalId(), diff);
                pruned += diff;
            }
        }
        if (pruned > 0) {
            taskLog("Pruned %d messages\n", pruned);
        }
    }
}
