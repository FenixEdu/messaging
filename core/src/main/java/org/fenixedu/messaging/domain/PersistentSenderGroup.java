package org.fenixedu.messaging.domain;

import org.fenixedu.bennu.core.groups.Group;

public class PersistentSenderGroup extends PersistentSenderGroup_Base {
    protected PersistentSenderGroup() {
        super();
    }

    @Override
    public Group toGroup() {
        return new SenderGroup();
    }

    public static PersistentSenderGroup getInstance() {
        return MessagingSystem.getInstance().getPersistentSenderGroup();
    }
}
