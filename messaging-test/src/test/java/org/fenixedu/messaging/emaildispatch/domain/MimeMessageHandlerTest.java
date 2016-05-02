package org.fenixedu.messaging.emaildispatch.domain;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.UserProfile;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.domain.MessagingSystem;
import org.junit.Test;
import org.junit.runner.RunWith;
import pt.ist.fenixframework.test.core.FenixFrameworkRunner;

import javax.mail.internet.MimeMessage;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(FenixFrameworkRunner.class)
public class MimeMessageHandlerTest {
    Locale pt = new Locale("pt-PT"), en = new Locale("en-UK"), es = new Locale("es-ES");
    String joan = "joan@gmail.com", joana = "joana@gmail.com", joao = "joao@gmail.com", jane = "jane@gmail.com",
            john = "john@gmail.com";
    User[] users =
            { new User(new UserProfile("John", "Doe", null, john, en)), new User(new UserProfile("Jane", "Doe", null, jane, en)),
                    new User(new UserProfile("Joao", "Doe", null, joao, pt)),
                    new User(new UserProfile("Joana", "Doe", null, joana, pt)),
                    new User(new UserProfile("Joan", "Doe", null, joan, es)) };
    LocalizedString subject = new LocalizedString(pt, "Olá").with(en, "Hello").with(es, "Hola"), body = new LocalizedString(pt,
            "... do outro lado!\n--Adélia").with(en, "... from the other side!\n--Adele").with(es,
            "... de el otro lado!\n--Adelita");

    @Test
    public void testDispatcher(){
        //TODO check dispatcher is set... at the moment it's private. and test below fails with a null report...
    }


    @Test
    public void testMimeMessageHandlerBasics() throws javax.mail.MessagingException {
        //XXX we assume max recipients is default (2) and dispatcher is set.
        Message m =
                Message.from(MessagingSystem.systemSender()).replyToSender().subject(subject).textBody(body)
                        .bcc(Group.users(users)).preferredLocale(pt).send();
        //test message to handler conversion
        LocalEmailMessageDispatchReport report = (LocalEmailMessageDispatchReport) MessagingSystem.dispatch(m);
        Set<MimeMessageHandler> handlers = report.getHandlerSet();
        assertEquals(handlers.size(), 3); // three handlers, one for each language given max recipients is not exceeded
        handlers.forEach(h -> {
            assertEquals(h.getReport(), report);
            assertTrue(h.getCcAddresses().isEmpty());
            assertTrue(h.getToAddresses().isEmpty());
            String[] bccs = h.getBccAddresses().split(",");
            boolean isEs = h.getLocale().equals(es);
            assertEquals(bccs.length, isEs ? 1 : 2);
            if (isEs) {
                assertEquals(bccs[0], joan);
            } else if (h.getLocale().equals(en)) {
                assertTrue(bccs[0].equals(john) || bccs[0].equals(jane));
                assertTrue(bccs[1].equals(john) || bccs[1].equals(jane));
            } else if (h.getLocale().equals(pt)) {
                assertTrue(bccs[0].equals(joao) || bccs[0].equals(joana));
                assertTrue(bccs[1].equals(joao) || bccs[1].equals(joana));
            } else {
                fail("Unknown Locale: " + h.getLocale().toString());
            }
            assertTrue(h.getFrom().contains(MessagingSystem.systemSender().getAddress()));
            //TODO assert each has a different locale
        });
        for(MimeMessageHandler handler : handlers) {
            //test handler to mime message conversion
            MimeMessage mm = handler.mimeMessage();
            assertTrue(handler.getFrom().contains(m.getSender().getAddress()));
            assertEquals(mm.getFrom().length, 1);
            assertTrue(mm.getFrom()[0].toString().contains(m.getSender().getAddress()));
        }
    }
}
