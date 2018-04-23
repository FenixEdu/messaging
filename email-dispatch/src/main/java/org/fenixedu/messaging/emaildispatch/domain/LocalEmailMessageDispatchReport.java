package org.fenixedu.messaging.emaildispatch.domain;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.mail.MessagingException;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.UserProfile;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.domain.MessagingSystem;
import org.fenixedu.messaging.emaildispatch.EmailDispatchConfiguration;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class LocalEmailMessageDispatchReport extends LocalEmailMessageDispatchReport_Base {
    private static final Logger logger = LoggerFactory.getLogger(LocalEmailMessageDispatchReport.class);
    private static final boolean RECIPIENTS_AS_BCCS = EmailDispatchConfiguration.getConfiguration().recipientsAsBccs();

    public LocalEmailMessageDispatchReport(Collection<MimeMessageHandler> handlers, Integer validCount, Integer invalidCount) {
        super();
        getHandlerSet().addAll(handlers);
        setTotalCount(validCount + invalidCount);
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
        } else if (getQueue() != null) {
            finishUpDelivery();
        }
    }

    @Atomic(mode = TxMode.WRITE)
    private void finishUpDelivery() {
        setFinishedDelivery(new DateTime());
        setQueue(null);
    }

    public static LocalEmailMessageDispatchReport dispatch(Message message) {
        List<String> invalids = new ArrayList<String>();
        EmailBlacklist blacklist = EmailBlacklist.getInstance();
        Predicate<String> validator = email -> {
            boolean valid = MessagingSystem.Util.isValidEmail(email);
            if (!valid) {
                invalids.add(email);
            }
            return valid;
        };
        Set<UserProfile> tos = getProfiles(message.getToGroups()), ccs = getProfiles(message.getCcGroups()), bccs =
                getProfiles(message.getBccGroups());
        Collection<MimeMessageHandler> handlers;
        int valids;

        Map<Locale, Set<String>> tosByLocale, ccsByLocale, bccsByLocale;
        Locale defLocale = message.getPreferredLocale();
        Set<Locale> messageLocales = message.getContentLocales();

        if (RECIPIENTS_AS_BCCS) {
            bccs.addAll(tos);
            bccs.addAll(ccs);

            tosByLocale = Maps.newHashMap();
            ccsByLocale = Maps.newHashMap();
            bccsByLocale = emailsByMessageLocale(bccs, validator, defLocale, messageLocales);
            Set<String> singleBccs = message.getSingleBccsSet().stream().filter(validator).collect(Collectors.toSet());
            bccsByLocale.computeIfAbsent(message.getPreferredLocale(), k -> new HashSet<>()).addAll(singleBccs);
        } else {
            //XXX force disjoint recipient lists - priority order: tos > ccs > bccs > single bccs
            ccs.removeAll(tos);
            bccs.removeAll(tos);
            bccs.removeAll(ccs);
            Set<String> singleBccs = message.getSingleBccsSet();
            tos.stream().map(UserProfile::getEmail).forEach(singleBccs::remove);
            ccs.stream().map(UserProfile::getEmail).forEach(singleBccs::remove);
            bccs.stream().map(UserProfile::getEmail).forEach(singleBccs::remove);

            tosByLocale = emailsByMessageLocale(tos, validator, defLocale, messageLocales);
            ccsByLocale = emailsByMessageLocale(ccs, validator, defLocale, messageLocales);
            bccsByLocale = emailsByMessageLocale(bccs, validator, defLocale, messageLocales);
            singleBccs = singleBccs.stream().filter(validator).collect(Collectors.toSet());
            bccsByLocale.computeIfAbsent(message.getPreferredLocale(), k -> new HashSet<>()).addAll(singleBccs);
        }

        handlers = MimeMessageHandler.create(tosByLocale, ccsByLocale, bccsByLocale);
        valids = Stream.of(tosByLocale, ccsByLocale, bccsByLocale).flatMap(m -> m.values().stream()).mapToInt(Collection::size)
                .sum();

        invalids.stream().forEach(blacklist::addInvalidAddress);

        return new LocalEmailMessageDispatchReport(handlers, valids, invalids.size());
    }

    private static Map<Locale, Set<String>> emailsByMessageLocale(Set<UserProfile> users, Predicate<String> emailValidator,
            Locale defLocale, Set<Locale> messageLocales) {
        Map<Locale, Set<String>> emails = new HashMap<>();
        users.stream().filter(p -> emailValidator.test(p.getEmail())).forEach(p -> {
            Locale locale = p.getEmailLocale();
            if (locale == null || !messageLocales.contains(locale)) {
                locale = defLocale;
            }
            emails.computeIfAbsent(locale, k -> new HashSet<>()).add(p.getEmail());
        });
        return emails;
    }

    private static Set<UserProfile> getProfiles(Set<Group> groups) {
        return groups.stream().flatMap(Group::getMembers)
                .filter(MessagingSystem.getInstance()::isOptedIn)
                .map(User::getProfile).filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public void delete() {
        setQueue(null);
        getHandlerSet().forEach(MimeMessageHandler::delete);
        super.delete();
    }
}
