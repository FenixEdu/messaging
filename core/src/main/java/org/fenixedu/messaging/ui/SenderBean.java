package org.fenixedu.messaging.ui;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.validator.routines.EmailValidator;
import org.fenixedu.bennu.core.domain.exceptions.BennuCoreDomainException;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.messaging.domain.MessageDeletionPolicy;
import org.fenixedu.messaging.domain.ReplyTo;
import org.fenixedu.messaging.domain.ReplyTo.ConcreteReplyTo;
import org.fenixedu.messaging.domain.ReplyTo.CurrentUserReplyTo;
import org.fenixedu.messaging.domain.ReplyTo.UserReplyTo;
import org.fenixedu.messaging.domain.Sender;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

import com.google.common.base.Strings;

public class SenderBean {
    protected static final String BUNDLE = "MessagingResources";
    private static final String USER_REPLY_TO = UserReplyTo.class.getSimpleName();
    private static final String CURRENT_USER_REPLY_TO = CurrentUserReplyTo.class.getSimpleName();
    private static final String CONCRETE_REPLY_TO = ConcreteReplyTo.class.getSimpleName();

    private Boolean htmlSender, unlimitedPolicy;
    private String fromName, fromAddress, members, policy, periodPolicy = "";
    Map<String, String> replyTosByType;
    private int amountPolicy = -1;
    private Set<String> recipients, replyTos, errors;

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
        Set<String> replyTos = getReplyTos();
        if (replyTos != null) {
            for (String replyTo : replyTos) {
                if (ReplyTo.parse(replyTo) == null) {
                    errors.add(BundleUtil.getString(BUNDLE, "error.sender.validation.replyTo.invalid", replyTo));
                }
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

    public Boolean getReplyToCurrentUser() {
        return replyTosByType == null ? false : replyTosByType.containsKey(CURRENT_USER_REPLY_TO);
    }

    public String getReplyToUsers() {
        return replyTosByType == null ? "" : replyTosByType.get(USER_REPLY_TO);
    }

    public String getReplyToEmails() {
        return replyTosByType == null ? "" : replyTosByType.get(CONCRETE_REPLY_TO);
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

    public void setPolicy(String policy) {
        this.policy = policy;
        if (policy.equals("-1")) {
            amountPolicy = -1;
            periodPolicy = "";
            unlimitedPolicy = true;
        } else if (policy.startsWith("M")) {
            unlimitedPolicy = false;
            periodPolicy = "";
            amountPolicy = Integer.parseInt(policy.substring(1));
        } else {
            unlimitedPolicy = false;
            amountPolicy = -1;
            periodPolicy = policy;
        }
    }

    public Set<String> getRecipients() {
        return recipients;
    }

    public Set<String> getReplyTos() {
        return replyTos;
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

    public void setReplyTos(Set<String> replyTos) {
        this.replyTos = replyTos;
        replyTosByType =
                replyTos.stream().collect(
                        Collectors.groupingBy(rt -> ReplyTo.parse(rt).getClass().getSimpleName(), Collectors.joining(", ")));
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
            if (getReplyTos() != null) {
                sender.setReplyTos(getReplyTos().stream().map(s -> ReplyTo.parse(s)).collect(Collectors.toSet()));
            } else {
                sender.setReplyTos(Collections.emptySet());
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
                setPolicy(sender.getPolicy().serialize());
            }
            if (getMembers() == null) {
                setMembers(sender.getMembers().getExpression());
            }
            if (getHtmlSender() == null) {
                setHtmlSender(sender.getHtmlSender());
            }
            if (getReplyTos() == null) {
                TreeSet<String> replyTos = new TreeSet<String>();
                replyTos.addAll(sender.getReplyTos().stream().map(rt -> rt.serialize()).collect(Collectors.toSet()));
                setReplyTos(replyTos);
            }
            if (getRecipients() == null) {
                TreeSet<String> recipients = new TreeSet<String>();
                recipients.addAll(sender.getRecipients().stream().map(r -> r.getExpression()).collect(Collectors.toSet()));
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
            if (getReplyTos() != null) {
                sender.setReplyTos(getReplyTos().stream().map(s -> ReplyTo.parse(s)).collect(Collectors.toSet()));
            } else {
                sender.setReplyTos(Collections.emptySet());
            }
            if (getRecipients() != null) {
                sender.setRecipients(getRecipients().stream().map(e -> Group.parse(e)).collect(Collectors.toSet()));
            } else {
                sender.setRecipients(Collections.emptySet());
            }
        }
        return errors;
    }

}
