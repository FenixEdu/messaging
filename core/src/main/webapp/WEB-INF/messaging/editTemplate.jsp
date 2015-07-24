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

<form:form modelAttribute="templateBean" role="form" class="form-horizontal" method="post">
	<div class="form-group">
		<label class="control-label col-sm-2" for="subject"><spring:message code="label.message.subject"/>:</label>
		<div class="col-sm-10">
			<input class="form-control" id="subject" name="subject" value='<c:out value="${templateBean.subject.json()}"/>' bennu-localized-string required-any/>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for="body"><spring:message code="label.message.body"/>:</label>
		<div class="col-sm-10">
			<textarea class="form-control" id="body" name="body" bennu-localized-string>${templateBean.body.json()}</textarea>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2" for="htmlBody"><spring:message code="label.message.body.html"/>:</label>
		<div class="col-sm-10">
			<textarea class="form-control" id="htmlBody" name="htmlBody" bennu-html-editor bennu-localized-string/>${templateBean.htmlBody.json()}</textarea>
		</div>
	</div>
	<div class="form-group">
		<label class="control-label col-sm-2"><spring:message code="label.template.footer"/>:</label>
		<div class="col-sm-10">
			<div class="btn-group btn-group-xs" data-toggle="buttons">
  				<label class="footer-switch btn btn-${templateBean.automaticFooter ? 'primary active' : 'default'}">
    				<input type="radio" name="automaticFooter" id="yes" value="true" ${templateBean.automaticFooter ? 'checked' : ''}> Yes
  				</label>
  				<label class="footer-switch btn btn-${templateBean.automaticFooter ? 'default' : 'primary active'}">
    				<input type="radio" name="automaticFooter" id="no" value="false" ${templateBean.automaticFooter ? '' : 'checked'}> No
    			</label>
			</div>
		</div>
	</div>
	<div class="form-group">
		<div class="col-sm-offset-2 col-sm-10">
			<button class="btn btn-primary" type="submit"><spring:message code="action.template.save"/></button>
		</div>
	</div>
</form:form>

	<script>
	(function(){
		var switches = $('label.footer-switch');
		switches.click(function(event){
			if(!$(event.target).hasClass('active')){
				switches.toggleClass('btn-primary btn-default active');
			}
		});
	})();
	</script>