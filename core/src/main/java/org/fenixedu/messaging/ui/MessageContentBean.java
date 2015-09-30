package org.fenixedu.messaging.ui;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.commons.i18n.LocalizedString;

public class MessageContentBean implements Serializable {

    protected static final String BUNDLE = "MessagingResources";
    private static final long serialVersionUID = -1169433676716037752L;
    private LocalizedString subject, body, htmlBody;
    private boolean automaticFooter;
    private Set<String> errors;

    public LocalizedString getSubject() {
        return subject;
    }

    public LocalizedString getBody() {
        return body;
    }

    public LocalizedString getHtmlBody() {
        return htmlBody;
    }

    public boolean isAutomaticFooter() {
        return automaticFooter;
    }

    public void setSubject(LocalizedString subject) {
        this.subject = subject;
    }

    public void setBody(LocalizedString body) {
        this.body = body;
    }

    public void setHtmlBody(LocalizedString htmlBody) {
        this.htmlBody = htmlBody;
    }

    public void setAutomaticFooter(boolean automaticFooter) {
        this.automaticFooter = automaticFooter;
    }

    public Set<String> getErrors() {
        return errors;
    }

    protected void setErrors(Set<String> errors) {
        this.errors = errors;
    }

    public SortedSet<String> validate() {
        SortedSet<String> errors = new TreeSet<String>();

        LocalizedString subject = getSubject(), body = getBody(), htmlBody = getHtmlBody();
        if (subject == null || subject.isEmpty()) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.subject.empty"));
        }

        if ((body == null || body.isEmpty()) && (htmlBody == null || htmlBody.isEmpty())) {
            errors.add(BundleUtil.getString(BUNDLE, "error.message.validation.message.empty"));
        }

        this.errors = errors;
        return errors;
    }

}
