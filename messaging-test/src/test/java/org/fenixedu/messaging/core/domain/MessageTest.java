package org.fenixedu.messaging.core.domain;

import jersey.repackaged.com.google.common.collect.Lists;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.UserProfile;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.commons.i18n.I18N;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.core.domain.Message.MessageBuilder;
import org.fenixedu.messaging.core.exception.MessagingDomainException;
import org.fenixedu.messaging.test.mock.MockDispatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pt.ist.fenixframework.test.core.FenixFrameworkRunner;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(FenixFrameworkRunner.class)
public class MessageTest {

    private static final User TEST_USER = new User(new UserProfile("John", "Doe", null, "johndoe@gmail.com", null));
    private static final Group TEST_GRP_A = Group.anyone(), TEST_GRP_B = Group.nobody(), TEST_GRP_C = Group.managers();
    private static final String TEST_STR_A = "test-a", TEST_STR_B = "test-b", TEST_STR_C = "test-c", TEST_ADDR = "tester@test.com";
    private static final Locale TEST_LOC = new Locale("aa","bb","cc"), DEF_LOC = I18N.getLocale();
    private static final LocalizedString TEST_LS_A = new LocalizedString(TEST_LOC, TEST_STR_A),
            TEST_LS_AA = TEST_LS_A.with(DEF_LOC, TEST_STR_A),
            TEST_LS_AB = TEST_LS_A.with(DEF_LOC, TEST_STR_B),
            TEST_LS_B = new LocalizedString(TEST_LOC, TEST_STR_B),
            TEST_LS_BB = TEST_LS_B.with(DEF_LOC, TEST_STR_B),
            TEST_LS_BC = TEST_LS_B.with(DEF_LOC, TEST_STR_C),
            TEST_LS_C = new LocalizedString(TEST_LOC, TEST_STR_C),
            TEST_LS_CA = TEST_LS_C.with(DEF_LOC, TEST_STR_A),
            TEST_LS_CC = TEST_LS_C.with(DEF_LOC, TEST_STR_C),
            EMPTY_LS = new LocalizedString();

    @Before
    public void mockUser() {
        Authenticate.mock(TEST_USER);
        MessagingSystem.setMessageDispatcher(new MockDispatcher());
    }

    @After
    public void unmockUser() {
        Authenticate.unmock();
        MessagingSystem.setMessageDispatcher(null);
    }

    @Test
    public void deletion() {
        //FIXME XXX object deletion cannot be tested with current backend
        /*
            MessagingSystem sys = MessagingSystem.getInstance();
            Sender s = SenderTest.newEmptySender();
            Message m = MessageTest.newEmptyMessage(s);
            MessagingSystem.dispatch(m);
            m.delete();
            Stream<Set<Message>> relations = Stream.of(TEST_USER.getMessageSet(), s.getMessageSet(), sys.getMessageSet(), sys.getMessagePendingDispatchSet());
            assertFalse(relations.parallel().anyMatch(collection -> collection.contains(m)));
            assertTrue(m.getBccGroups().isEmpty());
            assertTrue(m.getCcGroups().isEmpty());
            assertTrue(m.getToGroups().isEmpty());
        */
    }

    @Test(expected = MessagingDomainException.class)
    public void nullSender() {
        // Builder throws when required sender parameter is null.

        Message.from(null).send();
    }

    @Test
    public void emptyMessage() {
        // Builder provides default values so build method can be called right after creation resulting in the default empty message.

        Sender sender = SenderTest.newRegularSender();
        Message message = Message.from(sender).send();
        testEmptyMessage(message, sender);

        message = Message.fromSystem().send();
        testEmptyMessage(message, MessagingSystem.systemSender());
    }

