<div class="menuButtons" 
		xmlns="http://www.w3.org/1999/xhtml"
		xmlns:ui="http://java.sun.com/jsf/facelets"
		xmlns:h="http://java.sun.com/jsf/html"
		xmlns:f="http://java.sun.com/jsf/core"
		xmlns:ice="http://www.icesoft.com/icefaces/component"  
		xmlns:s="http://jboss.com/products/seam/taglib">
  <ice:panelGroup id="menuPanelGroup" styleClass="formBorderHighlight">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr>
	   <td class="iceDatTblColHdr2">
	      <ice:panelGrid id="menuPanelGrid" columns="2" columnClasses="leftMenu,rightMenu">
                 <ice:panelGroup id="leftMenuId" >
                        <ice:outputText id="menuProjectNameId" value="${'#'}{projectName}:"/>
                            <s:link id="menuHomeId" view="/home.xhtml" value="Home" propagation="none"/>
<#foreach entity in c2j.getPOJOIterator(cfg.classMappings)>
				    <s:link view="/${entity.shortName}List.xhtml" 
	       				value="${entity.shortName} List" 
						id="${entity.shortName}Id"
	 					propagation="none">
                        <f:param name="from"/>
                    </s:link>
</#foreach>         
                    <!-- @newMenuItem@ -->
 		     </ice:panelGroup>
                 <ice:panelGroup id="rightMenuId">
                                <h:outputText id="menuWelcomeId" value="signed in as: ${'#'}{credentials.username}" rendered="${'#'}{identity.loggedIn}"/>
                                <s:link view="/login.xhtml" id="menuLoginId" value="Login" rendered="${'#'}{not identity.loggedIn}" propagation="none"/>
                                <s:link view="/home.xhtml" id="menuLogoutId" action="${'#'}{identity.logout}" value="Logout" rendered="${'#'}{identity.loggedIn}" propagation="none"/>
                </ice:panelGroup>
            </ice:panelGrid>
         </td>
      </tr>
    </table>
  </ice:panelGroup>
</div>

