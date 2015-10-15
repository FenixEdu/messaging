<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<h2><spring:message code="title.templates.config"/></h2>
<c:if test="${not empty templates}">
<table class="table table-hover table-condensed">
	<thead>
		<tr>
			<th>
				<spring:message code="label.template.id"/>
			</th>
			<th>
				<spring:message code="label.template.description"/>
			</th>
			<th></th>
		</tr>
	</thead>
	<tbody>
	<c:forEach items="${templates}" var="template">
		<c:choose>
		<c:when test="${empty template.description}">
		<spring:message code="error.template.undeclared" var="tooltip"/>
		<tr class="danger" data-toggle="tooltip" data-placement="left" title="${tooltip}"
		</c:when>
		<c:when test="${template.error}">
		<spring:message code="notification.template.empty" var="tooltip"/>
		<tr class="warning" data-toggle="tooltip" data-placement="left" title="${tooltip}"
		</c:when>
		<c:otherwise><tr</c:otherwise>
		</c:choose>
			onClick="location.href='${pageContext.request.contextPath}/messaging/config/templates/${template.externalId}'">
			<td class="col-sm-2">
				<code>${template.id}</code>
			</td>
			<td class="col-sm-7">
				${template.description.content}
			</td>
			<td class="col-sm-3">
				<div class="btn-group btn-group-xs pull-right">
					<a class="btn btn-primary" href="${pageContext.request.contextPath}/messaging/config/templates/${template.externalId}/edit">
						<spring:message code="action.template.edit"/>
					</a>
					<a class="btn btn-default" href="${pageContext.request.contextPath}/messaging/config/templates/${template.externalId}">
						<spring:message code="action.view.details"/>
					</a>
				</div>
			</td>
		</tr>
	</c:forEach>
	</tbody>
	<tfoot>
		<tr>
			<td colspan="4">
				<%@ include file="pagination.jsp" %>
			</td>
		</tr>
	</tfoot>
</table>
<script>
(function () {
  $('[data-toggle="tooltip"]').tooltip()
})();
</script>
</c:if>