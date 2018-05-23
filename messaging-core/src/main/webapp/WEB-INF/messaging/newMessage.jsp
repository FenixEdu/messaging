<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="org.fenixedu.messaging.tags.sorter" prefix="sort" %>
${portal.toolkit()}

<link href="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.6-rc.0/css/select2.min.css" rel="stylesheet" />
<script src="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.6-rc.0/js/select2.min.js"></script>
<script>
    function checkRequiredFields() {
        function getLocalizedInputValue(inputEl) {
            var inputObject = inputEl.val() ? JSON.parse(inputEl.val()) : null;
            return Bennu.locales.map(function (x) {
                return Bennu.localizedString.getContent(inputObject, x, true) || false;
            }).reduce(function (x, y) {
                return x || y;
            }, false);
		}
		var hasAnySubject = getLocalizedInputValue($("#subject"));
        var hasAnyMessageBody = getLocalizedInputValue($("#textBody")) || getLocalizedInputValue($("#htmlBody"));
        var hasAnyRecipient = $("#selectRecipients").val() || $("#singleRecipients").val();
        if (!hasAnySubject || !hasAnyMessageBody || !hasAnyRecipient) {
            $('#message-send-button').prop('disabled', true);
		}
		else {
            $('#message-send-button').prop('disabled', false);
		}
    }
</script>

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
		<label class="control-label col-sm-2" for="senderSelect"><spring:message code="label.message.sender"/>:</label>
		<div class="col-sm-8">
		<c:if test="${not messageBean.senderLocked or empty messageBean.sender}">
			<form:select class="form-control" id="selectSender" path="sender" required="true">
				<c:forEach var="sender" items="${sort:uniqueSort(senders)}">
					<form:option value="${sender.externalId}">${sender.name} (${sender.address})</form:option>
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
		<div class="col-sm-8">
			<spring:message code="hint.email.list" var="placeholder"/>
			<form:input type="email" multiple="multiple" class="form-control" id="replyTo" path="replyTo" value="${messageBean.replyTo}" placeholder="${placeholder}"/>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2"><spring:message code="label.message.recipients"/>:</label>
		<div class="col-sm-8">
			<form:select class="form-control " id="selectRecipients" path="selectedRecipients" multiple="true">

			</form:select>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for="singleRecipients"><spring:message code="label.message.recipients.single"/>:</label>
		<div class="col-sm-8">
			<spring:message code="hint.email.list" var="placeholder"/>
			<input type="email" multiple="multiple" class="form-control" id="singleRecipients" name="singleRecipients" placeholder="${placeholder}" value="${messageBean.singleRecipients}" oninput="checkRequiredFields()"/>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for=preferredLocale><spring:message code="label.message.locale.preferred"/>:</label>
		<div class="col-sm-8">
			<form:select class="form-control" id="preferredLocale" path="preferredLocale">
			<c:forEach items="${locales}" var="locale">
				<form:option value="${locale}">${locale.getDisplayName(locale)}</form:option>
			</c:forEach>
			</form:select>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for="subject"><spring:message code="label.message.subject"/>:</label>
		<div class="col-sm-8">
			<input class="form-control" id="subject" name="subject" onchange="checkRequiredFields()" value='<c:out value="${messageBean.subject.json()}"/>' bennu-localized-string/>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for="textBody"><spring:message code="label.message.body.text"/>:</label>
		<div class="col-sm-8">
			<textarea class="form-control" id="textBody" name="textBody" onchange="checkRequiredFields()" rows="12" bennu-localized-string>${messageBean.textBody.json()}</textarea>
		</div>
	</div>
	<div id="htmlMessage" class="form-group" hidden>
		<label class="control-label col-sm-2" for="htmlBody"><spring:message code="label.message.body.html"/>:</label>
		<div class="col-sm-8">
			<textarea class="form-control" id="htmlBody" name="htmlBody" onchange="checkRequiredFields()" bennu-html-editor bennu-localized-string>${messageBean.htmlBody.json()}</textarea>
		</div>
	</div>
	<div id="attachmentsDiv" class="form-group">
		<label class="control-label col-sm-2" for="textBody"><spring:message code="label.message.attachments"/>:</label>
		<div id="attachments-container" class="col-sm-3">
			<input type="file" multiple="true" id="addAttachment" name="addAttachment"/>
		</div>
	</div>
	<div class="form-group">
		<div class="col-sm-offset-2 col-sm-8">
			<button id="message-send-button" class="btn btn-primary" type="submit" disabled><spring:message code="action.send.message"/></button>
		</div>
	</div>
</form:form>

<style>
    <%-- CSS custom style to remove selected options from dropdown --%>
    .select2-results__option[aria-selected="true"] {
        display: none;
    }
