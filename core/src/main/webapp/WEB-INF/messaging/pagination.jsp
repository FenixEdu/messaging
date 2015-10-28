<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<nav style="display:flex; align-items: center;">
	<div class="form-inline col-xs-6">
		<c:if test="${pages>1}">
		<div class="input-group input-group-sm">
			<span class="input-group-btn">
				<a class="btn btn-info ${page <= 1 ? 'disabled' : ''}" href="${pageContext.request.contextPath}/${path}?page=${page-1}&items=${items}">&laquo;</a>
			</span>
			<span class="input-group-addon" style="padding: 0 5px;"><spring:message code="label.page"/></span>
			<input id="page-select" class="form-control" style="text-align: right; width: 55px; padding: 0 5px;" type="number" min="1" max="${pages}" value="${page}"/>
			<span class="input-group-addon" style="padding: 0 5px;"><spring:message code="label.of"/> ${pages}</span>
			<span class="input-group-btn">
				<a class="btn btn-info ${page == pages ? 'disabled' : ''}" href="${pageContext.request.contextPath}/${path}?page=${page+1}&items=${items}">&raquo;</a>
			</span>
		</div>
		</c:if>
	</div>
	<div class="col-xs-6">
		<div class="input-group input-group-sm pull-right" style="width: 100px;">
			<span class="input-group-addon" style="padding: 0 5px;"><spring:message code="label.show"/></span>
			<select id="items-select" class="form-control" style="padding: 0 5px; text-align: center;"></select>
		</div>
	</div>
</nav>
<script>
(function(){
	var itemsSelect = $('#items-select'),
		pageSelect = $('#page-select');
		[${items},10,20,50,100].filter(function(val,idx,arr){
			return idx === arr.indexOf(val);
		}).forEach(function(val){
			itemsSelect.append('<option value="'+val+'">'+val+'</option>')
		});
	itemsSelect.change(function(){
		window.location = "${pageContext.request.contextPath}/${path}?page=${page}&items=" + itemsSelect.val();
	});
	itemsSelect.find("option[value='${items}']").attr("selected","selected");
	pageSelect.keypress(function(e){
		if (e.keyCode == 13) {
			e.preventDefault();
			window.location = "${pageContext.request.contextPath}/${path}?page=" + pageSelect.val() + "&items=${items}";
		}
	});
})();
</script>