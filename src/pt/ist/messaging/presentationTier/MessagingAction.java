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
package pt.ist.messaging.presentationTier;

import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import myorg.domain.VirtualHost;
import myorg.domain.contents.ActionNode;
import myorg.domain.contents.Node;
import myorg.presentationTier.actions.ContextBaseAction;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
import pt.ist.fenixWebFramework.servlets.functionalities.CreateNodeAction;
import pt.ist.fenixWebFramework.struts.annotations.Mapping;
import pt.ist.messaging.domain.EmailBean;
import pt.ist.messaging.domain.Message;
import pt.ist.messaging.domain.Sender;
import pt.ist.messaging.domain.SenderGroup;
import pt.utl.ist.fenix.tools.util.i18n.Language;

@Mapping(path = "/messagingAction")
/**
 * 
 * @author Luis Cruz
 * 
 */
public class MessagingAction extends ContextBaseAction {

    @CreateNodeAction(bundle = "MESSAGING_RESOURCES", key = "add.node.messaging.interface", groupKey = "label.module.messaging")
    public ActionForward createExpenditureNodes(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	final VirtualHost virtualHost = getDomainObject(request, "virtualHostToManageId");
	final Node node = getDomainObject(request, "parentOfNodesToManageId");

	ActionNode.createActionNode(virtualHost, node, "/messagingAction", "listSenders",
		"resources.MessagingResources", "label.module.messaging", SenderGroup.getInstance());

	return forwardToMuneConfiguration(request, virtualHost, node);
    }

    public ActionForward listSenders(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	final SortedSet<Sender> senders = Sender.getAvailableSenders();
	request.setAttribute("senders", senders);
	return forward(request, "/messaging/listSenders.jsp");
    }

    public ActionForward viewSender(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	final Sender sender = getDomainObject(request, "senderId");
	request.setAttribute("sender", sender);
	return forward(request, "/messaging/viewSender.jsp");
    }

    public ActionForward viewSender(final HttpServletRequest request, final Sender sender) throws Exception {
	request.setAttribute("sender", sender);
	return forward(request, "/messaging/viewSender.jsp");
    }

    public ActionForward newEmail(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	EmailBean emailBean = getRenderedObject("emailBean");

	if (emailBean == null) {
	    emailBean = (EmailBean) request.getAttribute("emailBean");
	}

	if (emailBean == null) {
	    emailBean = new EmailBean();

	    final Sender sender = getDomainObject(request, "senderId");
	    if (sender != null) {
		emailBean.setSender(sender);
	    } else {
		final Set<Sender> availableSenders = Sender.getAvailableSenders();
		if (availableSenders.size() == 1) {
		    emailBean.setSender(availableSenders.iterator().next());
		}
	    }
	}
	RenderUtils.invalidateViewState();

	request.setAttribute("emailBean", emailBean);

	return forward(request, "/messaging/newEmail.jsp");
    }

    public ActionForward sendEmail(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
	    HttpServletResponse response) {
	EmailBean emailBean = getRenderedObject("emailBean");
	RenderUtils.invalidateViewState();
	String validate = emailBean.validate();
	if (validate != null) {
	    final ResourceBundle resourceBundle = ResourceBundle.getBundle("resources.MessagingResources", Language.getLocale());
	    final String noneSentString = resourceBundle.getString("error.email.none.sent");
	    request.setAttribute("errorMessage", noneSentString + " " + validate);
	    request.setAttribute("emailBean", emailBean);
	    return forward(request, "/messaging/newEmail.jsp");
	}
	final Message message = emailBean.send();
	request.setAttribute("created", Boolean.TRUE);
	return viewMessage(mapping, request, message);
    }

    public ActionForward viewMessage(final ActionMapping mapping, final HttpServletRequest request, final Message message) {
	request.setAttribute("message", message);
	return forward(request, "/messaging/viewMessage.jsp");
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
