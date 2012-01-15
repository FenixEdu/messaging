<%@page import="pt.ist.messaging.domain.MessagingSystem"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr" %>

<h2>
	<bean:message key="label.module.messaging" bundle="MESSAGING_RESOURCES"/>
</h2>

<br/>

<logic:present name="senders">
	<table class="tstyle2">
		<tr>
			<th>
				<bean:message key="label.messaging.sender.group" bundle="MESSAGING_RESOURCES"/>
			</th>
		</tr>
		<logic:iterate id="sender" name="senders">
			<tr>
				<td>
					<html:link action="/messagingAction.do?method=viewSender" paramId="senderId" paramName="sender" paramProperty="externalId">
						<bean:write name="sender" property="fromName"/>
					</html:link>
				</td>
			</tr>
		</logic:iterate>		
	</table>
</logic:present>