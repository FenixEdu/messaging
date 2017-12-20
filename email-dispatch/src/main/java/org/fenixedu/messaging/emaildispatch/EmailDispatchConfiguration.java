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

        @ConfigurationProperty(key = "mail.smtp.port")
        public String mailSmtpPort();

        @ConfigurationProperty(key = "mailSender.max.recipients", defaultValue = "50")
        public Integer mailSenderMaxRecipients();

        @ConfigurationProperty(key = "mail.mime.id.suffix", defaultValue = "email-dispatch")
        public String mailMimeMessageIdSuffix();

        @ConfigurationProperty(
                key = "mailSender.bcc.recipients",
                defaultValue = "false",
                description = "If true, To and Cc recipients will be treated as Bcc recipients. The dispatcher does not guarantee the visibility of these types of recipients in sent emails. This flag exists for the cases where it may be preferable to guarantee that none are visible.")
        public Boolean recipientsAsBccs();

    }

    public static ConfigurationProperties getConfiguration() {
        return ConfigurationInvocationHandler.getConfiguration(ConfigurationProperties.class);
    }
}
