package org.fenixedu.messaging.test.mock;

import org.fenixedu.messaging.core.template.DeclareMessageTemplate;

@DeclareMessageTemplate(id = "template1", description = "template.first.description", text = "template.example.text",
        html = "template.example.html", bundle = "resources.MessagingResources")
@DeclareMessageTemplate(id = "template2", description = "template.second.description", text = "template.example.text",
        bundle = "resources.MessagingResources")
@DeclareMessageTemplate(id = "template3", description = "template.third.description", bundle = "resources.MessagingResources")
@DeclareMessageTemplate(id = "empty-template", description = "An empty template")
class TestTemplates {

}