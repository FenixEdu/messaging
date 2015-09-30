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
					<spring:message code="label.template.name"/>
				</th>
				<th>
					<spring:message code="label.template.description"/>
				</th>
				<th></th>
			</tr>
		</thead>
		<tbody>
		<c:forEach items="${templates}" var="template">
			<tr class="${(template.subject.isEmpty() or (template.body.isEmpty() and template.htmlBody.isEmpty())) ? 'warning' : 'default' }" onClick="location.href='${pageContext.request.contextPath}/messaging/config/templates/${template.code}'">
				<td class="col-sm-2">
					<code>${template.id}</code>
				</td>
				<td class="col-sm-3">
					${template.name.content}
				</td>
				<td class="col-sm-4">
					${template.description.content}
				</td>
				<td class="col-sm-3">
					<div class="btn-group btn-group-xs pull-right">
						<a class="btn btn-primary" href="${pageContext.request.contextPath}/messaging/config/templates/${template.code}/edit">
							<spring:message code="action.edit"/>
						</a>
						<a class="btn btn-default" href="${pageContext.request.contextPath}/messaging/config/templates/${template.code}">
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
</c:if>