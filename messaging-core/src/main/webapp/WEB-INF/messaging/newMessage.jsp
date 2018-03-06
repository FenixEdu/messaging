<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="org.fenixedu.messaging.tags.sorter" prefix="sort" %>

${portal.toolkit()}

<h2><spring:message code="title.message.new"/></h2>

<c:if test="${not empty messageBean.errors}">
	<div class="alert alert-danger">
		<span><spring:message code="error.message.not.sent"/></span>
		<c:forEach items="${sort:uniqueSort(messageBean.errors)}" var="error">
			<br/><span style="padding-left: 2em;">${error}</span>
		</c:forEach>
	</div>
</c:if>

<spring:eval expression="T(org.fenixedu.messaging.core.domain.Sender).available()" var="senders"/>
<spring:eval expression="T(org.fenixedu.bennu.core.util.CoreConfiguration).supportedLocales()" var="locales"/>
<form:form modelAttribute="messageBean" role="form" class="form-horizontal" action="${pageContext.request.contextPath}/messaging/message" method="post">
	${csrf.field()}
	<div class="form-group">
		<c:forEach var="adHocRecipient" items="${messageBean.adHocRecipients}">
			<form:input type="hidden" path="adHocRecipients" value="${adHocRecipient}"/>
		</c:forEach>
		<form:input type="hidden" path="senderLocked" value="${messageBean.senderLocked}"/>
		<label class="control-label col-sm-2" for="senderSelect"><spring:message code="label.message.sender"/>:</label>
		<div class="col-sm-10">
		<c:if test="${not messageBean.senderLocked or empty messageBean.sender}">
			<form:select class="form-control" id="senderSelect" path="sender" required="true">
				<form:option class="form-control" value=""><spring:message code="hint.sender.select"/></form:option>
				<c:forEach var="sender" items="${sort:uniqueSort(senders)}">
					<form:option class="form-control" value="${sender.externalId}">${sender.name} (${sender.address})</form:option>
				</c:forEach>
			</form:select>
		</c:if>
		<c:if test="${not empty messageBean.sender and messageBean.senderLocked}">
			<form:input type="hidden" path="sender" value="${messageBean.sender.externalId}"/>
			<form:input path="" type="text" class="form-control" id="senderSelect" readonly="true"
						value="${messageBean.sender.name} (${messageBean.sender.address})"/>
		</c:if>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2"><spring:message code="label.message.replyTo"/>:</label>
		<div class="col-sm-10">
			<spring:message code="hint.email" var="placeholder"/>
			<form:input type="email" class="form-control" id="replyTo" path="replyTo" value="${messageBean.replyTo}" placeholder="${placeholder}"/>
		</div>
	</div>
	<div id="recipients-container" class="form-group">
		<label class="control-label col-sm-2"><spring:message code="label.message.recipients"/>:</label>
		<div id="recipients" class="form-inline col-sm-10">
		<c:forEach  var="recipient" items="${sort:uniqueSort(messageBean.selectedRecipients)}">
			<input style="display:none;" type="checkbox" value='<c:out value="${recipient}"/>' checked/>
		</c:forEach>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for="singleRecipients"><spring:message code="label.message.recipients.single"/>:</label>
		<div class="col-sm-10">
			<spring:message code="hint.email.list" var="placeholder"/>
			<input type="email" multiple="multiple" class="form-control" id="singleRecipients" name="singleRecipients" placeholder="${placeholder}" value="${messageBean.singleRecipients}"/>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for=preferredLocale><spring:message code="label.message.locale.preferred"/>:</label>
		<div class="col-sm-10">
			<form:select class="form-control" id="preferredLocale" path="preferredLocale">
			<c:forEach items="${locales}" var="locale">
				<form:option value="${locale}">${locale.getDisplayName(locale)}</form:option>
			</c:forEach>
			</form:select>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for="subject"><spring:message code="label.message.subject"/>:</label>
		<div class="col-sm-10">
			<input class="form-control" id="subject" name="subject" value='<c:out value="${messageBean.subject.json()}"/>' bennu-localized-string required-any/>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for="textBody"><spring:message code="label.message.body.text"/>:</label>
		<div class="col-sm-10">
			<textarea class="form-control" id="textBody" name="textBody" bennu-localized-string>${messageBean.textBody.json()}</textarea>
		</div>
	</div>
	<div id="htmlMessage" class="form-group">
		<label class="control-label col-sm-2" for="htmlBody"><spring:message code="label.message.body.html"/>:</label>
		<div class="col-sm-10">
			<textarea class="form-control" id="htmlBody" name="htmlBody" bennu-html-editor bennu-localized-string>${messageBean.htmlBody.json()}</textarea>
		</div>
	</div>
	<div class="form-group">
		<div class="col-sm-offset-2 col-sm-10">
			<button class="btn btn-primary" type="submit"><spring:message code="action.send.message"/></button>
		</div>
	</div>
