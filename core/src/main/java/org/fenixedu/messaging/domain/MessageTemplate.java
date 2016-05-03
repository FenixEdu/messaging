package org.fenixedu.messaging.domain;

import static pt.ist.fenixframework.FenixFramework.atomic;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.exception.MessagingDomainException;
import org.fenixedu.messaging.template.DeclareMessageTemplate;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.PebbleEngine.Builder;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.loader.StringLoader;

import pt.ist.fenixframework.Atomic;

public class MessageTemplate extends MessageTemplate_Base implements Comparable<MessageTemplate> {
    private static final HashMap<String, DeclareMessageTemplate> declareAnnotations = Maps.newHashMap();
    private static final HashMap<String, MessageTemplateDeclaration> declarations = Maps.newHashMap();
    private static final PebbleEngine engine;
    static {
        final Builder builder = new PebbleEngine.Builder();
        builder.loader(new StringLoader());
        engine = builder.build();
    }

    public static class MessageTemplateDeclaration {

        private LocalizedString description, defaultSubject, defaultTextBody, defaultHtmlBody;
        private Map<String, LocalizedString> parameters;

        public LocalizedString getDescription() {
            return description;
        }

        public LocalizedString getDefaultSubject() {
            return defaultSubject;
        }

        public LocalizedString getDefaultTextBody() {
            return defaultTextBody;
        }

        public LocalizedString getDefaultHtmlBody() {
            return defaultHtmlBody;
        }

        public Map<String, LocalizedString> getParameters() {
            return ImmutableMap.copyOf(parameters);
        }

        protected MessageTemplateDeclaration(DeclareMessageTemplate decl) {
            String bundle = decl.bundle();
            this.description = localized(decl.description(), bundle);
            this.defaultSubject = localized(decl.subject(), bundle);
            this.defaultTextBody = localized(decl.text(), bundle);
            this.defaultHtmlBody = localized(decl.html(), bundle);
            this.parameters =
                    Arrays.stream(decl.parameters()).collect(
                            Collectors.toMap(param -> param.id(), param -> localized(param.description(), bundle)));
        }
    }

    private static LocalizedString localized(String key, String bundle) {
        if (key == null) {
            return new LocalizedString();
        }
        if (key.isEmpty() || Strings.isNullOrEmpty(bundle)) {
            return new LocalizedString(I18N.getLocale(), key);
        }
        return BundleUtil.getLocalizedString(bundle, key);
    }

    private static String content(LocalizedString ls, Locale l) {
        String content = ls.getContent(l);
        return content != null ? content : ls.getContent();
    }

    protected MessageTemplate(DeclareMessageTemplate declaration) {
        super();
        setMessagingSystem(MessagingSystem.getInstance());
        setId(declaration.id());
        declarations.put(getId(), new MessageTemplateDeclaration(declaration));
        reset();
    }

    @Override
    public String getId() {
        // FIXME remove when the framework supports read-only properties
        return super.getId();
    }

    public MessageTemplateDeclaration getDeclaration() {
        return declarations.get(getId());
    };

    public boolean isDeclared() {
        return declarations.containsKey(getId());
    };

    public Set<Locale> getContentLocales() {
        return Stream.of(getSubject(), getTextBody(), getHtmlBody()).filter(Objects::nonNull)
                .flatMap(c -> c.getLocales().stream()).collect(Collectors.toSet());
    }

    public LocalizedString getCompiledSubject(Map<String, Object> context) {
        return compile(getSubject(), context);
    }

    public LocalizedString getCompiledTextBody(Map<String, Object> context) {
        return compile(getTextBody(), context);
    }

    public LocalizedString getCompiledHtmlBody(Map<String, Object> context) {
        return compile(getHtmlBody(), context);
    }

    private LocalizedString compile(LocalizedString template, Map<String, Object> rawContext) {
        List<String> localizable =
                rawContext.keySet().stream().filter(k -> rawContext.get(k) instanceof LocalizedString)
                        .collect(Collectors.toList());
        Map<String, Object> context = new HashMap<>(rawContext);

        LocalizedString.Builder builder = new LocalizedString.Builder();
        for (Locale locale : template.getLocales()) {
            context.putAll(localizable.stream().collect(
                    Collectors.toMap(Function.identity(), key -> content((LocalizedString) rawContext.get(key), locale))));
            try (StringWriter writer = new StringWriter()) {
                engine.getTemplate(template.getContent(locale)).evaluate(writer, context, locale);
                builder.with(locale, writer.toString());
            } catch (PebbleException | IOException e) {
                throw MessagingDomainException.malformedTemplate(e, getId());
            }
        };
        return builder.build();
    }

    public static Set<MessageTemplate> all() {
        return Sets.newHashSet(MessagingSystem.getInstance().getTemplateSet());
    }

    public static MessageTemplate get(String id) {
        return MessagingSystem.getInstance().getTemplateSet().stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }

    public static void declare(DeclareMessageTemplate decl) { //XXX should only be used in servlet initialization
        declareAnnotations.put(decl.id(), decl);
    };

    protected static void reifyDeclared(Set<MessageTemplate> knownTemplates) {
        knownTemplates.forEach(t -> {
            declareAnnotations.computeIfPresent(t.getId(), (id, declaration) -> {
                declarations.put(id, new MessageTemplateDeclaration(declaration));
                return null;
            });
        });
        declareAnnotations.forEach((id, declaration) -> atomic(() -> {
            new MessageTemplate(declaration);
        }));
        declareAnnotations.clear();
    }

    public void reset() {
        MessageTemplateDeclaration declaration = getDeclaration();
        if (declaration != null) {
            setSubject(declaration.getDefaultSubject());
            setHtmlBody(declaration.getDefaultHtmlBody());
            setTextBody(declaration.getDefaultTextBody());
        }
    }

    @Atomic
    public void delete() {
        setMessagingSystem(null);
        deleteDomainObject();
    }

    @Override
    public int compareTo(MessageTemplate template) {
        int c = getId().compareTo(template.getId());
        return c != 0 ? c : getExternalId().compareTo(template.getExternalId());
    }

}
