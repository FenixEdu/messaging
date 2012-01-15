package pt.ist.messaging.presentationTier.provider;

import java.util.Set;
import java.util.TreeSet;

import myorg.domain.groups.PersistentGroup;

import pt.ist.fenixWebFramework.renderers.DataProvider;
import pt.ist.fenixWebFramework.renderers.components.converters.Converter;
import pt.ist.messaging.domain.EmailBean;
import pt.ist.messaging.domain.Sender;

public class EmailRecipientsProvider implements DataProvider {

    public Object provide(final Object source, final Object currentValue) {
	final EmailBean emailBean = (EmailBean) source;
	final Sender sender = emailBean.getSender();
	final Set<PersistentGroup> recipients = new TreeSet<PersistentGroup>(PersistentGroup.COMPARATOR_BY_NAME);
	recipients.addAll(emailBean.getRecipients());
	if (sender != null) {
	    recipients.addAll(sender.getRecipientsSet());
	}
	return recipients;
    }

    public Converter getConverter() {
	return null;
    }

}
