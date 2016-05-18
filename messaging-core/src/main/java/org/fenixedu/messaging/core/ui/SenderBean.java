package org.fenixedu.messaging.core.ui;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.domain.exceptions.BennuCoreDomainException;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.messaging.core.domain.MessageStoragePolicy;
import org.fenixedu.messaging.core.domain.MessagingSystem;
import org.fenixedu.messaging.core.domain.Sender;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class SenderBean {
    protected static final String BUNDLE = "MessagingResources";

    private Boolean htmlEnabled, allPolicy, nonePolicy;
    private String name, address, members, replyTo, policy, periodPolicy = "";
    private int amountPolicy = -1;
    private Collection<String> recipients, errors;

    public Collection<String> validate() {
        Collection<String> errors = Lists.newArrayList();
        String address = getAddress();
        if (Strings.isNullOrEmpty(address)) {
            errors.add(BundleUtil.getString(BUNDLE, "error.sender.validation.address.empty"));
        }
        if (!MessagingSystem.Util.isValidEmail(address)) {
            errors.add(BundleUtil.getString(BUNDLE, "error.sender.validation.address.invalid", address));
        }
        if (getHtmlEnabled() == null) {
            errors.add(BundleUtil.getString(BUNDLE, "error.sender.validation.html.required"));
        }
        if (Strings.isNullOrEmpty(getName())) {
            errors.add(BundleUtil.getString(BUNDLE, "error.sender.validation.name.empty"));
        }
        if (Strings.isNullOrEmpty(getPolicy())) {
            errors.add(BundleUtil.getString(BUNDLE, "error.sender.validation.policy.empty"));
        } else {
            try {
                MessageStoragePolicy.internalize(getPolicy());
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
        if (!(Strings.isNullOrEmpty(replyTo) || MessagingSystem.Util.isValidEmail(replyTo))) {
            errors.add(BundleUtil.getString(BUNDLE, "error.sender.validation.replyTo.invalid", replyTo));
        }
        setErrors(errors);
        return errors;
    }

    public Boolean getAllPolicy() {
        return allPolicy;
    }

    public Boolean getNonePolicy() {
        return nonePolicy;
    }

    public String getPeriodPolicy() {
        return periodPolicy;
    }

    public int getAmountPolicy() {
        return amountPolicy;
    }

    public Boolean getHtmlEnabled() {
        return htmlEnabled;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getMembers() {
        return members;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String[] parts) {
        setPolicy(MessageStoragePolicy.internalize(parts));
    }

    public void setPolicy(String policy) {
        setPolicy(MessageStoragePolicy.internalize(policy));
    }

    public void setPolicy(MessageStoragePolicy policy) {
        this.policy = policy.serialize();
        allPolicy = policy.isKeepAll();
        nonePolicy = policy.isKeepNone();
        amountPolicy = policy.getAmount() == null ? -1 : policy.getAmount();
        periodPolicy = policy.getPeriod() == null ? "" : policy.getPeriod().toString().substring(1);
    }

    public Set<String> getRecipients() {
        return recipients == null ? Sets.newHashSet() : Sets.newHashSet(recipients);
    }

    public String getReplyTo() {
        return replyTo;
    }

    public Collection<String> getErrors() {
        return errors;
    }

    public void setHtmlEnabled(boolean htmlEnabled) {
        this.htmlEnabled = htmlEnabled;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public void setRecipients(Collection<String> recipients) {
        this.recipients = recipients;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    protected void setErrors(Collection<String> errors) {
        this.errors = errors;
    }

    Sender newSender() {
        Sender sender = null;
        if (validate().isEmpty()) {
            Stream<Group> recipients = getRecipients().stream().map(Group::parse);
            sender = Sender.from(getAddress()).as(getName()).members(Group.parse(getMembers()))
                    .storagePolicy(MessageStoragePolicy.internalize(getPolicy())).htmlEnabled(getHtmlEnabled())
                    .replyTo(getReplyTo()).recipients(recipients).build();
        }
        return sender;
    }

    protected void copy(Sender sender) {
        if (sender != null) {
            setName(sender.getName());
            setAddress(sender.getAddress());
            setPolicy(sender.getPolicy());
            setMembers(sender.getMembers().getExpression());
            setHtmlEnabled(sender.getHtmlEnabled());
            setReplyTo(sender.getReplyTo());
            setRecipients(sender.getRecipients().stream().map(Group::getExpression).collect(Collectors.toSet()));
        }
    }

    @Atomic(mode = TxMode.WRITE)
    protected Collection<String> configure(Sender sender) {
        Collection<String> errors = validate();
        if (errors.isEmpty()) {
            sender.setName(getName());
            sender.setAddress(getAddress());
            sender.setPolicy(MessageStoragePolicy.internalize(getPolicy()));
            sender.setMembers(Group.parse(getMembers()));
            sender.setHtmlEnabled(getHtmlEnabled());
            sender.setReplyTo(getReplyTo());
            sender.setRecipients(getRecipients().stream().map(Group::parse).collect(Collectors.toSet()));
        }
        return errors;
    }

}
