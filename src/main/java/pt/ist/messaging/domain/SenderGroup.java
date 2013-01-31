/*
 * @(#)SenderGroup.java
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
package pt.ist.messaging.domain;

import java.util.HashSet;
import java.util.Set;

import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.VirtualHost;
import pt.ist.bennu.core.domain.groups.PersistentGroup;
import pt.ist.bennu.core.util.BundleUtil;
import pt.ist.fenixWebFramework.services.Service;

/**
 * 
 * @author Luis Cruz
 * 
 */
public class SenderGroup extends SenderGroup_Base {

	protected SenderGroup() {
		super();
		setSystemGroupMyOrg(getMyOrg());
	}

	protected String getNameLable() {
		return "label.persistent.group.sender.name";
	}

	@Override
	public String getName() {
		return BundleUtil.getStringFromResourceBundle("resources/MessagingResources", getNameLable());
	}

	@Override
	public boolean isMember(final User user) {
		for (final Sender sender : MessagingSystem.getInstance().getSenderSet()) {
			if (sender.getVirtualHost() == VirtualHost.getVirtualHostForThread() && sender.isMember(user)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<User> getMembers() {
		final Set<User> members = new HashSet<User>();
		for (final Sender sender : MessagingSystem.getInstance().getSenderSet()) {
			if (sender.getVirtualHost() == VirtualHost.getVirtualHostForThread()) {
				final PersistentGroup group = sender.getMembers();
				if (group != null) {
					members.addAll(group.getMembers());
				}
			}
		}
		return members;
	}

	@Service
	public static SenderGroup getInstance() {
		final SenderGroup group = (SenderGroup) PersistentGroup.getSystemGroup(SenderGroup.class);
		return group == null ? new SenderGroup() : group;
	}

}
