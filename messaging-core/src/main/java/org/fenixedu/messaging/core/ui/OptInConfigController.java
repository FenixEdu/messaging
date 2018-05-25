package org.fenixedu.messaging.core.ui;

import org.fenixedu.bennu.MessagingConfiguration;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.signals.Signal;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.messaging.core.domain.MessagingSystem;
import org.fenixedu.messaging.core.domain.Sender;
import org.fenixedu.messaging.core.domain.Sender_Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;
import pt.ist.fenixframework.FenixFramework;

import java.util.HashMap;

@SpringFunctionality(app = MessagingController.class, title = "title.messaging.optInConfig", accessGroup = "anyone")
@RequestMapping("/messaging/subscriptions")
public class OptInConfigController {

    private static final Logger logger = LoggerFactory.getLogger(OptInConfigController.class);

    @RequestMapping(value = { "", "/" })
    public RedirectView redirectToConfiguration() {
        return new RedirectView("/messaging/subscriptions/senders", true);
    }

    @RequestMapping(value = { "/senders", "/senders/" })
    public String listSenders(final Model model) {
        HashMap<Sender, Boolean> optInRequiredSenders = new HashMap<>();
        Sender.all().stream()
                .filter(Sender_Base::getOptInRequired)
                .filter(sender -> sender.getRecipients().stream().anyMatch(group -> group.isMember(Authenticate.getUser())))
                .forEach(sender -> optInRequiredSenders.put(sender, sender.getOptedInUsers().contains(Authenticate.getUser())));

        final boolean canConfigOptOut = MessagingSystem.getInstance().getOptOutAvailable().isMember(Authenticate.getUser()) ||
                MessagingSystem.getInstance().getOptedOutGroup().isMember(Authenticate.getUser());
        
        model.addAttribute("canConfigOptOut", canConfigOptOut);
        if (canConfigOptOut) {
            model.addAttribute("isOptedOut", MessagingSystem.getInstance().getOptedOutGroup().isMember(Authenticate.getUser()));
        }

        model.addAttribute("optInRequiredSenders", optInRequiredSenders);
        return "/messaging/listOptIns";
    }

    @RequestMapping(value = {"/optIn/{sender}"})
    public ResponseEntity optIn(@PathVariable Sender sender){
        final User user = Authenticate.getUser();
        FenixFramework.atomic(() -> {
            sender.addOptedInUser(user);
            Signal.emit(MessagingConfiguration.OPTIN_STATUS_UPDATE, new OptInUpdateEvent(sender, user, true));
        });
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = {"/optOut/{sender}"})
    public ResponseEntity optOut(@PathVariable Sender sender){
        final User user = Authenticate.getUser();
        FenixFramework.atomic(() -> {
            sender.removeOptedInUser(user);
            Signal.emit(MessagingConfiguration.OPTIN_STATUS_UPDATE, new OptInUpdateEvent(sender, user, false));
        });
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = {"/globalOptIn"})
    public ResponseEntity globalOptIn(){
        final User user = Authenticate.getUser();
        FenixFramework.atomic(() -> MessagingSystem.getInstance().optIn(user));
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = {"/globalOptOut"})
    public ResponseEntity globalOptOut(){
        final User user = Authenticate.getUser();
        FenixFramework.atomic(() -> MessagingSystem.getInstance().optOut(user));
        return ResponseEntity.ok().build();
    }

}
