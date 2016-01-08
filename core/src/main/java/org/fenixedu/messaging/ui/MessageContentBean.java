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

public class MessageContentBean implements Serializable {

    private static final long serialVersionUID = 7123930613219154850L;
    protected static final String BUNDLE = "MessagingResources";

    private Set<String> errors;
    private LocalizedString subject, textBody, htmlBody;

    public MessageContentBean() {
    }

    public MessageContentBean(MessageTemplateDeclaration d) {
        subject = d.getDefaultSubject();
        textBody = d.getDefaultTextBody();
        htmlBody = d.getDefaultHtmlBody();
    }

    public MessageContentBean(MessageTemplate t) {
        subject = t.getSubject();
        textBody = t.getTextBody();
        htmlBody = t.getHtmlBody();
    }

    Set<String> validate() {
        SortedSet<String> errors = new TreeSet<String>();

        if (getSubject() == null || getSubject().isEmpty()) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.subject.empty"));
        }

        if ((getTextBody() == null || getTextBody().isEmpty()) && (getHtmlBody() == null || getHtmlBody().isEmpty())) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.message.empty"));
        }

        this.errors = errors;
        return errors;
    }

    public Set<String> getErrors() {
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

    protected void setErrors(Set<String> errors) {
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

    public void copy(MessageTemplate template) {
        if (getSubject() == null) {
            subject = template.getSubject();
        }

        if (getTextBody() == null) {
            textBody = template.getTextBody();
        }
        if (getHtmlBody() == null) {
            htmlBody = template.getHtmlBody();
        }
    }

    public boolean edit(MessageTemplate template) {
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
