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

@Bootstrapper(bundle = "resources.MessagingResources", name = "label.messaging.bootstrapper",
        sections = SystemSenderSection.class, after = AdminUserBootstrapper.class)
public class MessagingSystemBootstrap {

    @Bootstrap
    public static List<BootstrapError> bootstrapSystemSender(SystemSenderSection section) {
        if (Strings.isNullOrEmpty(section.getFromAddress())) {
            return Collections.singletonList(new BootstrapError(AdminUserSection.class, "getFromAddress",
                    "error.messaging.bootstrapper.emptyFromAddress", "resources.MessagingResources"));
        }
        if (Strings.isNullOrEmpty(section.getFromName())) {
            return Collections.singletonList(new BootstrapError(AdminUserSection.class, "getFromName",
                    "error.messaging.bootstrapper.emptyFromName", "resources.MessagingResources"));
        }
        if (Strings.isNullOrEmpty(section.getGroupExpression())) {
            return Collections.singletonList(new BootstrapError(AdminUserSection.class, "getGroupExpression",
                    "error.messaging.bootstrapper.emptyGroupExpression", "resources.MessagingResources"));
        }

        Sender sender =
                new Sender(section.getFromName(), section.getFromAddress(), Group.parse(section.getGroupExpression()),
                        MessageDeletionPolicy.unlimited());
        sender.addRecipient(AnyoneGroup.get());
        MessagingSystem.getInstance().setSystemSender(sender);

        return Collections.emptyList();
    }

    @Section(name = "label.messaging.bootstraper.systemsender",
            description = "label.messaging.bootstraper.systemsender.description", bundle = "resources.MessagingResources")
    public static interface SystemSenderSection {
        @Field(name = "label.messaging.sender.fromName", hint = "label.messaging.sender.hint.fromName", order = 1)
        public String getFromName();

        @Field(name = "label.messaging.sender.fromAddress", hint = "label.messaging.sender.hint.fromAddress",
                fieldType = FieldType.EMAIL, order = 2)
        public String getFromAddress();

        @Field(name = "label.messaging.sender.group", hint = "label.messaging.sender.hint.group", order = 3)
        public String getGroupExpression();
    }
}
