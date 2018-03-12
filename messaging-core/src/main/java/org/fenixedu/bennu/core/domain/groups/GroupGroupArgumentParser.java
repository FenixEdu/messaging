package org.fenixedu.bennu.core.domain.groups;

import org.fenixedu.bennu.core.annotation.GroupArgumentParser;
import org.fenixedu.bennu.core.groups.ArgumentParser;
import org.fenixedu.bennu.core.groups.Group;

@GroupArgumentParser
public class GroupGroupArgumentParser implements ArgumentParser<Group> {

    @Override public Group parse(String s) {
        return Group.parse(s);
    }

    @Override public String serialize(Group group) {
        return group.getExpression();
    }

    @Override public Class<Group> type() {
        return Group.class;
    }
}