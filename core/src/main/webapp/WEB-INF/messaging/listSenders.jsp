<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>


<c:choose>
	<c:when test="${configure}">
		<h2><spring:message code="title.senders.config"/></h2>
		<c:set var="sender" value="${systemSender}" scope="request" />
		<c:set var="recipients" value="${systemRecipients}" scope="request" />
		<c:set var="replyTos" value="${systemReplyTos}" scope="request" />
		<c:set var="system" value="${true}" scope="request"/>
		<%@ include file="viewSender.jsp" %>
	</c:when>
	<c:otherwise>
		<h2><spring:message code="title.senders"/></h2>
	</c:otherwise>
</c:choose>
<c:if test="${not empty senders}">
	<c:if test="${configure}">
		<h3><spring:message code="title.senders.others"/></h3>
		<a class="btn btn-primary" href="${pageContext.request.contextPath}/messaging/config/senders/new">
			<spring:message code="action.new"/>
		</a>
	</c:if>
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
			<tr onClick="location.href='${pageContext.request.contextPath}/messaging/${path}/${sender.externalId}'">
				<td class="col-sm-5">
					${sender.fromName}
				</td>
				<td class="col-sm-4">
					<code>${sender.fromAddress}</code>
				</td>
				<td class="col-sm-3">
					<div class="btn-group btn-group-xs pull-right">
						<c:choose>
						<c:when test="${configure}">
						<a class="btn btn-primary" href="${pageContext.request.contextPath}/messaging/config/senders/${sender.externalId}/edit">
							<spring:message code="action.configure"/>
						</a>
						</c:when>
						<c:otherwise>
						<a class="btn btn-primary" href="${pageContext.request.contextPath}/messaging/message?sender=${sender.externalId}">
							<spring:message code="action.message.new"/>
						</a>
						</c:otherwise>
						</c:choose>
						<a class="btn btn-default" href="${pageContext.request.contextPath}/messaging/${path}/${sender.externalId}">
							<spring:message code="action.view"/>
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