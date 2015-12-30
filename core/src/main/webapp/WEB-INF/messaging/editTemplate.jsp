<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

${portal.toolkit()}

<h2><spring:message code="title.template.edit"/></h2>

<c:if test="${not empty templateBean.errors}">
<div class="alert alert-danger">
	<span><spring:message code="error.template.not.saved"/></span>
	<c:forEach items="${templateBean.errors}" var="error">
		<br/><span style="padding-left: 2em;">${error}</span>
	</c:forEach>
</div>
</c:if>

<form:form modelAttribute="templateBean" role="form" class="form-horizontal" action="${pageContext.request.contextPath}/messaging/config/templates/${template.externalId}/edit" method="post">
	<div class="collapse form-group template-info">
		<label class="control-label col-sm-2"><spring:message code="label.template.id"/></label>
		<div class="col-sm-10" style="padding-top: 7px;">
			<code>${template.id}</code>
		</div>
	</div>
	<div class="collapse form-group template-info">
		<label class="control-label col-sm-2"><spring:message code="label.template.description"/></label>
		<div class="col-sm-10" style="padding-top: 7px;">
			${template.description.content}
		</div>
	</div>
	<c:if test="${not empty template.parameters}">
	<div class="collapse form-group template-info">
		<label class="control-label col-sm-2"><spring:message code="label.template.parameters"/></label>
		<div class="col-sm-10">
			<ul class="list-unstyled">
			<c:forEach items="${template.parameters}" var="entry">
				<li><em>${entry.key}</em>: ${entry.value.content}</li>
			</c:forEach>
			</ul>
		</div>
	</div>
	</c:if>
	<div class="form-group">
		<label class="control-label col-sm-2" for="subject"><spring:message code="label.message.subject"/>:</label>
		<div class="col-sm-10">
			<textarea class="form-control" id="subject" name="subject" bennu-localized-string>${templateBean.subject.json()}</textarea>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for="textBody"><spring:message code="label.message.body"/>:</label>
		<div class="col-sm-10">
			<textarea class="form-control" id="textBody" name="textBody" bennu-localized-string>${templateBean.textBody.json()}</textarea>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for="htmlBody"><spring:message code="label.message.body.html"/>:</label>
		<div class="col-sm-10">
			<textarea class="form-control" id="htmlBody" name="htmlBody" bennu-html-editor bennu-localized-string/>${templateBean.htmlBody.json()}</textarea>
		</div>
	</div>
	<div class="form-group">
		<div class="col-sm-offset-2 col-sm-10 btn-group">
				<button class="btn btn-primary" type="submit"><spring:message code="action.save"/></button>
				<div class="btn-group">
					<button class="btn btn-default dropdown-toggle" type="button" data-toggle="dropdown">
						<spring:message code="label.actions"/>
						<span class="caret"></span>
					</button>
					<ul class="dropdown-menu dropdown-menu-right">
						<li><a href="reset"><spring:message code="action.reset.template"/></a></li>
						<li id="showInfo"><a data-toggle="collapse" href=".template-info"><spring:message code="action.show.template.info"/></a></li>
						<li id="hideInfo"><a data-toggle="collapse" href=".template-info"><spring:message code="action.hide.template.info"/></a></li>
					</ul>
				</div>
			</div>
		</div>
	</div>
</form:form>
<script>
(function(){
	var show = $('#showInfo'),
		hide = $('#hideInfo');
	show.click(function() {
		hide.show();
		show.hide();
	});
	hide.click(function() {
		hide.hide();
		show.show();
	});
	hide.hide();
})();
</script>