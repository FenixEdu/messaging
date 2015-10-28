<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<h2><spring:message code="title.senders"/></h2>
<c:if test="${not empty senders}">
<table class="table table-hover table-condensed">
	<thead>
		<tr>
			<th>
				<spring:message code="label.sender.name"/>
			</th>
			<th>
				<spring:message code="label.sender.address"/>
			</th>
			<th></th>
		</tr>
	</thead>
	<tbody>
	<c:forEach items="${senders}" var="sender">
		<tr onClick="location.href='${pageContext.request.contextPath}/messaging/senders/${sender.externalId}'">
			<td class="col-sm-5">
				${sender.fromName}
			</td>
			<td class="col-sm-4">
				<code>${sender.fromAddress}</code>
			</td>
			<td class="col-sm-3">
				<div class="btn-group btn-group-xs pull-right">
					<a class="btn btn-primary" href="${pageContext.request.contextPath}/messaging/message?sender=${sender.externalId}">
						<spring:message code="action.message.new"/>
					</a>
					<a class="btn btn-default" href="${pageContext.request.contextPath}/messaging/senders/${sender.externalId}">
						<spring:message code="action.view.details"/>
					</a>
				</div>
			</td>
		</tr>
	</c:forEach>
	</tbody>
	<tfoot>
		<tr>
			<td colspan="3">
				<%@ include file="pagination.jsp" %>
			</td>
		</tr>
	</tfoot>
</table>
</c:if>