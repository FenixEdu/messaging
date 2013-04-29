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
package pt.ist.messaging.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import pt.ist.bennu.core.domain.groups.PersistentGroup;
import pt.ist.fenixframework.Atomic;
import pt.utl.ist.fenix.tools.util.EMail;
import pt.utl.ist.fenix.tools.util.i18n.Language;

/**
 * 
 * @author Luis Cruz
 * 
 */
public class EmailBean implements Serializable {

    private Sender sender;
    private Set<PersistentGroup> recipients;
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
        this.bccs = message.getBccString();
        this.createdDate = message.getCreated();
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(final Sender sender) {
        this.sender = sender;
    }

    public List<PersistentGroup> getRecipients() {
        final List<PersistentGroup> result = new ArrayList<PersistentGroup>();
        if (recipients != null) {
            for (final PersistentGroup recipient : recipients) {
                result.add(recipient);
            }
        }
        return result;
    }

    public void setRecipients(List<PersistentGroup> recipients) {
        if (recipients == null) {
            this.recipients = null;
        } else {
            this.recipients = new HashSet<PersistentGroup>();
            for (final PersistentGroup recipient : recipients) {
                this.recipients.add(recipient);
            }
        }
    }

    public List<ReplyTo> getReplyTos() {
        final List<ReplyTo> result = new ArrayList<ReplyTo>();
        if (replyTos != null) {
            for (final ReplyTo replyTo : replyTos) {
                result.add(replyTo);
            }
        }
        return result;
    }

    public void setReplyTos(List<ReplyTo> replyTos) {
        if (replyTos == null) {
            this.replyTos = null;
        } else {
            this.replyTos = new HashSet<ReplyTo>();
            for (final ReplyTo replyTo : replyTos) {
                this.replyTos.add(replyTo);
            }
        }
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
        final ResourceBundle resourceBundle = ResourceBundle.getBundle("resources.MessagingResources", Language.getLocale());

        String bccs = getBccs();
        if (getRecipients().isEmpty() && StringUtils.isEmpty(bccs)) {
            return resourceBundle.getString("error.email.validation.no.recipients");
        }

        if (!StringUtils.isEmpty(bccs)) {
            String[] emails = bccs.split(",");
            for (String emailString : emails) {
                final String email = emailString.trim();
                if (!email.matches(EMail.W3C_EMAIL_SINTAX_VALIDATOR)) {
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
        final String bccs = getBccs() == null ? null : getBccs().replace(" ", "");
        final String htmlMessage = getHtmlMessage();
        return new Message(getSender(), getReplyTos(), Collections.EMPTY_SET, Collections.EMPTY_SET, getRecipients(), bccs,
                getSubject(), getMessage(), htmlMessage);
    }

//    @Service
//    public void removeRecipients() {
//	for(final PersistentGroup recipient : getRecipients()) {
//	    getSender().removeRecipients(recipient);
//	}
//	setRecipients(null);
//    }

}
