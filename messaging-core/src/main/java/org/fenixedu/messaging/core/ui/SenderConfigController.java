package org.fenixedu.messaging.core.ui;

import com.google.common.collect.ImmutableMap;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.messaging.core.domain.MessagingSystem;
import org.fenixedu.messaging.core.domain.Sender;
import org.fenixedu.messaging.core.exception.MessagingDomainException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ist.fenixframework.FenixFramework.atomic;

@SpringFunctionality(app = MessagingController.class, title = "title.messaging.senders", accessGroup = "#managers")
@RequestMapping("/messaging/config")
public class SenderConfigController {

    @RequestMapping(value = { "", "/" })
    public RedirectView redirectToConfiguration() {
        return new RedirectView("/messaging/config/senders", true);
    }

    @RequestMapping(value = { "/senders", "/senders/" })
    public String listSenders(Model model, @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "items", defaultValue = "10") int items,
            @RequestParam(value = "search", defaultValue = "") String search) {
        model.addAttribute("configure", true);
        Sender systemSender = MessagingSystem.systemSender();
        model.addAttribute("sender", systemSender);
        model.addAttribute("system", true);
        Set<Sender> senderSet = Sender.all().stream()
                .filter(s -> s.getName().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toSet());
        senderSet.remove(systemSender);
        PaginationUtils.paginate(model, "messaging/config/senders", "senders", senderSet, items, page);
        model.addAttribute("search", search);
        return "messaging/listSenders";
    }

    @RequestMapping("/senders/{sender}")
    public ModelAndView viewSender(@PathVariable Sender sender) throws Exception {
        return new ModelAndView("messaging/viewSender",
                ImmutableMap.of("configure", true, "sender", sender, "system", sender.equals(MessagingSystem.systemSender())));
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
        return new ModelAndView("messaging/editSender", ImmutableMap.of("create", true, "senderBean", bean));

    }

    @RequestMapping("/senders/{sender}/edit")
    public ModelAndView editSender(@PathVariable Sender sender, @ModelAttribute("senderBean") SenderBean bean) throws Exception {
        bean.copy(sender);
        return new ModelAndView("messaging/editSender", "senderBean", bean);
    }

    @RequestMapping(value = "/senders/{sender}/edit", method = RequestMethod.POST)
    public ModelAndView saveSender(@PathVariable Sender sender, @ModelAttribute("senderBean") SenderBean bean) throws Exception {
        Collection<String> errors = bean.configure(sender);
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
        atomic(sender::delete);
        return "redirect:/messaging/config/senders";
    }
}
