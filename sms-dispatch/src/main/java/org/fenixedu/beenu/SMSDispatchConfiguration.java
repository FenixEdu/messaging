package org.fenixedu.beenu;

import org.fenixedu.bennu.spring.BennuSpringModule;
import org.fenixedu.commons.configuration.ConfigurationInvocationHandler;
import org.fenixedu.commons.configuration.ConfigurationManager;
import org.fenixedu.commons.configuration.ConfigurationProperty;

@BennuSpringModule(basePackages = "org.fenixedu.messaging.smsdispatch", bundles = "SMSDispatchResources")
public class SMSDispatchConfiguration {

    @ConfigurationManager(description = "SMS Dispatcher Configurations")
    public interface ConfigurationProperties {
        @ConfigurationProperty(key = "twilio.from.number")
        public String getTwilioFromNumber();

        @ConfigurationProperty(key = "twilio.from.name")
        public String getTwilioFromName();

        @ConfigurationProperty(key = "twilio.sid")
        public String getTwilioSid();

        @ConfigurationProperty(key = "twilio.stoken")
        public String getTwilioStoken();

        @ConfigurationProperty(key = "twilio.default.messaging.service.sid")
        public String getTwilioDefaultMessagingServiceSid();

        @ConfigurationProperty(key = "ciist.sms.gateway.url")
        public String getGatewaySMSUrl();

        @ConfigurationProperty(key = "ciist.sms.username")
        public String getGatewaySMSUsername();

        @ConfigurationProperty(key = "ciist.sms.password")
        public String getGatewaySMSPassword();
    }

    public static ConfigurationProperties getConfiguration() {
        return ConfigurationInvocationHandler.getConfiguration(ConfigurationProperties.class);
    }

}