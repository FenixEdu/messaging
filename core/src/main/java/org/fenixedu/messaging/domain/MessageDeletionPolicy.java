package org.fenixedu.messaging.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
    private Period keepPeriod = null;
    private Integer keepAmount = null;

    protected MessageDeletionPolicy() {
    }

    protected MessageDeletionPolicy(Period period, Integer amount) {
        this.keepPeriod = period;
        if (amount != null) {
            this.keepAmount = amount > 0 ? amount : 0;
        }
    }

    public static MessageDeletionPolicy keepAmountForDuration(Integer amount, Period period) {
        return new MessageDeletionPolicy(period, amount);
    }

    public static MessageDeletionPolicy keepForDuration(Period period) {
        return new MessageDeletionPolicy(period, null);
    }

    public static MessageDeletionPolicy keepAmount(Integer amount) {
        return new MessageDeletionPolicy(null, amount);
    }

    public static MessageDeletionPolicy unlimited() {
        return new MessageDeletionPolicy(null, null);
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

    protected void pruneSender(Sender sender) {
        Stream<Message> messages = sender.getMessageSet().stream().filter(m -> m.getSent() == null);
        if (keepPeriod != null) {
            DateTime cut = DateTime.now().minus(keepPeriod);
            messages = messages.filter(m -> !m.getCreated().isBefore(cut));
        }
        if (keepAmount != null) {
            messages = messages.sorted().skip(keepAmount);
        }
        messages.forEach(Message::delete);
    }

    public static MessageDeletionPolicy internalize(String serialized) {
        if (serialized.contains("-1")) {
            return MessageDeletionPolicy.unlimited();
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
        return new MessageDeletionPolicy(period, amount);
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
