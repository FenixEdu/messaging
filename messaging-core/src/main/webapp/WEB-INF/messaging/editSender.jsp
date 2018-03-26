<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="org.fenixedu.messaging.tags.sorter" prefix="sort" %>

<h2><spring:message code="title.sender.edit${ create? '.new' : '' }"/></h2>

<c:if test="${not empty senderBean.errors}">
	<div class="alert alert-danger">
		<span><spring:message code="error.sender.not.saved"/></span>
		<c:forEach items="${sort:uniqueSort(senderBean.errors)}" var="error">
			<br/><span style="padding-left: 2em;">${error}</span>
		</c:forEach>
	</div>
</c:if>

<form:form modelAttribute="senderBean" role="form" class="form-horizontal" method="post">
	${csrf.field()}
	<div class="form-group">
		<label class="control-label col-sm-2" for="name"><spring:message code="label.sender.name"/>:</label>
		<div class="col-sm-10">
			<form:input type="text" class="form-control" id="name" path="name" value="${senderBean.name}" required="required"/>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for="address"><spring:message code="label.sender.address"/>:</label>
		<div class="col-sm-10">
			<form:input type="email" class="form-control" id="address" path="address" value="${senderBean.address}" required="required"/>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for="members"><spring:message code="label.sender.members"/>:</label>
		<div class="col-sm-10">
			<form:input type="text" class="form-control" id="members" path="members" value="${senderBean.members}" required="required"/>
		</div>
	</div>
	<div class="form-group row">
		<label class="control-label col-sm-2"><spring:message code="label.sender.replyTo"/>:</label>
		<div class="col-sm-10">
			<spring:message code="hint.email" var="placeholder"/>
			<form:input type="email" class="form-control" id="replyTo" path="replyTo" value="${senderBean.replyTo}" placeholder="${placeholder}"/>
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
					<input type="checkbox" name="policy" id="policy-all" value="A"
					${senderBean.allPolicy ? "checked" : ""}>&nbsp;
					<label style="margin: 0;" for="policy-all"><spring:message code="label.sender.policy.all"/></label>
				</span>
				<span class="input-group-addon">
					<input type="checkbox" name="policy" id="policy-period" value="${senderBean.periodPolicy}" ${senderBean.periodPolicy.isEmpty() ? "" : "checked"}>&nbsp;
					<label style="margin: 0;" for="policy-period"><spring:message code="label.sender.policy.period"/></label>
				</span>
				<spring:message code="hint.period" var="placeholder"/>
				<input id="policy-period-value" type="text" pattern="(\d+Y)?(\d+M)?(\d+W)?(\d+D)?(T(?=.)(\d+H)?(\d+M)?(\d+S)?)?" class="form-control" value="${senderBean.periodPolicy}" placeholder="${placeholder}"/>
				<span class="input-group-addon">
					<input type="checkbox" name="policy" id="policy-amount" value="${senderBean.amountPolicy > 0 ? 'Q'.concat(senderBean.amountPolicy) : ''}" ${senderBean.amountPolicy > 0 ? "checked" : ""}>&nbsp;
					<label style="margin: 0;" for="policy-amount"><spring:message code="label.sender.policy.amount"/></label>
				</span>
				<input id="policy-amount-value" type="number" min="1" class="form-control" value="${senderBean.amountPolicy > 0 ? senderBean.amountPolicy : ''}"/>
				<span class="input-group-addon" style="text-align: left;">
					<input type="checkbox" name="policy" id="policy-none" value="N"
						${senderBean.nonePolicy ? "checked" : ""}>&nbsp;
					<label style="margin: 0;" for="policy-none"><spring:message code="label.sender.policy.none"/></label>
				</span>
			</div>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2"><spring:message code="label.sender.html"/>:</label>
		<div class="col-sm-10">
			<div class="btn-group btn-group-xs" data-toggle="buttons">
				<label class="html-switch btn btn-${senderBean.htmlEnabled ? 'primary active' : 'default'}">
					<input type="radio" name="htmlEnabled" id="yes" value="true" ${senderBean.htmlEnabled ? 'checked' : ''}>
					<spring:message code="label.on"/>
				</label>
				<label class="html-switch btn btn-${senderBean.htmlEnabled  ? 'default' : 'primary active'}">
					<input type="radio" name="htmlEnabled" id="no" value="false" ${senderBean.htmlEnabled ? '' : 'checked'}>
					<spring:message code="label.off"/>
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

	//message storage policy choice
	var allPolicy = $('#policy-all'),
		nonePolicy = $('#policy-none'),
		periodPolicy = $('#policy-period').attr('aux-data-mark','P'),
		amountPolicy = $('#policy-amount').attr('aux-data-mark','Q'),
		policyParts = $([periodPolicy, amountPolicy]).map(function () {return this.toArray();});

	var getCheckbox = function (value) {
			return $('#'+value.attr('id').match(/(\w+\-\w+)\-value/)[1]);
	},	getValue = function (checkbox) {
			return $('#'+checkbox.attr('id')+'-value');
	},	policyPartValues = policyParts.map(function(){ return getValue($(this)).toArray();});

	function allUnchecked(){
		return !allPolicy.is(':checked') && !nonePolicy.is(':checked') &&
				policyParts.filter(function(){return $(this).is(':checked');}).first().get().length === 0;
	}

	allPolicy.change(function() {
		var checked = $(this).is(':checked');
		if(checked) {
			nonePolicy.prop('checked', false);
			policyParts.prop('checked', false);
			policyPartValues.prop('required', false).prop('disabled', true);
		} else if(allUnchecked()) {
			nonePolicy.prop('checked', true);
		}
	});
	nonePolicy.change(function() {
		var checked = $(this).is(':checked');
		if(checked) {
			allPolicy.prop('checked', false);
			policyParts.prop('checked', false);
			policyPartValues.prop('required', false).prop('disabled', true);
		} else if(allUnchecked()) {
			allPolicy.prop('checked', true);
		}
	});
	policyParts.change(function(){
		if(allUnchecked()) {
			allPolicy.prop('checked', true);
		} else {
			allPolicy.prop('checked', false);
			nonePolicy.prop('checked', false);
		}
		var checkbox = $(this), checked = checkbox.is(':checked');
		getValue(checkbox).prop('required', checked).prop('disabled', !checked);
	}).change();
	policyPartValues.change(function() {
		var value = $(this), checkbox = getCheckbox(value);
		checkbox.val(checkbox.attr('aux-data-mark')+value.val());
	}).change();

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
		if (event.keyCode === 13) {
			event.preventDefault();
			button.click();
		}
	});
	button.click(function () {
		var value = input.val();
		value && addRecipient(value);
	});

	[
	<c:forEach var="recipient" items="${sort:uniqueSort(senderBean.recipients)}" varStatus="loop">"${recipient}"<c:if test="${!loop.last}">,</c:if></c:forEach>
	].forEach(addRecipient);
})();
</script>