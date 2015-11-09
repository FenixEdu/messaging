package org.fenixedu.messaging.domain;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.security.Authenticate;

public abstract class ReplyTo implements Comparable<ReplyTo> {
    private static final String BUNDLE = "MessagingResources";

    public abstract String getAddress();

    public abstract String serialize();

    public static class ConcreteReplyTo extends ReplyTo {
        private String email;

        public ConcreteReplyTo(String email) {
            this.email = email;
        }

        @Override
        public String getAddress() {
            return email;
        }

        @Override
        public String serialize() {
            return email;
        }
    }

    public static class UserReplyTo extends ReplyTo {
        private User user;

        public UserReplyTo(User user) {
            this.user = user;
        }

        @Override
        public String getAddress() {
            return user.getProfile().getEmail();
        }

        @Override
        public String serialize() {
            return user.getUsername();
        }

        public User getUser() {
            return user;
        }

        @Override
        public String toString() {
            return user.getProfile().getDisplayName();
        }
    }

    public static class CurrentUserReplyTo extends ReplyTo {
        CurrentUserReplyTo instance = null;

        private CurrentUserReplyTo() {
        }

        public CurrentUserReplyTo instance() {
            if (instance == null) {
                instance = new CurrentUserReplyTo();
            }
            return instance;
        }

        @Override
        public String getAddress() {
            return Authenticate.getUser() != null ? Authenticate.getUser().getProfile().getEmail() : null;
        }

        @Override
        public String serialize() {
            return "-1";
        }

        @Override
        public String toString() {
            return BundleUtil.getString(BUNDLE, "name.reply.to.current.user");
        }
    }

    public static ReplyTo concrete(String email) {
        return new ConcreteReplyTo(email);
    }

    public static ReplyTo user(User user) {
        return new UserReplyTo(user);
    }

    public static ReplyTo currentUser() {
        return new CurrentUserReplyTo();
    }

    public static ReplyTo parse(String serialized) {
        if (serialized.contains("@")) {
            return concrete(serialized);
        } else if (serialized.equals("-1")) {
            return currentUser();
        } else {
            User user = User.findByUsername(serialized);
            if (user != null) {
                return user(user);
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return getAddress();
    }

    @Override
    public int compareTo(ReplyTo rt) {
        return toString().compareTo(rt.toString());
    }
}