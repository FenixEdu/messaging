<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<h2><spring:message code="title.message.new"/></h2>

<c:if test="${not empty messageBean.errors}">
	<div class="alert alert-danger">
		<span><spring:message code="error.message.not.sent"/></span>
		<c:forEach items="${messageBean.errors}" var="error">
			<br/><span style="padding-left: 2em;">${error}</span>
		</c:forEach>
	</div>
</c:if>

<br/>
<spring:eval expression="T(org.fenixedu.messaging.domain.Sender).getAvailableSenders()" var="senders"/>
<form:form modelAttribute="messageBean" role="form" class="form-horizontal" action="${pageContext.request.contextPath}/messaging/message" method="post">
	<div class="form-group">
		<label class="control-label col-sm-2" for="senderSelect"><spring:message code="label.message.sender"/>:</label>
		<div class="col-sm-10">
			<form:select class="form-control" id="senderSelect" path="sender" required="true">
				<form:option class="form-control" value=""><spring:message code="hint.sender.select"/></form:option>
				<c:forEach  var="sender" items="${senders}">
					<form:option class="form-control" value="${sender.externalId}">${sender.fromName} (${sender.fromAddress})</form:option>
				</c:forEach>
			</form:select>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2"><spring:message code="label.message.replyTos"/>:</label>
		<div id="replyTos" class="col-sm-10">
			<c:forEach  var="replyTo" items="${messageBean.replyTos}">
				<input style="display:none;" type="checkbox" value="${replyTo}" checked/>
			</c:forEach>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2"><spring:message code="label.message.bccs"/>:</label>
		<div id="recipients" class="col-sm-10">
			<c:forEach  var="recipient" items="${messageBean.recipients}">
				<input style="display:none;" type="checkbox" value="${recipient}" checked/>
			</c:forEach>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for="bccs"><spring:message code="label.message.bccs.extra"/>:</label>
		<div class="col-sm-10">
			<spring:message code="hint.extra.bccs" var="placeholder"/>
			<form:input class="form-control" id="bccs" size="50" path="bccs" placeholder="${placeholder}" pattern="^\s*[^@,\s]+@[^@,\s]+(\s*,\s*[^@,\s]+@[^@,\s]+)*$"/>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for="subject"><spring:message code="label.message.subject"/>:</label>
		<div class="col-sm-10">
			<form:input class="form-control" id="subject" size="50" path="subject" required="true"/>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for="message"><spring:message code="label.message.body"/>:</label>
		<div class="col-sm-10">
			<form:textarea class="form-control" id="message" cols="80" rows="10" path="message"/>
		</div>
	</div>
	<div id="htmlMessage" class="form-group">
		<label class="control-label col-sm-2" for="message"><spring:message code="label.message.body.html"/>:</label>
		<div class="col-sm-10">
			<form:textarea class="form-control" id="message" cols="80" rows="10" path="htmlMessage"/>
		</div>
	</div>
	<div class="form-group">
		<div class="col-sm-offset-2 col-sm-10">
			<button class="btn btn-primary" type="submit"><spring:message code="action.message.send"/></button>
		</div>
	</div>
	<script>
		function recallCheckboxes(container){
			var checked = [];
			container.children("input:checked").each(function() {
				checked.push($(this).val());
		    });
			container.empty();
			return checked;
		}

		function caseInsensitiveCompare(property, a, b) {
			a = property ? a[property] : a;
			b = property ? b[property] : b;
			a = a.toUpperCase();
			b = b.toUpperCase();
			if(a < b) return -1;
			if(a > b) return 1;
			return 0;
		}

		function appendCheckbox(element, checked, name, label, id){
			$('<input/>', { type: 'checkbox', id: id, value: id, name: name, checked: checked.indexOf(id) >= 0 }).appendTo(element);
			element.append("&nbsp;");
			$('<label class="label label-default" for="'+id+'">&nbsp;'+label+'</label>').appendTo(element);
			$('<br/>').appendTo(element);
		}

		function populateCheckboxes(info, path, element, sortProperty, labelProperty, idProperty) {
			element.parent().hide();
			var checked = recallCheckboxes(element),
				data = info[path];
			if(data.length !== 0) {
				element.parent().show();
				data.sort(caseInsensitiveCompare.bind(undefined, sortProperty)).forEach(function(item){
					appendCheckbox(element, checked, path, labelProperty ? item[labelProperty] : item, idProperty ? item[idProperty] : item);
				});
			}
		}

		function senderUpdate(sender){
			if(sender){
				$.getJSON('sender/' + sender, function(info){
					populateCheckboxes(info, 'replyTos', $('#replyTos'), null, null, null);
					populateCheckboxes(info, 'recipients', $('#recipients'), 'name', 'name', 'expression');
					if(info.html){
						$('#htmlMessage').show();
					} else {
						$('#htmlMessage').hide();
					}
				});
			}
		}

		$('#senderSelect').change(function(){
			senderUpdate(this.value);
		});
		senderUpdate(<c:out value="${messageBean.sender.externalId}"/>);
	</script>
</form:form>
