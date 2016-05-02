package org.fenixedu.messaging.test.domain;

import org.joda.time.DateTime;

public class MockEmailMessageDispatchReport extends MockEmailMessageDispatchReport_Base {
    public MockEmailMessageDispatchReport(int nMails) {
        super();
        setTotalCount(nMails);
        setStartedDelivery(DateTime.now());
        int[] status = {0,0,0};
        while (nMails-- > 0) { status[(int) Math.round(Math.random() * 2)]++; }
        setFailedCount(status[0]);
        setDeliveredCount(status[1]);
        setInvalidCount(status[2]);
        setFinishedDelivery(DateTime.now());
    }
}