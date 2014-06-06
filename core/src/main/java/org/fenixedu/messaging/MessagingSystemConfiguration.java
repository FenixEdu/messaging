package org.fenixedu.messaging;

import org.fenixedu.commons.configuration.ConfigurationInvocationHandler;
import org.fenixedu.commons.configuration.ConfigurationManager;
import org.fenixedu.commons.configuration.ConfigurationProperty;

public class MessagingSystemConfiguration {
    @ConfigurationManager(description = "Messaging System Configurations")
    public interface ConfigurationProperties {
        @ConfigurationProperty(key = "daysToKeepSentMessages",
                description = "Number of days to keep sent messages in the system for status access and historic",
                defaultValue = "7")
        public Integer daysToKeepSentMessages();
    }

    public static ConfigurationProperties getConfiguration() {
        return ConfigurationInvocationHandler.getConfiguration(ConfigurationProperties.class);
    }
}
