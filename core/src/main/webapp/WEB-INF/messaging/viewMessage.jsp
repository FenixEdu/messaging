<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="org.fenixedu.messaging.tags.sorter" prefix="sort" %>

${portal.toolkit()}

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
				${message.sender.name}
			</td>
		</tr>
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.sender.address"/>
			</th>
			<td>
				<code>${message.sender.address}</code>
			</td>
		</tr>
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.sent.by"/>
			</th>
			<td>
				${message.user.profile.displayName}
			</td>
		</tr>
		<c:if test="${not empty message.replyTo}">
			<tr>
				<th class="col-md-2" scope="row">
					<spring:message code="label.message.replyTo"/>
				</th>
				<td>
					<code>${message.replyTo}</code>
				</td>
			</tr>
		</c:if>
		<c:if test="${not empty message.toGroups}">
			<tr>
				<th class="col-md-2" scope="row">
					<spring:message code="label.message.tos"/>
				</th>
				<td>
					<div style="overflow-y:auto; max-height:85px; display:block;">
					<c:forEach items="${sort:uniqueSort(message.toGroups)}" var="to">
						<code style="display: inline-block; margin: 2px;">${to.presentationName}</code>
					</c:forEach>
					</div>
				</td>
			</tr>
		</c:if>
		<c:if test="${not empty message.ccGroups}">
			<tr>
				<th class="col-md-2" scope="row">
					<spring:message code="label.message.ccs"/>
				</th>
				<td>
					<div style="overflow-y:auto; max-height:85px; display:block;">
					<c:forEach items="${sort:uniqueSort(message.ccGroups)}" var="cc">
						<code style="display: inline-block; margin: 2px;">${cc.presentationName}</code>
					</c:forEach>
					</div>
				</td>
			</tr>
		</c:if>
		<c:if test="${not empty message.bccGroups}">
			<tr>
				<th class="col-md-2" scope="row">
					<spring:message code="label.message.recipients"/>
				</th>
				<td>
					<div style="overflow-y:auto; max-height:85px; display:block;">
					<c:forEach items="${sort:uniqueSort(message.bccGroups)}" var="bcc">
						<code style="display: inline-block; margin: 2px;">${bcc.presentationName}</code>
					</c:forEach>
					</div>
				</td>
			</tr>
		</c:if>
		<c:if test="${not empty message.singleBccsSet}">
			<tr>
				<th class="col-md-2" scope="row">
					<spring:message code="label.message.recipients.single"/>
				</th>
				<td>
				<c:forEach items="${sort:uniqueSort(message.singleBccsSet)}" var="bcc">
					<code>${bcc}</code>
				</c:forEach>
				</td>
			</tr>
		</c:if>
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.locale.preferred"/>
			</th>
			<td>
				${message.preferredLocale.getDisplayName(message.preferredLocale)}
			</td>
		</tr>
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.locale"/>
			</th>
			<td>
				<ul class="nav nav-pills">
				<c:forEach items="${locales}" var="locale">
					<li><a class="btn-sm localized" id="locale-${locale}">${locale.getDisplayName(locale)}</a></li>
				</c:forEach>
				</ul>
			</td>
		</tr>
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.subject"/>
			</th>
			<td>
			<c:forEach items="${locales}" var="locale">
				<div class="panel panel-default localized locale-${locale}" style="margin:0;">
					<div style="white-space: pre-wrap;" class="panel-heading"><c:out value="${message.subject.getContent(locale)}" default="${message.subject.getContent()}"/></div>
				</div>
			</c:forEach>
			</td>
		</tr>
		<c:if test="${not message.textBody.isEmpty()}">
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.body.text"/>
			</th>
			<td>
			<c:forEach items="${locales}" var="locale">
				<div class="panel panel-default localized locale-${locale}" style="margin:0;">
					<div style="white-space: pre-wrap;" class="panel-heading"><c:out value="${message.textBody.getContent(locale)}" default="${message.textBody.getContent()}"/></div>
				</div>
			</c:forEach>
			</td>
		</tr>
		</c:if>
		<c:if test="${not message.htmlBody.isEmpty()}">
		<tr>
			<th class="col-md-2" scope="row">
				<spring:message code="label.message.body.html"/>
			</th>
			<td>
			<c:forEach items="${locales}" var="locale">
				<div class="panel panel-default localized locale-${locale}" style="margin:0;">
					<div style="white-space: pre-wrap;" class="panel-heading"><c:out value="${message.htmlBody.getContent(locale)}" default="${message.htmlBody.getContent()}" escapeXml="false"/></div>
				</div>
			</c:forEach>
			</td>
		</tr>
		</c:if>
		<c:if test="${deletable}">
		<tr>
			<th></th>
			<td>
				<form action="${pageContext.request.contextPath}/messaging/messages/${message.externalId}/delete" method="post">
					${csrf.field()}
					<button class="btn btn-danger" type="submit"><spring:message code="action.delete"/></button>
				</form>
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
	selectLocale("locale-${pageContext.response.locale}");
})();
</script>