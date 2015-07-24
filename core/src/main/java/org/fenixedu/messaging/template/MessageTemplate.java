package org.fenixedu.messaging.template;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.domain.Message;
import org.fenixedu.messaging.domain.ReplyTo;
import org.fenixedu.messaging.domain.Sender;
import org.fenixedu.messaging.exception.MessageTemplateException;
import org.fenixedu.messaging.ui.MessageBean;
import org.fenixedu.messaging.ui.MessageContentBean;

import com.google.common.base.Strings;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.loader.StringLoader;

public class MessageTemplate {

    private static final PebbleEngine engine = new PebbleEngine(new StringLoader());
    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Decoder DECODER = Base64.getDecoder();
    public static final Comparator<MessageTemplate> COMPARATOR_BY_ID = new Comparator<MessageTemplate>() {
        @Override
        public int compare(MessageTemplate o1, MessageTemplate o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };

    private String id, code;
    private boolean automaticFooter;
    private LocalizedString name, description, subject, body, htmlBody;

    private LocalizedString getLocalizedString(String bundle, String key) {
        LocalizedString result = new LocalizedString();
        if (!Strings.isNullOrEmpty(key)) {
            if (Strings.isNullOrEmpty(bundle)) {
                result = new LocalizedString(I18N.getLocale(), key);
            } else {
                result = BundleUtil.getLocalizedString(bundle, key);
            }
        }
        return result;
    }

    public MessageTemplate(org.fenixedu.messaging.annotation.MessageTemplate annotation) {
        String bundle = annotation.bundle();
        id = annotation.id();
        code = encodeId(id);
        name = getLocalizedString(bundle, annotation.name());
        description = getLocalizedString(bundle, annotation.description());
        setSubject(getLocalizedString(bundle, annotation.subject()));
        setBody(getLocalizedString(bundle, annotation.body()));
        setHtmlBody(getLocalizedString(bundle, annotation.html()));
        setAutomaticFooter(annotation.footer());
    }

    public String getId() {
        return id;
    }

    public LocalizedString getName() {
        return name;
    }

    public LocalizedString getDescription() {
        return description;
    }

    public LocalizedString getBody() {
        return body;
    }

    public void setBody(LocalizedString body) {
        this.body = body;
    }

    public LocalizedString getSubject() {
        return subject;
    }

    public void setSubject(LocalizedString subject) {
        this.subject = subject;
    }

    public LocalizedString getHtmlBody() {
        return htmlBody;
    }

    public void setHtmlBody(LocalizedString htmlBody) {
        this.htmlBody = htmlBody;
    }

    public boolean isAutomaticFooter() {
        return automaticFooter;
    }

    public void setAutomaticFooter(boolean automaticFooter) {
        this.automaticFooter = automaticFooter;
    }

    public Message send(Map<String, Object> context, Sender sender, Set<ReplyTo> replyTos, Set<Group> bccs,
            Set<String> extraBccs, Locale extraBccsLocale) throws MessageTemplateException {

        MessageBean bean = compile(context);
        bean.setSender(sender);
        bean.setReplyToObjects(replyTos);
        bean.setRecipientGroups(bccs);
        bean.setBccsSet(extraBccs);
        bean.setExtraBccsLocale(extraBccsLocale);

        try {
            return bean.send();
        } catch (Exception e) {
            throw MessageTemplateException.invalid(e, getId());
        }
    }

    public Message send(Map<String, Object> context, Sender sender, Set<ReplyTo> replyTos, Set<Group> bccs, Set<String> extraBccs)
            throws MessageTemplateException {
        return send(context, sender, replyTos, bccs, extraBccs, I18N.getLocale());
    }

    public Message send(Map<String, Object> context, Set<Group> bccs, Sender sender, Set<ReplyTo> replyTos)
            throws MessageTemplateException {
        return send(context, sender, replyTos, bccs, null, I18N.getLocale());
    }

    public Message send(Sender sender, Set<ReplyTo> replyTos, Set<Group> bccs, Set<String> extraBccs, Locale extraBccsLocale)
            throws MessageTemplateException {
        return send(null, sender, replyTos, bccs, extraBccs, extraBccsLocale);
    }

    public Message send(Sender sender, Set<ReplyTo> replyTos, Set<Group> bccs, Set<String> extraBccs)
            throws MessageTemplateException {
        return send(null, sender, replyTos, bccs, extraBccs, I18N.getLocale());
    }

    public Message send(Set<Group> bccs, Sender sender, Set<ReplyTo> replyTos) throws MessageTemplateException {
        return send(null, sender, replyTos, bccs, null, I18N.getLocale());
    }

    public Message send(Map<String, Object> context, Sender sender, Set<Group> bccs, Set<String> extraBccs, Locale extraBccsLocale)
            throws MessageTemplateException {
        return send(context, sender, sender.getReplyTos(), bccs, extraBccs, extraBccsLocale);
    }

    public Message send(Map<String, Object> context, Sender sender, Set<Group> bccs, Set<String> extraBccs)
            throws MessageTemplateException {
        return send(context, sender, sender.getReplyTos(), bccs, extraBccs, I18N.getLocale());
    }

    public Message send(Map<String, Object> context, Sender sender, Set<Group> bccs) throws MessageTemplateException {
        return send(context, sender, sender.getReplyTos(), bccs, null, I18N.getLocale());
    }

    public Message send(Sender sender, Set<Group> bccs, Set<String> extraBccs, Locale extraBccsLocale)
            throws MessageTemplateException {
        return send(null, sender, sender.getReplyTos(), bccs, extraBccs, extraBccsLocale);
    }

    public Message send(Sender sender, Set<Group> bccs, Set<String> extraBccs) throws MessageTemplateException {
        return send(null, sender, sender.getReplyTos(), bccs, extraBccs, I18N.getLocale());
    }

    public Message send(Sender sender, Set<Group> bccs) throws MessageTemplateException {
        return send(null, sender, sender.getReplyTos(), bccs, null, I18N.getLocale());
    }

    private static LocalizedString compileLocalizedString(LocalizedString templates, Map<String, Object> regularContext,
            Map<String, LocalizedString> localizableContext) throws PebbleException, IOException {
        LocalizedString result = new LocalizedString();
        for (Locale locale : templates.getLocales()) {
            localizableContext.entrySet().forEach(e -> {
                LocalizedString value = e.getValue();
                String localized = value.getContent(locale);
                regularContext.put(e.getKey(), localized != null ? localized : value.getContent());
            });
            StringWriter writer = new StringWriter();
            engine.getTemplate(templates.getContent(locale)).evaluate(writer, regularContext, locale);
            result = result.with(locale, writer.toString());
        }
        return result;
    }

    public MessageBean compile(Map<String, Object> context) throws MessageTemplateException {
        Map<String, Object> regularContext = new HashMap<String, Object>();
        Map<String, LocalizedString> localizableContext = new HashMap<String, LocalizedString>();
        if (context != null) {
            context.entrySet().stream().forEach(e -> {
                Object value = e.getValue();
                if (value instanceof LocalizedString) {
                    localizableContext.put(e.getKey(), (LocalizedString) value);
                } else {
                    regularContext.put(e.getKey(), value);
                }
            });
        }

        MessageBean bean = new MessageBean();
        bean.setAutomaticFooter(isAutomaticFooter());

        try {
            bean.setSubject(compileLocalizedString(getSubject(), regularContext, localizableContext));
            bean.setBody(compileLocalizedString(getBody(), regularContext, localizableContext));
            bean.setHtmlBody(compileLocalizedString(getHtmlBody(), regularContext, localizableContext));
        } catch (PebbleException | IOException e) {
            throw MessageTemplateException.malformed(e, getId());
        }
        return bean;
    }

    public static String encodeId(String id) {
        return ENCODER.encodeToString(id.getBytes());
    }

    public String getCode() {
        return code;
    }

    public static String decodeId(String code) {
        return new String(DECODER.decode(code));
    }

    public void copy(MessageContentBean bean) {
        LocalizedString attr = bean.getSubject();
        if (attr != null) {
            setSubject(attr);
        }
        attr = bean.getBody();
        if (attr != null) {
            setBody(attr);
        }
        attr = bean.getHtmlBody();
        if (attr != null) {
            setHtmlBody(attr);
        }
        setAutomaticFooter(bean.isAutomaticFooter());
    };

    public MessageContentBean bean() {
        MessageContentBean bean = new MessageContentBean();
        bean.setSubject(getSubject());
        bean.setBody(getBody());
        bean.setHtmlBody(getHtmlBody());
        bean.setAutomaticFooter(isAutomaticFooter());
        return bean;
    };

}