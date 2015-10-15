package org.fenixedu.messaging.ui;

import static pt.ist.fenixframework.FenixFramework.atomic;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.domain.MessageTemplate;
import org.fenixedu.messaging.template.MessageTemplateDeclaration;

public class MessageBodyBean implements Serializable {

    private static final long serialVersionUID = 7123930613219154850L;
    protected static final String BUNDLE = "MessagingResources";

    private Set<String> errors;
    private LocalizedString textBody, htmlBody;

    public MessageBodyBean() {
    }

    public MessageBodyBean(MessageTemplateDeclaration d) {
        textBody = d.getDefaultTextBody();
        htmlBody = d.getDefaultHtmlBody();
    }

    public MessageBodyBean(MessageTemplate t) {
        textBody = t.getTextBody();
        htmlBody = t.getHtmlBody();
    }

    Set<String> validate() {
        SortedSet<String> errors = new TreeSet<String>();
        if ((textBody == null || textBody.isEmpty()) && (htmlBody == null || htmlBody.isEmpty())) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.message.empty"));
        }
        this.errors = errors;
        return errors;
    }

    public Set<String> getErrors() {
        return errors;
    }

    public LocalizedString getTextBody() {
        return textBody;
    }

    public LocalizedString getHtmlBody() {
        return htmlBody;
    }

    protected void setErrors(Set<String> errors) {
        this.errors = errors;
    }

    public void setTextBody(LocalizedString textBody) {
        this.textBody = textBody;
    }

    public void setHtmlBody(LocalizedString htmlBody) {
        this.htmlBody = htmlBody;
    }

    public void copy(MessageTemplate template) {
        if (textBody == null) {
            textBody = template.getTextBody();
        }
        if (htmlBody == null) {
            htmlBody = template.getHtmlBody();
        }
    }

    public boolean edit(MessageTemplate template) {
        validate();
        if (errors.isEmpty()) {
            atomic(() -> {
                template.setTextBody(textBody);
                template.setHtmlBody(htmlBody);
            });
            return true;
        }
        return false;
    }

}
