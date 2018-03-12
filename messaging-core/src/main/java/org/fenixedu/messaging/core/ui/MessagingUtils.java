package org.fenixedu.messaging.core.ui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class MessagingUtils {

    public static final String MESSAGING_MESSAGE_BEAN = "MESSAGING_MESSAGE_BEAN";

    public static <T> T redirectToNewMessage(HttpServletRequest request, HttpServletResponse response, MessageBean messageBean)
            throws IOException {
        request.getSession().setAttribute(MESSAGING_MESSAGE_BEAN, messageBean);
        response.sendRedirect("/messaging/message");
        return null;
    }

    public static Optional<MessageBean> getMessageBeanFromSession(HttpServletRequest request) {
        return Optional.ofNullable((MessageBean) request.getSession().getAttribute(MESSAGING_MESSAGE_BEAN));
    }

    public static void clearMessageBeanFromSession(HttpServletRequest request) {
        request.getSession().removeAttribute(MessagingUtils.MESSAGING_MESSAGE_BEAN);
    }
}
