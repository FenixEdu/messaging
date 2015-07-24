package org.fenixedu.messaging.template;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.domain.MessageTemplate;
import org.fenixedu.messaging.template.annotation.DeclareMessageTemplate;

import com.google.common.base.Strings;

public class MessageTemplateDeclaration {

    private MessageTemplate template;
    private LocalizedString description;
    private LocalizedString defaultTextBody;
    private LocalizedString defaultHtmlBody;
    private Map<String, LocalizedString> parameters;

    public MessageTemplate getTemplate() {
        return template;
    }

    public String getId() {
        return template.getId();
    }

    public String getExternalId() {
        return template.getExternalId();
    }

    public LocalizedString getDescription() {
        return description;
    }

    public LocalizedString getDefaultTextBody() {
        return defaultTextBody;
    }

    public LocalizedString getTextBody() {
        return template.getTextBody();
    }

    public LocalizedString getDefaultHtmlBody() {
        return defaultHtmlBody;
    }

    public LocalizedString getHtmlBody() {
        return template.getHtmlBody();
    }

    public Map<String, LocalizedString> getParameters() {
        return parameters;
    }

    public MessageTemplateDeclaration(MessageTemplate template, DeclareMessageTemplate decl) {
        if (template == null) {
            throw new NullPointerException();
        }
        this.template = template;
        String bundle = decl.bundle();
        this.description = localized(decl.description(), bundle);
        this.defaultTextBody = localized(decl.text(), bundle);
        this.defaultHtmlBody = localized(decl.html(), bundle);
        this.parameters =
                Arrays.stream(decl.parameters()).collect(
                        Collectors.toMap(param -> param.id(), param -> localized(param.description(), bundle)));
    }

    private static LocalizedString localized(String key, String bundle) {
        LocalizedString localized = null;
        if (key != null) {
            localized =
                    (key.isEmpty() || Strings.isNullOrEmpty(bundle)) ? new LocalizedString(I18N.getLocale(), key) : BundleUtil
                            .getLocalizedString(bundle, key);
        }
        return localized;
    }

}
