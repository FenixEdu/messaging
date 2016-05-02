package org.fenixedu.messaging.core.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import pt.ist.fenixframework.test.core.FenixFrameworkRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(FenixFrameworkRunner.class)
public class MessagingSystemTest {

    @Test
    public void systemSender() {
        //TODO redo
        Sender system = MessagingSystem.systemSender();
        assertNotNull(system);
        assertNotNull(system.getAddress());
        assertNotNull(system.getName());
        assertNotNull(system.getMembers());
        assertNotNull(system.getPolicy());
    }

}
