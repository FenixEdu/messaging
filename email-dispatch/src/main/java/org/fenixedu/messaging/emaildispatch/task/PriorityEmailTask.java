/*
 * @(#)EmailTask.java
 *
 * Copyright 2012 Instituto Superior Tecnico
 * Founding Authors: Luis Cruz
 *
 *      https://fenix-ashes.ist.utl.pt/
 *
 *   This file is part of the E-mail SMTP Adapter Module.
 *
 *   The E-mail SMTP Adapter Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version
 *   3 of the License, or (at your option) any later version.
 *
 *   The E-mail Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the E-mail Module. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.fenixedu.messaging.emaildispatch.task;

import org.fenixedu.bennu.scheduler.CronTask;
import org.fenixedu.bennu.scheduler.annotation.Task;
import org.fenixedu.messaging.core.domain.MessagingSystem;
import org.fenixedu.messaging.emaildispatch.domain.LocalEmailMessageDispatchReport;
import pt.ist.fenixframework.FenixFramework;

@Task(englishTitle = "Email Sender For System", readOnly = true)
public class PriorityEmailTask extends CronTask {
    @Override
    public void runTask() throws Exception {
        MessagingSystem.getInstance().getUnfinishedReportsSet()
                .parallelStream()
                .forEach(this::process);
    }

    private void process(final LocalEmailMessageDispatchReport report) {
        FenixFramework.atomic(() -> {
            if (report.getMessage().getSender() == MessagingSystem.systemSender()) {
                report.deliver();
            }
        });
    }
}
