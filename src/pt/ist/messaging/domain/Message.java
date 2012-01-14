package pt.ist.messaging.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import jvstm.TransactionalCommand;
import module.organization.domain.Person;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;
import myorg.domain.groups.PersistentGroup;

import org.joda.time.DateTime;

import pt.ist.emailNotifier.domain.Email;
import pt.ist.emailNotifier.domain.MessageId;
import pt.ist.emailNotifier.util.EmailAddressList;
import pt.ist.fenixframework.pstm.Transaction;

public class Message extends Message_Base {

    static final public Comparator<Message> COMPARATOR_BY_CREATED_DATE_OLDER_FIRST = new Comparator<Message>() {
	public int compare(Message o1, Message o2) {
	    return o1.getCreated().compareTo(o2.getCreated());
	}
    };

    static final public Comparator<Message> COMPARATOR_BY_CREATED_DATE_OLDER_LAST = new Comparator<Message>() {
	public int compare(Message o1, Message o2) {
	    return o2.getCreated().compareTo(o1.getCreated());
	}
    };

    public static final int NUMBER_OF_SENT_EMAILS_TO_STAY = 500;

    public Message() {
	super();
	final MessagingSystem messagingSystem = MessagingSystem.getInstance();
	setMessagingSystem(messagingSystem);
	setMessagingSystemFromPendingDispatch(messagingSystem);
    }

    public Message(final Sender sender, String to, String subject, String body) {
	this(sender, sender.getReplyToSet(), null, subject, body, to);
    }

    public Message(final Sender sender, final Collection<? extends ReplyTo> replyTos, final Collection<PersistentGroup> tos,
	    final Collection<PersistentGroup> ccs, final Collection<PersistentGroup> bccs,
	    final String subject, final String body, final Set<String> bccStrings) {
	this(sender, replyTos, bccs, subject, body, bccStrings);
	if (tos != null) getToSet().addAll(tos);
	if (ccs != null) getCcSet().addAll(ccs);
    }

    public Message(final Sender sender, final Collection<? extends ReplyTo> replyTos, final Collection<PersistentGroup> tos,
	    final Collection<PersistentGroup> ccs, final Collection<PersistentGroup> bccs,
	    final String subject, final String body, final Set<String> bccStrings, final String htmlBody) {
	this(sender, replyTos, bccs, subject, body, bccStrings);
	if (tos != null) getToSet().addAll(tos);
	if (ccs != null) getCcSet().addAll(ccs);
    }

    public Message(final Sender sender, final PersistentGroup bcc, final String subject, final String body) {
	this(sender, sender.getConcreteReplyTos(), Collections.singleton(bcc), subject, body, new EmailAddressList(
		Collections.EMPTY_LIST).toString());
    }

    public Message(final Sender sender, final Collection<? extends ReplyTo> replyTos, final Collection<PersistentGroup> bccs,
	    final String subject, final String body, final Set<String> bccStrings) {
	this(sender, replyTos, bccs, subject, body, new EmailAddressList(bccStrings).toString());
    }

    public Message(final Sender sender, final Collection<? extends ReplyTo> replyTos, final Collection<PersistentGroup> bccs,
	    final String subject, final String body, final String bccString) {
	this();
	setSender(sender);
	if (replyTos != null) getReplyToSet().addAll(replyTos);
	if (bccs != null) getBccSet().addAll(bccs);
	setSubject(subject);
	setBody(body);
	setBccString(bccString);
	setUser(UserView.getCurrentUser());
	setCreated(new DateTime());
    }

    public Message(final Sender sender, final Collection<? extends ReplyTo> replyTos, final Collection<PersistentGroup> bccs,
	    final String subject, final String body, final String bccString, final String htmlBody) {
	this(sender, replyTos, bccs, subject, body, bccString);
	setHtmlBody(htmlBody);
    }

    public void safeDelete() {
	if (getSent() == null) {
	    delete();
	}
    }

    public void delete() {
	getToSet().clear();
	getCcSet().clear();
	getBccSet().clear();
	for (final ReplyTo replyTo : getReplyToSet()) {
	    removeReplyTo(replyTo);
	    if (!replyTo.hasAnySender()) {
		replyTo.delete();
	    }
	}
	for (final Email email : getEmailSet()) {
	    email.delete();
	}

	removeSender();
	removeUser();
	removeMessagingSystemFromPendingDispatch();
	removeMessagingSystem();
	deleteDomainObject();
    }

    public String getRecipientsAsText() {
	final StringBuilder stringBuilder = new StringBuilder();
	recipients2Text(stringBuilder, getBccSet());
	if (getBccString() != null && !getBccString().isEmpty()) {
	    if (stringBuilder.length() > 0) {
		stringBuilder.append("\n");
	    }
	    stringBuilder.append(getBccString());
	}
	return stringBuilder.toString();
    }

    public String getRecipientsAsToText() {
	return recipients2Text(getToSet());
    }

    public String getRecipientsAsCcText() {
	return recipients2Text(getCcSet());
    }

    protected static String recipients2Text(final Set<PersistentGroup> recipients) {
	final StringBuilder stringBuilder = new StringBuilder();
	recipients2Text(stringBuilder, recipients);
	return stringBuilder.toString();
    }

