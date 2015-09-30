package org.fenixedu.messaging.ui;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
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

    private Boolean htmlSender, unlimitedPolicy, replyToCurrentUser;
    private String fromName, fromAddress, members, policy, periodPolicy = "", replyToUsers = "", replyToEmails = "";
    private int amountPolicy = -1;
    private SortedSet<String> recipients, replyTos, errors;

    public SortedSet<String> validate() {
        SortedSet<String> errors = new TreeSet<String>();
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
        return replyToCurrentUser;
    }

    public String getReplyToUsers() {
        return replyToUsers;
    }

    public String getReplyToEmails() {
        return replyToEmails;
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

    public void setRecipients(SortedSet<String> recipients) {
        this.recipients = recipients;
    }

    public void setReplyTos(SortedSet<String> replyTos) {
        this.replyTos = replyTos;
        replyTos.stream().map(ReplyTo::parse).forEach(this::addReplyToByType);
        if (!this.replyToEmails.isEmpty()) {
            this.replyToEmails = this.replyToEmails.substring(1);
        }
        if (!this.replyToUsers.isEmpty()) {
            this.replyToUsers = this.replyToUsers.substring(1);
        }
    }

    private void addReplyToByType(ReplyTo rt) {
        if (rt instanceof ConcreteReplyTo) {
            this.replyToEmails += "," + rt.serialize();
        } else if (rt instanceof UserReplyTo) {
            this.replyToUsers += "," + rt.serialize();
        } else if (rt instanceof CurrentUserReplyTo) {
            this.replyToCurrentUser = true;
        }
    }

    public void setErrors(SortedSet<String> errors) {
        this.errors = errors;
    }

    @Atomic(mode = TxMode.WRITE)
    public Sender newSender() {
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

    public void copy(Sender sender) {
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
    public Set<String> configure(Sender sender) {
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
