<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

${portal.toolkit()}

<h2><spring:message code="title.template"/></h2>

<c:choose>
<c:when test="${template.subject.isEmpty() and template.htmlBody.isEmpty() and template.body.isEmpty()}">
	<p class="alert alert-warning"><spring:message code="notification.template.empty"/></p>
</c:when>
<c:when test="${template.subject.isEmpty()}">
	<p class="alert alert-warning"><spring:message code="notification.template.subject.empty"/></p>
</c:when>
<c:when test="${template.htmlBody.isEmpty() and template.body.isEmpty()}">
	<p class="alert alert-warning"><spring:message code="notification.template.message.empty"/></p>
</c:when>
</c:choose>

<a class="btn btn-primary" href="${pageContext.request.contextPath}/messaging/templates/${template.code}/edit">
	<spring:message code="action.template.edit"/>
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
				<spring:message code="label.template.name"/>
			</th>
			<td>
				${template.name.content}
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
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.template.footer"/>
			</th>
			<td>
				<c:choose>
					<c:when test="${ template.automaticFooter }">
						<spring:message code="label.yes"/>
					</c:when>
					<c:otherwise>
						<spring:message code="label.no"/>
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<c:if test="${not template.htmlBody.isEmpty() or not template.body.isEmpty() or not template.subject.isEmpty()}">
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
		<c:if test="${not template.body.isEmpty()}">
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.body"/>
			</th>
			<td>
			<c:forEach items="${templateLocales}" var="locale">
				<div class="panel panel-default localized locale-${locale}" style="margin:0;">
					<div style="white-space: pre-wrap;" class="panel-heading"><c:out value="${template.body.getContent(locale)}" default="${template.body.getContent()}"/></div>
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