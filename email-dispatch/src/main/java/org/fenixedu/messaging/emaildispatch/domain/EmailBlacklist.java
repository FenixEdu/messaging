package org.fenixedu.messaging.emaildispatch.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.messaging.domain.MessagingSystem;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class EmailBlacklist extends EmailBlacklist_Base {
    private static final Logger logger = LoggerFactory.getLogger(LocalEmailMessageDispatchReport.class);

    private static final String TIMESTAMP = "ts";
    private static final String EMAIL = "eml";
    private static final String STATUS = "st";
    private static final String STATUS_INVALID = "invalid";
    private static final String STATUS_FAILED = "failed";

    protected EmailBlacklist() {
        super();
        setMessagingSystem(MessagingSystem.getInstance());
    }

    public static EmailBlacklist getInstance() {
        EmailBlacklist instance = MessagingSystem.getInstance().getBlacklist();
        return instance != null ? instance : create();
    }

    @Atomic(mode = TxMode.WRITE)
    private static EmailBlacklist create() {
        EmailBlacklist instance = MessagingSystem.getInstance().getBlacklist();
        return instance != null ? instance : new EmailBlacklist();
    }

    @Override
    protected JsonArray getBlacklist() {
        return super.getBlacklist() == null || super.getBlacklist().isJsonNull() ? new JsonArray() : super.getBlacklist()
                .getAsJsonArray();
    }

    public void addInvalidAddress(String invalid) {
        log(invalid, STATUS_INVALID);
        logger.warn("Blacklisting email {} because is invalid", invalid);
    }

    public void addFailedAddress(String failed) {
        log(failed, STATUS_FAILED);
        logger.warn("Blacklisting email {} because it failed a deliver", failed);
    }

    public void pruneOldLogs(DateTime before) {
        JsonArray newBl = new JsonArray();
        for (JsonElement log : getBlacklist().getAsJsonArray()) {
            String ts = log.getAsJsonObject().get(TIMESTAMP).getAsString();
            if (!DateTime.parse(ts).isBefore(before)) {
                newBl.add(log);
            }
        }
        setBlacklist(newBl);
    }

    public Set<String> getInvalidEmails() {
        Set<String> invalid = new HashSet<>();
        for (JsonElement log : getBlacklist().getAsJsonArray()) {
            if (log.getAsJsonObject().get(STATUS).getAsString().equals(STATUS_INVALID)) {
                invalid.add(log.getAsJsonObject().get(EMAIL).getAsString());
            }
        }
        return invalid;
    }

    public Set<String> getFailedEmails(int times) {
        Multiset<String> failed = HashMultiset.create();
        for (JsonElement log : getBlacklist().getAsJsonArray()) {
            if (log.getAsJsonObject().get(STATUS).getAsString().equals(STATUS_FAILED)) {
                failed.add(log.getAsJsonObject().get(EMAIL).getAsString());
            }
        }
        return failed.stream().filter(email -> failed.count(email) > times).collect(Collectors.toSet());
    }

    private void log(String email, String status) {
        JsonObject log = new JsonObject();
        log.addProperty(TIMESTAMP, new DateTime().toString());
        log.addProperty(EMAIL, email);
        log.addProperty(STATUS, status);
        JsonArray bl = getBlacklist();
        bl.add(log);
        setBlacklist(bl);
    }
}
