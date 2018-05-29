<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="org.fenixedu.messaging.tags.sorter" prefix="sort" %>

<link href="${pageContext.request.contextPath}/static/lib/bootstrap-switch/css/bootstrap3/bootstrap-switch.min.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="${pageContext.request.contextPath}/static/lib/bootstrap-switch/js/bootstrap-switch.min.js"></script>

<script>
    function updateGlobalOptOut(checkbox) {
        $.ajax("${pageContext.request.contextPath}/messaging/subscriptions/" + (checkbox.checked ? "globalOptIn" : "globalOptOut"));
        checkbox.checked ? $("#optInRequiredDiv").show() : $("#optInRequiredDiv").hide();
    }

    function updateOptIn(checkbox){
        $.ajax("${pageContext.request.contextPath}/messaging/subscriptions/" + (checkbox.checked ? "optIn/" : "optOut/") + checkbox.value)
    }
</script>
<div class="col-sm-6">
    <h3><spring:message code="title.messaging.optInConfig"/></h3>
    <c:if test="${canConfigOptOut}">
        <div style="display: inline-block">
            <label class="control-label">
                <spring:message code="label.messaging.globalOptOut.question" />
            </label>
            <div class="checkbox">
                <input type="checkbox" ${not isOptedOut ? "checked" : ""}
                       data-on-text=<spring:message code="label.yes"/>
                       data-off-text=<spring:message code="label.no"/>
                       onchange="updateGlobalOptOut(this)">
            </div>
        </div>
    </c:if>
    <div id="optInRequiredDiv">
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
</div>

<script>
    if (${isOptedOut}){
        $("#optInRequiredDiv").hide();
    }
    function init() {
        $.fn.bootstrapSwitch.defaults.size = 'medium';
        $(".checkbox > input").addClass('bootstrap-switch-mini');
        $(".checkbox > input").bootstrapSwitch();
        $(".checkbox").css("display", "inline-block");
    }
    init();
</script>
