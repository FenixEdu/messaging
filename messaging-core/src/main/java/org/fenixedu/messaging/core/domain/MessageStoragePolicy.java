package org.fenixedu.messaging.core.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.map.ReferenceMap;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.I18N;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import com.google.common.base.Joiner;

public class MessageStoragePolicy implements Serializable {
    private static final long serialVersionUID = 1535994777149570075L;
    private static final String BUNDLE = "MessagingResources", SERIALIZATION_SEPARATOR = "\\s*,\\s*", ALL_PREFIX = "A",
            NONE_PREFIX = "N", AMOUNT_PREFIX = "Q", PERIOD_PREFIX = "P";
    private static final Joiner PRESENTATION_JOINER = Joiner.on(" "), SERIALIZATION_JOINER = Joiner.on(",");
    private static final MessageStoragePolicy ALL = new MessageStoragePolicy(null, null), NONE =
            new MessageStoragePolicy(0, null);
    private static final ReferenceMap POLICIES;

    static {
        POLICIES = new ReferenceMap();
        POLICIES.put(ALL_PREFIX, ALL);
        POLICIES.put(NONE_PREFIX, NONE);
    }

    private Period period;
    private Integer amount;

    protected MessageStoragePolicy(Integer amount, Period period) {
        this.period = period;
        if(amount != null && amount < 0) {
            throw new IllegalArgumentException("Message storage policy amount cannot be negative.");
        }
        this.amount = amount;
    }

    public static MessageStoragePolicy keep(Integer amount, Period period) {
        String serialization = serialize(amount, period);
        MessageStoragePolicy policy = (MessageStoragePolicy) POLICIES.get(serialization);
        if (policy != null) {
            return policy;
        }
        policy = new MessageStoragePolicy(amount, period);
        POLICIES.put(serialization, policy);
        return policy;
    }

    public static MessageStoragePolicy keep(Period period) {
        return keep(null, period);
    }

    public static MessageStoragePolicy keep(Integer amount) {
        return keep(amount, null);
    }

    public static MessageStoragePolicy keepAll() {
        return ALL;
    }

    public static MessageStoragePolicy keepNone() {
        return NONE;
    }

    public Integer getAmount() {
        return amount;
    }

    public Period getPeriod() {
        return period;
    }

    public boolean isKeepAll() {
        return isKeepAll(amount, period);
    }

    public boolean isKeepNone() {
        return isKeepNone(amount);
    }

    protected static boolean isKeepAll(Integer amount, Period period) {
        return amount == null && period == null;
    }

    protected static boolean isKeepNone(Integer amount) {
        return amount != null && amount.intValue() == 0;
    }

    protected void pruneMessages(Sender sender) {
        Set<Message> sent = sender.getMessageSet().stream().filter(m -> m.getSent() != null).collect(Collectors.toSet());
        if (!isKeepAll()) {
            if (!isKeepNone()) {
                Stream<Message> keep = sent.stream();
                if (period != null) {
                    DateTime cut = DateTime.now().minus(period);
                    keep = keep.filter(m -> m.getCreated().isAfter(cut));
                }
                if (amount != null) {
                    keep = keep.sorted().limit(amount);
                }
                sent.removeAll(keep.collect(Collectors.toSet()));
            }
            sent.forEach(Message::delete);
        }
    }

    public static MessageStoragePolicy internalize(String serialization) {
        String[] attrs = serialization.split(SERIALIZATION_SEPARATOR);
        Integer amount = null;
        Period period = null;
        for (String attr : attrs) {
            switch (attr.substring(0, 1)) {
            case AMOUNT_PREFIX:
                amount = Integer.valueOf(attr.substring(1));
                break;
            case PERIOD_PREFIX:
                period = Period.parse(attr);
                break;
            case ALL_PREFIX:
                return keepAll();
            case NONE_PREFIX:
                return keepNone();
            }
        }
        return keep(amount, period);
    }

    public static MessageStoragePolicy internalize(String[] parts) {
        return internalize(SERIALIZATION_JOINER.join(parts));
    }

    public String serialize() {
        return serialize(amount, period);
    }

    protected static String serialize(Integer amount, Period period) {
        if (isKeepAll(amount, period)) {
            return ALL_PREFIX;
        }
        if (isKeepNone(amount)) {
            return NONE_PREFIX;
        }
        List<String> parts = new ArrayList<>();
        if (period != null) {
            parts.add(period.toString());
        }
        if (amount != null) {
            parts.add(AMOUNT_PREFIX + amount.intValue());
        }
        return SERIALIZATION_JOINER.join(parts);
    }

    @Override
    public String toString() {
        String action = BundleUtil.getString(BUNDLE, "name.storage.policy");
        if (isKeepAll()) {
            return PRESENTATION_JOINER.join(action, BundleUtil.getString(BUNDLE, "name.storage.policy.all"));
        }
        if (isKeepNone()) {
            return PRESENTATION_JOINER.join(action, BundleUtil.getString(BUNDLE, "name.storage.policy.none"));
        }
        List<String> parts = new ArrayList<>();
        parts.add(action);
        if (amount != null) {
            parts.add(BundleUtil.getString(BUNDLE, "name.storage.policy.amount", Integer.toString(amount)));
        }
        if (period != null) {
            parts.add(BundleUtil
                    .getString(BUNDLE, "name.storage.policy.period", PeriodFormat.wordBased(I18N.getLocale()).print(period)));
        }
        return PRESENTATION_JOINER.join(parts);
    }
}
