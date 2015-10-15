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

import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.bennu.spring.portal.SpringApplication;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.domain.Message;
import org.fenixedu.messaging.domain.ReplyTo;
import org.fenixedu.messaging.domain.Sender;
import org.fenixedu.messaging.exception.MessagingDomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@SpringApplication(path = "messaging", title = "title.messaging", group = "logged", hint = "Messaging")
@SpringFunctionality(app = MessagingController.class, title = "title.messaging.sending")
@RequestMapping("/messaging")
public class MessagingController {

    @RequestMapping(value = { "", "/" })
    public RedirectView redirectToSending() {
        return new RedirectView("/messaging/senders", true);
    }

    @RequestMapping(value = { "/senders", "/senders/" })
    public String listSenders(Model model, @RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(
            value = "items", defaultValue = "10") int items) {
        List<Sender> senders =
                Sender.getAvailableSenders().stream().sorted(Sender.COMPARATOR_BY_FROM_NAME).collect(Collectors.toList());
        PaginationUtils.paginate(model, "messaging/senders", "senders", senders, items, page);
        return "/messaging/listSenders";
    }

    @RequestMapping(value = "/senders/{sender}", method = RequestMethod.GET)
    public String viewSender(@PathVariable Sender sender, Model model,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "items", defaultValue = "10") int items) {
        if (!allowedSender(sender)) {
            throw MessagingDomainException.forbidden();
        }
        model.addAttribute("sender", sender);
        List<Message> messages =
                sender.getMessageSet().stream().sorted(Message.COMPARATOR_BY_CREATED_DATE_OLDER_LAST)
                        .collect(Collectors.toList());
        TreeSet<ReplyTo> replyTos = new TreeSet<ReplyTo>(ReplyTo.COMPARATOR_BY_NAME);
        replyTos.addAll(sender.getReplyTos());
        model.addAttribute("replyTos", replyTos);
        TreeSet<Group> recipients = new TreeSet<Group>(Sender.RECIPIENT_COMPARATOR_BY_NAME);
        recipients.addAll(sender.getRecipients());
        model.addAttribute("recipients", recipients);
        PaginationUtils.paginate(model, "messaging/senders/" + sender.getExternalId(), "messages", messages, items, page);
        return "/messaging/viewSender";
    }

    @RequestMapping(value = "/senders/{sender}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<String> viewSenderInfo(@PathVariable Sender sender) {
        if (!allowedSender(sender)) {
            throw MessagingDomainException.forbidden();
        }

        Base64.Encoder encoder = Base64.getEncoder();
        JsonObject info = new JsonObject();
        JsonArray array = new JsonArray();
        for (Group g : sender.getRecipients()) {
            JsonObject group = new JsonObject();
            group.addProperty("name", g.getPresentationName());
            group.addProperty("expression", encoder.encodeToString(g.getExpression().getBytes()));
            array.add(group);
        }
        info.add("recipients", array);
        array = new JsonArray();
        for (ReplyTo rt : sender.getReplyTos()) {
            JsonObject replyTo = new JsonObject();
            replyTo.addProperty("name", rt.toString());
            replyTo.addProperty("expression", rt.serialize());
            array.add(replyTo);
        }
        info.add("replyTos", array);
        info.addProperty("html", sender.getHtmlSender());
        return new ResponseEntity<String>(info.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/message", method = RequestMethod.GET)
    public String newMessage(Model model, @ModelAttribute("messageBean") MessageBean messageBean) {
        if (messageBean.getSender() != null && !allowedSender(messageBean.getSender())) {
            throw MessagingDomainException.forbidden();
        }

        if (messageBean.getSender() == null) {
            final Set<Sender> availableSenders = Sender.getAvailableSenders();
            if (availableSenders.size() == 1) {
                messageBean.setSender(availableSenders.iterator().next());
            }
        }
        model.addAttribute("supportedLocales", CoreConfiguration.supportedLocales());
        model.addAttribute("messageBean", messageBean);
        return "/messaging/newMessage";
    }

    @RequestMapping(value = "/message", method = RequestMethod.POST)
    public ModelAndView sendMessage(Model model, @ModelAttribute("messageBean") MessageBean messageBean,
            RedirectAttributes redirectAttributes) {
        if (messageBean != null) {
            if (allowedSender(messageBean.getSender())) {
                Message message = messageBean.send();
                if (message != null) {
                    redirectAttributes.addFlashAttribute("justCreated", true);
                    return new ModelAndView(new RedirectView("/messaging/messages/" + message.getExternalId(), true));
                }
            } else {
                throw MessagingDomainException.forbidden();
            }
        }
        return new ModelAndView(newMessage(model, messageBean));
    }

    @RequestMapping(value = "/messages/{message}", method = RequestMethod.GET)
    public String viewMessage(Model model, @PathVariable Message message, boolean created) {
        if (!allowedSender(message.getSender())) {
            throw MessagingDomainException.forbidden();
        }
        Set<Locale> locales = new HashSet<Locale>();
        locales.addAll(CoreConfiguration.supportedLocales());
        locales.addAll(message.getSubject().getLocales());
        LocalizedString content = message.getBody();
        if (content != null) {
            locales.addAll(content.getLocales());
        }
        content = message.getHtmlBody();
        if (content != null) {
            locales.addAll(content.getLocales());
        }
        locales.add(message.getExtraBccsLocale());
        model.addAttribute("messageLocales", locales);
        model.addAttribute("message", message);
        return "/messaging/viewMessage";
    }

    @RequestMapping(value = "/messages/{message}/delete", method = RequestMethod.POST)
    public String deleteMessage(@PathVariable Message message, Model model) {
        if (!message.isLoggedUserCreator()) {
            throw MessagingDomainException.forbidden();
        }

        final Sender sender = message.getSender();
        message.safeDelete();
        return viewSender(sender, model, 1, 10);
    }

    private boolean allowedSender(Sender sender) {
        return sender.getMembers().isMember(Authenticate.getUser());
    }

}
