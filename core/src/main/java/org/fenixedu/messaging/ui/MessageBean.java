/*
 * @(#)MessageBean.java
 *
 * Copyright 2012 Instituto Superior Tecnico
 * Founding Authors: Luis Cruz
 *
 *      https://fenix-ashes.ist.utl.pt/
 *
 *   This file is part of the Messaging Module.
 *
 *   The Messaging Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version
 *   3 of the License, or (at your option) any later version.
 *
 *   The Messaging Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the Messaging Module. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.fenixedu.messaging.ui;

import java.io.Serializable;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.messaging.domain.Sender;

import com.google.common.base.Strings;

public class MessageBean implements Serializable {
    private static final long serialVersionUID = -2004655177098978589L;

    private boolean automaticFooter = true;
    private Sender sender;
    private String bccs, subject, message, htmlMessage;
    private Set<String> errors, recipients, replyTos;

    public boolean isAutomaticFooter() {
        return automaticFooter;
    }

    public void setAutomaticFooter(boolean automaticFooter) {
        this.automaticFooter = automaticFooter;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(final Sender sender) {
        this.sender = sender;
    }

    public Set<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(Set<String> recipients) {
        this.recipients = recipients;
    }

    public Set<String> getReplyTos() {
        return replyTos;
    }

    public void setReplyTos(Set<String> replyTos) {
        this.replyTos = replyTos;
    }

    public String getBccs() {
        return bccs;
    }

    public void setBccs(String bccs) {
        this.bccs = bccs;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getHtmlMessage() {
        return htmlMessage;
    }

    public void setHtmlMessage(final String htmlMessage) {
        this.htmlMessage = htmlMessage;
    }

    public Set<String> getErrors() {
        return errors;
    }

    public void setErrors(Set<String> errors) {
        this.errors = errors;
    }

    private static final String BUNDLE = "MessagingResources";

    Set<String> validate() {
        Set<String> errors = new HashSet<String>();
        if (getSender() == null) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.sender.empty"));
        }

        String bccs = getBccs();
        Set<String> recipients = getRecipients();
        if ((recipients == null || recipients.isEmpty()) && Strings.isNullOrEmpty(bccs)) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.recipients.empty"));
        }

        if (recipients != null && !recipients.isEmpty()) {
            try {
                Base64.Decoder decoder = Base64.getDecoder();
                recipients.forEach(e -> Group.parse(new String(decoder.decode(e))));
            } catch (DomainException e) {
                String[] args = e.getArgs();
                errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.recipient.erroneous", args != null
                        && args.length > 0 ? args[0] : ""));
            }
        }

        if (!Strings.isNullOrEmpty(bccs)) {
            String[] emails = bccs.split(",");
            for (String emailString : emails) {
                final String email = emailString.trim();
                if (!isValidEmailAddress(email)) {
                    errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.bcc.invalid", email));
                }
            }
        }

        if (Strings.isNullOrEmpty(getSubject())) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.subject.empty"));
        }

        if (Strings.isNullOrEmpty(getMessage()) && Strings.isNullOrEmpty(getHtmlMessage())) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.message.empty"));
        }

        if (!Strings.isNullOrEmpty(getHtmlMessage()) && !sender.getHtmlSender()) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.html.forbidden"));
        }

        return errors;
    }

    private static boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

}