    protected static void recipients2Text(final StringBuilder stringBuilder, final Set<PersistentGroup> recipients) {
	for (final PersistentGroup recipient : recipients) {
	    if (stringBuilder.length() > 0) {
		stringBuilder.append("\n");
	    }
	    stringBuilder.append(recipient.getPresentationName());
	}
    }

    private static class Worker extends Thread {

	private final Set<PersistentGroup> recipients;

	private final Set<String> emailAddresses = new HashSet<String>();

	private Worker(final Set<PersistentGroup> recipients) {
	    this.recipients = recipients;
	}

	@Override
	public void run() {
	    Transaction.withTransaction(new TransactionalCommand() {
		@Override
		public void doIt() {
		    for (final PersistentGroup recipient : recipients) {
			addDestinationEmailAddresses(recipient, emailAddresses);
		    }
		}
	    });
	}

    }

    protected static Set<String> getRecipientAddresses(Set<PersistentGroup> recipients) {
	final Set<String> emailAddresses = new HashSet<String>();
	for (final PersistentGroup recipient : recipients) {
	    addDestinationEmailAddresses(recipient, emailAddresses);
	}
	return emailAddresses;
    }

    public static void addDestinationEmailAddresses(final PersistentGroup persistentGroup, final Set<String> emailAddresses) {
	for (final User user : persistentGroup.getMembers()) {
	    final String value = user.getEmail();
	    if (value != null && !value.isEmpty()) {
		emailAddresses.add(value);
	    }
	}
    }

    protected Set<String> getDestinationBccs() {
	final Set<String> emailAddresses = new HashSet<String>();
	if (getBccString() != null && !getBccString().isEmpty()) {
	    for (final String emailAddress : getBccString().replace(',', ' ').replace(';', ' ').split(" ")) {
		final String trimmed = emailAddress.trim();
		if (!trimmed.isEmpty()) {
		    emailAddresses.add(emailAddress);
		}
	    }
	}
	final Worker worker = new Worker(getBccSet());
	worker.start();
	try {
	    worker.join();
	} catch (final InterruptedException e) {
	    throw new Error(e);
	}
	emailAddresses.addAll(worker.emailAddresses);
	return emailAddresses;
    }

    protected String[] getReplyToAddresses(final User user) {
	final String[] replyToAddresses = new String[getReplyToCount()];
	int i = 0;
	for (final ReplyTo replyTo : getReplyToSet()) {
	    replyToAddresses[i++] = replyTo.getReplyToAddress(user);
	}
	return replyToAddresses;
    }

    public void dispatch() {
	final Sender sender = getSender();
	final User user = getUser();
	final Set<String> destinationBccs = getDestinationBccs();
	for (final Set<String> bccs : split(destinationBccs)) {
	    if (!bccs.isEmpty()) {
		final Email email = new Email(sender.getFromName(user), sender.getFromAddress(), getReplyToAddresses(user),
			Collections.EMPTY_SET, Collections.EMPTY_SET, bccs, getSubject(), getBody(), getHtmlBody());
		email.setMessage(this);
	    }
	}
	final Set<String> tos = getRecipientAddresses(getToSet());
	final Set<String> ccs = getRecipientAddresses(getCcSet());
	if (!tos.isEmpty() || !ccs.isEmpty()) {
	    final Email email = new Email(sender.getFromName(user), sender.getFromAddress(), getReplyToAddresses(user), tos,
		    ccs, Collections.EMPTY_SET, getSubject(), getBody(), getHtmlBody());
	    email.setMessage(this);
	}
	removeMessagingSystemFromPendingDispatch();
	setSent(new DateTime());
    }

    private Set<Set<String>> split(final Set<String> destinations) {
	final Set<Set<String>> result = new HashSet<Set<String>>();
	int i = 0;
	Set<String> subSet = new HashSet<String>();
	for (final String destination : destinations) {
	    if (i++ == 50) {
		result.add(subSet);
		subSet = new HashSet<String>();
		i = 1;
	    }
	    subSet.add(destination);
	}
	result.add(subSet);
	return result;
    }

    public int getPossibleRecipientsCount() {
	int count = 0;
	for (final PersistentGroup recipient : getBccSet()) {
	    count += recipient.getMembers().size();
	}
	return count;
    }

    public int getRecipientsWithEmailCount() {
	int count = 0;
	final long start = System.currentTimeMillis();
	for (final PersistentGroup recipient : getBccSet()) {
	    final Set<User> elements = recipient.getMembers();
	    for (final User user : elements) {
		if (user.getEmail() != null) {
		    count++;
		}
	    }
	}
	final long end = System.currentTimeMillis();
	System.out.println("getRecipientsWithEmailCount time : " + (end - start) + "ms");
	return count;
    }

    public int getSentMailsCount() {
	int count = 0;
	for (final Email email : getEmailSet()) {
	    final EmailAddressList confirmedAddresses = email.getConfirmedAddresses();
	    if (confirmedAddresses != null && !confirmedAddresses.isEmpty()) {
		count += confirmedAddresses.toCollection().size();
	    }
	}
	return count;
    }

    public int getFailedMailsCount() {
	int count = 0;
	for (final Email email : getEmailSet()) {
	    EmailAddressList failedAddresses = email.getFailedAddresses();

	    if (failedAddresses != null && !failedAddresses.isEmpty()) {
		count += failedAddresses.size();
	    }
	}
	return count;
    }

}
