package org.fenixedu.messaging.core.bootstrap;

import java.util.List;

import org.fenixedu.bennu.core.bootstrap.AdminUserBootstrapper;
import org.fenixedu.bennu.core.bootstrap.BootstrapError;
import org.fenixedu.bennu.core.bootstrap.annotations.Bootstrap;
import org.fenixedu.bennu.core.bootstrap.annotations.Bootstrapper;
import org.fenixedu.bennu.core.bootstrap.annotations.Field;
import org.fenixedu.bennu.core.bootstrap.annotations.FieldType;
import org.fenixedu.bennu.core.bootstrap.annotations.Section;
import org.fenixedu.bennu.core.domain.exceptions.BennuCoreDomainException;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.messaging.core.bootstrap.MessagingSystemBootstrap.SystemSenderSection;
import org.fenixedu.messaging.core.domain.MessageDeletionPolicy;
import org.fenixedu.messaging.core.domain.MessagingSystem;
import org.fenixedu.messaging.core.domain.Sender;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Bootstrapper(bundle = "MessagingResources", name = "title.bootstrapper", sections = SystemSenderSection.class,
        after = AdminUserBootstrapper.class)
public class MessagingSystemBootstrap {

    private static final String BUNDLE = "MessagingResources";

    @Bootstrap
    public static List<BootstrapError> bootstrapSystemSender(SystemSenderSection section) {
        String name = section.getName(), address = section.getAddress(), expression = section.getGroupExpression();
        List<BootstrapError> errors = Lists.newArrayList();
        if (Strings.isNullOrEmpty(name)) {
            errors.add(new BootstrapError(SystemSenderSection.class, "getName", "error.bootstrapper.systemsender.name.empty",
                    BUNDLE));
        }
        if (Strings.isNullOrEmpty(address)) {
            errors.add(
                    new BootstrapError(SystemSenderSection.class, "getAddress", "error.bootstrapper.systemsender.address.empty",
                            BUNDLE));
        }
        if (!MessagingSystem.Util.isValidEmail(address)) {
            errors.add(
                    new BootstrapError(SystemSenderSection.class, "getAddress", "error.bootstrapper.systemsender.address.invalid",
                            BUNDLE));
        }
        if (Strings.isNullOrEmpty(expression)) {
            errors.add(new BootstrapError(SystemSenderSection.class, "getGroupExpression",
                    "error.bootstrapper.systemsender.group.empty", BUNDLE));
        }
        Group group = null;
        try {
            group = Group.parse(expression);
        } catch (BennuCoreDomainException e) {
            errors.add(new BootstrapError(SystemSenderSection.class, "getGroupExpression",
                    "error.bootstrapper.systemsender.group.invalid", BUNDLE));
        }
        if (errors.isEmpty()) {
            Sender sender = MessagingSystem.systemSender();
            sender.setName(name);
            sender.setAddress(address);
            sender.setMembers(group);
            sender.setPolicy(MessageDeletionPolicy.keepAll());
            sender.addRecipient(Group.anyone());
        }
        return errors;
    }

    @Section(name = "title.bootstrapper.systemsender", description = "title.bootstrapper.systemsender.description",
            bundle = BUNDLE)
    public static interface SystemSenderSection {
        @Field(name = "label.bootstrapper.systemsender.name", defaultValue = "System Sender", order = 1)
        public String getName();

        @Field(name = "label.bootstrapper.systemsender.address", defaultValue = "system@fenixedu.org",
                fieldType = FieldType.EMAIL, order = 2)
        public String getAddress();

        @Field(name = "label.bootstrapper.systemsender.group", hint = "hint.bootstrapper.systemsender.group", defaultValue = "nobody", order = 3)
        public String getGroupExpression();
    }
}
