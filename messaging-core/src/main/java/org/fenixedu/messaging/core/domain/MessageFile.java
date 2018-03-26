package org.fenixedu.messaging.core.domain;

import org.fenixedu.bennu.core.domain.User;

public class MessageFile extends MessageFile_Base {

    public MessageFile(Sender sender, String displayName, String filename, byte[] content) {
        super();
        init(displayName, filename, content);
        setSender(sender);
    }

    @Override public boolean isAccessible(User user) {
        return getSender().getMembers().isMember(user);
    }
}
