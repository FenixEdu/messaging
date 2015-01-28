package org.fenixedu.messaging.domain;

import org.joda.time.DateTime;

public abstract class MessageDispatchReport extends MessageDispatchReport_Base {
    public MessageDispatchReport() {
        super();
        setStartedDelivery(new DateTime());
    }

    @Override
    public Message getMessage() {
        //FIXME remove when framework support read-only properties
        return super.getMessage();
    }

    public boolean isFinished() {
        return getTotalCount() == getFailedCount() + getInvalidCount() + getDeliveredCount();
    }

    public void delete() {
        setMessage(null);
        deleteDomainObject();
    }
}
