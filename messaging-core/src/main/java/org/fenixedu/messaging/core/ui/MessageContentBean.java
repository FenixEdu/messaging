package org.fenixedu.messaging.core.ui;

import java.io.Serializable;
import java.util.Collection;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.core.domain.MessageTemplate;
import org.fenixedu.messaging.core.domain.MessageTemplate.MessageTemplateDeclaration;

import com.google.common.collect.Lists;

import static pt.ist.fenixframework.FenixFramework.atomic;

public class MessageContentBean implements Serializable {

    private static final long serialVersionUID = 7123930613219154850L;
    protected static final String BUNDLE = "MessagingResources";

    private Collection<String> errors;
    private LocalizedString subject, textBody, htmlBody;

    public MessageContentBean() {
    }

    public MessageContentBean(MessageTemplateDeclaration declaration) {
        if (declaration != null) {
            subject = declaration.getDefaultSubject();
            textBody = declaration.getDefaultTextBody();
            htmlBody = declaration.getDefaultHtmlBody();
        } else {
            subject = new LocalizedString();
            textBody = new LocalizedString();
            htmlBody = new LocalizedString();
        }
    }

    public MessageContentBean(MessageTemplate template) {
        copy(template);
    }

    public Collection<String> validate() {
        Collection<String> errors = Lists.newArrayList();

        if (getSubject() == null || getSubject().isEmpty()) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.subject.empty"));
        }

        if ((getTextBody() == null || getTextBody().isEmpty()) && (getHtmlBody() == null || getHtmlBody().isEmpty())) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.message.empty"));
        }

        this.errors = errors;
        return errors;
    }

    public Collection<String> getErrors() {
        return errors;
    }

    public LocalizedString getSubject() {
        return subject;
    }

    public LocalizedString getTextBody() {
        return textBody;
    }

    public LocalizedString getHtmlBody() {
        return htmlBody;
    }

    protected void setErrors(Collection<String> errors) {
        this.errors = errors;
    }

    public void setSubject(LocalizedString subject) {
        this.subject = subject;
    }

    public void setTextBody(LocalizedString textBody) {
        this.textBody = textBody;
    }

    public void setHtmlBody(LocalizedString htmlBody) {
        this.htmlBody = htmlBody;
    }

    void copy(MessageTemplate template) {
        subject = template.getSubject();
        textBody = template.getTextBody();
        htmlBody = template.getHtmlBody();
    }

    boolean edit(MessageTemplate template) {
        validate();
        if (getErrors().isEmpty()) {
            atomic(() -> {
                template.setSubject(getSubject());
                template.setTextBody(getTextBody());
                template.setHtmlBody(getHtmlBody());
            });
            return true;
        }
        return false;
    }

}
