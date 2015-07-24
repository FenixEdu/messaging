package org.fenixedu.messaging.domain;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.exception.MessagingDomainException;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.loader.StringLoader;

public class MessageTemplate extends MessageTemplate_Base {
    private static final PebbleEngine engine = new PebbleEngine(new StringLoader());

    protected MessageTemplate() {
        super();
        setMessagingSystem(MessagingSystem.getInstance());
    }

    public LocalizedString getCompiledTextBody(Map<String, Object> context) {
        return compile(getTextBody(), context);
    }

    public LocalizedString getCompiledHtmlBody(Map<String, Object> context) {
        return compile(getHtmlBody(), context);
    }

    private LocalizedString compile(LocalizedString template, Map<String, Object> context) {
        List<String> localizable =
                context.keySet().stream().filter(k -> context.get(k) instanceof LocalizedString).collect(Collectors.toList());
        Map<String, Object> localized = new HashMap<>(context);

        LocalizedString result = new LocalizedString();
        for (Locale locale : template.getLocales()) {
            localizable.forEach(k -> {
                LocalizedString toLocalize = (LocalizedString) context.get(k);
                String content = toLocalize.getContent(locale);
                localized.put(k, content != null ? content : toLocalize.getContent());
            });
            try (StringWriter writer = new StringWriter()) {
                engine.getTemplate(template.getContent(locale)).evaluate(writer, localized, locale);
                result = result.with(locale, writer.toString());
            } catch (PebbleException | IOException e) {
                throw MessagingDomainException.malformedTemplate(e, getId());
            }
        }
        return result;
    }

    public void delete() {
        setMessagingSystem(null);
        deleteDomainObject();
    }

}
