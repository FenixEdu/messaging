package org.fenixedu.messaging.emaildispatch.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.mail.MessagingException;

import org.fenixedu.messaging.domain.Message;
import org.fenixedu.messaging.domain.MessagingSystem;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class LocalEmailMessageDispatchReport extends LocalEmailMessageDispatchReport_Base {
    private static final Logger logger = LoggerFactory.getLogger(LocalEmailMessageDispatchReport.class);

    public LocalEmailMessageDispatchReport(Set<MimeMessageHandler> handlers, Integer totalCount, Integer invalidCount) {
        super();
        getHandlerSet().addAll(handlers);
        setTotalCount(totalCount);
        setDeliveredCount(0);
        setFailedCount(0);
        setInvalidCount(invalidCount);
        setQueue(MessagingSystem.getInstance());
    }

    @Override
    public boolean isFinished() {
        return getHandlerSet().isEmpty();
    }

    public void deliver() {
        if (!isFinished()) {
            for (MimeMessageHandler handler : getHandlerSet()) {
                try {
                    handler.deliver();
                } catch (MessagingException e) {
                    logger.error("Error sending message", e);
                }
            }
            if (isFinished()) {
                if (!super.isFinished()) {
                    logger.error("Numbers are not right: total {} delivered {} invalid {} failed {}", getTotalCount(),
                            getDeliveredCount(), getInvalidCount(), getFailedCount());
                }
                finishUpDelivery();
            }
        }
    }

    @Atomic(mode = TxMode.WRITE)
    private void finishUpDelivery() {
        setFinishedDelivery(new DateTime());
        setQueue(null);
    }

    @Override
    public void delete() {
        setQueue(null);
        for (MimeMessageHandler handler : getHandlerSet()) {
            handler.delete();
        }
        super.delete();
    }

    public static LocalEmailMessageDispatchReport dispatch(Message message) {
        List<String> invalids = new ArrayList<String>();
        Predicate<String> countingBlackListValidator = email -> {
            if (isValid(email)) {
                return true;
            }
            invalids.add(email);
            EmailBlacklist.getInstance().addInvalidAddress(email);
            return false;
        };
        Map<Locale, Set<String>> tos = message.getTosByLocale(countingBlackListValidator);
        Map<Locale, Set<String>> ccs = message.getCcsByLocale(countingBlackListValidator);
        Map<Locale, Set<String>> bccs = message.getBccsByLocale(countingBlackListValidator);

        return new LocalEmailMessageDispatchReport(MimeMessageHandler.create(tos, ccs, bccs), tos.size() + ccs.size()
                + bccs.size(), invalids.size());
    }

    private static boolean isValid(String email) {
        if ((email == null) || (email.length() == 0)) {
            return false;
        }

        if (email.indexOf(' ') > 0) {
            return false;
        }

        String[] atSplit = email.split("@");
        if (atSplit.length != 2) {
            return false;
        } else if ((atSplit[0].length() == 0) || (atSplit[1].length() == 0)) {
            return false;
        }

        String domain = new String(atSplit[1]);

        if (domain.lastIndexOf('.') == (domain.length() - 1)) {
            return false;
        }

        if (domain.indexOf('.') <= 0) {
            return false;
        }

        return true;
    }
}
