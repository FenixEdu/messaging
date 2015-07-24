/*
 * @(#)Message.java
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
package org.fenixedu.messaging.domain;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.UserProfile;
import org.fenixedu.bennu.core.domain.groups.PersistentGroup;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

/**
 *
 * @author Luis Cruz
 *
 */
public final class Message extends Message_Base implements Comparable<Message> {
    public static final class MessageBuilder implements Serializable {
        private static final long serialVersionUID = 525424959825814582L;

        private Sender sender;
        private LocalizedString subject, body, htmlBody;
        private Locale extraBccsLocale = I18N.getLocale();
        private Set<Group> to = new HashSet<>(), cc = new HashSet<>(), bcc = new HashSet<>();
        private Set<String> extraBcc = new HashSet<>();
        private Set<ReplyTo> replyTo = new HashSet<>();

        public MessageBuilder(Sender sender, LocalizedString subject, LocalizedString body) {
            this.sender = sender;
            this.subject = subject;
            this.body = body;
        }

        public MessageBuilder(Sender sender, String subject, String body, Locale locale) {
            this.sender = sender;
            this.subject = new LocalizedString(locale, subject);
            this.body = new LocalizedString(locale, body);
        }

        public MessageBuilder subject(LocalizedString subject) {
            this.subject = subject;
            return this;
        }

        public MessageBuilder subjectLocale(Locale locale, String subject) {
            this.subject = this.subject.with(locale, subject);
            return this;
        }

        public MessageBuilder body(LocalizedString body) {
            this.body = body;
            return this;
        }

        public MessageBuilder bccLocale(Locale extraBccsLocale) {
            this.extraBccsLocale = extraBccsLocale;
            return this;
        }

        public MessageBuilder bodyLocale(Locale locale, String body) {
            this.body = this.body.with(locale, body);
            return this;
        }

        public MessageBuilder contentLocale(Locale locale, String subject, String body) {
            this.subject = this.subject.with(locale, subject);
            this.body = this.body.with(locale, body);
            return this;
        }

        public MessageBuilder htmlBody(LocalizedString htmlBody) {
            this.htmlBody = htmlBody;
            return this;
        }

        public MessageBuilder htmlBodyLocale(Locale locale, String htmlBody) {
            if (this.htmlBody != null) {
                this.htmlBody = this.htmlBody.with(locale, htmlBody);
            } else {
                this.htmlBody = new LocalizedString(locale, htmlBody);
            }
            return this;
        }

        public MessageBuilder to(Group... to) {
            for (Group group : to) {
                this.to.add(group);
            }
            return this;
        }

        public MessageBuilder cc(Group... cc) {
            for (Group group : cc) {
                this.cc.add(group);
            }
            return this;
        }

        public MessageBuilder bcc(Group... bcc) {
            for (Group group : bcc) {
                this.bcc.add(group);
            }
            return this;
        }

        public MessageBuilder bcc(Set<String> bcc) {
            for (String group : bcc) {
                this.extraBcc.add(group);
            }
            return this;
        }

        public MessageBuilder bcc(String... bcc) {
            for (String group : bcc) {
                this.extraBcc.add(group);
            }
            return this;
        }

        public MessageBuilder replyToSystem() {
            this.replyTo.addAll(sender.getReplyTos());
            return this;
        }

        public MessageBuilder replyTo(Set<ReplyTo> replyTos) {
            this.replyTo.addAll(replyTos);
            return this;
        }

        public MessageBuilder replyTo(ReplyTo... replyTos) {
            for (ReplyTo replyTo : replyTos) {
                this.replyTo.add(replyTo);
            }
            return this;
        }

        public MessageBuilder replyTo(String... emails) {
            for (String email : emails) {
                this.replyTo.add(ReplyTo.concrete(email));
            }
            return this;
        }

        public Message send() {
            return new Message(sender, subject, body, htmlBody, to, cc, bcc, extraBcc, replyTo, extraBccsLocale);
        }
    }

    static final public Comparator<Message> COMPARATOR_BY_CREATED_DATE_OLDER_FIRST = new Comparator<Message>() {
        @Override
        public int compare(Message o1, Message o2) {
            return o1.getCreated().compareTo(o2.getCreated());
        }
    };

    static final public Comparator<Message> COMPARATOR_BY_CREATED_DATE_OLDER_LAST = new Comparator<Message>() {
        @Override
        public int compare(Message o1, Message o2) {
            return o2.getCreated().compareTo(o1.getCreated());
        }
    };

    protected Message() {
        super();
        final MessagingSystem messagingSystem = MessagingSystem.getInstance();
        setMessagingSystem(messagingSystem);
        setMessagingSystemFromPendingDispatch(messagingSystem);
        setCreated(new DateTime());
        setUser(Authenticate.getUser());
        setExtraBccsLocale(I18N.getLocale());
    }

    Message(Sender sender, LocalizedString subject, LocalizedString body, LocalizedString htmlBody, Set<Group> to, Set<Group> cc,
            Set<Group> bcc, Set<String> extraBccs, Set<ReplyTo> replyTos) {
        this();
        setSender(sender);
        if (to != null) {
            for (Group group : to) {
                addTo(group.toPersistentGroup());
            }
        }
        if (cc != null) {
            for (Group group : cc) {
                addCc(group.toPersistentGroup());
            }
        }
        if (bcc != null) {
            for (Group group : bcc) {
                addBcc(group.toPersistentGroup());
            }
        }
        if (replyTos != null) {
            setReplyToArray(new ReplyTos(replyTos));
        }
        setExtraBccs(Joiner.on(", ").join(extraBccs));
        setSubject(subject);
        setBody(body);
        setHtmlBody(htmlBody);
    }

