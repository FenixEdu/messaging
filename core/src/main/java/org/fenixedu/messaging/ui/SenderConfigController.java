package org.fenixedu.messaging.ui;

import static pt.ist.fenixframework.FenixFramework.atomic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.messaging.domain.MessagingSystem;
import org.fenixedu.messaging.domain.ReplyTo;
import org.fenixedu.messaging.domain.Sender;
import org.fenixedu.messaging.exception.MessagingDomainException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@SpringFunctionality(app = MessagingController.class, title = "title.messaging.senders", accessGroup = "#managers")
@RequestMapping("/messaging/config")
public class SenderConfigController {

    @RequestMapping(value = { "", "/" })
    public RedirectView redirectToConfiguration() {
        return new RedirectView("/messaging/config/senders", true);
    }

    @RequestMapping(value = { "/senders", "/senders/" })
    public String listSenders(Model model, @RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(
            value = "items", defaultValue = "10") int items) {
        model.addAttribute("configure", true);
        Sender systemSender = MessagingSystem.getInstance().getSystemSender();
        model.addAttribute("systemSender", systemSender);
        TreeSet<ReplyTo> replyTos = new TreeSet<ReplyTo>(ReplyTo.COMPARATOR_BY_NAME);
        replyTos.addAll(systemSender.getReplyTos());
        model.addAttribute("systemReplyTos", replyTos);
        TreeSet<Group> recipients = new TreeSet<Group>(Sender.RECIPIENT_COMPARATOR_BY_NAME);
        recipients.addAll(systemSender.getRecipients());
        model.addAttribute("systemRecipients", recipients);
        Set<Sender> senderSet = new HashSet<Sender>(MessagingSystem.getInstance().getSenderSet());
        senderSet.remove(systemSender);
        List<Sender> senders = senderSet.stream().sorted(Sender.COMPARATOR_BY_FROM_NAME).collect(Collectors.toList());
        PaginationUtils.paginate(model, "messaging/config/senders", "senders", senders, items, page);
        return "messaging/listSenders";
    }

    @RequestMapping("/senders/{sender}")
    public ModelAndView viewSender(@PathVariable Sender sender) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("configure", true);
        model.put("sender", sender);
        TreeSet<ReplyTo> replyTos = new TreeSet<ReplyTo>(ReplyTo.COMPARATOR_BY_NAME);
        replyTos.addAll(sender.getReplyTos());
        model.put("replyTos", replyTos);
        TreeSet<Group> recipients = new TreeSet<Group>(Sender.RECIPIENT_COMPARATOR_BY_NAME);
        recipients.addAll(sender.getRecipients());
        model.put("recipients", recipients);
        return new ModelAndView("messaging/viewSender", model);
    }

    @RequestMapping("/senders/new")
    public ModelAndView newSender(Model model) throws Exception {
        model.addAttribute("create", true);
        return editSender(null, new SenderBean());
    }

    @RequestMapping(value = "/senders/new", method = RequestMethod.POST)
    public ModelAndView createSender(@ModelAttribute("senderBean") SenderBean bean) throws Exception {
        Sender sender = bean.newSender();
        if (sender != null) {
            return viewSender(sender);
        }
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("create", true);
        model.put("senderBean", bean);
        return new ModelAndView("messaging/editSender", model);

    }

    @RequestMapping("/senders/{sender}/edit")
    public ModelAndView editSender(@PathVariable Sender sender, @ModelAttribute("senderBean") SenderBean bean) throws Exception {
        bean.copy(sender);
        return new ModelAndView("messaging/editSender", "senderBean", bean);
    }

    @RequestMapping(value = "/senders/{sender}/edit", method = RequestMethod.POST)
    public ModelAndView saveSender(@PathVariable Sender sender, @ModelAttribute("senderBean") SenderBean bean) throws Exception {
        Set<String> errors = bean.configure(sender);
        if (errors.isEmpty()) {
            return viewSender(sender);
        }
        return new ModelAndView("messaging/editSender", "senderBean", bean);
    }

    @RequestMapping("/senders/{sender}/delete")
    public String deleteSender(Model model, @PathVariable Sender sender) throws Exception {
        if (MessagingSystem.systemSender().equals(sender)) {
            throw MessagingDomainException.forbidden();
        }
        atomic(() -> sender.delete());
        return listSenders(model, 1, 10);
    }
}
