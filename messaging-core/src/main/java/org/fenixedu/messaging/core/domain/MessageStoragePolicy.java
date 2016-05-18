package org.fenixedu.messaging.core.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.I18N;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import com.google.common.base.Joiner;

public class MessageStoragePolicy implements Serializable {
    private static final long serialVersionUID = 1535994777149570075L;
    private static final String BUNDLE = "MessagingResources", SERIALIZATION_SEPARATOR = "\\s*,\\s*";
    private static final Joiner PRESENTATION_JOINER = Joiner.on(" "), SERIALIZATION_JOINER = Joiner.on(",");
    private static final MessageStoragePolicy ALL = new MessageStoragePolicy(null, null), NONE =
            new MessageStoragePolicy(0, null);

    private Period keepPeriod;
    private Integer keepAmount;

    protected MessageStoragePolicy(Integer amount, Period period) {
        this.keepPeriod = period;
        this.keepAmount = amount;
    }

    public static MessageStoragePolicy keep(Integer amount, Period period) {
        if (amount == null && period == null) {
            return keepAll();
        }
        if (amount != null && amount <= 0) {
            return keepNone();
        }
        return new MessageStoragePolicy(amount, period);
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
        return keepAmount;
    }

    public Period getPeriod() {
        return keepPeriod;
    }

    public boolean isKeepAll() {
        return keepAmount == null && keepPeriod == null;
    }

    public boolean isKeepNone() {
        return keepAmount != null && keepAmount <= 0;
    }

    protected void pruneMessages(Sender sender) {
        Set<Message> sent = sender.getMessageSet().stream().filter(m -> m.getSent() != null).collect(Collectors.toSet());
        if(!isKeepAll()) {
            if(!isKeepNone()) {
                Stream<Message> keep = sent.stream();
                if (keepPeriod != null) {
                    DateTime cut = DateTime.now().minus(keepPeriod);
                    keep = keep.filter(m -> m.getCreated().isAfter(cut));
                }
                if (keepAmount != null) {
                    keep = keep.sorted().limit(keepAmount);
                }
                sent.removeAll(keep.collect(Collectors.toSet()));
            }
            sent.forEach(Message::delete);
        }
    }

    public static MessageStoragePolicy internalize(String serialized) {
        if (serialized.contains("-1")) {
            return MessageStoragePolicy.keepAll();
        }
        String[] attrs = serialized.split(SERIALIZATION_SEPARATOR);
        Integer amount = null;
        Period period = null;
        for (String attr : attrs) {
            if (attr.startsWith("M")) {
                amount = Integer.valueOf(attr.substring(1));
            } else if(!attr.isEmpty()) {
                period = Period.parse(attr);
            }
        }
        return keep(amount, period);
    }

    public static MessageStoragePolicy internalize(String[] parts) {
        return internalize(SERIALIZATION_JOINER.join(parts));
    }

    public String serialize() {
        if (isKeepAll()) {
            return "-1";
        }
        if (isKeepNone()) {
            return "M0";
        }
        List<String> parts = new ArrayList<>();
        if (keepPeriod != null) {
            parts.add(keepPeriod.toString());
        }
        if (keepAmount != null) {
            parts.add("M" + keepAmount);
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
        if (keepAmount != null) {
            parts.add(BundleUtil.getString(BUNDLE, "name.storage.policy.amount", Integer.toString(keepAmount)));
        }
        if (keepPeriod != null) {
            parts.add(BundleUtil.getString(BUNDLE, "name.storage.policy.period",
                    PeriodFormat.wordBased(I18N.getLocale()).print(keepPeriod)));
        }
        return PRESENTATION_JOINER.join(parts);
    }
}
