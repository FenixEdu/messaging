package org.fenixedu.messaging.domain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.User;

import com.google.common.collect.ImmutableSet;

public class ReplyTos implements Serializable {
    private static final long serialVersionUID = 7797650299594485656L;

    private final ImmutableSet<ReplyTo> replyTos;

    public ReplyTos(ImmutableSet<ReplyTo> replyTos) {
        this.replyTos = replyTos;
    }

    public ReplyTos(Set<ReplyTo> replyTos) {
        this.replyTos = ImmutableSet.copyOf(replyTos);
    }

    public ReplyTos(String... replyTos) {
        this(Arrays.stream(replyTos).map(rt -> ReplyTo.parse(rt)).collect(Collectors.toSet()));
    }

    public ReplyTos add(ReplyTo replyTo) {
        return new ReplyTos(ImmutableSet.<ReplyTo> builder().addAll(replyTos).add(replyTo).build());
    }

    public ReplyTos add(String email) {
        return new ReplyTos(ImmutableSet.<ReplyTo> builder().addAll(replyTos).add(ReplyTo.concrete(email)).build());
    }

    public ReplyTos add(User user) {
        return new ReplyTos(ImmutableSet.<ReplyTo> builder().addAll(replyTos).add(ReplyTo.user(user)).build());
    }

    public ReplyTos addCurrentLoggedUser() {
        return new ReplyTos(ImmutableSet.<ReplyTo> builder().addAll(replyTos).add(ReplyTo.currentUser()).build());
    }

    public Set<ReplyTo> replyTos() {
        return replyTos;
    }

    public Set<String> addresses() {
        return replyTos.stream().filter(r -> r.getAddress() != null).map(r -> r.getAddress()).collect(Collectors.toSet());
    }

    public static ReplyTos internalize(String serialized) {
        Set<ReplyTo> replyTos = new HashSet<>();
        for (String email : serialized.split(",")) {
            ReplyTo replyTo = ReplyTo.parse(email);
            if (replyTo != null) {
                replyTos.add(replyTo);
            }
        }
        return new ReplyTos(replyTos);
    }

    public String serialize() {
        return replyTos.stream().map(r -> r.serialize()).collect(Collectors.joining(","));
    }
}
