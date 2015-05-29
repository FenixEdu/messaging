/*
 * @(#)MessagingAction.java
 *
 * Copyright 2012 Instituto Superior Tecnico
 * Founding Authors: Luis Cruz
 *
 *      https://fenix-ashes.ist.utl.pt/
 *
 *   This file is part of the Messaging Module.
 *
 *   The Messaging Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version
 *   3 of the License, or (at your option) any later version.
 *
 *   The Messaging Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the Messaging Module. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.fenixedu.messaging.ui;

import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.fenixedu.bennu.core.domain.groups.PersistentGroup;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.struts.annotations.Mapping;
import org.fenixedu.bennu.struts.base.BaseAction;
import org.fenixedu.bennu.struts.portal.EntryPoint;
import org.fenixedu.bennu.struts.portal.StrutsApplication;
import org.fenixedu.bennu.struts.portal.StrutsFunctionality;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.messaging.domain.Message;
import org.fenixedu.messaging.domain.Sender;

import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
import pt.ist.fenixframework.FenixFramework;

/**
 *
 * @author Luis Cruz
 *
 */
@StrutsApplication(bundle = "MessagingResources", path = "messaging", titleKey = "title.messaging", accessGroup = "sender",
        hint = "Messaging")
@StrutsFunctionality(app = MessagingAction.class, path = "email", titleKey = "title.messaging.emails")
@Mapping(path = "/messagingAction")
public class MessagingAction extends BaseAction {
    @EntryPoint
    public ActionForward listSenders(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final SortedSet<Sender> senders = Sender.getAvailableSenders();
        request.setAttribute("senders", senders);
        return forward("/messaging/listSenders.jsp");
    }

    public ActionForward viewSender(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final Sender sender = getDomainObject(request, "senderId");
        request.setAttribute("sender", sender);
        return forward("/messaging/viewSender.jsp");
    }

    public ActionForward viewSender(final HttpServletRequest request, final Sender sender) throws Exception {
        request.setAttribute("sender", sender);
        return forward("/messaging/viewSender.jsp");
    }

    public ActionForward newEmail(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        EmailBean emailBean = getRenderedObject("emailBean");

        if (emailBean == null) {
            emailBean = (EmailBean) request.getAttribute("emailBean");
        }

        if (emailBean == null) {
            emailBean = new EmailBean();

            Boolean automaticFooter = (Boolean) request.getAttribute("automaticFooter");
            if (automaticFooter != null) {
                emailBean.setAutomaticFooter(automaticFooter);
            }

            final Sender sender = getDomainObject(request, "senderId");
            if (sender != null) {
                emailBean.setSender(sender);
            } else {
                final Set<Sender> availableSenders = Sender.getAvailableSenders();
                if (availableSenders.size() == 1) {
                    emailBean.setSender(availableSenders.iterator().next());
                }
            }

            String[] recipientsParameter = request.getParameterValues("recipientIds");
            if (recipientsParameter != null) {
                List<Group> recipients =
                        Stream.of(recipientsParameter)
                                .map(recipientExternalId -> ((PersistentGroup) FenixFramework
                                        .getDomainObject(recipientExternalId)).toGroup()).collect(Collectors.toList());
                emailBean.setRecipients(recipients);
            }

        }
        RenderUtils.invalidateViewState();

        request.setAttribute("emailBean", emailBean);

        return forward("/messaging/newEmail.jsp");
    }

    public ActionForward sendEmail(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) {
        EmailBean emailBean = getRenderedObject("emailBean");
        RenderUtils.invalidateViewState();
        String validate = emailBean.validate();
        if (validate != null) {
            final ResourceBundle resourceBundle = ResourceBundle.getBundle("resources.MessagingResources", I18N.getLocale());
            final String noneSentString = resourceBundle.getString("error.email.none.sent");
            request.setAttribute("errorMessage", noneSentString + " " + validate);
            request.setAttribute("emailBean", emailBean);
            return forward("/messaging/newEmail.jsp");
        }
        final Message message = emailBean.send();
        request.setAttribute("created", Boolean.TRUE);
        return viewMessage(mapping, request, message);
    }

    public ActionForward viewMessage(final ActionMapping mapping, final HttpServletRequest request, final Message message) {
        request.setAttribute("message", message);
        return forward("/messaging/viewMessage.jsp");
    }

    public ActionForward viewMessage(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) {
        final Message message = getDomainObject(request, "messagesId");
        return viewMessage(mapping, request, message);
    }

    public ActionForward deleteMessage(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        final Message message = getDomainObject(request, "messagesId");
        final Sender sender = message.getSender();
        message.delete();
        return viewSender(request, sender);
    }

}
