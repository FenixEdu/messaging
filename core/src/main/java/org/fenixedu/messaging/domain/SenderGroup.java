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
package org.fenixedu.messaging.domain;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.fenixedu.bennu.core.annotation.GroupOperator;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.groups.PersistentGroup;
import org.fenixedu.bennu.core.groups.CustomGroup;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.joda.time.DateTime;

/**
 *
 * @author Luis Cruz
 *
 */
@GroupOperator("sender")
public class SenderGroup extends CustomGroup {
    private static final long serialVersionUID = 804590197340198679L;

    public SenderGroup() {
        super();
    }

    @Override
    public String getPresentationName() {
        return BundleUtil.getString("resources/MessagingResources", "label.persistent.group.sender.name");
    }

    @Override
    public PersistentGroup toPersistentGroup() {
        return PersistentSenderGroup.getInstance();
    }

    @Override
    public Set<User> getMembers() {
        final Set<User> members = new HashSet<User>();
        for (final Sender sender : MessagingSystem.getInstance().getSenderSet()) {
            members.addAll(sender.getMembers().getMembers());
        }
        return members;
    }

    @Override
    public Set<User> getMembers(DateTime when) {
        final Set<User> members = new HashSet<User>();
        for (final Sender sender : MessagingSystem.getInstance().getSenderSet()) {
            members.addAll(sender.getMembers().getMembers(when));
        }
        return members;
    }

    @Override
    public boolean isMember(User user) {
        for (final Sender sender : MessagingSystem.getInstance().getSenderSet()) {
            final User user1 = user;
            if (sender.getMembers().isMember(user1)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isMember(User user, DateTime when) {
        for (final Sender sender : MessagingSystem.getInstance().getSenderSet()) {
            if (sender.getMembers().isMember(user, when)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof SenderGroup;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(SenderGroup.class);
    }
}
