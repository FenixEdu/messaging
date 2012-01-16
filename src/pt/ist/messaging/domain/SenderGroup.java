package pt.ist.messaging.domain;

import java.util.HashSet;
import java.util.Set;

import myorg.domain.User;
import myorg.domain.VirtualHost;
import myorg.domain.groups.PersistentGroup;
import myorg.util.BundleUtil;
import pt.ist.fenixWebFramework.services.Service;

public class SenderGroup extends SenderGroup_Base {

    protected SenderGroup() {
        super();
        setSystemGroupMyOrg(getMyOrg());
    }

    protected String getNameLable() {
	return "label.persistent.group.sender.name";
    }

    @Override
    public String getName() {
	return BundleUtil.getStringFromResourceBundle("resources/MessagingResources", getNameLable());
    }

    @Override
    public boolean isMember(final User user) {
	for (final Sender sender : MessagingSystem.getInstance().getSenderSet()) {
	    if (sender.getVirtualHost() == VirtualHost.getVirtualHostForThread() && sender.isMember(user)) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public Set<User> getMembers() {
	final Set<User> members = new HashSet<User>();
	for (final Sender sender : MessagingSystem.getInstance().getSenderSet()) {
	    if (sender.getVirtualHost() == VirtualHost.getVirtualHostForThread()) {
		final PersistentGroup group = sender.getMembers();
		if (group != null) {
		    members.addAll(group.getMembers());
		}
	    }
	}
	return members;
    }

    @Service
    public static SenderGroup getInstance() {
	final SenderGroup group = (SenderGroup) PersistentGroup.getSystemGroup(SenderGroup.class);
	return group == null ? new SenderGroup() : group;
    }

}
