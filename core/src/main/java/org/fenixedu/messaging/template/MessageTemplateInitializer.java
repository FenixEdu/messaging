package org.fenixedu.messaging.template;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import org.fenixedu.messaging.annotation.MessageTemplate;
import org.fenixedu.messaging.annotation.MessageTemplates;
import org.fenixedu.messaging.domain.MessagingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@HandlesTypes({ MessageTemplate.class, MessageTemplates.class })
public class MessageTemplateInitializer implements ServletContainerInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(MessageTemplateInitializer.class);

    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext ctx) throws ServletException {
        LOG.info("Processing messaging templates.");
        if (classes != null) {
            for (Class<?> type : classes) {
                for (MessageTemplate t : type.getAnnotationsByType(MessageTemplate.class)) {
                    MessagingSystem.addTemplate(t);
                }
            }
        }
    }

}
