<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="org.fenixedu.messaging.tags.sorter" prefix="sort" %>

<script>
    function updateOptIn(checkbox){
        $.ajax("${pageContext.request.contextPath}/messaging/subscriptions/" + (checkbox.checked ? "optIn/" : "optOut/") + checkbox.value)
    }
</script>
<div class="col-sm-5">
    <h3><spring:message code="title.messaging.optInConfig"/></h3>
    <div class="well well-sm"><spring:message code="help.optInConfig"/></div>
    <c:if test="${not empty optInRequiredSenders}">
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
                <td class="col-sm-9"><c:out value="${sender.key.name}"/></td>
                <td class="col-sm-1" style="text-align: center">
                    <input type="checkbox" onclick="updateOptIn(this)" value="<c:out value="${sender.key.externalId}"/>"  <c:out value="${sender.value ? 'checked' : ''}"/>/>
                </td>
            </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:if>
</div>

