package org.fenixedu.messaging.emaildispatch.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.mail.MessagingException;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.Group;
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

    private static Map<Locale, Set<String>> emailsByPreferredLocale(Set<Group> groups, Predicate<String> emailValidator) {
        Map<Locale, Set<String>> emails = new HashMap<Locale, Set<String>>();
        Locale defLocale = Locale.getDefault();
        groups.stream().flatMap(g -> g.getMembers().stream()).map(User::getProfile).distinct()
                .filter(p -> emailValidator.test(p.getEmail())).forEach(p -> {
                    Locale l = p.getPreferredLocale();
                    if (l == null) {
                        l = defLocale;
                    }
                    emails.computeIfAbsent(l, k -> new HashSet<>()).add(p.getEmail());
                });
        return emails;
    }

    public static LocalEmailMessageDispatchReport dispatch(Message message) {
        List<String> invalids = new ArrayList<String>();
        EmailBlacklist blacklist = EmailBlacklist.getInstance();
        Predicate<String> validator = email -> {
            if (isValid(email)) {
                return true;
            }
            invalids.add(email);
            return false;
        };

        Map<Locale, Set<String>> tos = emailsByPreferredLocale(message.getToGroup(), validator), ccs =
                emailsByPreferredLocale(message.getCcGroup(), validator), bccs =
                emailsByPreferredLocale(message.getBccGroup(), validator);
        Set<String> extraBccs = bccs.computeIfAbsent(message.getExtraBccsLocale(), k -> new HashSet<>());
        message.getExtraBccsSet().stream().filter(validator).forEach(extraBccs::add);

        invalids.stream().distinct().forEach(blacklist::addInvalidAddress);

        return new LocalEmailMessageDispatchReport(MimeMessageHandler.create(tos, ccs, bccs), Stream.of(tos, ccs, bccs)
                .flatMap(m -> m.values().stream()).mapToInt(Set::size).sum(), invalids.size());
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
