package org.fenixedu.messaging.core.task;

import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.messaging.core.domain.MessageTemplate;

@Task(englishTitle = "Delete Undeclared Templates Task", readOnly = true)
public class DeleteUndeclaredTemplates extends CronTask{

    @Override
    public void runTask() {
        MessageTemplate.undeclared().forEach(MessageTemplate::delete);
    }
}