    private void testEmptyMessage(Message message, Sender sender) {
        // Message itself is not null
        assertNotNull(message);
        // Message sender is the provided sender
        assertSame(message.getSender(), sender);

        // Default properties and relations
        // - Empty recipient sets
        assertTrue(message.getBccGroups().isEmpty());
        assertTrue(message.getCcGroups().isEmpty());
        assertTrue(message.getToGroups().isEmpty());
        assertNull(message.getSingleBccs());
        // - Empty content
        LocalizedString ls = message.getSubject();
        assertNotNull(ls);
        assertTrue(ls.isEmpty());
        ls = message.getHtmlBody();
        assertNotNull(ls);
        assertTrue(ls.isEmpty());
        ls = message.getTextBody();
        assertNotNull(ls);
        assertTrue(ls.isEmpty());
        // - Preferred locale is the default locale
        assertSame(message.getPreferredLocale(), DEF_LOC);
        // - Reply to address is null
        assertNull(message.getReplyTo());

        // Automatic properties and relations
        // - Message user is test user
        assertSame(message.getUser(), TEST_USER);
        // - Creation date is non null and 'now'
        assertNotNull(message.getCreated()); //TODO how should the 'now' part? Can i mock it or should I give it a credible window?
        // - Dispatch data is null
        assertNull(message.getSent());
        assertNull(message.getDispatchReport());
        // - Message is pending dispatch
        assertSame(message.getMessagingSystemFromPendingDispatch(), MessagingSystem.getInstance());

        // Message dispatch proceeds normally
        MessageDispatchReport report = MessagingSystem.getInstance().dispatch(message);
        // Message is linked to a dispatch report
        assertNotNull(report);
        assertSame(report, message.getDispatchReport());
        // Message sent date is updated when report's delivery finishes
        assertNotNull(message.getSent());
        // XXX Delivery and report handling should be tested further within dispatcher modules
    }

    @Test
    public void messageBcc() {
        // Array and Stream setters are additive. Collection setter clears previous groups.
        // Null safety - Group and Stream setters ignore null. Collection setter still clears groups leaving an empty set.
        // All setters filter null groups within collections.
        // There should be no repeating groups (handled by Set).

        MessageBuilder builder = Message.fromSystem();
        Set<Group> groups;

        builder.bcc(TEST_GRP_A, null, TEST_GRP_B);
        groups = builder.send().getBccGroups();
        assertTrue(groups.size() == 2 && groups.contains(TEST_GRP_A) && groups.contains(TEST_GRP_B));

        builder.bcc(Stream.of(null, TEST_GRP_B, TEST_GRP_B, TEST_GRP_C));
        groups = builder.send().getBccGroups();
        assertTrue(groups.size() == 3 && groups.contains(TEST_GRP_A) && groups.contains(TEST_GRP_B) && groups.contains(TEST_GRP_C));

        builder.bcc(Lists.newArrayList(TEST_GRP_B, TEST_GRP_C, TEST_GRP_B, null));
        groups = builder.send().getBccGroups();
        assertTrue(groups.size() == 2 && groups.contains(TEST_GRP_B) && groups.contains(TEST_GRP_C));

        builder.bcc((Group[]) null);
        groups = builder.send().getBccGroups();
        assertTrue(groups.size() == 2 && groups.contains(TEST_GRP_B) && groups.contains(TEST_GRP_C));

        builder.bcc((Stream<Group>) null);
        groups = builder.send().getBccGroups();
        assertTrue(groups.size() == 2 && groups.contains(TEST_GRP_B) && groups.contains(TEST_GRP_C));

        builder.bcc((Collection<Group>) null);
        groups = builder.send().getBccGroups();
        assertTrue(groups.isEmpty());
    }

    @Test
    public void messageCc() {
        // Array and Stream setters are additive. Collection setter clears previous groups.
        // Null safety - Group and Stream setters ignore null. Collection setter still clears groups leaving an empty set.
        // All setters filter null groups within collections.
        // There should be no repeating groups (handled by Set).

        MessageBuilder builder = Message.fromSystem();
        Set<Group> groups;

        builder.cc(TEST_GRP_A, null, TEST_GRP_B);
        groups = builder.send().getCcGroups();
        assertTrue(groups.size() == 2 && groups.contains(TEST_GRP_A) && groups.contains(TEST_GRP_B));

        builder.cc(Stream.of(null, TEST_GRP_B, TEST_GRP_B, TEST_GRP_C));
        groups = builder.send().getCcGroups();
        assertTrue(groups.size() == 3 && groups.contains(TEST_GRP_A) && groups.contains(TEST_GRP_B) && groups.contains(TEST_GRP_C));

        builder.cc(Lists.newArrayList(TEST_GRP_B, TEST_GRP_C, TEST_GRP_B, null));
        groups = builder.send().getCcGroups();
        assertTrue(groups.size() == 2 && groups.contains(TEST_GRP_B) && groups.contains(TEST_GRP_C));

        builder.cc((Group[]) null);
        groups = builder.send().getCcGroups();
        assertTrue(groups.size() == 2 && groups.contains(TEST_GRP_B) && groups.contains(TEST_GRP_C));

        builder.cc((Stream<Group>) null);
        groups = builder.send().getCcGroups();
        assertTrue(groups.size() == 2 && groups.contains(TEST_GRP_B) && groups.contains(TEST_GRP_C));

        builder.cc((Collection<Group>) null);
        groups = builder.send().getCcGroups();
        assertTrue(groups.isEmpty());
    }

