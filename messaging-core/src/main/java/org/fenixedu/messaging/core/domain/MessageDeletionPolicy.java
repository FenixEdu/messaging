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

public class MessageDeletionPolicy implements Serializable {
    private static final long serialVersionUID = 1535994777149570075L;
    private static final String BUNDLE = "MessagingResources";
    private static final Joiner PRESENTATION_JOINER = Joiner.on(", "), SERIALIZATION_JOINER = Joiner.on(",");
    private static final MessageDeletionPolicy ALL = new MessageDeletionPolicy(null, null), NONE =
            new MessageDeletionPolicy(0, null);

    private Period keepPeriod;
    private Integer keepAmount;

    protected MessageDeletionPolicy(Integer amount, Period period) {
        this.keepPeriod = period;
        this.keepAmount = amount;
    }

    public static MessageDeletionPolicy keep(Integer amount, Period period) {
        if (amount == null && period == null) {
            return keepAll();
        }
        if (amount.intValue() <= 0) {
            return keepNone();
        }
        return new MessageDeletionPolicy(amount, period);
    }

    public static MessageDeletionPolicy keep(Period period) {
        return keep(null, period);
    }

    public static MessageDeletionPolicy keep(Integer amount) {
        return keep(amount, null);
    }

    public static MessageDeletionPolicy keepAll() {
        return ALL;
    }

    public static MessageDeletionPolicy keepNone() {
        return NONE;
    }

    public Integer getAmount() {
        return keepAmount;
    }

    public Period getPeriod() {
        return keepPeriod;
    }

    public boolean isUnlimited() {
        return keepAmount == null && keepPeriod == null;
    }

    protected void pruneMessages(Sender sender) {
        Set<Message> sent = sender.getMessageSet().stream().filter(m -> m.getSent() != null).collect(Collectors.toSet());
        Stream<Message> keep = sent.stream();
        if (keepPeriod != null) {
            DateTime cut = DateTime.now().minus(keepPeriod);
            keep = keep.filter(m -> m.getCreated().isAfter(cut));
        }
        if (keepAmount != null) {
            keep = keep.sorted().limit(keepAmount);
        }
        keep.forEach(sent::remove);
        sent.forEach(Message::delete);
    }

    public static MessageDeletionPolicy internalize(String serialized) {
        if (serialized.contains("-1")) {
            return MessageDeletionPolicy.keepAll();
        }
        String[] attrs = serialized.split("\\s*,\\s*");
        Integer amount = null;
        Period period = null;
        for (String attr : attrs) {
            if (!attr.isEmpty()) {
                if (attr.startsWith("M")) {
                    amount = Integer.valueOf(attr.substring(1));
                } else {
                    period = Period.parse(attr);
                }
            }
        }
        return keep(amount, period);
    }

    public static MessageDeletionPolicy internalize(String[] parts) {
        return internalize(SERIALIZATION_JOINER.join(parts));
    }

    public String serialize() {
        if (isUnlimited()) {
            return "-1";
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
        if (keepPeriod == null && keepAmount == null) {
            return BundleUtil.getString(BUNDLE, "name.deletion.policy.unlimited");
        }
        List<String> parts = new ArrayList<>();
        if (keepPeriod != null) {
            parts.add(BundleUtil.getString(BUNDLE, "name.deletion.policy.period",
                    PeriodFormat.wordBased(I18N.getLocale()).print(keepPeriod)));
        }
        if (keepAmount != null) {
            parts.add(BundleUtil.getString(BUNDLE, "name.deletion.policy.amount", Integer.toString(keepAmount)));
        }
        return PRESENTATION_JOINER.join(parts);
    }
}
