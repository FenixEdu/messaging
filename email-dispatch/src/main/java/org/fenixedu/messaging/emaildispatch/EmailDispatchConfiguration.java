package org.fenixedu.messaging.emaildispatch;

import org.fenixedu.commons.configuration.ConfigurationInvocationHandler;
import org.fenixedu.commons.configuration.ConfigurationManager;
import org.fenixedu.commons.configuration.ConfigurationProperty;

public class EmailDispatchConfiguration {
    @ConfigurationManager(description = "Email Dispatcher Configurations")
    public interface ConfigurationProperties {
        @ConfigurationProperty(key = "mail.smtp.host")
        public String mailSmtpHost();

        @ConfigurationProperty(key = "mail.smtp.name")
        public String mailSmtpName();

        @ConfigurationProperty(key = "mailSender.max.recipients")
        public String mailSenderMaxRecipients();

        @ConfigurationProperty(key = "mailingList.host.name")
        public String mailingListHostName();
    }

    public static ConfigurationProperties getConfiguration() {
        return ConfigurationInvocationHandler.getConfiguration(ConfigurationProperties.class);
    }
}