    Message(Sender sender, LocalizedString subject, LocalizedString body, LocalizedString htmlBody, Set<Group> to, Set<Group> cc,
            Set<Group> bcc, Set<String> extraBccs, Set<ReplyTo> replyTos, Locale extraBccsLocale) {
        this(sender, subject, body, htmlBody, to, cc, bcc, extraBccs, replyTos);
        setExtraBccsLocale(extraBccsLocale);
    }

    Message(Sender sender, String subject, String body, String htmlBody, Set<Group> to, Set<Group> cc, Set<Group> bcc,
            Set<String> extraBccs, Set<ReplyTo> replyTos, Locale locale) {
        this(sender, new LocalizedString(locale, subject), new LocalizedString(locale, body), new LocalizedString(locale,
                htmlBody), to, cc, bcc, extraBccs, replyTos);
    }

    Message(Sender sender, String subject, String body, String htmlBody, Set<Group> to, Set<Group> cc, Set<Group> bcc,
            Set<String> extraBccs, Set<ReplyTo> replyTos) {
        this(sender, subject, body, htmlBody, to, cc, bcc, extraBccs, replyTos, I18N.getLocale());
    }

    @Override
    public User getUser() {
        // FIXME remove when the framework supports read-only properties
        return super.getUser();
    }

    @Override
    public MessageDispatchReport getDispatchReport() {
        // FIXME remove when the framework supports read-only properties
        return super.getDispatchReport();
    }

    public DateTime getSent() {
        return getDispatchReport() != null ? getDispatchReport().getFinishedDelivery() : null;
    }

    public void safeDelete() {
        if (getDispatchReport() == null) {
            delete();
        }
    }

    @Atomic
    public void delete() {
        getToSet().clear();
        getCcSet().clear();
        getBccSet().clear();
        if (getDispatchReport() != null) {
            getDispatchReport().delete();
        }
        setSender(null);
        setUser(null);
        setMessagingSystemFromPendingDispatch(null);
        setMessagingSystem(null);
        deleteDomainObject();
    }

    public Set<Group> getToGroup() {
        return getToSet().stream().map(g -> g.toGroup()).collect(Collectors.toSet());
    }

    public Set<String> getTos() {
        return recipientsToEmails(getToSet());
    }

    public Set<Group> getCcGroup() {
        return getCcSet().stream().map(g -> g.toGroup()).collect(Collectors.toSet());
    }

    public Set<String> getCcs() {
        return recipientsToEmails(getCcSet());
    }

    public Set<Group> getBccGroup() {
        return getBccSet().stream().map(g -> g.toGroup()).collect(Collectors.toSet());
    }

    public Set<String> getBccs() {
        Set<String> base = recipientsToEmails(getBccSet());
        base.addAll(getExtraBccsSet());
        return base;
    }

    public Set<String> getExtraBccsSet() {
        String extraBccs = getExtraBccs();
        if (!Strings.isNullOrEmpty(extraBccs)) {
            return Sets.newHashSet(getExtraBccs().replace(',', ' ').replace(';', ' ').split("\\s+"));
        } else {
            return Sets.newHashSet();
        }
    }

    private static Map<Locale, Set<String>> emailsByLocale(Set<PersistentGroup> groups, Predicate<String> emailValidator) {
        Map<Locale, Set<String>> emails = new HashMap<Locale, Set<String>>();
        Locale defLocale = I18N.getLocale();
        for (PersistentGroup group : groups) {
            for (User u : group.getMembers()) {
                UserProfile profile = u.getProfile();
                Locale l = profile.getPreferredLocale();
                if (l == null) {
                    l = defLocale;
                }
                String email = profile.getEmail();
                if (emailValidator.test(email)) {
                    Set<String> localeEmails = emails.get(l);
                    if (localeEmails == null) {
                        localeEmails = new HashSet<>();
                        emails.put(l, localeEmails);
                    }
                    localeEmails.add(email);
                }
            }
        }
        return emails;
    }

    public Map<Locale, Set<String>> getTosByLocale() {
        return getTosByLocale(e -> true);
    }

    public Map<Locale, Set<String>> getTosByLocale(Predicate<String> emailValidator) {
        return emailsByLocale(getToSet(), emailValidator);
    }

    public Map<Locale, Set<String>> getCcsByLocale() {
        return getCcsByLocale(e -> true);
    }

    public Map<Locale, Set<String>> getCcsByLocale(Predicate<String> emailValidator) {
        return emailsByLocale(getCcSet(), emailValidator);
    }

    public Map<Locale, Set<String>> getBccsByLocale() {
        return getCcsByLocale(e -> true);
    }

    public Map<Locale, Set<String>> getBccsByLocale(Predicate<String> emailValidator) {
        Map<Locale, Set<String>> bccs = emailsByLocale(getBccSet(), emailValidator);
        Locale extraBccsLocale = getExtraBccsLocale();
        Set<String> extraBccsLocaleEmails = bccs.get(extraBccsLocale);
        if (extraBccsLocaleEmails == null) {
            extraBccsLocaleEmails = new HashSet<String>();
            bccs.put(extraBccsLocale, extraBccsLocaleEmails);
        }
        extraBccsLocaleEmails.addAll(getExtraBccsSet());
        return bccs;
    }

    public Set<String> getReplyTos() {
        return getReplyToArray().addresses();
    }

    private Set<String> recipientsToEmails(Set<PersistentGroup> recipients) {
        return recipients.stream().map(g -> g.toGroup()).flatMap(g -> g.getMembers().stream()).distinct()
                .map(user -> user.getProfile().getEmail()).filter(Strings::isNullOrEmpty).collect(Collectors.toSet());
    }

    @Override
    public int compareTo(Message o) {
        int date = -getCreated().compareTo(o.getCreated());
        return date != 0 ? date : getExternalId().compareTo(o.getExternalId());
    }
}
