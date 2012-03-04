/*
 * @(#)EmailRecipientsProvider.java
 *
 * Copyright 2012 Instituto Superior Tecnico
 * Founding Authors: Luis Cruz
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the Messaging Module.
 *
 *   The Messaging Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The Messaging Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the Messaging Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package pt.ist.messaging.presentationTier.provider;

import java.util.Set;
import java.util.TreeSet;

import myorg.domain.groups.PersistentGroup;

import pt.ist.fenixWebFramework.renderers.DataProvider;
import pt.ist.fenixWebFramework.renderers.components.converters.Converter;
import pt.ist.messaging.domain.EmailBean;
import pt.ist.messaging.domain.Sender;

/**
 * 
 * @author Luis Cruz
 * 
 */
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
