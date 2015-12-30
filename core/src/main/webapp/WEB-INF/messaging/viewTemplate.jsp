<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

${portal.toolkit()}

<h2><spring:message code="title.template"/></h2>

<c:choose>
<c:when test="${template.subject.isEmpty()}">
	<p class="alert alert-warning"><spring:message code="notification.template.subject.empty"/></p>
</c:when>
<c:when test="${template.htmlBody.isEmpty() and template.textBody.isEmpty()}">
	<p class="alert alert-warning"><spring:message code="notification.template.body.empty"/></p>
</c:when>
</c:choose>

<a class="btn btn-primary" href="${pageContext.request.contextPath}/messaging/config/templates/${template.externalId}/edit">
	<spring:message code="action.edit"/>
</a>
<table class="table table-condensed">
	<tbody>
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.template.id"/>
			</th>
			<td>
					<code>${template.id}</code>
			</td>
		</tr>
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.template.description"/>
			</th>
			<td>
				${template.description.content}
			</td>
		</tr>
		<c:if test="${not empty template.parameters}">
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.template.parameters"/>
			</th>
			<td>
				<ul class="list-unstyled">
				<c:forEach items="${template.parameters}" var="entry">
					<li><em>${entry.key}</em>: ${entry.value.content}</li>
				</c:forEach>
				</ul>
			</td>
		</tr>
		</c:if>
		<c:if test="${not template.subject.isEmpty() or not template.htmlBody.isEmpty() or not template.textBody.isEmpty()}">
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.locale"/>
			</th>
			<td>
				<ul class="nav nav-pills">
				<c:forEach items="${templateLocales}" var="locale">
					<li><a class="btn-sm localized" id="locale-${locale}">${locale.getDisplayName(locale)}</a></li>
				</c:forEach>
				</ul>
			</td>
		</tr>
		</c:if>
		<c:if test="${not template.subject.isEmpty()}">
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.subject"/>
			</th>
			<td>
			<c:forEach items="${templateLocales}" var="locale">
				<div class="panel panel-default localized locale-${locale}" style="margin:0;">
					<div style="white-space: pre-wrap;" class="panel-heading"><c:out value="${template.subject.getContent(locale)}" default="${template.subject.getContent()}"/></div>
				</div>
			</c:forEach>
			</td>
		</tr>
		</c:if>
		<c:if test="${not template.textBody.isEmpty()}">
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.body"/>
			</th>
			<td>
			<c:forEach items="${templateLocales}" var="locale">
				<div class="panel panel-default localized locale-${locale}" style="margin:0;">
					<div style="white-space: pre-wrap;" class="panel-heading"><c:out value="${template.textBody.getContent(locale)}" default="${template.textBody.getContent()}"/></div>
				</div>
			</c:forEach>
			</td>
		</tr>
		</c:if>
		<c:if test="${not template.htmlBody.isEmpty()}">
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.body.html"/>
			</th>
			<td>
			<c:forEach items="${templateLocales}" var="locale">
				<div class="panel panel-default localized locale-${locale}" style="margin:0;">
					<div style="white-space: pre-wrap;" class="panel-heading"><c:out value="${template.htmlBody.getContent(locale)}" default="${template.htmlBody.getContent()}" escapeXml="false"/></div>
				</div>
			</c:forEach>
			</td>
		</tr>
		</c:if>
	</tbody>
</table>
<script>
(function() {
	var pills=$("a.localized");
	var content=$("div.localized");
	function selectLocale(locale){
		pills.parent().removeClass("active");
		$("#"+locale).parent().addClass("active");
		content.hide();
		$("div."+locale).show();
	}
	pills.click(function(event){
		selectLocale($(event.target).attr('id'));
	})
	var firstLocale="<c:forEach items="${templateLocales}" var="locale" end="0">locale-<c:out value="${locale}"/></c:forEach>";
	selectLocale(firstLocale);
})();
</script>