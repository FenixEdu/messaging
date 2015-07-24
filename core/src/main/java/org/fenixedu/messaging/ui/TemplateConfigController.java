package org.fenixedu.messaging.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.domain.MessagingSystem;
import org.fenixedu.messaging.exception.MessagingDomainException;
import org.fenixedu.messaging.template.MessageTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@SpringFunctionality(app = MessagingController.class, title = "title.messaging.templates", accessGroup = "#managers")
@RequestMapping("/messaging/templates")
public class TemplateConfigController {

    @RequestMapping
    public String listTemplates(Model model, @RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(
            value = "items", defaultValue = "10") int items) {
        List<MessageTemplate> templates =
                MessagingSystem.getTemplates().stream().sorted(MessageTemplate.COMPARATOR_BY_ID).collect(Collectors.toList());
        PaginationUtils.paginate(model, "templates", templates, items, page);
        return "messaging/listTemplates";
    }

    @RequestMapping("/{code}")
    public ModelAndView viewTemplate(@PathVariable String code) throws Exception {
        MessageTemplate t = MessagingSystem.getTemplateByCode(code);
        if (t == null) {
            throw MessagingDomainException.missingTemplate(MessageTemplate.decodeId(code));
        }

        Set<Locale> locales = new HashSet<Locale>();
        locales.addAll(CoreConfiguration.supportedLocales());
        locales.addAll(t.getSubject().getLocales());
        LocalizedString content = t.getBody();
        if (content != null) {
            locales.addAll(content.getLocales());
        }
        content = t.getHtmlBody();
        if (content != null) {
            locales.addAll(content.getLocales());
        }

        Map<String, Object> model = new HashMap<>();
        model.put("templateLocales", locales);
        model.put("template", t);
        return new ModelAndView("messaging/viewTemplate", model);
    }

    @RequestMapping("/{code}/edit")
    public ModelAndView editTemplate(@PathVariable String code, @ModelAttribute("templateBean") MessageContentBean bean)
            throws Exception {
        MessageTemplate template = MessagingSystem.getTemplateByCode(code);
        if (template == null) {
            throw MessagingDomainException.missingTemplate(MessageTemplate.decodeId(code));
        }
        bean = template.bean();
        return new ModelAndView("messaging/editTemplate", "templateBean", bean);
    }

    @RequestMapping(value = "/{code}/edit", method = RequestMethod.POST)
    public ModelAndView saveTemplate(@PathVariable String code, @ModelAttribute("templateBean") MessageContentBean bean)
            throws Exception {
        Set<String> errors = bean.validate();
        if (errors.isEmpty()) {
            MessageTemplate template = MessagingSystem.getTemplateByCode(code);
            if (template == null) {
                throw MessagingDomainException.missingTemplate(MessageTemplate.decodeId(code));
            }
            template.copy(bean);
            return viewTemplate(code);
        }
        return new ModelAndView("messaging/editTemplate", "templateBean", bean);
    }
}
