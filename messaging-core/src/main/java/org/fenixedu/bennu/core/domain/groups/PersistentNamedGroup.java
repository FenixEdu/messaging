package org.fenixedu.bennu.core.domain.groups;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.commons.i18n.LocalizedString;
import pt.ist.fenixframework.dml.runtime.Relation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class PersistentNamedGroup extends PersistentNamedGroup_Base {

    protected PersistentNamedGroup(LocalizedString name, Group group) {
        super();
        setName(name);
        setGroup(group.toPersistentGroup());
        setRootForNamedGroups(Bennu.getInstance());
    }
    @Override public Group toGroup() {
        return new NamedGroup(getName(),getGroup().toGroup());
    }

    @Override protected Collection<Relation<?, ?>> getContextRelations() {
        Set<Relation<?, ?>> set = new HashSet<>();
        set.add(getRelationNamedGroupsRoot());
        set.add(getRelationPersistentNamedGroupGroup());
        set.addAll(super.getContextRelations());
        return set;
    }

    public static PersistentGroup getInstance(LocalizedString name, Group group) {
        return singleton( () -> select(name,group), () -> new PersistentNamedGroup(name,group));
    }

    private static  Optional<PersistentNamedGroup> select(LocalizedString name, Group group) {
        Stream<PersistentNamedGroup> stream = Bennu.getInstance().getNamedGroupsSet().stream();
        return stream.filter(namedGroup -> Objects.equals(namedGroup.getName(),name)
                && Objects.equals(namedGroup.getGroup(),group.toPersistentGroup())).findAny();
    }

}
