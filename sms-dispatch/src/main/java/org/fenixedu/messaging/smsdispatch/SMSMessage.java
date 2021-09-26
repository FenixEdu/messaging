package org.fenixedu.messaging.smsdispatch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.twilio.Twilio;
import com.twilio.exception.AuthenticationException;
import com.twilio.http.HttpMethod;
import com.twilio.http.Request;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.commons.lang.StringUtils;
import org.fenixedu.beenu.SMSDispatchConfiguration;
import org.fenixedu.bennu.core.api.SystemResource;
import org.fenixedu.bennu.core.rest.Healthcheck;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.standards.telecommunications.DailingCode;

import java.util.Base64;
import java.util.stream.Stream;

public class SMSMessage {

    private static final Logger logger = LoggerFactory.getLogger(SMSMessage.class);

    private static final String TWILIO_SID = SMSDispatchConfiguration.getConfiguration().getTwilioSid();
    private static final String TWILIO_STOKEN = SMSDispatchConfiguration.getConfiguration().getTwilioStoken();
    private static final String TWILIO_DEFAULT_MESSAGING_SERVICE_SID = SMSDispatchConfiguration.getConfiguration().getTwilioDefaultMessagingServiceSid();
    private static final String TWILIO_FROM_NUMBER = SMSDispatchConfiguration.getConfiguration().getTwilioFromNumber();
    private static final String TWILIO_FROM_NAME = SMSDispatchConfiguration.getConfiguration().getTwilioFromName();

    private static final String GATEWAY_SMS_URL = SMSDispatchConfiguration.getConfiguration().getGatewaySMSUrl();
    private static final String GATEWAY_SMS_USERNAME = SMSDispatchConfiguration.getConfiguration().getGatewaySMSUsername();
    private static final String GATEWAY_SMS_PASSWORD = SMSDispatchConfiguration.getConfiguration().getGatewaySMSPassword();
    private static final String GATEWAY_SMS_HASH = new String(Base64.getEncoder().encode((GATEWAY_SMS_USERNAME + ":" + GATEWAY_SMS_PASSWORD).getBytes()));

    private static SMSMessage instance;

    private TwilioRestClient TWILIO_CLIENT;

    private static synchronized SMSMessage init() {
        if (instance == null) {
            instance = new SMSMessage().initTwilio();
        }
        return instance;
    }

    public static SMSMessage getInstance() {
        return instance == null ? init() : instance;
    }

    private SMSMessage initTwilio() {
        if (TWILIO_SID != null) {
            try {
                Twilio.init(TWILIO_SID, TWILIO_STOKEN);
                TWILIO_CLIENT = Twilio.getRestClient();
            } catch (AuthenticationException e) {
                logger.error("Failed authenticate on Twilio initialization: " + e.getMessage(), e);
                TWILIO_CLIENT = null;
                return this;
            }

            if (Stream.of(TWILIO_SID, TWILIO_STOKEN, TWILIO_FROM_NUMBER).noneMatch(StringUtils::isEmpty)) {
                final String TWILIO_ACCOUNT_URL = String.format("/2010-04-01/Accounts/%s.json", TWILIO_SID);

                SystemResource.registerHealthcheck(new Healthcheck() {
                    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

                    @Override
                    public String getName() {
                        return "Twilio";
                    }

                    @Override
                    protected Result check() throws Exception {
                        JsonElement json = new JsonParser()
                                .parse(TWILIO_CLIENT.request(new Request(HttpMethod.GET, TWILIO_ACCOUNT_URL)).getContent());
                        return Result.healthy(gson.toJson(json));
                    }
                });
            }
        }
        return this;
    }

    private String sendTwilioSMS(final String number, String message) {
        return Message.creator(new PhoneNumber(number), new PhoneNumber(TWILIO_FROM_NAME), message)
                .setMessagingServiceSid(TWILIO_DEFAULT_MESSAGING_SERVICE_SID)
                .create(TWILIO_CLIENT).getSid();
    }

    private boolean sendGatewaySMS(final String number, final String message) {
        final HttpResponse<String> post = Unirest.post(GATEWAY_SMS_URL)
                .header("Authorization", "Basic " + GATEWAY_SMS_HASH)
                .field("number", number)
                .field("msg", message)
                .asString();
        return post.getStatus() == 200;
    }

    public boolean sendSMS(final String number, final String message) {
        if (CoreConfiguration.getConfiguration().developmentMode().booleanValue()) {
            System.out.println("SMS to " + number + " : " + message);
//        } else if (TWILIO_CLIENT != null && isAllowedAlphaSender(number)) {
//            final String mid = sendTwilioSMS(number, message);
        } else {
            return sendGatewaySMS(number, message);
        }
        return true;
    }

    private boolean isAllowedAlphaSender(final String number) {
        return DailingCode.allowAlphaSenders().anyMatch(prefix -> number.startsWith(prefix));
    }

}
