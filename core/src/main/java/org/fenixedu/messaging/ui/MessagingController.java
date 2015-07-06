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

import static pt.ist.fenixframework.FenixFramework.atomic;

import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.exceptions.DomainException;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.spring.portal.SpringApplication;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.messaging.domain.Message;
import org.fenixedu.messaging.domain.Message.MessageBuilder;
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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@SpringApplication(path = "messaging", title = "title.messaging", group = "logged", hint = "Messaging")
@SpringFunctionality(app = MessagingController.class, title = "title.messaging.sending")
@RequestMapping("/messaging")
public class MessagingController {

    @RequestMapping
    public String listSenders(Model model, @RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(
            value = "items", defaultValue = "10") int items) throws Exception {
        List<Sender> senders =
                Sender.getAvailableSenders().stream().sorted(Sender.COMPARATOR_BY_FROM_NAME).collect(Collectors.toList());
        paginate(model, "senders", senders, items, page);
        return "/messaging/listSenders";
    }

    @RequestMapping(value = "/sender/{sender}", method = RequestMethod.GET)
    public String viewSender(@PathVariable Sender sender, Model model,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "items", defaultValue = "10") int items) throws DomainException {
        if (!allowedSender(sender)) {
            throw MessagingDomainException.forbidden();
        }
        model.addAttribute("sender", sender);
        List<Message> messages =
                sender.getMessageSet().stream().sorted(Message.COMPARATOR_BY_CREATED_DATE_OLDER_LAST)
                        .collect(Collectors.toList());
        paginate(model, "messages", messages, items, page);
        return "/messaging/viewSender";
    }

    @RequestMapping(value = "/sender/{sender}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<String> viewSenderInfo(@PathVariable Sender sender) throws DomainException {
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
            array.add(new JsonPrimitive(rt.getAddress()));
        }
        info.add("replyTos", array);
        info.addProperty("html", sender.getHtmlSender());
        return new ResponseEntity<String>(info.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/message", method = RequestMethod.GET)
    public ModelAndView newMessage(@ModelAttribute("messageBean") MessageBean messageBean) {
        if (messageBean != null && messageBean.getSender() != null && !allowedSender(messageBean.getSender())) {
            throw MessagingDomainException.forbidden();
        }

        if (messageBean == null) {
            messageBean = new MessageBean();
            final Set<Sender> availableSenders = Sender.getAvailableSenders();
            if (availableSenders.size() == 1) {
                messageBean.setSender(availableSenders.iterator().next());
            }
        }
        return new ModelAndView("/messaging/newMessage", "messageBean", messageBean);
    }

    @RequestMapping(value = "/message", method = RequestMethod.POST)
    public ModelAndView sendMessage(@ModelAttribute("messageBean") MessageBean messageBean, RedirectAttributes redirectAttributes)
            throws Exception {
        if (messageBean != null) {
            if (allowedSender(messageBean.getSender())) {
                Message message = send(messageBean);
                if (message != null) {
                    redirectAttributes.addFlashAttribute("justCreated", true);
                    return new ModelAndView(new RedirectView("/messaging/message/" + message.getExternalId(), true));
                }
            } else {
                throw MessagingDomainException.forbidden();
            }
        }
        return newMessage(messageBean);
    }

    @RequestMapping(value = "/message/{message}", method = RequestMethod.GET)
    public ModelAndView viewMessage(@PathVariable Message message, boolean created) {
        if (!allowedSender(message.getSender())) {
            throw MessagingDomainException.forbidden();
        }

        return new ModelAndView("/messaging/viewMessage", "message", message);
    }

    @RequestMapping(value = "/message/{message}/delete", method = RequestMethod.POST)
    public String deleteMessage(@PathVariable Message message, Model model) throws Exception {
        if (!isCreator(message)) {
            throw MessagingDomainException.forbidden();
        }

        final Sender sender = message.getSender();
        message.delete();
        return viewSender(sender, model, 1, 10);
    }

    private boolean allowedSender(Sender sender) {
        return sender.getMembers().isMember(Authenticate.getUser());
    }

    private boolean isCreator(Message message) {
        return message.getUser().equals(Authenticate.getUser());
    }

    private Message send(MessageBean bean) throws Exception {
        Set<String> validate = bean.validate();
        if (!validate.isEmpty()) {
            bean.setErrors(validate);
            return null;
        } else {
            Base64.Decoder decoder = Base64.getDecoder();
            String fullBody = null;
            Sender sender = bean.getSender();
            Set<String> recipientExpressions = bean.getRecipients();
            List<Group> recipients = null;
            if (recipientExpressions != null) {
                recipients =
                        recipientExpressions.stream().map(e -> Group.parse(new String(decoder.decode(e))))
                                .collect(Collectors.toList());
            }
            if (!Strings.isNullOrEmpty(bean.getMessage())) {
                if (bean.isAutomaticFooter() && recipients != null && !recipients.isEmpty()) {
                    fullBody =
                            BundleUtil.getString("MessagingResources", "message.footer", bean.getMessage(), sender.getFromName(),
                                    recipients.stream().map(r -> r.getPresentationName()).collect(Collectors.joining("\n\t")));
                } else {
                    fullBody = bean.getMessage();
                }
            }
            final MessageBuilder builder = new MessageBuilder(sender, bean.getSubject(), fullBody);
            String htmlMessage = bean.getHtmlMessage();
            if (!Strings.isNullOrEmpty(htmlMessage)) {
                builder.htmlBody(htmlMessage);
            }
            for (Group recipient : recipients) {
                builder.bcc(recipient);
            }
            String bccs = bean.getBccs();
            if (!Strings.isNullOrEmpty(bccs)) {
                for (String bcc : bccs.split(",")) {
                    if (!Strings.isNullOrEmpty(bcc.trim())) {
                        builder.bcc(bcc.trim());
                    }
                }
            }
            Set<String> replyTos = bean.getReplyTos();
            if (replyTos != null) {
                for (String replyTo : replyTos) {
                    builder.replyTo(replyTo);
                }
            }
            return atomic(() -> builder.send());
        }
    }

    private <T> List<T> paginate(Model model, String property, List<T> list, int items, int page) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        items = itemsClip(items, list.size());
        List<List<T>> pages = Lists.partition(list, items);
        page = pageClip(page, pages.size());
        List<T> selected = pages.get(page - 1);
        if (model != null) {
            if (!Strings.isNullOrEmpty(property)) {
                model.addAttribute(property, selected);
            }
            model.addAttribute("page", page);
            model.addAttribute("items", items);
            model.addAttribute("pages", pages.size());
        }
        return selected;
    }

    private int itemsClip(int val, int max) {
        if (val < 1) {
            return max;
        }
        return val;
    }

    private int pageClip(int val, int max) {
        val = val % max;
        if (val < 1) {
            return max + val;
        }
        return val;
    }
}
