package org.fenixedu.bennu.core.domain.groups;

import com.google.common.base.Objects;
import org.fenixedu.bennu.core.annotation.GroupArgument;
import org.fenixedu.bennu.core.annotation.GroupOperator;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.CustomGroup;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.commons.i18n.LocalizedString;
import org.joda.time.DateTime;

import java.util.stream.Stream;

@GroupOperator("NG")
public class NamedGroup extends CustomGroup {

    @GroupArgument("name")
    private LocalizedString name;

    @GroupArgument("group")
    private Group group;

    public NamedGroup() {
    }

    public NamedGroup(LocalizedString name) {
        this.name = name;
    }

    public NamedGroup(LocalizedString name, Group group) {
        this.name = name;
        this.group = group;
    }

    @Override public String getPresentationName() {
        return name.getContent();
    }

    @Override public PersistentGroup toPersistentGroup() {
        return PersistentNamedGroup.getInstance(name,group);
    }

    @Override public Stream<User> getMembers() {
        return group.getMembers();
    }

    @Override public Stream<User> getMembers(DateTime dateTime) {
        return group.getMembers(dateTime);
    }

    @Override public boolean isMember(User user) {
        return group.isMember(user);
    }

    @Override public boolean isMember(User user, DateTime dateTime) {
        return group.isMember(user, dateTime);
    }

    @Override public boolean equals(Object o) {
        if (o instanceof NamedGroup) {
            NamedGroup other = (NamedGroup) o;
            return Objects.equal(name, other.name) && Objects.equal(group, other.group);
        }
        return false;
    }

    @Override public int hashCode() {
        return group.hashCode();
    }
}
