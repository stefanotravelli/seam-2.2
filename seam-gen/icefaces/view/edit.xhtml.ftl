<!DOCTYPE composition PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
                             "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#assign entityName = pojo.shortName>
<#assign componentName = entityName?uncap_first>
<#assign homeName = componentName + "Home">
<#assign masterPageName = entityName + "List">
<#assign pageName = entityName>

<ui:composition xmlns="http://www.w3.org/1999/xhtml"
                xmlns:s="http://jboss.com/products/seam/taglib"
                xmlns:ui="http://java.sun.com/jsf/facelets"
                xmlns:f="http://java.sun.com/jsf/core"
                xmlns:h="http://java.sun.com/jsf/html"
		xmlns:ice="http://www.icesoft.com/icefaces/component" 
                template="layout/template.xhtml">
                       
<ui:define name="body">
    
    <ice:form id="${componentName}" styleClass="edit">
     <ice:panelGroup  id="edit${componentName}GroupId" styleClass="formBorderHighlight">
          <table width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr>
                  <td class="iceDatTblColHdr2">
			    <ice:outputText id="editTextBoxId" value="${'#'}{${homeName}.managed ? 'Edit' : 'Add'} ${componentName}"/>
                  </td>
              </tr>
          </table>

        <ice:panelGroup id="editPanelGroupId" styleClass="edit">		 
<#foreach property in pojo.allPropertiesIterator>
<#include "editproperty.xhtml.ftl">
</#foreach>
            <div style="clear:both">
                <span class="required">*</span> 
                required fields
            </div>
          </ice:panelGroup>
       </ice:panelGroup>
                  
       <div class="actionButtons">
        
            <ice:commandButton id="save" 
                          value="Save" 
                         action="${'#'}{${homeName}.persist}"
                       disabled="${'#'}{!${homeName}.wired}"
                       rendered="${'#'}{!${homeName}.managed}"/>  
                          			  
            <ice:commandButton id="update" 
                          value="Save" 
                         action="${'#'}{${homeName}.update}"
                       rendered="${'#'}{${homeName}.managed}"/>
                        			  
            <ice:commandButton id="delete" 
                          value="Delete" 
                         action="${'#'}{${homeName}.remove}"
                      immediate="true"
                       rendered="${'#'}{${homeName}.managed}"/>
                    
            <s:button id="cancelEdit" styleClass="iceCmdBtn"
                   value="Cancel"
             propagation="end"
                    view="/${pageName}.xhtml"
                rendered="${'#'}{${homeName}.managed}"/>
                
            <s:button id="cancelAdd${homeName}" styleClass="iceCmdBtn"
                   value="Cancel"
             propagation="end"
                    view="/${'#'}{empty ${componentName}From ? '${masterPageName}' : ${componentName}From}.xhtml"
                rendered="${'#'}{!${homeName}.managed}"/>
                
        </div>
        
    </ice:form>
<#assign hasAssociations=false>
<#foreach property in pojo.allPropertiesIterator>
<#if isToOne(property) || c2h.isOneToManyCollection(property)>
<#assign hasAssociations=true>
</#if>
</#foreach>

<#if hasAssociations>
 <ice:form id="form2${homeName}">  
        <ice:panelTabSet id="editPanelTab${homeName}Id" styleClass="componentPanelTabSetLayout" style="margin-bottom:5px;margin-top:10px;">
</#if>
<#foreach property in pojo.allPropertiesIterator>
<#if isToOne(property)>
<#assign parentPojo = c2j.getPOJOClass(cfg.getClassMapping(property.value.referencedEntityName))>
<#assign parentPageName = parentPojo.shortName>
<#assign parentName = parentPojo.shortName?uncap_first>
   
<#if property.optional>
	<ice:panelTab id="editTab${property.name}Id" label="${label(property.name)}">
<#else>
 	<ice:panelTab id="editTab${property.name}Id" label="${label(property.name)} *" >
</#if>
		<div class="association" id="${property.name}Parent">
    
        	<h:outputText id="edit${property.name}TextId" value="There is no ${property.name} associated with this ${componentName}." 
                   rendered="${'#'}{${homeName}.instance.${property.name} == null}"/>
       	<ice:dataTable var="${parentName}" 
                   value="${'#'}{${homeName}.instance.${property.name}}" 
                rendered="${'#'}{${homeName}.instance.${property.name} != null}"
              rowClasses="rvgRowOne,rvgRowTwo"
		  columnClasses="allCols"
                      id="edit${property.name}TableId">
<#foreach parentProperty in parentPojo.allPropertiesIterator>
<#if !c2h.isCollection(parentProperty) && !isToOne(parentProperty) && parentProperty != parentPojo.versionProperty!>
<#if parentPojo.isComponent(parentProperty)>
<#foreach componentProperty in parentProperty.value.propertyIterator>
            <ice:column id="editColumn${componentProperty.name}Id">
                <f:facet name="header">${label(componentProperty.name)}</f:facet>
                ${'#'}{${parentName}.${parentProperty.name}.${componentProperty.name}}
            </ice:column>
</#foreach>
<#else>
            <ice:column id="$editColumn${parentProperty.name}Id">
                <f:facet name="header">${label(parentProperty.name)}</f:facet>
                ${'#'}{${parentName}.${parentProperty.name}}
            </ice:column>
