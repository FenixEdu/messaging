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
    private boolean missingSubject, missingBody;
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

    public boolean isMissingSubject() {
        return missingSubject;
    }

    public boolean isMissingBody() {
        return missingBody;
    }

    public MessageTemplateDescriptionBean(MessageTemplateDeclaration d) {
        this(d.getTemplate());
        this.description = d.getDescription();
    }

    public MessageTemplateDescriptionBean(MessageTemplate t) {
        this.id = t.getId();
        this.externalId = t.getExternalId();
        this.missingSubject = t.getSubject().isEmpty();
        this.missingBody = t.getHtmlBody().isEmpty() && t.getTextBody().isEmpty();
    }

}
