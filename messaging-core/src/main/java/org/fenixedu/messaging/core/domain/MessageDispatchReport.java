package org.fenixedu.messaging.core.domain;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

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

    @Override
    public DateTime getStartedDelivery() {
        // FIXME remove when the framework supports read-only properties
        return super.getStartedDelivery();
    }

    public boolean isFinished() {
        return getTotalCount() == getResolvedCount();
    }

    public int getResolvedCount() {
        return getFailedCount() + getInvalidCount() + getDeliveredCount();
    }

    @Atomic(mode = TxMode.WRITE)
    public void delete() {
        setMessage(null);
        deleteDomainObject();
    }
}
