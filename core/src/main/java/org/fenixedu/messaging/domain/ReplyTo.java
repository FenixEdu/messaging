package org.fenixedu.messaging.domain;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;

public abstract class ReplyTo {
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
            return user.getEmail();
        }

        @Override
        public String serialize() {
            return user.getUsername();
        }
    }

    public static class CurrentUserReplyTo extends ReplyTo {
        @Override
        public String getAddress() {
            return Authenticate.getUser() != null ? Authenticate.getUser().getEmail() : null;
        }

        @Override
        public String serialize() {
            return "-1";
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
}