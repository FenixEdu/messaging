package pt.ist.messaging.presentationTier.provider;

import pt.ist.fenixWebFramework.rendererExtensions.converters.DomainObjectKeyConverter;
import pt.ist.fenixWebFramework.renderers.DataProvider;
import pt.ist.fenixWebFramework.renderers.components.converters.Converter;
import pt.ist.messaging.domain.Sender;

public class EmailSenderProvider implements DataProvider {

    public Object provide(final Object source, final Object currentValue) {
	return Sender.getAvailableSenders();
    }

    public Converter getConverter() {
	return new DomainObjectKeyConverter();
    }

}
