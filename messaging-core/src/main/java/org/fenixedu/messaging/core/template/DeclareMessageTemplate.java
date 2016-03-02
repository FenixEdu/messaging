package org.fenixedu.messaging.core.template;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DeclareMessageTemplates.class)
public @interface DeclareMessageTemplate {
    String id();

    String description() default "";

    String subject() default "";

    String text() default "";

    String html() default "";

    String bundle() default "";

    TemplateParameter[] parameters() default {};
}