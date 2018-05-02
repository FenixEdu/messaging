package org.fenixedu.messaging.core.domain;

import org.fenixedu.bennu.MessagingConfiguration;
import org.fenixedu.bennu.core.domain.User;
import pt.ist.fenixframework.Atomic;

public class MessageFile extends MessageFile_Base {

    public MessageFile(final Sender sender, final String displayName, final String filename, final byte[] content) {
        super();
        init(displayName, filename, content);
        setSender(sender);
    }

    @Override public boolean isAccessible(final User user) {
        return getSender().getMembers().isMember(user);
    }

    public void pruneOrphanFile(){
        final Integer minPruning = MessagingConfiguration.getConfiguration().minPruningDays();
        if (getCreationDate().plusDays(minPruning).isBeforeNow() && getMessageSet().isEmpty()){
            delete();
        }
    }

    @Atomic(mode = Atomic.TxMode.WRITE)
    public void delete() {
        getMessageSet().clear();
        setSender(null);
        super.delete();
    }
}
