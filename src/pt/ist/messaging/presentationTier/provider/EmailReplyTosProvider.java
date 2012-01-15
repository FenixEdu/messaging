package pt.ist.messaging.presentationTier.provider;

import java.util.HashSet;
import java.util.Set;

import pt.ist.fenixWebFramework.renderers.DataProvider;
import pt.ist.fenixWebFramework.renderers.components.converters.Converter;
import pt.ist.messaging.domain.EmailBean;
import pt.ist.messaging.domain.ReplyTo;
import pt.ist.messaging.domain.Sender;

public class EmailReplyTosProvider implements DataProvider {

    public Object provide(final Object source, final Object currentValue) {
	final EmailBean emailBean = (EmailBean) source;
	final Sender sender = emailBean.getSender();
	final Set<ReplyTo> replyTos = new HashSet<ReplyTo>();
	if (sender != null) {
	    replyTos.addAll(sender.getConcreteReplyTos());
	}
	return replyTos;
    }

    public Converter getConverter() {
	return null;
    }

}
