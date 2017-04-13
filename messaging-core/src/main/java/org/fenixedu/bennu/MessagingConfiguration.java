package org.fenixedu.bennu;

import org.fenixedu.bennu.spring.BennuSpringModule;
import org.fenixedu.commons.configuration.ConfigurationInvocationHandler;
import org.fenixedu.commons.configuration.ConfigurationManager;
import org.fenixedu.commons.configuration.ConfigurationProperty;

@BennuSpringModule(basePackages = "org.fenixedu.messaging", bundles = "MessagingResources")
public class MessagingConfiguration {

    @ConfigurationManager(description = "Email Dispatcher Configurations")
    public interface ConfigurationProperties {
        @ConfigurationProperty(key = "messaging.jwt.api.secret.key")
        String jwtKey();

        @ConfigurationProperty(key = "messaging.jwt.api.algorithm", defaultValue = "HS256")
        String jwtAlgorithm();

        @ConfigurationProperty(key = "messaging.jwt.api.ttl.ms", defaultValue = "1800000")
        Long jwtTTL();
    }

    public static ConfigurationProperties getConfiguration() {
        return ConfigurationInvocationHandler.getConfiguration(ConfigurationProperties.class);
    }

}