    @Test
    public void messageTo() {
        // Array and Stream setters are additive. Collection setter clears previous groups.
        // Null safety - Group and Stream setters ignore null. Collection setter still clears groups leaving an empty set.
        // All setters filter null groups within collections.
        // There should be no repeating groups (handled by Set).

        MessageBuilder builder = Message.fromSystem();
        Set<Group> groups;

        builder.to(TEST_GRP_A, null, TEST_GRP_B);
        groups = builder.send().getToGroups();
        assertTrue(groups.size() == 2 && groups.contains(TEST_GRP_A) && groups.contains(TEST_GRP_B));

        builder.to(Stream.of(null, TEST_GRP_B, TEST_GRP_B, TEST_GRP_C));
        groups = builder.send().getToGroups();
        assertTrue(groups.size() == 3 && groups.contains(TEST_GRP_A) && groups.contains(TEST_GRP_B) && groups.contains(TEST_GRP_C));

        builder.to(Lists.newArrayList(TEST_GRP_B, TEST_GRP_C, TEST_GRP_B, null));
        groups = builder.send().getToGroups();
        assertTrue(groups.size() == 2 && groups.contains(TEST_GRP_B) && groups.contains(TEST_GRP_C));

        builder.to((Group[]) null);
        groups = builder.send().getToGroups();
        assertTrue(groups.size() == 2 && groups.contains(TEST_GRP_B) && groups.contains(TEST_GRP_C));

        builder.to((Stream<Group>) null);
        groups = builder.send().getToGroups();
        assertTrue(groups.size() == 2 && groups.contains(TEST_GRP_B) && groups.contains(TEST_GRP_C));

        builder.to((Collection<Group>) null);
        groups = builder.send().getToGroups();
        assertTrue(groups.isEmpty());
    }

