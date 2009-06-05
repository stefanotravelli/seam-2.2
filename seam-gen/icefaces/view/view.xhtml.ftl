<!DOCTYPE composition PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
                             "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#include "../util/TypeInfo.ftl">

<#assign entityName = pojo.shortName>
<#assign componentName = entityName?uncap_first>
<#assign homeName = componentName + "Home">
<#assign masterPageName = entityName + "List">
<#assign editPageName = entityName + "Edit">

<ui:composition xmlns="http://www.w3.org/1999/xhtml"
                xmlns:s="http://jboss.com/products/seam/taglib"
                xmlns:ui="http://java.sun.com/jsf/facelets"
                xmlns:f="http://java.sun.com/jsf/core"
                xmlns:h="http://java.sun.com/jsf/html"
		xmlns:ice="http://www.icesoft.com/icefaces/component"   
                template="layout/template.xhtml">
                       
<ui:define name="body">
    
      <ice:panelGroup  id="view${homeName}PanelGroupId" styleClass="formBorderHighlight">
          <table width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr>
                  <td class="iceDatTblColHdr2">
			    <ice:outputText id="viewText${homeName}Id" value="${entityName}"/>
                  </td>
              </tr>
          </table>
      
<#foreach property in pojo.allPropertiesIterator>
<#if !c2h.isCollection(property) && !isToOne(property) && property != pojo.versionProperty!>
<#include "viewproperty.xhtml.ftl">
</#if>
</#foreach>

          <div style="clear:both"/>
 

    <div id="view${editPageName}searchButtons" class="actionButtons">      

        <s:button view="/${editPageName}.xhtml" 
                    id="edit" styleClass="iceCmdBtn"
                 value="Edit"/>

        <s:button view="/${'#'}{empty ${componentName}From ? '${masterPageName}' : ${componentName}From}.xhtml"
                    id="done" styleClass="iceCmdBtn"
                 value="Done"/>

    </div>
  </ice:panelGroup>

<#assign hasAssociations=false>
<#foreach property in pojo.allPropertiesIterator>
<#if isToOne(property) || c2h.isOneToManyCollection(property)>
<#assign hasAssociations=true>
</#if>
</#foreach>

<#if hasAssociations>
    <ice:form id="view2${homeName}FormId">
       <ice:panelTabSet id="view${homeName}TabSetId" 
                styleClass="componentPanelTabSetLayout" 
		style="margin-bottom:5px;margin-top:10px;">
</#if>
<#foreach property in pojo.allPropertiesIterator>
<#if isToOne(property)>
<#assign parentPojo = c2j.getPOJOClass(cfg.getClassMapping(property.value.referencedEntityName))>
<#assign parentPageName = parentPojo.shortName>
<#assign parentName = parentPojo.shortName?uncap_first>

	<ice:panelTab id="view${property.name}panelTab" label="${label(property.name)}">
   		<div class="association" id="${property.name}Parent">
        
       		 <h:outputText value="There is no ${property.name} associated with this ${componentName}." 
		                  id="view${property.name}TextId"
                   rendered="${'#'}{${homeName}.instance.${property.name} == null}"/>
   
        	<ice:dataTable  var="${parentName}" 
                   value="${'#'}{${homeName}.instance.${property.name}}" 
                rendered="${'#'}{${homeName}.instance.${property.name} != null}"
              rowClasses="rvgRowOne,rvgRowTwo"
	        columnClasses="allCols"
                      id="view${property.name}TableId">
<#foreach parentProperty in parentPojo.allPropertiesIterator>
<#if !c2h.isCollection(parentProperty) && !isToOne(parentProperty) && parentProperty != parentPojo.versionProperty!>
<#if parentPojo.isComponent(parentProperty)>
<#foreach componentProperty in parentProperty.value.propertyIterator>
            <ice:column id="viewColumn${componentProperty.name}Id">
                <f:facet name="header">${label(componentProperty.name)}</f:facet>
                ${'#'}{${parentName}.${parentProperty.name}.${componentProperty.name}}
            </ice:column>
</#foreach>
<#else>
            <ice:column id="view${parentProperty.name}Id">
                <f:facet name="header">${label(parentProperty.name)}</f:facet>
                ${'#'}{${parentName}.${parentProperty.name}}
            </ice:column>
