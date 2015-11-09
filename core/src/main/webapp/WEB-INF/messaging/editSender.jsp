<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<h2><spring:message code="title.sender.edit${ create? '.new' : '' }"/></h2>

<c:if test="${not empty senderBean.errors}">
	<div class="alert alert-danger">
		<span><spring:message code="error.sender.not.saved"/></span>
		<c:forEach items="${senderBean.errors}" var="error">
			<br/><span style="padding-left: 2em;">${error}</span>
		</c:forEach>
	</div>
</c:if>

<form:form modelAttribute="senderBean" role="form" class="form-horizontal" method="post">
	<div class="form-group">
		<label class="control-label col-sm-2" for="name"><spring:message code="label.sender.name"/>:</label>
		<div class="col-sm-10">
			<form:input type="text" class="form-control" id="name" path="fromName" value="${senderBean.fromName}" required="required"/>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for="address"><spring:message code="label.sender.address"/>:</label>
		<div class="col-sm-10">
			<form:input type="email" class="form-control" id="address" path="fromAddress" value="${senderBean.fromAddress}" required="required"/>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for="members"><spring:message code="label.sender.members"/>:</label>
		<div class="col-sm-10">
			<form:input type="text" class="form-control" id="members" path="members" value="${senderBean.members}" required="required"/>
		</div>
	</div>
	<div class="form-group row">
		<label class="control-label col-sm-2"><spring:message code="label.sender.replyTos"/>:</label>
		<div class="col-sm-10">
			<div class="input-group form-inline">
				<input type="hidden" name="replyTos" id="replyTos"/>
				<span class="input-group-addon" style="text-align: left;">
					<input type="checkbox" id="replyTo-current" value="-1" ${senderBean.replyToCurrentUser ? "checked" : ""}>&nbsp;
					<label style="margin: 0;" for="replyTo-current"><spring:message code="label.sender.replyTo.current"/></label>
				</span>
				<span class="input-group-addon">
					<label style="margin: 0;" for="replyTo-user"><spring:message code="label.sender.replyTo.user"/></label>
				</span>
				<spring:message code="hint.user.list" var="placeholder"/>
				<input type="text" class="form-control" id="replyTo-user" value="${senderBean.replyToUsers}" placeholder="${placeholder}"/>
				<span class="input-group-addon">
					<label style="margin: 0;" for="replyTo-email"><spring:message code="label.sender.replyTo.email"/></label>
				</span>
				<spring:message code="hint.email.list" var="placeholder"/>
				<input type="text" class="form-control" id="replyTo-email" value="${senderBean.replyToEmails}" placeholder="${placeholder}"/>
			</div>
		</div>
	</div>
	<div class="form-group row">
		<label class="control-label col-sm-2"><spring:message code="label.sender.recipients"/>:</label>
		<div class="col-sm-10">
			<div id="recipient-add" class="input-group" style="margin-bottom: 5px;">
				<spring:message code="hint.group.expression" var="placeholder"/>
				<input class="form-control" type="text" placeholder="${placeholder}"/>
				<span class="input-group-btn"><button type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus"></span></button></span>
			</div>
			<div id="recipient-container">
			</div>
		</div>
	</div>
	<div class="form-group row">
		<label class="control-label col-sm-2"><spring:message code="label.sender.policy"/>:</label>
		<div class="col-sm-10">
			<div class="input-group form-inline">
				<span class="input-group-addon" style="text-align: left;">
					<input type="radio" name="policy" id="policy-unlimited" value="-1" ${senderBean.unlimitedPolicy ? "checked" : ""}>&nbsp;
					<label style="margin: 0;" for="policy-unlimited"><spring:message code="label.sender.policy.unlimited"/></label>
				</span>
				<span class="input-group-addon">
					<input type="radio" name="policy" id="policy-period" value="" ${senderBean.periodPolicy.isEmpty() ? "" : "checked"}>&nbsp;
					<label style="margin: 0;" for="policy-period"><spring:message code="label.sender.policy.period"/></label>
				</span>
				<spring:message code="hint.period" var="placeholder"/>
				<input id="policy-period-value" type="text" class="form-control" value="${senderBean.periodPolicy.isEmpty() ? '' : senderBean.periodPolicy.substring(1)}" placeholder="${placeholder}"/>
				<span class="input-group-addon">
					<input type="radio" name="policy" id="policy-amount" value="" ${senderBean.amountPolicy >= 0 ? "checked" : ""}>&nbsp;
					<label style="margin: 0;" for="policy-amount"><spring:message code="label.sender.policy.amount"/></label>
				</span>
				<input id="policy-amount-value" type="number" min="0" class="form-control" value="${senderBean.amountPolicy >= 0 ? senderBean.amountPolicy : 0}"/>
			</div>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2"><spring:message code="label.sender.html"/>:</label>
		<div class="col-sm-10">
			<div class="btn-group btn-group-xs" data-toggle="buttons">
				<label class="html-switch btn btn-${senderBean.htmlSender ? 'primary active' : 'default'}">
					<input type="radio" name="htmlSender" id="yes" value="true" ${senderBean.htmlSender ? 'checked' : ''}> Yes
				</label>
				<label class="html-switch btn btn-${senderBean.htmlSender  ? 'default' : 'primary active'}">
					<input type="radio" name="htmlSender" id="no" value="false" ${senderBean.htmlSender ? '' : 'checked'}> No
				</label>
			</div>
		</div>
	</div>
	<div class="form-group">
		<div class="col-sm-offset-2 col-sm-10">
			<button class="btn btn-primary" type="submit"><spring:message code="action.${create ? 'create' : 'configure'}"/></button>
		</div>
	</div>
