package org.fenixedu.messaging.core.ui;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.messaging.core.domain.Sender;

public class OptInUpdateEvent {
    private Sender sender;
    private User user;
    private boolean newOptInStatus;

    public OptInUpdateEvent(Sender sender, User user, boolean newOptInStatus) {
        this.sender = sender;
        this.user = user;
        this.newOptInStatus = newOptInStatus;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isNewOptInStatus() {
        return newOptInStatus;
    }

    public void setNewOptInStatus(boolean newOptInStatus) {
        this.newOptInStatus = newOptInStatus;
    }
}
