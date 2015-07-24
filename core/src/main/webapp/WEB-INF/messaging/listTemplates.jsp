<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<h2><spring:message code="title.templates"/></h2>
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
			<tr class="${(template.subject.isEmpty() or (template.body.isEmpty() and template.htmlBody.isEmpty())) ? 'warning' : 'default' }" onClick="location.href='${pageContext.request.contextPath}/messaging/templates/${template.code}'">
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
						<a class="btn btn-primary" href="${pageContext.request.contextPath}/messaging/templates/${template.code}/edit">
							<spring:message code="action.template.edit"/>
						</a>
						<a class="btn btn-default" href="${pageContext.request.contextPath}/messaging/templates/${template.code}">
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
									<a href="${pageContext.request.contextPath}/messaging/templates?page=${page-1}&items=${items}"><span>&laquo;</span></a>
								</li>
								<c:forEach begin="1" end="${pages}" var="p" >
									<c:if test="${p == page}"><li class="active"></c:if>
									<c:if test="${p != page}"><li></c:if>
								<a href="${pageContext.request.contextPath}/messaging/templates?page=${p}&items=${items}">${p}</a></li>
								</c:forEach>
								<c:if test="${page == pages}"><li class="disabled"></c:if>
								<c:if test="${page < pages}"><li></c:if>
									<a href="${pageContext.request.contextPath}/messaging/templates?page=${page+1}&items=${items}"><span>&raquo;</span></a>
								</li>
							</ul>
						</nav>
					</td>
				</tr>
			</tfoot>
		</c:if>
	</table>
</c:if>