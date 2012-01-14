package pt.ist.messaging.domain;

import myorg.domain.User;

public class ConcreteReplyTo extends ConcreteReplyTo_Base {

    public ConcreteReplyTo(final String address) {
	super();
	setReplyToAddress(address);
    }

    @Override
    public String getReplyToAddress(final User user) {
	return getReplyToAddress();
    }

}
