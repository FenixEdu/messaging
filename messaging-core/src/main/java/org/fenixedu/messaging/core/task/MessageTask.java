package org.fenixedu.messaging.core.task;

import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.domain.MessageDispatchReport;
import org.fenixedu.messaging.core.domain.MessagingSystem;
import org.fenixedu.messaging.core.domain.Sender;

import java.util.HashSet;
import java.util.Set;

@Task(englishTitle = "Message Task", readOnly = true)
public class MessageTask extends CronTask {
    @Override
    public void runTask() {
        Set<Sender> sendersFromMessages = new HashSet<>();
        int dispatched = 0;
        for (final Message message : MessagingSystem.getPendingMessages()) {
            long start = System.currentTimeMillis();
            MessageDispatchReport report = MessagingSystem.dispatch(message);
            if (report != null) {
                getLogger().info("Dispatched message: {} in {}ms for {} addresses", message.getExternalId(), System
                        .currentTimeMillis() - start, report.getTotalCount());
                dispatched++;
                sendersFromMessages.add(message.getSender());
            }
        }
        if (dispatched > 0) {
            taskLog("Dispatched %d messages\n", dispatched);
        }

        taskLog("%d possible senders to prune", sendersFromMessages.size());
        int pruned = 0;
        for (Sender sender : sendersFromMessages){
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
