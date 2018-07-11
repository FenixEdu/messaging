<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="org.fenixedu.messaging.tags.sorter" prefix="sort" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>


<c:choose>
<c:when test="${configure}">
<h2><spring:message code="title.senders.config"/></h2>
<%@ include file="viewSender.jsp" %>
</c:when>
<c:otherwise>
<h2><spring:message code="title.senders"/></h2>
</c:otherwise>
</c:choose>

<c:if test="${configure}">
<h3><spring:message code="title.senders.others"/></h3>
<a class="btn btn-primary" href="${pageContext.request.contextPath}/messaging/config/senders/new">
	<spring:message code="action.new.sender"/>
</a>
</c:if>

<c:if test="${empty senders}">
    <h4><spring:message code="title.senders.empty"/></h4>
</c:if>

<c:if test="${not empty senders}">
<div class="input-group input-group-sm" style="margin-top: 20px; margin-bottom: 5px;">
	<spring:message code="hint.sender.search" var="placeholder"/>
	<input id="filter-select" class="form-control" style="text-align: left; width: 400px; padding: 0 5px;" type="text" value="${search}" placeholder="${placeholder}"/>
</div>
<table class="table table-hover table-condensed">
	<thead>
		<tr>
			<th><spring:message code="label.sender.name"/></th>
			<th><spring:message code="label.sender.lastMessageSent"/></th>
			<th></th>
		</tr>
	</thead>
	<tbody>
	<c:forEach items="${senders}" var="sender">
		<tr>
			<td class="col-sm-5">
				<c:out value="${sender.name}"/>
			</td>
			<td class="col-sm-4">
				<code><joda:format value="${sender.getLastMessageSentDate()}" style="LM" /></code>
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
						<spring:message code="action.new.message"/>
					</a>
					</c:otherwise>
					</c:choose>
					<a class="btn btn-default" href="${pageContext.request.contextPath}/${path}/${sender.externalId}">
						<spring:message code="action.view"/>
					</a>
				</div>
			</td>
		</tr>
	</c:forEach>
	</tbody>
</table>
<%@ include file="pagination.jsp" %>
</c:if>