</style>
<script>
(function(){
    var senderSelectEl = $('#selectSender');
    var recipientsSelectEl = $("#selectRecipients");

    senderSelectEl.select2();

    // Custom functions to use instead of default atob/btoa functions
	// due to Unicode issue in Base64 encoding use cases
    function b64DecodeUnicode(str) {
        return decodeURIComponent(atob(str).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
    }

    function b64EncodeUnicode(str) {
        return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g,
            function toSolidBytes(match, p1) {
                return String.fromCharCode('0x' + p1);
            }));
    }


    function readRecipient(recipient) {
        try {
            return JSON.parse(b64DecodeUnicode(recipient));
        } catch(e) { console.log("An erroneous recipient was discarded: " + recipient); }
    }

    function writeRecipient(recipient) {
	    return b64EncodeUnicode(JSON.stringify({expression: recipient.expression, jwt: recipient.jwt}));
    }

    recipientsSelectEl.select2({
        allowClear: true,
        placeholder: '<spring:message code="label.message.selectedRecipients.placeholder" text="Select Recipient(s)..."/>'
	});

    senderSelectEl.change(function(){
        senderUpdate(this.value);
    });
    senderUpdate("${messageBean.sender.externalId}");

    function senderUpdate(sender) {

        function nameCompare(a, b) {
            a = a && a['name'] || "";
            b = b && b['name'] || "";
            if(a < b) return -1;
            if(a > b) return 1;
            return 0;
        }

        $.getJSON('senders/' + sender, function (info) {
            var result = [];
            info.recipients.sort(nameCompare).forEach(function(recipient){
                result.push({ "id": writeRecipient(recipient), "text": recipient.name});
            });
            <c:forEach var="adHocRecipient" items="${messageBean.adHocRecipients}">
				var recipient = readRecipient("${adHocRecipient}");
				result.push({ "id": writeRecipient(recipient), "text": recipient.name, "selected": "true"});
            </c:forEach>

            recipientsSelectEl.empty();
            recipientsSelectEl.select2({
                placeholder: '<spring:message code="label.message.selectedRecipients.placeholder" text="Select Recipient(s)..."/>',
				data: result
            });
            // Custom select2 fix to have insertion order in selected options
            recipientsSelectEl.on("select2:select", function (evt) {
                var element = evt.params.data.element;
                var $element = $(element);

                $element.detach();
                $(this).append($element);
                $(this).trigger("change");
            });

            // Custom select2 configuration to detect on change event
            recipientsSelectEl.on('change', function(e){
                checkRequiredFields();
            });

            $('#replyTo').val(info.replyTo);
            toggleHtml(info);
            toggleAttachments(info);
        });
    }

    function toggleHtml(info) {
        var htmlMessageEl = $('#htmlMessage'),
            htmlBodyEL = $('#htmlBody');
        if (info.html) {
            htmlMessageEl.show();
            htmlBodyEL.attr('name', 'htmlBody');
        } else {
            htmlMessageEl.hide();
            htmlBodyEL.removeAttr('name');
        }
    }
    function toggleAttachments(info) {
        var htmlMessageEl = $('#attachmentsDiv'),
            htmlBodyEL = $('#addAttachment');
        if (info.attachmentsEnabled) {
            htmlMessageEl.show();
            htmlBodyEL.attr('name', 'addAttachment');
        } else {
            htmlMessageEl.hide();
            htmlBodyEL.removeAttr('name');
        }
    }

	var addAttachmentEl = $('#addAttachment');
	addAttachmentEl.change(function(event){
        var files = event.target.files;
        for (var i = 0; i < files.length; i++) {
            var oMyForm = new FormData();
            oMyForm.append("file", files[i]);
            $.ajax({
                url : "messages/uploadFile?sender="+senderSelectEl.val(),
                data : oMyForm,
                type : "POST",
                enctype: 'multipart/form-data',
                processData: false,
                contentType:false,
                cache:false,
                headers: { '${csrf.headerName}' :  '${csrf.token}' },
                success : function(result) {
                    addAttachment(result.fileid, result.filename);
                }
            });
        }
	});

    function addAttachment(fileid,filename, locked) {
        var groupBtnEl = $('<span class="input-group-btn"></span>'),
            removeBtnEl = $('<button class="btn btn-danger" type="button"></button>'),
            lockedBtnEl = $('<button class="btn btn-primary" type="button" disabled></button>'),
            iconEl = $('<span class="glyphicon glyphicon-remove"></span>'),
            lockediconEl = $('<span class="glyphicon glyphicon-lock"></span>'),
            input1El = $('<input hidden name="attachments" value="' + fileid + '"/>'),
            input2El = $('<div class="form-control">' + filename + '</div>'),
            inputGroupEl = $('<div class="input-group" style="margin-bottom: 10px;"></div>'),
            groupEl = $('<div style="display: inline;"></div>');
        if (!locked) {
            removeBtnEl.append(iconEl);
            groupBtnEl.append(removeBtnEl);
        }
        else{
            lockedBtnEl.append(lockediconEl);
            groupBtnEl.append(lockedBtnEl);
        }
        inputGroupEl.append(input1El).append(input2El).append(groupBtnEl);
        groupEl.append(inputGroupEl);
        removeBtnEl.click(function () {
            groupEl.remove();
        });
        var recipientContainer = $('#attachments-container');
        recipientContainer.append(groupEl);
    }

    <c:forEach var="lockedAttachment" items="${messageBean.lockedAttachments}">
		addAttachment("${lockedAttachment.externalId}","${lockedAttachment.filename}", true);
    </c:forEach>

    <c:forEach var="attachment" items="${messageBean.attachments}">
		addAttachment("${attachment.externalId}","${attachment.filename}", false);
    </c:forEach>
})();
</script>