    @Test
    public void messageContent() {
        // Syntax sugar for setting subject, text and html bodies simultaneously.
        // Consequently falls under the same rules as individual setters.
        // Additionally order should always be subject, text body, html body

        MessageBuilder builder = Message.fromSystem();
        Message message;

        builder.content(TEST_LS_A, TEST_LS_B, TEST_LS_C);
        message = builder.send();
        assertSame(message.getSubject(), TEST_LS_A);
        assertSame(message.getTextBody(), TEST_LS_B);
        assertSame(message.getHtmlBody(), TEST_LS_C);

        builder.content(TEST_STR_A, TEST_STR_B, TEST_STR_C);
        message = builder.send();
        assertEquals(message.getSubject(), TEST_LS_AA);
        assertEquals(message.getTextBody(), TEST_LS_BB);
        assertEquals(message.getHtmlBody(), TEST_LS_CC);

        builder.content(TEST_STR_B, TEST_STR_C, TEST_STR_A);
        message = builder.send();
        assertEquals(message.getSubject(), TEST_LS_AB);
        assertEquals(message.getTextBody(), TEST_LS_BC);
        assertEquals(message.getHtmlBody(), TEST_LS_CA);

        builder.content((LocalizedString)null,null,null);
        message = builder.send();
        assertEquals(message.getSubject(), EMPTY_LS);
        assertEquals(message.getTextBody(), EMPTY_LS);
        assertEquals(message.getHtmlBody(), EMPTY_LS);

        builder.content(TEST_STR_C, TEST_STR_A, TEST_STR_B, TEST_LOC);
        message = builder.send();
        assertEquals(message.getSubject(), TEST_LS_C);
        assertEquals(message.getTextBody(), TEST_LS_A);
        assertEquals(message.getHtmlBody(), TEST_LS_B);

        builder.content(TEST_STR_A, TEST_STR_B, TEST_STR_C, TEST_LOC);
        message = builder.send();
        assertEquals(message.getSubject(), TEST_LS_A);
        assertEquals(message.getTextBody(), TEST_LS_B);
        assertEquals(message.getHtmlBody(), TEST_LS_C);

        builder.content(TEST_LS_AA, TEST_LS_BB, TEST_LS_CC);
        message = builder.send();
        assertSame(message.getSubject(), TEST_LS_AA);
        assertSame(message.getTextBody(), TEST_LS_BB);
        assertSame(message.getHtmlBody(), TEST_LS_CC);

        builder.content((String)null,null,null);
        message = builder.send();
        assertEquals(message.getSubject(), TEST_LS_A);
        assertEquals(message.getTextBody(), TEST_LS_B);
        assertEquals(message.getHtmlBody(), TEST_LS_C);

        builder.content(TEST_STR_A, TEST_STR_A, TEST_STR_A,null);
        message = builder.send();
        assertEquals(message.getSubject(), TEST_LS_A);
        assertEquals(message.getTextBody(), TEST_LS_B);
        assertEquals(message.getHtmlBody(), TEST_LS_C);

        builder.content(null,null,null, TEST_LOC);
        message = builder.send();
        assertEquals(message.getSubject(), EMPTY_LS);
        assertEquals(message.getTextBody(), EMPTY_LS);
        assertEquals(message.getHtmlBody(), EMPTY_LS);

    }

    @Test
    public void messageSubject() {
        // Localized String setter provides complete override (not a merge).
        // String and Locale setter overrides locale content only.
        // String setter is a shorthand for using the I18N default locale.
        // Null content String erases the locale's content. Null Locale produces no changes.
        // Null LocalizedString sets sets an empty LocalizedString.

        MessageBuilder builder = Message.fromSystem();

        builder.subject(TEST_LS_A);
        assertSame(builder.send().getSubject(), TEST_LS_A);

        builder.subject(TEST_STR_A);
        assertEquals(builder.send().getSubject(), TEST_LS_AA);

        builder.subject(TEST_STR_B);
        assertEquals(builder.send().getSubject(), TEST_LS_AB);

        builder.subject((LocalizedString) null);
        assertEquals(builder.send().getSubject(), EMPTY_LS);

        builder.subject(TEST_STR_B, TEST_LOC);
        assertEquals(builder.send().getSubject(), TEST_LS_B);

        builder.subject(TEST_STR_A, TEST_LOC);
        assertEquals(builder.send().getSubject(), TEST_LS_A);

        builder.subject(TEST_LS_AA);
        assertSame(builder.send().getSubject(), TEST_LS_AA);

        builder.subject((String) null);
        assertEquals(builder.send().getSubject(), TEST_LS_A);

        builder.subject(TEST_STR_A, null);
        assertEquals(builder.send().getSubject(), TEST_LS_A);

        builder.subject(null, TEST_LOC);
        assertEquals(builder.send().getSubject(), EMPTY_LS);
    }

    @Test
    public void messageTextBody() {
        // Localized String setter provides complete override (not a merge).
        // String and Locale setter overrides locale content only.
        // String setter is a shorthand for using the I18N default locale.
        // Null content String erases the locale's content. Null Locale produces no changes.
        // Null LocalizedString sets sets an empty LocalizedString.

        MessageBuilder builder = Message.fromSystem();

        builder.textBody(TEST_LS_A);
        assertSame(builder.send().getTextBody(), TEST_LS_A);

        builder.textBody(TEST_STR_A);
        assertEquals(builder.send().getTextBody(), TEST_LS_AA);

        builder.textBody(TEST_STR_B);
        assertEquals(builder.send().getTextBody(), TEST_LS_AB);

        builder.textBody((LocalizedString) null);
        assertEquals(builder.send().getTextBody(), EMPTY_LS);

        builder.textBody(TEST_STR_B, TEST_LOC);
        assertEquals(builder.send().getTextBody(), TEST_LS_B);

        builder.textBody(TEST_STR_A, TEST_LOC);
        assertEquals(builder.send().getTextBody(), TEST_LS_A);

        builder.textBody(TEST_LS_AA);
        assertSame(builder.send().getTextBody(), TEST_LS_AA);

        builder.textBody((String) null);
        assertEquals(builder.send().getTextBody(), TEST_LS_A);

        builder.textBody(TEST_STR_A, null);
        assertEquals(builder.send().getTextBody(), TEST_LS_A);

        builder.textBody(null, TEST_LOC);
        assertEquals(builder.send().getTextBody(), EMPTY_LS);
    }

