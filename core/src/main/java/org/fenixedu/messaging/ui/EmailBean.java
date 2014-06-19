/*
 * @(#)EmailBean.java
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.messaging.domain.Message;
import org.fenixedu.messaging.domain.Message.MessageBuilder;
import org.fenixedu.messaging.domain.ReplyTo;
import org.fenixedu.messaging.domain.Sender;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;

import com.google.common.base.Strings;

/**
 *
 * @author Luis Cruz
 *
 */
public class EmailBean implements Serializable {
    private static final long serialVersionUID = -2004655177098978589L;

    private Sender sender;
    private List<Group> recipients = new ArrayList<>();
    private String tos, ccs, bccs;
    private String subject, message, htmlMessage;
    private Set<ReplyTo> replyTos;
    private DateTime createdDate;

    public EmailBean() {
    }

    public EmailBean(final Message message) {
        this.subject = message.getSubject();
        this.message = message.getBody();
        this.htmlMessage = message.getHtmlBody();
        this.bccs = message.getExtraBccs();
        this.createdDate = message.getCreated();
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(final Sender sender) {
        this.sender = sender;
    }

    public List<Group> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<Group> recipients) {
        this.recipients = recipients;
    }

    public Set<ReplyTo> getReplyTos() {
        return replyTos;
    }

    public void setReplyTos(Set<ReplyTo> replyTos) {
        this.replyTos = replyTos;
    }

    public String getTos() {
        return tos;
    }

    public void setTos(String tos) {
        this.tos = tos;
    }

    public String getCcs() {
        return ccs;
    }

    public void setCcs(String ccs) {
        this.ccs = ccs;
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

    public String validate() {
        final ResourceBundle resourceBundle = ResourceBundle.getBundle("resources.MessagingResources", I18N.getLocale());

        String bccs = getBccs();
        if (getRecipients().isEmpty() && StringUtils.isEmpty(bccs)) {
            return resourceBundle.getString("error.email.validation.no.recipients");
        }

        if (!StringUtils.isEmpty(bccs)) {
            String[] emails = bccs.split(",");
            for (String emailString : emails) {
                final String email = emailString.trim();
                if (!isValidEmailAddress(email)) {
                    StringBuilder builder = new StringBuilder(resourceBundle.getString("error.email.validation.bcc.invalid"));
                    builder.append(email);
                    return builder.toString();
                }
            }
        }

        if (StringUtils.isEmpty(getSubject())) {
            return resourceBundle.getString("error.email.validation.subject.empty");
        }

        if (StringUtils.isEmpty(getMessage()) && StringUtils.isEmpty(getHtmlMessage())) {
            return resourceBundle.getString("error.email.validation.message.empty");
        }

        return null;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(DateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Atomic
    public Message send() {
        String fullBody = null;
        if (!Strings.isNullOrEmpty(getMessage())) {
            fullBody =
                    BundleUtil.getString("resources.MessagingResources", "message.email.footer", getMessage(), getSender()
                            .getFromName(),
                            getRecipients().stream().map(r -> r.getPresentationName()).collect(Collectors.joining("\n\t")));
        }
        MessageBuilder builder = getSender().message(getSubject(), fullBody);
        if (!Strings.isNullOrEmpty(htmlMessage)) {
            builder = builder.htmlBody(htmlMessage);
        }
        for (Group recipient : recipients) {
            builder = builder.bcc(recipient);
        }
        if (!Strings.isNullOrEmpty(bccs)) {
            for (String bcc : bccs.split(",")) {
                if (!Strings.isNullOrEmpty(bcc.trim())) {
                    builder = builder.bcc(bcc.trim());
                }
            }
        }
        if (replyTos != null) {
            for (ReplyTo replyTo : replyTos) {
                builder = builder.replyTo(replyTo);
            }
        }
        return builder.send();
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
//    @Service
//    public void removeRecipients() {
//  for(final PersistentGroup recipient : getRecipients()) {
//      getSender().removeRecipients(recipient);
//  }
//  setRecipients(null);
//    }

}
