package org.fenixedu.messaging.phonecall;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.twilio.Twilio;
import com.twilio.exception.AuthenticationException;
import com.twilio.exception.TwilioException;
import com.twilio.http.HttpMethod;
import com.twilio.http.Request;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import org.apache.commons.lang.StringUtils;
import org.fenixedu.beenu.PhoneCallConfiguration;
import org.fenixedu.bennu.core.api.SystemResource;
import org.fenixedu.bennu.core.rest.Healthcheck;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;

public class PhoneCall {

    private static final Logger logger = LoggerFactory.getLogger(PhoneCall.class);

    private static final String TWILIO_SID = PhoneCallConfiguration.getConfiguration().getTwilioSid();
    private static final String TWILIO_STOKEN = PhoneCallConfiguration.getConfiguration().getTwilioStoken();
    private static final String TWILIO_FROM_NUMBER = PhoneCallConfiguration.getConfiguration().getTwilioFromNumber();

    private static PhoneCall instance;

    private TwilioRestClient TWILIO_CLIENT;

    private static synchronized PhoneCall init() {
        if (instance == null) {
            instance = new PhoneCall().initTwilio();
        }
        return instance;
    }

    public static PhoneCall getInstance() {
        return instance == null ? init() : instance;
    }

    private PhoneCall initTwilio() {
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

    public boolean makeCall(final String number, final String code, final String lang) {
        if (CoreConfiguration.getConfiguration().developmentMode().booleanValue()) {
            System.out.println("Phone Call code to " + number + " : " + code);
            return true;
        } else if (TWILIO_CLIENT != null) {
            try {
                Call.creator(new PhoneNumber(number), new PhoneNumber(TWILIO_FROM_NUMBER),
                        new URI(PhoneCallConfiguration.getConfiguration().phoneCallValidationUrl()
                                + "&code=" + code + "&lang=" + lang))
                        .create(TWILIO_CLIENT);
                return true;
            }
            catch (URISyntaxException | TwilioException e) {
                logger.error("Error makeCall: " + e.getMessage(), e);
                return false;
            }
        } else {
            throw new Error("Twilio Client Not Initialized!");
        }
    }

}
