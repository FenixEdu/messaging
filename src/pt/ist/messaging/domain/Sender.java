package pt.ist.messaging.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.RoleType;
import myorg.domain.User;
import myorg.domain.VirtualHost;
import myorg.domain.groups.PersistentGroup;
import pt.ist.fenixWebFramework.services.Service;

public class Sender extends Sender_Base {

    public static Comparator<Sender> COMPARATOR_BY_FROM_NAME = new Comparator<Sender>() {

	@Override
	public int compare(final Sender sender1, final Sender sender2) {
	    final int c = sender1.getFromName().compareTo(sender2.getFromName());
	    return c == 0 ? sender1.getExternalId().compareTo(sender2.getExternalId()) : c;
	}

    };

    public Sender() {
	super();
	setMessagingSystem(MessagingSystem.getInstance());
	setVirtualHost(VirtualHost.getVirtualHostForThread());
    }

    public Sender(final String fromName, final String fromAddress, final PersistentGroup members) {
	this();
	setFromName(fromName);
	setFromAddress(fromAddress);
	setMembers(members);
    }

    public void delete() {
	for (final Message message : getMessageSet()) {
	    message.delete();
	}
	removeMembers();
	getRecipientsSet().clear();
	removeMessagingSystem();
	deleteDomainObject();
    }

    public static SortedSet<Sender> getAvailableSenders() {
	final User user = UserView.getCurrentUser();

	final SortedSet<Sender> senders = new TreeSet<Sender>(Sender.COMPARATOR_BY_FROM_NAME);
	for (final Sender sender : MessagingSystem.getInstance().getSenderSet()) {
	    if (sender.isMember(user)) {
		senders.add(sender);
	    }
	}

	return senders;
    }

    public boolean isMember(final User user) {
	final PersistentGroup persistentGroup = getMembers();
	return (hasMembers() && persistentGroup.isMember(user)) || (user != null && user.hasRoleType(RoleType.MANAGER));
    }

    public static boolean userHasRecipients() {
	final User user = UserView.getCurrentUser();
	for (final Sender sender : MessagingSystem.getInstance().getSenderSet()) {
	    if (sender.isMember(user) && sender.hasAnyRecipients()) {
		return true;
	    }
	}
	return false;
    }

    @Service
    public List<ReplyTo> getConcreteReplyTos() {
	List<ReplyTo> replyTos = new ArrayList<ReplyTo>();
	for (ReplyTo replyTo : getReplyToSet()) {
	    if (replyTo instanceof CurrentUserReplyTo) {
		final User user = UserView.getCurrentUser();
		final UserReplyTo userReplyTo = user.hasUserReplyTo() ?
			user.getUserReplyTo() : UserReplyTo.createFor(user);
		replyTos.add(userReplyTo);
	    } else {
		replyTos.add(replyTo);
	    }
	}
	return replyTos;
    }

    public String getFromName(final User user) {
	return getFromName();
    }

    public void deleteOldMessages() {
	final SortedSet<Message> messages = new TreeSet<Message>(Message.COMPARATOR_BY_CREATED_DATE_OLDER_LAST);
	messages.addAll(getMessageSet());
	int sentCounter = 0;
	for (final Message message : messages) {
	    if (message.getSent() != null) {
		++sentCounter;
		if (sentCounter > Message.NUMBER_OF_SENT_EMAILS_TO_STAY) {
		    message.delete();
		}
	    }
	}
    }

}
