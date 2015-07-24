package org.fenixedu.messaging.exception;

import org.fenixedu.bennu.core.i18n.BundleUtil;

public class MessageTemplateException extends Exception {

    private static final long serialVersionUID = -7393170073739821990L;
    private static final String BUNDLE = "MessagingResources";

    private MessageTemplateException(String message) {
        super(message);
    }

    private MessageTemplateException(Throwable cause, String message) {
        super(message, cause);
    }

    public static MessageTemplateException missing(String id) {
        return new MessageTemplateException(BundleUtil.getString(BUNDLE, "error.template.missing", id));
    }

    public static MessageTemplateException malformed(Exception e, String id) {
        return new MessageTemplateException(e, BundleUtil.getString(BUNDLE, "error.template.malformed", id));
    }

    public static MessageTemplateException invalid(Exception e, String id) {
        return new MessageTemplateException(e, BundleUtil.getString(BUNDLE, "error.template.invalid", id));
    }

    public static MessageTemplateException sending(Exception e, String id) {
        return new MessageTemplateException(e, BundleUtil.getString(BUNDLE, "error.template.sending", id));
    }

}
