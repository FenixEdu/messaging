<%@page import="org.fenixedu.messaging.domain.MessagingSystem"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr" %>

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
