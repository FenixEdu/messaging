package org.fenixedu.messaging.domain;

import java.io.Serializable;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.I18N;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

public class MessageDeletionPolicy implements Serializable {
    private static final long serialVersionUID = 1535994777149570075L;
    private static final String BUNDLE = "MessagingResources";
    private Period keepPeriod = null;
    private Integer keepMessages = null;

    protected MessageDeletionPolicy() {
    }

    protected MessageDeletionPolicy(Period keepPeriod) {
        this.keepPeriod = keepPeriod;
    }

    protected MessageDeletionPolicy(int keepMessages) {
        this.keepMessages = keepMessages;
    }

    public static MessageDeletionPolicy keepForDuration(Period keepPeriod) {
        return new MessageDeletionPolicy(keepPeriod);
    }

    public static MessageDeletionPolicy keepAmountOfMessages(Integer keepMessages) {
        return new MessageDeletionPolicy(keepMessages);
    }

    public static MessageDeletionPolicy unlimited() {
        return new MessageDeletionPolicy();
    }

    void pruneSender(Sender sender) {
        if (keepPeriod != null) {
            DateTime cut = DateTime.now().minus(keepPeriod);
            sender.getMessageSet().stream().filter(m -> m.getSent() != null && !m.getCreated().isBefore(cut))
                    .forEach(m -> m.delete());
        } else if (keepMessages != null) {
            if (sender.getMessageSet().size() > keepMessages) {
                sender.getMessageSet().stream().filter(m -> m.getSent() != null).sorted().skip(keepMessages)
                        .forEach(m -> m.delete());
            }
        }
    }

    public static MessageDeletionPolicy internalize(String serialized) {
        if (serialized.equals("-1")) {
            return MessageDeletionPolicy.unlimited();
        }
        if (serialized.startsWith("M")) {
            return MessageDeletionPolicy.keepAmountOfMessages(Integer.valueOf(serialized.substring(1)));
        }
        return MessageDeletionPolicy.keepForDuration(Period.parse(serialized));
    }

    public String serialize() {
        if (keepPeriod != null) {
            return keepPeriod.toString();
        }
        if (keepMessages != null) {
            return "M" + keepMessages;
        }
        return "-1";
    }

    @Override
    public String toString() {
        if (keepPeriod != null) {
            return BundleUtil.getString(BUNDLE, "name.deletion.policy.period",
                    PeriodFormat.wordBased(I18N.getLocale()).print(keepPeriod));
        }
        if (keepMessages != null) {
            return BundleUtil.getString(BUNDLE, "name.deletion.policy.messages", Integer.toString(keepMessages));
        }
        return BundleUtil.getString(BUNDLE, "name.deletion.policy.unlimited");
    }
}