</#if>
</#if>
<#if isToOne(parentProperty)>
<#assign grandparentPojo = c2j.getPOJOClass(cfg.getClassMapping(parentProperty.value.referencedEntityName))>
<#if grandparentPojo.isComponent(grandparentPojo.identifierProperty)>
<#foreach componentProperty in grandparentPojo.identifierProperty.value.propertyIterator>
            <ice:column id="$editColumn${parentProperty.name}Id">
	    	    <f:facet name="header">${label(parentProperty.name)} ${label(componentProperty.name)?uncap_first}</f:facet>
		    	${'#'}{${parentName}.${parentProperty.name}.${grandparentPojo.identifierProperty.name}.${componentProperty.name}}
            </ice:column>
</#foreach>
<#else>
            <ice:column id="$editColumn${parentProperty.name}Id">
	    	    <f:facet name="header">${label(parentProperty.name)} ${label(grandparentPojo.identifierProperty.name)?uncap_first}</f:facet>
		    	${'#'}{${parentName}.${parentProperty.name}.${grandparentPojo.identifierProperty.name}}
            </ice:column>
</#if>
</#if>
</#foreach>
            <ice:column id="editColumn${parentName}LinkId">
                <f:facet name="header">Action</f:facet>
                <s:link view="/${parentPageName}.xhtml" 
                         id="view${parentName}" 
                      value="View" 
                propagation="none">
<#if parentPojo.isComponent(parentPojo.identifierProperty)>
<#foreach componentProperty in parentPojo.identifierProperty.value.propertyIterator>
                    <f:param name="${parentName}${componentProperty.name?cap_first}" 
                            value="${'#'}{${parentName}.${parentPojo.identifierProperty.name}.${componentProperty.name}}"/>
</#foreach>
<#else>
                    <f:param name="${parentName}${parentPojo.identifierProperty.name?cap_first}" 
                           value="${'#'}{${parentName}.${parentPojo.identifierProperty.name}}"/>
</#if>
                </s:link>
            </ice:column>
      </ice:dataTable>
<#if parentPojo.shortName!=pojo.shortName>
        <div class="actionButtons">
            <s:button value="Select ${property.name}"
	              id="selectParent"
                      view="/${parentPageName}List.xhtml">
                <f:param name="from" value="${pageName}Edit"/>
            </s:button>
        </div>
        
</#if>
    </div>
    </ice:panelTab>
</#if>
<#if c2h.isOneToManyCollection(property)>
 	<ice:panelTab id="editPanelTab${property.name}Id" label="${label(property.name)}">
        <div class="association" id="${property.name}Children">
        
<#assign childPojo = c2j.getPOJOClass(property.value.element.associatedClass)>
<#assign childPageName = childPojo.shortName>
<#assign childEditPageName = childPojo.shortName + "Edit">
<#assign childName = childPojo.shortName?uncap_first>
            <h:outputText value="There are no ${property.name} associated with this ${componentName}." 
	                     id="edit${property.name}TextId"
                       rendered="${'#'}{empty ${homeName}.${property.name}}"/>
           <ice:dataTable value="${'#'}{${homeName}.${property.name}}" 
                           var="${childName}" 
                      rendered="${'#'}{not empty ${homeName}.${property.name}}" 
                    rowClasses="rvgRowOne,rvgRowTwo"
			  columnClasses="allCols"
                            id="edit${property.name}Table">
<#foreach childProperty in childPojo.allPropertiesIterator>
<#if !c2h.isCollection(childProperty) && !isToOne(childProperty) && childProperty != childPojo.versionProperty!>
<#if childPojo.isComponent(childProperty)>
<#foreach componentProperty in childProperty.value.propertyIterator>
               <ice:column id="edit${componentProperty.name}Id">
                    <f:facet name="header">${label(componentProperty.name)}</f:facet>
                    ${'#'}{${childName}.${childProperty.name}.${componentProperty.name}}
                </ice:column>
</#foreach>
<#else>
                <ice:column id="edit${childProperty.name}Id">
                    <f:facet name="header">${label(childProperty.name)}</f:facet>
                    <h:outputText id="edit${childProperty.name}TextId" value="${'#'}{${childName}.${childProperty.name}}"/>
                </ice:column>
</#if>
</#if>
</#foreach>
                <ice:column id="edit${childName}Id">
                    <f:facet name="header">Action</f:facet>
                    <s:link view="/${childPageName}.xhtml" 
                              id="select${childName}" 
                           value="Select"
                     propagation="none">
<#if childPojo.isComponent(childPojo.identifierProperty)>
<#foreach componentProperty in childPojo.identifierProperty.value.propertyIterator>
                        <f:param name="${childName}${componentProperty.name?cap_first}" 
                                value="${'#'}{${childName}.${childPojo.identifierProperty.name}.${componentProperty.name}}"/>
</#foreach>
<#else>
                        <f:param name="${childName}${childPojo.identifierProperty.name?cap_first}" 
                                value="${'#'}{${childName}.${childPojo.identifierProperty.name}}"/>
</#if>
                        <f:param name="${childName}From" value="${entityName}"/>
                    </s:link>
                </ice:column>
           </ice:dataTable>
      </div>
        <f:subview rendered="${'#'}{${homeName}.managed}" id="${property.name}Id">
        <div class="actionButtons">
            <s:button id="add${childName}Id" 
                   value="Add ${childName}"
                    view="/${childEditPageName}.xhtml"
             propagation="none">
                 <f:param name="${componentName}${pojo.identifierProperty.name?cap_first}" 
                         value="${'#'}{${homeName}.instance.${pojo.identifierProperty.name}}"/>
                 <f:param name="${childName}From" value="${entityName}"/>
            </s:button>
        </div>
        </f:subview>
    </ice:panelTab>
</#if>
</#foreach>
<#if hasAssociations>
</ice:panelTabSet>
</ice:form>
</#if>
</ui:define>

</ui:composition>



