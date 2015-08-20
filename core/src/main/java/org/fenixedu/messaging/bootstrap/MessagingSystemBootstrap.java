package org.fenixedu.messaging.bootstrap;

import java.util.Collections;
import java.util.List;

import org.fenixedu.bennu.core.bootstrap.AdminUserBootstrapper;
import org.fenixedu.bennu.core.bootstrap.AdminUserBootstrapper.AdminUserSection;
import org.fenixedu.bennu.core.bootstrap.BootstrapError;
import org.fenixedu.bennu.core.bootstrap.annotations.Bootstrap;
import org.fenixedu.bennu.core.bootstrap.annotations.Bootstrapper;
import org.fenixedu.bennu.core.bootstrap.annotations.Field;
import org.fenixedu.bennu.core.bootstrap.annotations.FieldType;
import org.fenixedu.bennu.core.bootstrap.annotations.Section;
import org.fenixedu.bennu.core.groups.AnyoneGroup;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.messaging.bootstrap.MessagingSystemBootstrap.SystemSenderSection;
import org.fenixedu.messaging.domain.MessageDeletionPolicy;
import org.fenixedu.messaging.domain.MessagingSystem;
import org.fenixedu.messaging.domain.Sender;

import com.google.common.base.Strings;

@Bootstrapper(bundle = "MessagingResources", name = "title.bootstrapper", sections = SystemSenderSection.class,
        after = AdminUserBootstrapper.class)
public class MessagingSystemBootstrap {

    private static final String BUNDLE = "MessagingResources";

    @Bootstrap
    public static List<BootstrapError> bootstrapSystemSender(SystemSenderSection section) {
        if (Strings.isNullOrEmpty(section.getFromAddress())) {
            return Collections.singletonList(new BootstrapError(AdminUserSection.class, "getFromAddress",
                    "error.bootstrapper.systemsender.address.empty", BUNDLE));
        }
        if (Strings.isNullOrEmpty(section.getFromName())) {
            return Collections.singletonList(new BootstrapError(AdminUserSection.class, "getFromName",
                    "error.bootstrapper.systemsender.name.empty", BUNDLE));
        }
        if (Strings.isNullOrEmpty(section.getGroupExpression())) {
            return Collections.singletonList(new BootstrapError(AdminUserSection.class, "getGroupExpression",
                    "error.bootstrapper.systemsender.group.empty", BUNDLE));
        }

        Sender sender =
                new Sender(section.getFromName(), section.getFromAddress(), Group.parse(section.getGroupExpression()),
                        MessageDeletionPolicy.unlimited());
        sender.addRecipient(AnyoneGroup.get());
        MessagingSystem.getInstance().setSystemSender(sender);

        return Collections.emptyList();
    }

    @Section(name = "title.bootstrapper.systemsender",
            description = "title.bootstrapper.systemsender.description", bundle = BUNDLE)
    public static interface SystemSenderSection {
        @Field(name = "label.bootstrapper.systemsender.name", hint = "hint.bootstrapper.systemsender.name", order = 1)
        public String getFromName();

        @Field(name = "label.bootstrapper.systemsender.address", hint = "hint.bootstrapper.systemsender.address",
                fieldType = FieldType.EMAIL, order = 2)
        public String getFromAddress();

        @Field(name = "label.bootstrapper.systemsender.group", hint = "hint.bootstrapper.systemsender.group", order = 3)
        public String getGroupExpression();
    }
}
