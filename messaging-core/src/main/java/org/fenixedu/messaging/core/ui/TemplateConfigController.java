package org.fenixedu.messaging.core.ui;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.fenixedu.messaging.core.domain.MessageTemplate;
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
        PaginationUtils.paginate(model, "messaging/config/templates", "templates", MessageTemplate.all(), items, page);
        return "messaging/listTemplates";
    }

    @RequestMapping("/{template}")
    public ModelAndView viewTemplate(@PathVariable MessageTemplate template) throws Exception {
        Set<Locale> locales = new HashSet<Locale>(CoreConfiguration.supportedLocales());
        Stream.of(template.getSubject(), template.getTextBody(), template.getHtmlBody()).flatMap(ls -> ls.getLocales().stream())
                .forEach(locales::add);
        return new ModelAndView("messaging/viewTemplate", ImmutableMap.of("template", template, "locales",
                getSupportedLocales(template)));
    }

    @RequestMapping("/{template}/edit")
    public String editTemplate(Model model, @PathVariable MessageTemplate template,
            @ModelAttribute("templateBean") MessageContentBean bean) throws Exception {
        bean.copy(template);
        model.addAttribute("template", template);
        model.addAttribute("templateBean", bean);
        return "messaging/editTemplate";
    }

    @RequestMapping("/{template}/reset")
    public String resetTemplate(Model model, @PathVariable MessageTemplate template) throws Exception {
        model.addAttribute("template", template);
        model.addAttribute("templateBean", new MessageContentBean(template.getDeclaration()));
        return "messaging/editTemplate";
    }

    @RequestMapping(value = "/{template}/edit", method = RequestMethod.POST)
    public ModelAndView saveTemplate(Model model, @PathVariable MessageTemplate template,
            @ModelAttribute("templateBean") MessageContentBean bean) throws Exception {
        if (bean.edit(template)) {
            return viewTemplate(template);
        }
        model.addAttribute("template", template);
        model.addAttribute("templateBean", bean);
        return new ModelAndView("messaging/editTemplate", model.asMap());
    }

    private Set<Locale> getSupportedLocales(MessageTemplate template) {
        Set<Locale> locales = template.getContentLocales();
        locales.addAll(CoreConfiguration.supportedLocales());
        return locales;
    }
}
