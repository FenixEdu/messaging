package org.fenixedu.messaging.core.ui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public final class MessagingUtils {
    private static final String MESSAGING_MESSAGE_BEAN = "MESSAGING_MESSAGE_BEAN";

    private MessagingUtils(){
        // Utility classes should have a private constructor to prevent instantiation.
    }

    public static <T> T redirectToNewMessage(final HttpServletRequest request, final HttpServletResponse response, final MessageBean messageBean)
            throws IOException {
        request.getSession().setAttribute(MESSAGING_MESSAGE_BEAN, messageBean);
        response.sendRedirect(request.getContextPath() + "/messaging/message");
        return null;
    }

    public static Optional<MessageBean> getMessageBeanFromSession(final HttpServletRequest request) {
        return Optional.ofNullable((MessageBean) request.getSession().getAttribute(MESSAGING_MESSAGE_BEAN));
    }

    public static void clearMessageBeanFromSession(final HttpServletRequest request) {
        request.getSession().removeAttribute(MessagingUtils.MESSAGING_MESSAGE_BEAN);
    }
}
