<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<h2><spring:message code="title.senders"/></h2>
<c:if test="${not empty senders}">
	<table class="table table-hover table-condensed">
		<thead>
			<tr>
				<th>
					<spring:message code="label.bootstrapper.systemsender.name"/>
				</th>
				<th>
					<spring:message code="label.bootstrapper.systemsender.address"/>
				</th>
				<th></th>
			</tr>
		</thead>
		<tbody>
		<c:forEach items="${senders}" var="sender">
			<tr onClick="location.href='${pageContext.request.contextPath}/messaging/sender/${sender.externalId}'">
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
						<a class="btn btn-default" href="${pageContext.request.contextPath}/messaging/sender/${sender.externalId}">
							<spring:message code="action.view.details"/>
						</a>
					</div>
				</td>
			</tr>
		</c:forEach>
		</tbody>
		<c:if test="${pages>1}">
			<tfoot>
				<tr>
					<td colspan="4" style="text-align: center;">
						<nav style="display:inline-block;">
							<ul class="pagination">
								<c:if test="${page == 1}"><li class="disabled"></c:if>
								<c:if test="${page > 1}"><li></c:if>
									<a href="${pageContext.request.contextPath}/messaging?page=${page-1}&items=${items}"><span>&laquo;</span></a>
								</li>
								<c:forEach begin="1" end="${pages}" var="p" >
									<c:if test="${p == page}"><li class="active"></c:if>
									<c:if test="${p != page}"><li></c:if>
								<a href="${pageContext.request.contextPath}/messaging?page=${p}&items=${items}">${p}</a></li>
								</c:forEach>
								<c:if test="${page == pages}"><li class="disabled"></c:if>
								<c:if test="${page < pages}"><li></c:if>
									<a href="${pageContext.request.contextPath}/messaging?page=${page+1}&items=${items}"><span>&raquo;</span></a>
								</li>
							</ul>
						</nav>
					</td>
				</tr>
			</tfoot>
		</c:if>
	</table>
</c:if>