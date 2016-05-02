package org.fenixedu.messaging.core.domain;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import pt.ist.fenixframework.test.core.FenixFrameworkRunner;

@RunWith(FenixFrameworkRunner.class)
public class PruningTest {

    private Sender sender;

    @Before
    public void resetSender() {
        sender = SenderTest.newRegularSender();
        for (int i = 0; i < 10; i++) {
            MessageTest.newRegularMessage(sender);
        }
    }

    @Test
    public void someTest() {

    }

}
