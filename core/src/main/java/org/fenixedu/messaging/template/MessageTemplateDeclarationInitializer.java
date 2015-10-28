package org.fenixedu.messaging.template;

import java.util.Arrays;
import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import org.fenixedu.messaging.domain.MessagingSystem;
import org.fenixedu.messaging.template.annotation.DeclareMessageTemplate;
import org.fenixedu.messaging.template.annotation.DeclareMessageTemplates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@HandlesTypes({ DeclareMessageTemplate.class, DeclareMessageTemplates.class })
public class MessageTemplateDeclarationInitializer implements ServletContainerInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(MessageTemplateDeclarationInitializer.class);

    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext ctx) throws ServletException {
        LOG.info("Processing messaging templates.");
        if (classes != null) {
            classes.stream().forEach(
                    c -> Arrays.stream(c.getAnnotationsByType(DeclareMessageTemplate.class)).forEach(
                            MessagingSystem::declareTemplate));
        }
    }
}
