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
 *   You should have received a copy of the GNU Leneral Public License
 *   along with the Messaging Module. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.fenixedu.messaging.core.ui;

import static java.util.Objects.requireNonNull;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.Key;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.fenixedu.bennu.MessagingConfiguration;
import org.fenixedu.bennu.MessagingConfiguration.ConfigurationProperties;
import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.domain.Message.MessageBuilder;
import org.fenixedu.messaging.core.domain.MessagingSystem;
import org.fenixedu.messaging.core.domain.Sender;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class MessageBean extends MessageContentBean {

    private static final long serialVersionUID = 336571169494160668L;
    private static final String KEY_SENDER = "sender", KEY_EXPRESSION = "expression", KEY_JWT = "jwt", KEY_NAME = "name";

    private Sender sender;
    private boolean senderLocked = false;
    private String replyTo, singleRecipients;
    private Set<String> selectedRecipients = new HashSet<>();
    private Set<String> adHocRecipients = new HashSet<>();
    private Locale preferredLocale = I18N.getLocale();

    public Sender getSender() {
        return sender;
    }

    public void setSender(final Sender sender) {
        this.sender = sender;
    }

    public boolean isSenderLocked() {
        return senderLocked;
    }

    public void setSenderLocked(boolean senderLocked) {
        this.senderLocked = senderLocked;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String getSingleRecipients() {
        return singleRecipients;
    }

    public void setSingleRecipients(String singleRecipients) {
        this.singleRecipients = singleRecipients;
    }

    public Set<String> getSelectedRecipients() {
        return selectedRecipients;
    }

    public void setSelectedRecipients(Set<String> recipients) {
        this.selectedRecipients = recipients;
    }

    public Set<String> getAdHocRecipients() {
        return adHocRecipients;
    }

    public void setAdHocRecipients(Set<String> adHocRecipients) {
        this.adHocRecipients = adHocRecipients;
    }

    public Locale getPreferredLocale() {
        return preferredLocale;
    }

    public void setPreferredLocale(Locale preferredLocale) {
        this.preferredLocale = preferredLocale;
    }

    public void setLockedSender(final Sender sender) {
        setSender(sender);
        setSenderLocked(true);
    }

    public void selectRecipient(Group recipient) {
        requireNonNull(recipient);
        selectedRecipients.add(Base64.getEncoder().encodeToString(buildRecipientJson(null, recipient).toString().getBytes()));
    }

    public void addAdHocRecipient(Group recipient) {
        addAdHocRecipient(sender, recipient);
    }

    public void addAdHocRecipient(Sender sender, Group recipient) {
        requireNonNull(recipient);
        requireNonNull(sender);
        adHocRecipients.add(Base64.getEncoder().encodeToString(buildRecipientJson(sender, recipient).toString().getBytes()));
    }

    Message send() {
        Collection<String> errors = validate();
        if (errors.isEmpty()) {
            MessageBuilder builder =
                    Message.from(getSender()).preferredLocale(getPreferredLocale()).subject(getSubject());
            String replyTos = getReplyTo();
            if (replyTos != null){
                builder.replyTo(MessagingSystem.Util.toEmailSet(replyTos));
            }
            LocalizedString content = getTextBody();
            if (content != null) {
                builder.textBody(content);
            }
            content = getHtmlBody();
            if (content != null) {
                builder.htmlBody(content);
            }
            Set<Group> recipients = getGroupRecipients();
            if (recipients != null) {
                builder.bcc(recipients);
            }
            String bccs = getSingleRecipients();
            if (bccs != null) {
                builder.singleBcc(MessagingSystem.Util.toEmailSet(bccs));
            }
            return builder.wrapped().send();
        }
        return null;
    }

    private Set<Group> getGroupRecipients() {
        return getSelectedRecipients().stream().map(b64 -> new String(Base64.getDecoder().decode(b64.getBytes())))
                .map(json -> new JsonParser().parse(json).getAsJsonObject().getAsJsonPrimitive(KEY_JWT).getAsString())
                .map(MessageBean::parseJWT).map(claims -> claims.get(KEY_EXPRESSION, String.class)).map(Group::parse)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<String> validate() {
        Collection<String> errors = super.validate();
        Sender sender = getSender();
        String singleRecipients = getSingleRecipients();
        String replyTos = getReplyTo();
        Set<String> jsonRecipients = getSelectedRecipients();
        boolean hasReplyTos = !Strings.isNullOrEmpty(replyTos);
        boolean hasGroupRecipients = jsonRecipients != null && !jsonRecipients.isEmpty(), hasSingleRecipients =
                !Strings.isNullOrEmpty(singleRecipients);

        if (!(hasGroupRecipients || hasSingleRecipients)) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.recipients.empty"));
        }

        if (hasSingleRecipients) {
            MessagingSystem.Util.toEmailSet(singleRecipients).stream().map(String::trim)
                    .filter(email -> !MessagingSystem.Util.isValidEmail(email)).forEach(email -> errors
                    .add(BundleUtil.getString(BUNDLE, "error.message.validation.recipient.single.invalid", email)));
        }

        if (hasGroupRecipients) {
            jsonRecipients.forEach(b64 -> {
                try {
                    String json = new String(Base64.getDecoder().decode(b64.getBytes()));
                    Claims claims =
                            parseJWT(new JsonParser().parse(json).getAsJsonObject().getAsJsonPrimitive(KEY_JWT).getAsString());
                    Group recipient = Group.parse(claims.get(KEY_EXPRESSION, String.class));
                    if (sender != null && !sender.getExternalId().equals(claims.get(KEY_SENDER, String.class))) {
                        errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.recipient.forbidden",
                                recipient.getPresentationName()));
                    }
                } catch (ExpiredJwtException e) {
                    errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.recipient.token.expired"));
                } catch (DomainException | JsonSyntaxException | IllegalArgumentException | IllegalStateException | JwtException e) {
                    errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.recipient.token.erroneous"));
                }
            });
        }

        if (hasReplyTos){
            MessagingSystem.Util.toEmailSet(replyTos).stream().map(String::trim)
                    .filter(email -> !MessagingSystem.Util.isValidEmail(email))
                    .forEach(email -> errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.recipient.single.invalid", email)));
        }

        if (sender == null) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.sender.empty"));
        } else {
            if (!sender.getMembers().isMember(Authenticate.getUser())) {
                errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.sender.user.forbidden"));
            }
            if (getHtmlBody() != null && !getHtmlBody().isEmpty() && !sender.getHtmlEnabled()) {
                errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.html.forbidden"));
            }
        }

        setErrors(errors);
        return errors;
    }

    private static Claims parseJWT(String jwt) {
        String secretKey = requireNonNull(MessagingConfiguration.getConfiguration().jwtKey());
        return Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(secretKey)).parseClaimsJws(jwt).getBody();
    }

    /**
     * Defines the standard JSON format for an authorized recipient. Use to build the JSON representation of the recipients to be
     * provided to the interface (ad hoc/white-listed for a given sender) or to be pre-selected in the interface.
     * The full representation is inefficient when used for pre-selected recipients. In that case only the group expression is
     * required for the interface to match with the provided recipient's data.
     *
     * The client-side may safely return the selected recipients to the server as just the expression and JWT.
     * The JWT already contains the sender and expression information, however the redundant parameters should be provided by the
     * server regardless since decoding the jwt in the client is dicey. Doing it prevents encryption and exposes
     * other implementation details (such as the use of compression).
     *
     * @param recipient
     *         The group to be allowed as a recipient
     * @param sender
     *         The sender that is being authorized to use this recipient
     * @throws NullPointerException
     *         if recipient parameter or system configuration's secret key is not defined
     */
    static JsonObject buildRecipientJson(Sender sender, Group recipient) {
        requireNonNull(recipient);
        ConfigurationProperties config = MessagingConfiguration.getConfiguration();
        requireNonNull(config.jwtKey());
        JsonObject json = new JsonObject();

        String expression = recipient.getExpression();
        json.addProperty(KEY_EXPRESSION, expression);
        json.addProperty(KEY_NAME, recipient.getPresentationName());

        if (sender != null) {
            String senderId = sender.getExternalId();
            SignatureAlgorithm algorithm = SignatureAlgorithm.forName(config.jwtAlgorithm());
            long millis = System.currentTimeMillis();
            Date now = new Date(millis);
            Key signingKey = new SecretKeySpec(DatatypeConverter.parseBase64Binary(config.jwtKey()), algorithm.getJcaName());

            String jwt =
                    Jwts.builder().claim(KEY_EXPRESSION, expression).claim(KEY_SENDER, sender.getExternalId()).setIssuedAt(now)
                            .setExpiration(new Date(millis + config.jwtTTL())).signWith(algorithm, signingKey).compact();

            json.addProperty(KEY_SENDER, senderId);
            json.addProperty(KEY_JWT, jwt);
        }
        return json;
    }
}
