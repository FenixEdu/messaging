<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="org.fenixedu.messaging.tags.sorter" prefix="sort" %>

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
	<c:forEach items="${sort:uniqueSort(templates)}" var="template">
		<c:set var="rowClass" value="warning"/>
		<c:set var="toggle" value="tooltip"/>
		<c:set var="tooltip" value=""/>
		<c:choose>
		<c:when test="${not template.declared}">
			<c:set var="rowClass" value="danger"/>
			<spring:message code="error.template.undeclared" var="tooltip"/>
		</c:when>
		<c:when test="${template.subject.isEmpty() and template.textBody.isEmpty() and template.htmlBody.isEmpty()}">
			<spring:message code="notification.template.empty" var="tooltip"/>
		</c:when>
		<c:when test="${template.subject.isEmpty()}">
			<spring:message code="notification.template.empty.subject" var="tooltip"/>
		</c:when>
		<c:when test="${template.textBody.isEmpty() and template.htmlBody.isEmpty()}">
			<spring:message code="notification.template.empty.body" var="tooltip"/>
		</c:when>
		<c:otherwise>
			<c:set var="rowClass" value=""/>
			<c:set var="toggle" value=""/>
		</c:otherwise>
		</c:choose>
		<tr class="${rowClass}" data-toggle="${toggle}" data-placement="left" title="${tooltip}">
			<td class="col-sm-2">
				<code><c:out value="${template.id}"/></code>
			</td>
			<td class="col-sm-7">
				<c:out value="${template.declaration.description.content}"/>
			</td>
			<td class="col-sm-3">
				<div class="btn-group btn-group-xs pull-right">
					<a class="btn btn-primary" href="${pageContext.request.contextPath}/messaging/config/templates/${template.externalId}/edit">
						<spring:message code="action.edit"/>
					</a>
					<a class="btn btn-default" href="${pageContext.request.contextPath}/messaging/config/templates/${template.externalId}">
						<spring:message code="action.view"/>
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
