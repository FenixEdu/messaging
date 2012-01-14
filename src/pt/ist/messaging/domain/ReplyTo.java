package pt.ist.messaging.domain;

import java.util.Collection;
import java.util.Collections;

import myorg.domain.User;

public abstract class ReplyTo extends ReplyTo_Base {

    protected ReplyTo() {
	super();
    }

    public void delete() {
	getMessageSet().clear();
	getSenderSet().clear();
	deleteDomainObject();
    }

    public abstract String getReplyToAddress(final User user);

    public abstract String getReplyToAddress();

    public Collection<? extends ReplyTo> asCollection() {
	return Collections.singletonList(this);
    }

}
