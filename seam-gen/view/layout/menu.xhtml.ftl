<rich:toolBar
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:ui="http://java.sun.com/jsf/facelets"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:s="http://jboss.com/products/seam/taglib"
    xmlns:rich="http://richfaces.org/rich">
    <rich:toolBarGroup>
        <h:outputText value="${'#'}{projectName}:"/>
        <s:link id="menuHomeId" view="/home.xhtml" value="Home" propagation="none"/>
    </rich:toolBarGroup>
    <rich:dropDownMenu showDelay="250" hideDelay="0" submitMode="none">
        <f:facet name="label">Browse data</f:facet>
<#foreach entity in c2j.getPOJOIterator(cfg.classMappings)>
	<rich:menuItem>
    	<s:link view="/${entity.shortName}List.xhtml"
           	value="${entity.shortName} List"
           	id="${entity.shortName}Id"
			includePageParams="false"
     		propagation="none"/>
     </rich:menuItem>
</#foreach>
    </rich:dropDownMenu>
    <!-- @newMenuItem@ -->
    <rich:toolBarGroup location="right">
        <h:outputText id="menuWelcomeId" value="signed in as: ${'#'}{credentials.username}" rendered="${'#'}{identity.loggedIn}"/>
        <s:link id="menuLoginId" view="/login.xhtml" value="Login" rendered="${'#'}{not identity.loggedIn}" propagation="none"/>
        <s:link id="menuLogoutId" view="/home.xhtml" action="${'#'}{identity.logout}" value="Logout" rendered="${'#'}{identity.loggedIn}" propagation="none"/>
    </rich:toolBarGroup>
</rich:toolBar>
