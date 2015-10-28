package org.fenixedu.messaging.ui;

import java.io.Serializable;
import java.util.Comparator;

import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.messaging.domain.MessageTemplate;
import org.fenixedu.messaging.template.MessageTemplateDeclaration;

public class MessageTemplateDescriptionBean implements Serializable {

    private static final long serialVersionUID = -1125479922580337020L;
    public static final Comparator<? super MessageTemplateDescriptionBean> COMPARATOR_BY_ID =
            new Comparator<MessageTemplateDescriptionBean>() {
                @Override
                public int compare(MessageTemplateDescriptionBean b1, MessageTemplateDescriptionBean b2) {
                    return b1.getId().compareTo(b2.getId());
                }
            };
    private boolean error;
    private String id, externalId;
    private LocalizedString description;

    public String getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public LocalizedString getDescription() {
        return description;
    }

    public boolean getError() {
        return error;
    }

    public MessageTemplateDescriptionBean(MessageTemplateDeclaration d) {
        this.id = d.getId();
        this.description = d.getDescription();
        MessageTemplate t = d.getTemplate();
        this.externalId = t.getExternalId();
        this.error = t.getHtmlBody().isEmpty() && t.getTextBody().isEmpty();
    }

    public MessageTemplateDescriptionBean(MessageTemplate t) {
        this.id = t.getId();
        this.externalId = t.getExternalId();
        this.error = t.getHtmlBody().isEmpty() && t.getTextBody().isEmpty();
    }

}
