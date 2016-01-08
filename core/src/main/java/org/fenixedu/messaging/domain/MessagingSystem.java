/*
 * @(#)MessagingSystem.java
 *
 * Copyright 2012 Instituto Superior Tecnico
 * Founding Authors: Luis Cruz
 *
 *      https://fenix-ashes.ist.utl.pt/
 *
 *   This file is part of the Messaging Module.
 *
 *   The Messaging Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version
 *   3 of the License, or (at your option) any later version.
 *
 *   The Messaging Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the Messaging Module. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.fenixedu.messaging.domain;

import static pt.ist.fenixframework.FenixFramework.atomic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.messaging.exception.MessagingDomainException;
import org.fenixedu.messaging.template.MessageTemplateDeclaration;
import org.fenixedu.messaging.template.annotation.DeclareMessageTemplate;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

/**
 *
 * @author Luis Cruz
 *
 */
public class MessagingSystem extends MessagingSystem_Base {
    private static Map<String, DeclareMessageTemplate> declarations = new HashMap<>();
    private static Map<String, MessageTemplateDeclaration> templates = null;
    private static Set<MessageTemplate> undeclared = null;

    private MessagingSystem() {
        super();
        setBennu(Bennu.getInstance());
    }

    public static MessagingSystem getInstance() {
        if (Bennu.getInstance().getMessagingSystem() == null) {
            return createMessagingSystem();
        }
        return Bennu.getInstance().getMessagingSystem();
    }

    @Atomic(mode = TxMode.WRITE)
    private static MessagingSystem createMessagingSystem() {
        if (Bennu.getInstance().getMessagingSystem() == null) {
            return new MessagingSystem();
        }
        return Bennu.getInstance().getMessagingSystem();
    }

    private static MessageDispatcher dispatcher = null;

    public static void setMessageDispatcher(MessageDispatcher dispatcher) {
        MessagingSystem.dispatcher = dispatcher;
    }

    @Atomic(mode = TxMode.WRITE)
    public static void deleteOldMessages() {
        for (Sender sender : MessagingSystem.getInstance().getSenderSet()) {
            sender.pruneOldMessages();
        }
    }

    @Atomic(mode = TxMode.WRITE)
    public static MessageDispatchReport dispatch(Message message) {
        MessageDispatchReport report = dispatcher.dispatch(message);
        message.setMessagingSystemFromPendingDispatch(null);
        message.setDispatchReport(report);
        return report;
    }

    public static Sender systemSender() {
        return getInstance().getSystemSender();
    }

    public static MessageTemplateDeclaration getTemplateDeclaration(String id) {
        MessageTemplateDeclaration declaration = getTemplateDeclarations().get(id);
        if (declaration == null) {
            throw MessagingDomainException.missingTemplate(id);
        }
        return declaration;
    }

    static MessageTemplate getTemplate(String id) {
        MessageTemplateDeclaration declaration = getTemplateDeclaration(id);
        if (declaration.getTemplate() == null) {
            throw MessagingDomainException.missingTemplate(id);
        }
        return declaration.getTemplate();
    }

    public static void declareTemplate(DeclareMessageTemplate decl) {
        if (declarations != null) {
            declarations.put(decl.id(), decl);
        }
    }

    public static Map<String, MessageTemplateDeclaration> getTemplateDeclarations() {
        if (templates == null) {
            initializeTemplates();
        }
        return templates;
    }

    public static Set<MessageTemplate> getUndeclaredTemplates() {
        if (undeclared == null) {
            initializeTemplates();
        }
        return undeclared;
    }

    private static void initializeTemplates() {
        templates = new HashMap<>();
        Set<MessageTemplate> existing = getInstance().getTemplateSet();
        undeclared = new HashSet<>();
        existing.forEach(t -> {
            DeclareMessageTemplate declare = declarations.get(t.getId());
            if (declare == null) {
                undeclared.add(t);
            } else {
                templates.put(t.getId(), new MessageTemplateDeclaration(t, declare));
                declarations.remove(t.getId());
            }
        });
        declarations.values().forEach(declare -> atomic(() -> {
            MessageTemplate template = new MessageTemplate();
            template.setId(declare.id());
            MessageTemplateDeclaration declaration = new MessageTemplateDeclaration(template, declare);
            template.setHtmlBody(declaration.getDefaultHtmlBody());
            template.setTextBody(declaration.getDefaultTextBody());
            templates.put(declare.id(), declaration);
        }));
        declarations = null;
    }

    public static final String MAIL_LIST_SEPARATOR = "\\s*,\\s*";
    public static final Joiner MAIL_LIST_JOINER = Joiner.on(",").skipNulls();

    public static Set<String> toEmailSet(String s) {
        return Strings.isNullOrEmpty(s) ? Sets.newHashSet() : Sets.newHashSet(s.split(MAIL_LIST_SEPARATOR));
    }
}
