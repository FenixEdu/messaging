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
package org.fenixedu.messaging.core.ui;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.bennu.io.domain.GenericFile;
import org.fenixedu.bennu.io.servlet.FileDownloadServlet;
import org.fenixedu.bennu.spring.portal.SpringApplication;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.domain.MessageFile;
import org.fenixedu.messaging.core.domain.Sender;
import org.fenixedu.messaging.core.exception.MessagingDomainException;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;


import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static pt.ist.fenixframework.FenixFramework.atomic;

@SpringApplication(path = "messaging", title = "title.messaging", group = "anyone", hint = "Messaging")
@SpringFunctionality(app = MessagingController.class, title = "title.messaging.sending", accessGroup = "senders")
@RequestMapping("/messaging")
public class MessagingController {

    @RequestMapping(value = { "", "/" })
    public RedirectView redirectToSending() {
        return new RedirectView("/messaging/senders", true);
    }

    @RequestMapping(value = { "/senders", "/senders/" })
    public String listSenders(final Model model, @RequestParam(value = "page", defaultValue = "1") final int page,
            @RequestParam(value = "items", defaultValue = "10") final int items,
            @RequestParam(value = "search", defaultValue = "") final String search) {
        Set<Sender> senders = Sender.available().stream()
                .filter(sender -> sender.getName().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toSet());
        PaginationUtils.paginate(model, "messaging/senders", "senders", senders, items, page);
        model.addAttribute("search", search);
        return "/messaging/listSenders";
    }

    @RequestMapping(value = "/senders/{sender}", method = RequestMethod.GET)
    public String viewSender(final Model model, @PathVariable final Sender sender,  @RequestParam(value = "page", defaultValue = "1") final int page,
            @RequestParam(value = "items", defaultValue = "10") final int items) {
        if (!allowedSender(sender)) {
            throw MessagingDomainException.forbidden();
        }
        model.addAttribute("sender", sender);
        PaginationUtils
                .paginate(model, "messaging/senders/" + sender.getExternalId(), "messages", sender.getMessageSet(), items, page);
        return "/messaging/viewSender";
    }

    @RequestMapping(value = "/senders/{sender}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody ResponseEntity<String> viewSenderInfo(@PathVariable final Sender sender) {
        if (!allowedSender(sender)) {
            throw MessagingDomainException.forbidden();
        }

        JsonObject info = new JsonObject();
        JsonArray array = new JsonArray();
        sender.getRecipients().stream().map(recipient -> MessageBean.buildRecipientJson(sender, recipient))
                .forEach(array::add);
        info.add("recipients", array);
        String replyTo = sender.getReplyTo();
        if (!Strings.isNullOrEmpty(replyTo)) {
            info.add("replyTo", new JsonPrimitive(replyTo));
        }
        info.addProperty("html", sender.getHtmlEnabled());
        info.addProperty("attachmentsEnabled", sender.getAttachmentsEnabled());
        return new ResponseEntity<>(info.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/message", method = RequestMethod.GET)
    public String newMessage(final Model model, @ModelAttribute("messageBean") MessageBean messageBean, final HttpServletRequest request) {

        messageBean = MessagingUtils.getMessageBeanFromSession(request).orElse(messageBean);
        MessagingUtils.clearMessageBeanFromSession(request);

        final Sender sender = Optional.ofNullable(messageBean.getSender())
                .orElse(Sender.available().stream().findAny().orElseThrow(MessagingDomainException::forbidden));

        if (!allowedSender(sender)){
            throw MessagingDomainException.forbidden();
        }

        final Set<JsonObject> collect =
                sender.getRecipients().stream().map(recipient -> MessageBean.buildRecipientJson(sender, recipient))
                        .collect(Collectors.toSet());
        messageBean.setRecipients(collect);
        model.addAttribute("messageBean", messageBean);
        return "/messaging/newMessage";
    }

    @RequestMapping(value = "/message", method = RequestMethod.POST)
    public ModelAndView sendMessage(final Model model, @ModelAttribute("messageBean") MessageBean messageBean,
            final RedirectAttributes redirectAttributes, final HttpServletRequest request) {
        if (messageBean != null) {
            if (allowedSender(messageBean.getSender())) {
                final Message message = messageBean.send();
                if (message != null) {
                    redirectAttributes.addFlashAttribute("justCreated", true);
                    return new ModelAndView(new RedirectView("/messaging/messages/" + message.getExternalId(), true));
                }
            } else {
                throw MessagingDomainException.forbidden();
            }
        }
        return new ModelAndView(newMessage(model, messageBean, request));
    }

    @RequestMapping(value = "/messages/{message}", method = RequestMethod.GET)
    public String viewMessage(final Model model, @PathVariable final Message message) {
        if (!allowedSender(message.getSender())) {
            throw MessagingDomainException.forbidden();
        }
        model.addAttribute("locales", getSupportedLocales(message));
        model.addAttribute("message", message);
        final Map<String, String> messageFiles = new HashMap<>();
        for (GenericFile genericFile : message.getFileSet()) {
            messageFiles.put(genericFile.getFilename(), FileDownloadServlet.getDownloadUrl(genericFile));
        }
        model.addAttribute("files", messageFiles);
        return "/messaging/viewMessage";
    }

    private Set<Locale> getSupportedLocales(final Message message) {
        final Set<Locale> locales = message.getContentLocales();
        locales.addAll(CoreConfiguration.supportedLocales());
        locales.add(message.getPreferredLocale());
        return locales;
    }

    @RequestMapping(value = "/messages/{message}/delete", method = RequestMethod.POST)
    public String deleteMessage(final Model model, @PathVariable final Message message) {
        final Sender sender = message.getSender();
        try {
            message.safeDelete();
        } catch (IllegalStateException e) {
            throw MessagingDomainException.forbidden();
        }
        return viewSender(model, sender, 1, 10);
    }

    @RequestMapping(value = "/messages/uploadFile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody ResponseEntity<String> uploadAttachment(@RequestParam final Sender sender, @RequestParam("file") final MultipartFile file) throws Exception {
        if (!allowedSender(sender)){
            throw MessagingDomainException.forbidden();
        }
        final MessageFile messageFile = atomic(() -> new MessageFile(sender, file.getName(),file.getOriginalFilename(), file.getBytes()));

        final JsonObject info = new JsonObject();
        info.addProperty("fileid",messageFile.getExternalId());
        info.addProperty("filename", messageFile.getFilename());
        return new ResponseEntity<>(info.toString(), HttpStatus.OK);
    }

    private boolean allowedSender(final Sender sender) {
        return sender.getMembers().isMember(Authenticate.getUser());
    }

}