</#if>
</#if>
<#if isToOne(parentProperty)>
<#assign grandparentPojo = c2j.getPOJOClass(cfg.getClassMapping(parentProperty.value.referencedEntityName))>
<#if grandparentPojo.isComponent(grandparentPojo.identifierProperty)>
<#foreach componentProperty in grandparentPojo.identifierProperty.value.propertyIterator>
            <ice:column id="view${parentProperty.name}ColumnId">
	    	    <f:facet name="header">${label(parentProperty.name)} ${label(componentProperty.name)?uncap_first}</f:facet>
		    	${'#'}{${parentName}.${parentProperty.name}.${grandparentPojo.identifierProperty.name}.${componentProperty.name}}
            </ice:column>
</#foreach>
<#else>
            <ice:column id="view${grandparentPojo.identifierProperty.name}ColumnName">
	    	    <f:facet name="header">${label(parentProperty.name)} ${label(grandparentPojo.identifierProperty.name)?uncap_first}</f:facet>
		    	${'#'}{${parentName}.${parentProperty.name}.${grandparentPojo.identifierProperty.name}}
            </ice:column>
</#if>
</#if>
</#foreach>
            <ice:column id="view${parentName}ColumnId">
                <f:facet name="header">Action</f:facet>
                <s:link id="view${parentName}LinkId" 
                     value="View" 
                      view="/${parentPageName}.xhtml">
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
       
    </div>
    </ice:panelTab>
</#if>
<#if c2h.isOneToManyCollection(property)>

   <ice:panelTab label="${label(property.name)}">
    <div class="association" id="view${property.name}ChildrenId">
        
<#assign childPojo = c2j.getPOJOClass(property.value.element.associatedClass)>
<#assign childPageName = childPojo.shortName>
<#assign childEditPageName = childPojo.shortName + "Edit">
<#assign childName = childPojo.shortName?uncap_first>
<#assign childHomeName = childName + "Home">
        <h:outputText value="There are no ${property.name} associated with this ${componentName}." 
	                 id="view${property.name}ChildTextId"
                   rendered="${'#'}{empty ${homeName}.${property.name}}"/>
        
        <ice:dataTable value="${'#'}{${homeName}.${property.name}}" 
                       var="${childName}" 
                  rendered="${'#'}{not empty ${homeName}.${property.name}}" 
                rowClasses="rvgRowOne,rvgRowTwo"
		    columnClasses="allCols"
                        id="view${property.name}TableId">
<#foreach childProperty in childPojo.allPropertiesIterator>
<#if !c2h.isCollection(childProperty) && !isToOne(childProperty) && childProperty != childPojo.versionProperty!>
<#if childPojo.isComponent(childProperty)>
<#foreach componentProperty in childProperty.value.propertyIterator>
            <ice:column id="view${componentProperty.name}Id">
                <f:facet name="header">${label(componentProperty.name)}</f:facet>
                ${'#'}{${childName}.${childProperty.name}.${componentProperty.name}}
            </ice:column>
</#foreach>
<#else>
            <ice:column id="view${childProperty.name}Id">
                <f:facet name="header">${label(childProperty.name)}</f:facet>
                <h:outputText id="view${childProperty.name}TextId" 
		           value="${'#'}{${childName}.${childProperty.name}}"/>
            </ice:column>
</#if>
</#if>
</#foreach>
            <ice:column>
                <f:facet name="header">Action</f:facet>
                <s:link id="select${childName}LinkId" 
                     value="Select" 
                      view="/${childPageName}.xhtml">
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
    
    <div class="actionButtons">
        <s:button id="viewAdd${childName}ButtonId" 
               value="Add ${childName}"
                view="/${childEditPageName}.xhtml">
            <f:param name="${componentName}${pojo.identifierProperty.name?cap_first}" 
                    value="${'#'}{${homeName}.instance.${pojo.identifierProperty.name}}"/>
            <f:param name="${childName}From" value="${entityName}"/>
        </s:button>
    </div>        
   </ice:panelTab>
</#if>
</#foreach>
<#if hasAssociations>
</ice:panelTabSet>
</ice:form>
</#if> 
</ui:define>

</ui:composition>

