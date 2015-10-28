package org.fenixedu.messaging.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.domain.MessageTemplate;
import org.fenixedu.messaging.domain.MessagingSystem;
import org.fenixedu.messaging.template.MessageTemplateDeclaration;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.ImmutableMap;

@SpringFunctionality(app = MessagingController.class, title = "title.messaging.templates", accessGroup = "#managers")
@RequestMapping("/messaging/config/templates")
public class TemplateConfigController {

    @RequestMapping(value = { "", "/" })
    public String listTemplates(Model model, @RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(
            value = "items", defaultValue = "10") int items) {
        List<MessageTemplateDescriptionBean> templates =
                MessagingSystem.getUndeclaredTemplates().stream().map(t -> new MessageTemplateDescriptionBean(t))
                        .collect(Collectors.toList());
        MessagingSystem.getTemplateDeclarations().values().forEach(d -> templates.add(new MessageTemplateDescriptionBean(d)));
        templates.sort(MessageTemplateDescriptionBean.COMPARATOR_BY_ID);
        PaginationUtils.paginate(model, "messaging/config/templates", "templates", templates, items, page);
        return "messaging/listTemplates";
    }

    @RequestMapping("/{template}")
    public ModelAndView viewTemplate(@PathVariable MessageTemplate template) throws Exception {
        MessageTemplateDeclaration decl = MessagingSystem.getTemplateDeclaration(template.getId());

        Set<Locale> locales = new HashSet<Locale>(CoreConfiguration.supportedLocales());
        LocalizedString content = template.getTextBody();
        if (content != null) {
            locales.addAll(content.getLocales());
        }
        content = template.getHtmlBody();
        if (content != null) {
            locales.addAll(content.getLocales());
        }

        return new ModelAndView("messaging/viewTemplate", ImmutableMap.of("templateLocales", locales, "template", decl));
    }

    @RequestMapping("/{template}/edit")
    public String editTemplate(Model model, @PathVariable MessageTemplate template,
            @ModelAttribute("templateBean") MessageBodyBean bean) throws Exception {
        bean.copy(template);
        model.addAttribute("template", MessagingSystem.getTemplateDeclaration(template.getId()));
        model.addAttribute("templateBean", bean);
        return "messaging/editTemplate";
    }

    @RequestMapping("/{template}/reset")
    public String resetTemplate(Model model, @PathVariable MessageTemplate template) throws Exception {
        MessageTemplateDeclaration decl = MessagingSystem.getTemplateDeclaration(template.getId());
        model.addAttribute("template", decl);
        model.addAttribute("templateBean", new MessageBodyBean(decl));
        return "messaging/editTemplate";
    }

    @RequestMapping(value = "/{template}/edit", method = RequestMethod.POST)
    public ModelAndView saveTemplate(Model model, @PathVariable MessageTemplate template,
            @ModelAttribute("templateBean") MessageBodyBean bean) throws Exception {
        if (bean.edit(template)) {
            return viewTemplate(template);
        }
        model.addAttribute("template", MessagingSystem.getTemplateDeclaration(template.getId()));
        model.addAttribute("templateBean", bean);
        return new ModelAndView("messaging/editTemplate", model.asMap());
    }
}