</form:form>
<script>
(function(){
	//html sender switch
	var switches = $('label.html-switch');
	switches.click(function(event){
		if(!$(event.target).hasClass('active')){
			switches.toggleClass('btn-primary btn-default active');
		}
	});

	//message deletion policy choice
	var unlimitedPolicyRadio = $('#policy-unlimited'),
		periodPolicyRadio = $('#policy-period'),
		amountPolicyRadio = $('#policy-amount'),
		periodPolicy = $('#policy-period-value'),
		amountPolicy = $('#policy-amount-value'),
		updatePeriodPolicy = function(){
			if(periodPolicyRadio.is(':checked')){
				periodPolicyRadio.val("P"+periodPolicy.val());
			}
		},
		updateAmountPolicy = function(){
			if(amountPolicyRadio.is(':checked')){
				amountPolicyRadio.val("M"+amountPolicy.val());
			}
		},
		periodRequire = function(status){
			periodPolicy.prop('required',status);
		};
	unlimitedPolicyRadio.change(periodRequire.bind(undefined, false))
	periodPolicyRadio.change(updatePeriodPolicy);
	periodPolicyRadio.change(periodRequire.bind(undefined, true));
	periodPolicy.change(updatePeriodPolicy).change();
	amountPolicyRadio.change(updateAmountPolicy);
	amountPolicyRadio.change(periodRequire.bind(undefined, false));
	amountPolicy.change(updateAmountPolicy).change();

	//reply to list
	var replyTo = $('#replyTos'),
		elements = [$('#replyTo-current'), $('#replyTo-user'), $('#replyTo-email')],
		updateReplyTos = function(){
			replyTo.val(elements.map(function(e){ return $(e).val()}).filter(function(v){return v;}).join());
		};
	elements[0].change(function(){
		if(elements[0].is(':checked')){
			elements[0].val("-1");
		} else {
			elements[0].val("");
		}
	}).change();
	for(var idx = elements.length; idx--;){
		elements[idx].change(updateReplyTos).change();
	}

	//recipients list
	var recipientAdder = $('#recipient-add'),
		recipientContainer =  $('#recipient-container'),
		input = recipientAdder.find('input'),
		button = recipientAdder.find('button'),
		addRecipient = function(expression) {
			var groupBtnEl = $('<span class="input-group-btn"></span>'),
				removeBtnEl = $('<button class="btn btn-danger" type="button"></button>'),
				iconEl = $('<span class="glyphicon glyphicon-remove"></span>'),
				inputEl = $('<input class="form-control" type="text" name="recipients" value="'+expression+'"/>'),
				inputGroupEl = $('<div class="input-group"></div>'),
				groupEl = $('<div class="col-sm-4" style="margin-bottom: 5px;"></div>');
			removeBtnEl.append(iconEl);
			groupBtnEl.append(removeBtnEl);
			inputGroupEl.append(inputEl).append(groupBtnEl);
			groupEl.append(inputGroupEl);
			removeBtnEl.click(function(){
				groupEl.remove();
			});
			recipientContainer.append(groupEl);
		};
	input.keypress(function(event){
		if (event.keyCode == 13) {
			event.preventDefault();
			button.click();
		}
	});
	button.click(function () {
		var value = input.val();
		value && addRecipient(value);
	});

	[
	<c:forEach var="recipient" items="${senderBean.recipients}" varStatus="loop">"${recipient}"<c:if test="${!loop.last}">,</c:if></c:forEach>
	].forEach(addRecipient);
})();
</script>