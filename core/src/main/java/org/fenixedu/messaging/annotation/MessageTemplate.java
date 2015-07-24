package org.fenixedu.messaging.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MessageTemplates.class)
public @interface MessageTemplate {
    String id();

    String name();

    String description();

    String subject() default "";

    String body() default "";

    String html() default "";

    String bundle() default "";

    boolean footer() default true;
}
