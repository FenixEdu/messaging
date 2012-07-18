<%@page import="pt.ist.messaging.domain.MessagingSystem"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr" %>

<h2>
	<bean:message bundle="MESSAGING_RESOURCES" key="label.messaging.new.message"/>
</h2>

<logic:present name="errorMessage">
	<p>
		<span class="error0"><bean:write name="errorMessage"/></span>
	</p>
</logic:present>

<br/>

<form action="<%= request.getContextPath() + "/messagingAction.do" %>" method="post">
	<html:hidden property="method" value="sendEmail"/>

	<fr:edit id="emailBean" name="emailBean">
		<fr:schema type="pt.ist.messaging.domain.EmailBean" bundle="MESSAGING_RESOURCES">
			<fr:slot name="sender" bundle="MESSAGING_RESOURCES" key="label.fromName" layout="menu-select-postback"
					required="true">
		        <fr:property name="providerClass" value="pt.ist.messaging.presentationTier.provider.EmailSenderProvider" />
				<fr:property name="format" value="${fromName} (${fromAddress})" />
				<fr:property name="destination" value="selectSender"/>
			</fr:slot>
    		<fr:slot name="replyTos" layout="option-select" key="label.replyTos">
        		<fr:property name="providerClass" value="pt.ist.messaging.presentationTier.provider.EmailReplyTosProvider" />
        		<fr:property name="eachSchema" value="net.sourceforge.fenixedu.domain.util.email.ReplyTo.selectItem"/>
        		<fr:property name="eachLayout" value="values"/>
        		<fr:property name="classes" value="nobullet noindent"/>
        		<fr:property name="sortBy" value="replyToAddress"/>
    		</fr:slot>
    		<fr:slot name="recipients" layout="option-select" key="label.receiversGroup">
        		<fr:property name="providerClass" value="pt.ist.messaging.presentationTier.provider.EmailRecipientsProvider" />
        		<fr:property name="eachSchema" value="net.sourceforge.fenixedu.domain.util.email.Recipient.selectItem"/>
        		<fr:property name="eachLayout" value="values"/>
        		<fr:property name="classes" value="nobullet noindent"/>
        		<fr:property name="sortBy" value="name"/>
    		</fr:slot>
			<fr:slot name="bccs" bundle="MESSAGING_RESOURCES" key="label.receiversOfCopy">
				<fr:property name="size" value="50" />
			</fr:slot>
			<fr:slot name="subject" bundle="MESSAGING_RESOURCES" key="label.email.subject">
				<fr:property name="size" value="50" />
			</fr:slot>
			<fr:slot name="message" bundle="MESSAGING_RESOURCES" key="label.email.message" layout="longText">
				<fr:property name="columns" value="80"/>
				<fr:property name="rows" value="10"/>
			</fr:slot>
		</fr:schema>
		<fr:layout name="tabular">
			<fr:property name="classes" value="tstyle5 thright thlight mtop05 ulnomargin"/>
			<fr:property name="columnClasses" value=",,tdclear tderror1"/>
		</fr:layout>

		<fr:destination name="selectSender" path="/messagingAction.do?method=newEmail"/>
		<fr:destination name="cancel" path="/index.do"/>
	</fr:edit>

</form>
