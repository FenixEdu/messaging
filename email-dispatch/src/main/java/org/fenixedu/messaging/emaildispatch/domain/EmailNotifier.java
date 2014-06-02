package org.fenixedu.messaging.emaildispatch.domain;

import org.fenixedu.bennu.core.domain.Bennu;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class EmailNotifier extends EmailNotifier_Base {
    public static EmailNotifier getInstance() {
        EmailNotifier instance = Bennu.getInstance().getEmailNotifier();
        return instance != null ? instance : create();
    }

    @Atomic(mode = TxMode.WRITE)
    private static EmailNotifier create() {
        EmailNotifier instance = Bennu.getInstance().getEmailNotifier();
        return instance != null ? instance : new EmailNotifier();
    }

    private EmailNotifier() {
        super();
        setMyOrg(Bennu.getInstance());
    }

    @Deprecated
    public java.util.Set<org.fenixedu.messaging.emaildispatch.domain.Email> getEmails() {
        return getEmailsSet();
    }
}
