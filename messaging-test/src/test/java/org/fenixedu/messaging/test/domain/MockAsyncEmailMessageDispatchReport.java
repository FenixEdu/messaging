package org.fenixedu.messaging.test.domain;

import org.fenixedu.messaging.core.domain.MessageDispatchReport;
import org.joda.time.DateTime;

import static pt.ist.fenixframework.FenixFramework.atomic;

public class MockAsyncEmailMessageDispatchReport extends MockAsyncEmailMessageDispatchReport_Base {

    public class SendingThread extends Thread {
        public int nMails;
        public MessageDispatchReport report;

        public SendingThread(int nMails, MessageDispatchReport report) {
            this.nMails = nMails;
            this.report = report;
        }

        @Override
        public void run() {
            atomic(() -> report.setStartedDelivery(DateTime.now()));
            int[] status = {0,0,0};
            int times = nMails;
            while (times-- > 0) { status[(int) Math.round(Math.random() * 2)]++; }
            try { Thread.sleep(Math.round(Math.random()*nMails*1000)); } catch (InterruptedException e) {}
            atomic(() ->  {
                report.setFailedCount(status[0]);
                report.setDeliveredCount(status[1]);
                report.setInvalidCount(status[2]);
                report.setFinishedDelivery(DateTime.now());
            });
        };
    }

    public MockAsyncEmailMessageDispatchReport(int nMails) {
        super();
        setTotalCount(nMails);
        new SendingThread(nMails, this).start();
    }
}
