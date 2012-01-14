package pt.ist.messaging.domain;

import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;

public class CurrentUserReplyTo extends CurrentUserReplyTo_Base {

    public CurrentUserReplyTo() {
        super();
    }

    @Override
    public String getReplyToAddress(final User user) {
	return user == null ? getReplyToAddress() : user.getEmail();
    }

    public String getReplyToAddress() {
	final User user = UserView.getCurrentUser();
	return user == null ? null : user.getEmail();
    }

}
