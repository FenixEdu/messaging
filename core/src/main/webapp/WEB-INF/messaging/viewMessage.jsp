<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<h2><spring:message code="title.message"/></h2>

<c:if test="${justCreated}">
	<p class="alert alert-success"><spring:message code="notification.message.sent"/></p>
</c:if>

<table class="table table-condensed">
	<tbody>
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.created"/>
			</th>
			<td>
				${message.created.toString("dd-MM-yyyy HH:mm:ss")}
			</td>
		</tr>
		<tr>
			<th class="col-md-2" scope="row">
				<c:choose>
					<c:when test="${not empty message.sent}">
						<spring:message code="label.message.status.sent"/>
					</c:when>
					<c:otherwise>
						<spring:message code="label.message.status"/>
					</c:otherwise>
				</c:choose>
			</th>
			<td>
				<c:choose>
					<c:when test="${not empty message.sent}">
						${message.sent.toString("dd-MM-yyyy HH:mm:ss")}
					</c:when>
					<c:when test="${not empty message.dispatchReport.startedDelivery}">
						<spring:message code="label.message.status.dispatching"/>
					</c:when>
					<c:otherwise>
						<spring:message code="label.message.status.queued"/>
					</c:otherwise>
				</c:choose>
				<c:if test="${not empty message.dispatchReport.startedDelivery}">
					<c:set var="invalid" value="${message.dispatchReport.invalidCount}"/>
					<c:set var="failed" value="${message.dispatchReport.failedCount}"/>
					<c:set var="delivered" value="${message.dispatchReport.deliveredCount}"/>
					<c:set var="total" value="${message.dispatchReport.totalCount}"/>
					<c:set var="pInvalid" value="${100 *message.dispatchReport.invalidCount/total}"/>
					<c:set var="pFailed" value="${100 *message.dispatchReport.failedCount/total}"/>
					<c:set var="pDelivered" value="${100 * message.dispatchReport.deliveredCount/total}"/>
					<div class="progress" style="margin: 0;">
						<div class="progress-bar progress-bar-danger" style="width: ${pFailed}%" data-toggle="tooltip" data-placement="bottom" title="${failed} failed messages">
							<fmt:formatNumber type="number" maxFractionDigits="1" value="${pFailed}"/>%
						</div>
						<div class="progress-bar progress-bar-warning" style="width: ${pInvalid}%" data-toggle="tooltip" data-placement="bottom" title="${invalid} invalid messages">
							<fmt:formatNumber type="number" maxFractionDigits="1" value="${pInvalid}"/>%
						</div>
						<div class="progress-bar progress-bar-success" style="width: ${pDelivered}%" data-toggle="tooltip" data-placement="bottom"  title="${delivered} delivered messages">
							<fmt:formatNumber type="number" maxFractionDigits="1" value="${pDelivered}"/>%
						</div>
					</div>
					<script type="text/javascript">
						$(".progress-bar").tooltip();
					</script>
				</c:if>
			</td>
		</tr>
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.sender.name"/>
			</th>
			<td>
				${message.sender.fromName}
			</td>
		</tr>
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.sender.address"/>
			</th>
			<td>
				<span class="label label-default">${message.sender.fromAddress}</span>
			</td>
		</tr>
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.sent.by"/>
			</th>
			<td>
				${message.user.presentationName}
			</td>
		</tr>
		<c:if test="${not empty message.replyTos}">
			<tr>
				<th class="col-md-2" scope="row">
					<spring:message code="label.message.replyTos"/>
				</th>
				<td>
					<c:forEach items="${message.replyTos}" var="replyTo">
						<span class="label label-default">${replyTo}</span>
					</c:forEach>
				</td>
			</tr>
		</c:if>
		<c:if test="${not empty message.toGroup}">
			<tr>
				<th class="col-md-2" scope="row">
					<spring:message code="label.message.tos"/>
				</th>
				<td>
					<c:forEach items="${message.toGroup}" var="to">
						<span class="label label-default">${to.presentationName}</span>
					</c:forEach>
				</td>
			</tr>
		</c:if>
		<c:if test="${not empty message.ccGroup}">
			<tr>
				<th class="col-md-2" scope="row">
					<spring:message code="label.message.ccs"/>
				</th>
				<td>
					<c:forEach items="${message.ccGroup}" var="cc">
						<span class="label label-default">${cc.presentationName}</span>
					</c:forEach>
				</td>
			</tr>
		</c:if>
		<c:if test="${not empty message.bccGroup}">
			<tr>
				<th class="col-md-2" scope="row">
					<spring:message code="label.message.bccs"/>
				</th>
				<td>
					<c:forEach items="${message.bccGroup}" var="bcc">
						<span class="label label-default">${bcc.presentationName}</span>
					</c:forEach>
				</td>
			</tr>
		</c:if>
		<c:if test="${not empty message.extraBccs}">
			<tr>
				<th class="col-md-2" scope="row">
					<spring:message code="label.message.bccs.extra"/>
				</th>
				<td>
					<c:forEach items="${message.extraBccs}" var="bcc">
						<span class="label label-default">${bcc}</span>
					</c:forEach>
				</td>
			</tr>
		</c:if>
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.subject"/>
			</th>
			<td>
				<c:out value="${message.subject}"/>
			</td>
		</tr>
		<c:if test="${not empty message.body}">
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.body"/>
			</th>
			<td>
				<div class="panel panel-default">
					<div style="white-space: pre-wrap;" class="panel-body"><c:out value="${message.body}"/></div>
				</div>
			</td>
		</tr>
		</c:if>
		<c:if test="${not empty message.htmlBody}">
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.body.html"/>
			</th>
			<td>
				<div class="panel panel-default">
					<div style="white-space: pre-wrap;" class="panel-body"><c:out value="${message.htmlBody}"/></div>
				</div>
			</td>
		</tr>
		</c:if>
		<c:if test="${empty message.dispatchReport}">
			<tr>
				<th></th>
				<td>
					<form action="${pageContext.request.contextPath}/messaging/message/${message.externalId}/delete" method="post">
						<button class="btn btn-danger" type="submit"><spring:message code="action.message.delete"/></button>
					</form>
				</td>
			</tr>
		</c:if>
	</tbody>
</table>