</form:form>
<script>
(function(){
	var recipientsEl = $('#recipients'),
		recipientsContainerEl = $('#recipients-container'),
		replyToEl = $('#replyTo'),
		htmlMessageEl = $('#htmlMessage'),
		htmlBodyEL = $('#htmlBody'),
		senderSelectEl = $('#senderSelect');
	var originalSender = "${messageBean.sender.externalId}",
		adHocRecipients = [];

	function readRecipient(recipient) {
        try {
            return JSON.parse(atob(recipient));
        } catch(e) { console.log("An erroneous recipient was discarded: " + recipient); }
    }

    function writeRecipient(recipient) {
	    return btoa(JSON.stringify({expression: recipient.expression, jwt: recipient.jwt}));
    }
	var recipient;
    <c:forEach var="recipient" items="${messageBean.adHocRecipients}">
		recipient = readRecipient("${recipient}");
		if(recipient){ adHocRecipients.push(recipient); }
    </c:forEach>

	function nameCompare(a, b) {
		a = a && a['name'] || "";
		b = b && b['name'] || "";
		if(a < b) return -1;
		if(a > b) return 1;
		return 0;
	}

	function appendRecipientCheckbox(recipient, checked){
		var value = writeRecipient(recipient);
		var id = 'recipients-' + recipient.expression,
			labelEl = $('<label style="margin-right: 5px;" class="input-group input-group-sm" for="'+id+'"></label>'),
			checkboxEl = $('<input/>', { type: 'checkbox', id: id, value: value, name: 'selectedRecipients', checked: checked }),
			addonEl = $('<span class="input-group-addon"></span>'),
			spanEl = $('<span class="form-control">'+recipient.name+'</span>');
		addonEl.append(checkboxEl);
		labelEl.append(addonEl);
		labelEl.append(spanEl);
		recipientsEl.append(labelEl);
	}

	function recallRecipients(){
		var checked = [];
		recipientsEl.find("input:checked").each(function() {
            var recipient = readRecipient($(this).val());
            if(recipient){ checked.push(recipient); }
		});
		recipientsEl.empty();
		return checked;
	}

	function populateRecipients(providedRecipients) {
		recipientsContainerEl.hide();
		if(providedRecipients.length !== 0) {
			var selectedRecipients = recallRecipients();
			providedRecipients.sort(nameCompare).forEach(function(provided){
				var checked = !!$.grep(selectedRecipients, function(selected){
					return selected.expression === provided.expression;
				}).length;
				appendRecipientCheckbox(provided, checked);
			});
			recipientsContainerEl.show();
		}
	}

	var currentSenderReplyTo;
	function senderUpdate(sender){
		if(sender){
			$.getJSON('senders/' + sender, function(info){
				if(info.replyTo && (!replyToEl.val() || replyToEl.val() == currentSenderReplyTo)) {
					replyToEl.val(info.replyTo);
					currentSenderReplyTo = info.replyTo;
				}
				var expressions = $.map(info.recipients, function(recipient) { return recipient.expression; }),
					relevantAdHocRecipients = $.grep(adHocRecipients, function(recipient) {
						return recipient.sender === sender && expressions.indexOf(recipient.expression) < 0;
					});
				populateRecipients(info.recipients.concat(relevantAdHocRecipients));
				if(info.html){
					htmlMessageEl.show();
					htmlBodyEL.attr('name','htmlBody');
				} else {
					htmlMessageEl.hide();
					htmlBodyEL.removeAttr('name');
				}
			});
		} else {
			recipientsContainerEl.hide();
			if(replyToEl.val() == currentSenderReplyTo) {
				replyToEl.val(currentSenderReplyTo = '');
			}
		}
	}

	senderSelectEl.change(function(){
		senderUpdate(this.value);
	});

	senderUpdate(originalSender);
})();
</script>