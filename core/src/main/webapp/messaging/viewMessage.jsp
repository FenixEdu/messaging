<%@ page language="java" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr" %>

<%@page import="java.util.Set"%>
<%@page import="java.util.TreeSet"%>

<html:xhtml/>

<h2><bean:message bundle="MESSAGING_RESOURCES" key="title.email.sent.emails"/></h2>

<logic:present name="message">
	<logic:present name="created">
		<p class="mtop15">
			<span class="success0">
				<bean:message bundle="MESSAGING_RESOURCES" key="message.email.sent"/>
			</span>
		</p>
	</logic:present>

	<logic:notPresent name="message" property="dispatchReport">
		<html:link page="/messagingAction.do?method=deleteMessage" paramId="messagesId" paramName="message" paramProperty="externalId">
			<bean:message bundle="MESSAGING_RESOURCES" key="label.delete"/>
		</html:link>
	</logic:notPresent>

	<fr:view name="message">
		<fr:schema type="org.fenixedu.messaging.domain.Message" bundle="MESSAGING_RESOURCES">
			<fr:slot name="sender.fromName" bundle="MESSAGING_RESOURCES" key="label.fromName"/>
			<fr:slot name="sender.fromAddress" bundle="MESSAGING_RESOURCES" key="label.messaging.sender.fromAddress"/>
			<fr:slot name="created" bundle="MESSAGING_RESOURCES" key="label.email.created"/>
			<fr:slot name="user.presentationName" bundle="MESSAGING_RESOURCES" key="label.email.sent.by"/>
			<fr:slot name="replyTos" bundle="MESSAGING_RESOURCES" key="label.replyTos">
	    		<fr:property name="eachSchema" value="net.sourceforge.fenixedu.domain.util.email.ReplyTo.selectItem"/>
        		<fr:property name="eachLayout" value="values"/>
			</fr:slot>
			<fr:slot name="toGroup" bundle="MESSAGING_RESOURCES" layout="flowLayout" key="label.receiversGroup.to">
			    <fr:property name="htmlSeparator" value="<br/>"/>
			    <fr:property name="indented" value="false"/>
			</fr:slot>
			<fr:slot name="ccGroup" bundle="MESSAGING_RESOURCES" layout="flowLayout" key="label.receiversGroup.cc">
                <fr:property name="htmlSeparator" value="<br/>"/>
                <fr:property name="indented" value="false"/>
            </fr:slot>
			<fr:slot name="bccGroup" bundle="MESSAGING_RESOURCES" layout="flowLayout" key="label.receiversGroup">
                <fr:property name="htmlSeparator" value="<br/>"/>
                <fr:property name="indented" value="false"/>
            </fr:slot>
            <fr:slot name="extraBccs" bundle="MESSAGING_RESOURCES" key="label.receiversOfCopy"/>
			<fr:slot name="subject" bundle="MESSAGING_RESOURCES" key="label.email.subject"/>
			<fr:slot name="body" bundle="MESSAGING_RESOURCES" key="label.email.message" />
		</fr:schema>
		<fr:layout name="tabular">
			<fr:property name="classes" value="tstyle1 thlight thleft thtop"/>
			<fr:property name="columnClasses" value="width11em,,,"/>
		</fr:layout>
	</fr:view>

    <logic:present name="message" property="dispatchReport">
    	<h3><bean:message bundle="MESSAGING_RESOURCES" key="title.email.sent.emails.resume"/></h3>

        <bean:define id="total" name="message" property="dispatchReport.totalCount" type="java.lang.Integer" />
        <bean:define id="delivered" name="message" property="dispatchReport.deliveredCount" type="java.lang.Integer" />
        <bean:define id="invalid" name="message" property="dispatchReport.invalidCount" type="java.lang.Integer" />
        <bean:define id="failed" name="message" property="dispatchReport.failedCount" type="java.lang.Integer" />

    	<div class="progress">
          <div class="progress-bar progress-bar-success" style="width: <%= delivered * 100 / total %>%" data-toggle="tooltip" data-placement="bottom" title="<%= delivered %> delivered messages">
            <span class="sr-only"><%= delivered %> delivered messages</span>
          </div>
          <div class="progress-bar progress-bar-warning" style="width: <%= invalid * 100 / total %>%" data-toggle="tooltip" data-placement="bottom" title="<%= invalid %> invalid messages">
            <span class="sr-only"><%= invalid %> invalid messages</span>
          </div>
          <div class="progress-bar progress-bar-danger" style="width: <%= failed * 100 / total %>%" data-toggle="tooltip" data-placement="bottom" title="<%= failed %> failed messages">
            <span class="sr-only"><%= failed %> failed messages</span>
          </div>
        </div>
	</logic:present>

    <script type="text/javascript">
        $(".progress-bar").tooltip();
    </script>
	<%--
	<% final Set failed = new TreeSet(); %>
	<logic:iterate id="utilEmail" type="org.fenixedu.messaging.emaildispatch.domain.Email" name="message" property="emailSet">
		<%
			final EmailAddressList failedAddressList = utilEmail.getFailedAddresses();
			if (failedAddressList != null && !failedAddressList.isEmpty()) {
			    failed.addAll(failedAddressList.toCollection());
			}
		%>
	</logic:iterate>
	<%
		if (!failed.isEmpty()) {
		    %>
				<h3>
					N�o foi poss�vel entregar o e-mail aos seguintes destinat�rios:
				</h3>
		    	<ul>
		    <%
		    for (final Object addr : failed) {
			    %>
		    		<li><font color="red"><%= addr.toString() %></font></li>
			    <%
		    }
		    %>
		    	</ul>
		    <%
		}
	%>
	--%>
</logic:present>
