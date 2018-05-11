package org.fenixedu.messaging.core.task;

import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.messaging.core.domain.Sender;

@Task(englishTitle = "Delete Orphan MessageFiles Task")
public class DeleteOrphanMessageFiles extends CronTask {
    @Override public void runTask() throws Exception {
        taskLog("Start pruning orphan uploaded files...");
        Sender.all().forEach(Sender::pruneUploadedFiles);
        taskLog("Task complete.");
    }
}
