package org.fenixedu.messaging.ui;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.validator.routines.EmailValidator;
import org.fenixedu.bennu.core.domain.exceptions.BennuCoreDomainException;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.messaging.domain.MessageDeletionPolicy;
import org.fenixedu.messaging.domain.Sender;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

import com.google.common.base.Strings;

public class SenderBean {
    protected static final String BUNDLE = "MessagingResources";

    private Boolean htmlSender, unlimitedPolicy;
    private String fromName, fromAddress, members, replyTo, policy, periodPolicy = "";
    private int amountPolicy = -1;
    private Set<String> recipients, errors;

    public Set<String> validate() {
        Set<String> errors = new TreeSet<String>();
        String address = getFromAddress();
        if (Strings.isNullOrEmpty(address)) {
            errors.add(BundleUtil.getString(BUNDLE, "error.sender.validation.address.empty"));
        }
        if (!EmailValidator.getInstance().isValid(address)) {
            errors.add(BundleUtil.getString(BUNDLE, "error.sender.validation.address.invalid"));
        }
        if (getHtmlSender() == null) {
            errors.add(BundleUtil.getString(BUNDLE, "error.sender.validation.html.required"));
        }
        if (Strings.isNullOrEmpty(getFromName())) {
            errors.add(BundleUtil.getString(BUNDLE, "error.sender.validation.name.empty"));
        }
        if (Strings.isNullOrEmpty(getPolicy())) {
            errors.add(BundleUtil.getString(BUNDLE, "error.sender.validation.policy.empty"));
        } else {
            try {
                MessageDeletionPolicy.internalize(getPolicy());
            } catch (IllegalArgumentException e) {
                errors.add(BundleUtil.getString(BUNDLE, "error.sender.validation.policy.invalid"));
            }
        }
        if (Strings.isNullOrEmpty(getMembers())) {
            errors.add(BundleUtil.getString(BUNDLE, "error.sender.validation.members.empty"));
        } else {
            try {
                Group.parse(getMembers());
            } catch (BennuCoreDomainException e) {
                errors.add(BundleUtil.getString(BUNDLE, "error.sender.validation.members.invalid"));
            }
        }
        Set<String> recipients = getRecipients();
        if (recipients != null) {
            for (String recipient : recipients) {
                try {
                    Group.parse(recipient);
                } catch (BennuCoreDomainException e) {
                    errors.add(BundleUtil.getString(BUNDLE, "error.sender.validation.recipient.invalid", recipient));
                }
            }
        }
        String replyTo = getReplyTo();
        if (!Strings.isNullOrEmpty(replyTo)) {
            try {
                new InternetAddress(replyTo, true);
            } catch (AddressException e) {
                errors.add(BundleUtil.getString(BUNDLE, "error.sender.validation.replyTo.invalid", replyTo));
            }
        }
        setErrors(errors);
        return errors;
    }

    public Boolean getUnlimitedPolicy() {
        return unlimitedPolicy;
    }

    public String getPeriodPolicy() {
        return periodPolicy;
    }

    public int getAmountPolicy() {
        return amountPolicy;
    }

    public Boolean getHtmlSender() {
        return htmlSender;
    }

    public String getFromName() {
        return fromName;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getMembers() {
        return members;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String[] parts) {
        setPolicy(MessageDeletionPolicy.internalize(parts));
    }

    public void setPolicy(String policy) {
        setPolicy(MessageDeletionPolicy.internalize(policy));
    }

    public void setPolicy(MessageDeletionPolicy policy) {
        this.policy = policy.serialize();
        unlimitedPolicy = policy.isUnlimited();
        amountPolicy = policy.getAmount() == null ? -1 : policy.getAmount();
        periodPolicy = policy.getPeriod() == null ? "" : policy.getPeriod().toString().substring(1);
    }

    public Set<String> getRecipients() {
        return recipients;
    }

    public Set<String> getErrors() {
        return errors;
    }

    public void setHtmlSender(boolean htmlSender) {
        this.htmlSender = htmlSender;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public void setRecipients(Set<String> recipients) {
        this.recipients = recipients;
    }

    public void setErrors(Set<String> errors) {
        this.errors = errors;
    }

    @Atomic(mode = TxMode.WRITE)
    Sender newSender() {
        Set<String> errors = validate();
        if (errors.isEmpty()) {
            Sender sender =
                    new Sender(getFromName(), getFromAddress(), Group.parse(getMembers()),
                            MessageDeletionPolicy.internalize(getPolicy()));
            sender.setHtmlSender(getHtmlSender());
            String replyTo = getReplyTo();
            if (!Strings.isNullOrEmpty(replyTo)) {
                sender.setReplyTo(replyTo);
            }
            if (getRecipients() != null) {
                sender.setRecipients(getRecipients().stream().map(e -> Group.parse(e)).collect(Collectors.toSet()));
            } else {
                sender.setRecipients(Collections.emptySet());
            }
            return sender;
        }
        return null;
    }

    void copy(Sender sender) {
        if (sender != null) {
            if (getFromName() == null) {
                setFromName(sender.getFromName());
            }
            if (getFromAddress() == null) {
                setFromAddress(sender.getFromAddress());
            }
            if (getPolicy() == null) {
                setPolicy(sender.getPolicy());
            }
            if (getMembers() == null) {
                setMembers(sender.getMembers().getExpression());
            }
            if (getHtmlSender() == null) {
                setHtmlSender(sender.getHtmlSender());
            }
            if (getReplyTo() == null) {
                setReplyTo(sender.getReplyTo());
            }
            if (getRecipients() == null) {
                TreeSet<String> recipients =
                        new TreeSet<String>(sender.getRecipients().stream().map(r -> r.getExpression())
                                .collect(Collectors.toSet()));
                setRecipients(recipients);
            }
        }
    }

    @Atomic(mode = TxMode.WRITE)
    Set<String> configure(Sender sender) {
        Set<String> errors = validate();
        if (errors.isEmpty()) {
            sender.setFromName(getFromName());
            sender.setFromAddress(getFromAddress());
            sender.setPolicy(MessageDeletionPolicy.internalize(getPolicy()));
            sender.setMembers(Group.parse(getMembers()));
            sender.setHtmlSender(getHtmlSender());
            String replyTo = getReplyTo();
            if (!Strings.isNullOrEmpty(replyTo)) {
                sender.setReplyTo(replyTo);
            } else {
            }
            if (getRecipients() != null) {
                sender.setRecipients(getRecipients().stream().map(e -> Group.parse(e)).collect(Collectors.toSet()));
            } else {
                sender.setRecipients(Collections.emptySet());
            }
        }
        return errors;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

}
