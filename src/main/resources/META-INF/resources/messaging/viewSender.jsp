<%@page import="pt.ist.messaging.domain.MessagingSystem"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr" %>

<h2>
	<bean:message key="label.module.messaging" bundle="MESSAGING_RESOURCES"/>
</h2>

<br/>

<logic:present name="sender">
	<table class="tstyle3">
		<tr>
			<th>
				<bean:message key="label.messaging.sender.fromName" bundle="MESSAGING_RESOURCES"/>
			</th>
			<td>
				<bean:write name="sender" property="fromName"/>
			</td>
		</tr>
		<tr>
			<th>
				<bean:message key="label.messaging.sender.fromAddress" bundle="MESSAGING_RESOURCES"/>
			</th>
			<td>
				<bean:write name="sender" property="fromAddress"/>
			</td>
		</tr>
		<tr>
			<th>
				<bean:message key="label.messaging.sender.replyTos" bundle="MESSAGING_RESOURCES"/>
			</th>
			<td>
				<ul>
					<logic:iterate id="replyTo" name="sender" property="replyTo">
						<li>
							<bean:write name="replyTo" property="replyToAddress"/>
						</li>
					</logic:iterate>
				</ul>
			</td>
		</tr>
	</table>
</logic:present>

<br/>

<html:link action="/messagingAction.do?method=newEmail" paramId="senderId" paramName="sender" paramProperty="externalId">
	<bean:message key="label.messaging.new.message" bundle="MESSAGING_RESOURCES"/>
</html:link>

<br/>
<br/>

<div class="infoop">
   	<p class="mvert0">
   		<bean:message bundle="MESSAGING_RESOURCES" key="label.message.email.send.queue"/>
   	</p>
</div>

<br/>
<br/>

<fr:view name="sender" property="messageSet">
	<fr:schema type="pt.ist.messaging.domain.Message" bundle="MESSAGING_RESOURCES">
		<fr:slot name="created" bundle="MESSAGING_RESOURCES" key="label.email.created"/>
		<fr:slot name="subject" bundle="MESSAGING_RESOURCES" key="label.email.subject"/>
		<fr:slot name="sent" bundle="MESSAGING_RESOURCES" key="label.email.sentDate"/>
	</fr:schema>
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2 thlight"/>
		<fr:property name="columnClasses" value=",,aleft,"/>
		<fr:property name="link(view)" value="/messagingAction.do?method=viewMessage"/>
		<fr:property name="bundle(view)" value="MESSAGING_RESOURCES"/>
		<fr:property name="key(view)" value="link.view"/>
		<fr:property name="param(view)" value="externalId/messagesId"/>
		<fr:property name="order(view)" value="1"/>
		<fr:property name="sortBy" value="created=desc"/>
	</fr:layout>
</fr:view>