    @Test
    public void messageHtmlBody() {
        // Localized String setter provides complete override (not a merge).
        // String and Locale setter overrides locale content only.
        // String setter is a shorthand for using the I18N default locale.
        // Null content String erases the locale's content. Null Locale produces no changes.
        // Null LocalizedString sets sets an empty LocalizedString.

        MessageBuilder builder = Message.fromSystem();

        builder.htmlBody(TEST_LS_A);
        assertSame(builder.send().getHtmlBody(), TEST_LS_A);

        builder.htmlBody(TEST_STR_A);
        assertEquals(builder.send().getHtmlBody(), TEST_LS_AA);

        builder.htmlBody(TEST_STR_B);
        assertEquals(builder.send().getHtmlBody(), TEST_LS_AB);

        builder.htmlBody((LocalizedString) null);
        assertEquals(builder.send().getHtmlBody(), EMPTY_LS);

        builder.htmlBody(TEST_STR_B, TEST_LOC);
        assertEquals(builder.send().getHtmlBody(), TEST_LS_B);

        builder.htmlBody(TEST_STR_A, TEST_LOC);
        assertEquals(builder.send().getHtmlBody(), TEST_LS_A);

        builder.htmlBody(TEST_LS_AA);
        assertSame(builder.send().getHtmlBody(), TEST_LS_AA);

        builder.htmlBody((String) null);
        assertEquals(builder.send().getHtmlBody(), TEST_LS_A);

        builder.htmlBody(TEST_STR_A, null);
        assertEquals(builder.send().getHtmlBody(), TEST_LS_A);

        builder.htmlBody(null, TEST_LOC);
        assertEquals(builder.send().getHtmlBody(), EMPTY_LS);
    }

    @Test
    public void messageReplyTo() {
        // Reply to should be equal to what is provided.
        // Subsequent calls override the value.
        // Reply to sender method should copy sender's default reply to address.
        // Reply to must be a valid address. XXX not really for now.

        Sender sender = SenderTest.newRegularSender();
        MessageBuilder builder = Message.from(sender);

        builder.replyTo(null);
        assertNull(builder.send().getReplyTo());

        builder.replyTo(TEST_ADDR);
        assertEquals(builder.send().getReplyTo(), TEST_ADDR);

        builder.replyToSender();
        assertEquals(builder.send().getReplyTo(), sender.getReplyTo());
        //FIXME reply to address should be checked for validity. Or otherwise all validation responsability should be passed into dispatcher module.
        // expect domain exception
        // builder.replyTo("not really a valid address...");

    }

    @Test
    public void messagePreferredLocale() {
        // Preferred locale should be equal to what is provided.
        // Subsequent calls override the value.
        // Null value sets the default locale instead.

        MessageBuilder builder = Message.fromSystem();

        builder.preferredLocale(TEST_LOC);
        assertEquals(builder.send().getPreferredLocale(), TEST_LOC);

        builder.preferredLocale(null);
        assertEquals(builder.send().getPreferredLocale(), DEF_LOC);

        builder.preferredLocale(TEST_LOC);
        assertEquals(builder.send().getPreferredLocale(), TEST_LOC);
    }

    //TODO test content locales, single bccs and templates...


    public static Message newEmptyMessage(Sender sender) {
        return (sender == null ? Message.fromSystem() : Message.from(sender)).send();
    }

    public static Message newRegularMessage(Sender sender) {
        return (sender == null ? Message.fromSystem() : Message.from(sender)).send();
    }
}
