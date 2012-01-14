package pt.ist.messaging.domain;

import myorg.domain.User;
import myorg.domain.exceptions.DomainException;
import pt.ist.fenixWebFramework.services.Service;

public class UserReplyTo extends UserReplyTo_Base {
    
    public UserReplyTo(final User user) {
        super();
        if (user.hasUserReplyTo()) {
            throw new DomainException("error.person.already.has.reply.to");
        }
        setUser(user);
    }

    @Service
    public static UserReplyTo createFor(final User user) {
	return user.hasUserReplyTo() ? user.getUserReplyTo() : new UserReplyTo(user);
    }

    @Override
    public String getReplyToAddress(final User user) {
	return user.getEmail();
    }

    public String getReplyToAddress() {
	return getUser().getEmail();
    }

    @Override
    public void delete() {
	removeUser();
	super.delete();
    }

}
