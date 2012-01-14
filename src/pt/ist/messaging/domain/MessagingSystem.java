package pt.ist.messaging.domain;

import myorg.domain.MyOrg;
import pt.ist.fenixWebFramework.services.Service;

public class MessagingSystem extends MessagingSystem_Base {
    
    private MessagingSystem() {
        super();
        setMyOrg(MyOrg.getInstance());
    }

    public static MessagingSystem getInstance() {
	final MyOrg myOrg = MyOrg.getInstance();
	return myOrg.hasMessagingSystem() ? myOrg.getMessagingSystem() : createMessagingSystem(myOrg);
    }

    @Service
    private static MessagingSystem createMessagingSystem(final MyOrg myOrg) {
	return myOrg.hasMessagingSystem() ? myOrg.getMessagingSystem() : new MessagingSystem();
    }

}
