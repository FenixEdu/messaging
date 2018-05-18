<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="org.fenixedu.messaging.tags.sorter" prefix="sort" %>


<c:if test="${not empty optInRequiredSenders}">
<div class="col-sm-6">
    <h3><spring:message code="title.messaging.optInConfig.optInRequiredSenders"/></h3>
    <table class="table table-hover table-condensed">
        <thead>
        <tr>
            <th><spring:message code="label.sender.name"/></th>
            <th><spring:message code="label.sender.optInStatus"/></th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${optInRequiredSenders}" var="sender">
        <tr>
            <td class="col-sm-8"><c:out value="${sender.key.name}"/></td>
            <td class="col-sm-2">
                <c:choose>
                <c:when test="${sender.value}">
                    <spring:message code="label.yes"/> (<a href="${pageContext.request.contextPath}/messaging/optInConfig/optOut/${sender.key.externalId}"><spring:message code="action.remove"/></a>)
                </c:when>
                <c:otherwise>
                    <spring:message code="label.no"/> (<a href="${pageContext.request.contextPath}/messaging/optInConfig/optIn/${sender.key.externalId}"><spring:message code="action.add"/></a>)
                </c:otherwise>
                </c:choose>
            </td>
        </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
</c:if>

