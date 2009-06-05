<!DOCTYPE composition PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
                             "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#include "../util/TypeInfo.ftl">

<#assign entityName = pojo.shortName>
<#assign componentName = entityName?uncap_first>
<#assign listName = componentName + "List">
<#assign pageName = entityName>
<#assign editPageName = entityName + "Edit">
<#assign listPageName = entityName + "List">

<ui:composition xmlns="http://www.w3.org/1999/xhtml"
                xmlns:s="http://jboss.com/products/seam/taglib"
                xmlns:ui="http://java.sun.com/jsf/facelets"
                xmlns:f="http://java.sun.com/jsf/core"
                xmlns:h="http://java.sun.com/jsf/html"
		xmlns:ice="http://www.icesoft.com/icefaces/component"  
                template="layout/template.xhtml">
                       
<ui:define name="body">
    
    <ice:form id="${componentName}Search" styleClass="edit">
      <ice:panelGroup  id="searchGroup" styleClass="formBorderHighlight">
          <table width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr>
                  <td class="iceDatTblColHdr2">
                    <ice:outputText id="list${entityName}Id" value="${entityName} search filter"/>
                 </td>
              </tr>
         </table>	
         <ice:panelGroup id="listPanelGroup${entityName}Id" styleClass="edit">
		
<#foreach property in pojo.allPropertiesIterator>
<#if !c2h.isCollection(property) && !isToOne(property) && property != pojo.versionProperty!>
<#if c2j.isComponent(property)>
<#foreach componentProperty in property.value.propertyIterator>
<#if isString(componentProperty)>
            <s:decorate id="${componentProperty.name}decId" template="layout/display.xhtml">
                <ui:define name="label">${label(componentProperty.name)}</ui:define>
                  <ice:inputText id="${componentProperty.name}" 
                          value="${'#'}{${listName}.${componentName}.${property.name}.${componentProperty.name}}"
				  partialSubmit="true"/>
            </s:decorate>

</#if>
</#foreach>
<#else>
<#if isString(property)>
            <s:decorate id="${property.name}decId" template="layout/display.xhtml">
                <ui:define name="label">${label(property.name)}</ui:define>
                <ice:inputText id="${property.name}" 
                          value="${'#'}{${listName}.${componentName}.${property.name}}"
				  partialSubmit="true"/>
            </s:decorate>

</#if>
</#if>
</#if>
</#foreach>
            <s:decorate template="layout/display.xhtml">
                <ui:define name="label">Match</ui:define>
                <ice:selectOneRadio id="logic" value="${'#'}{${listName}.restrictionLogicOperator}" partialSubmit="true">
                    <f:selectItem itemLabel="All" itemValue="and"/>
                    <f:selectItem itemLabel="Any" itemValue="or"/>
                </ice:selectOneRadio>
            </s:decorate>
          
   
	  </ice:panelGroup>
  
        
        <div class="actionButtons">
            <ice:commandButton id="search" value="Search" action="/${listPageName}.xhtml"/>
        </div>
      </ice:panelGroup> 
    </ice:form>
    
    <ice:panelGroup styleClass="formBorderHighlight">

    <h3>${componentName}  search results</h3>

    <div class="searchResults" id="list${componentName}Results">
    <ice:outputText value="The ${componentName} search returned no results." 
               rendered="${'#'}{empty ${listName}.resultList}"/>
               
    <ice:dataTable id="${listName}" 
                  var="${componentName}"
                value="${'#'}{${listName}.resultList}" 
            resizable="true"
	columnClasses="allCols"
             rendered="${'#'}{not empty ${listName}.resultList}">
<#foreach property in pojo.allPropertiesIterator>
<#if !c2h.isCollection(property) && !isToOne(property) && property != pojo.versionProperty!>
<#if pojo.isComponent(property)>
<#foreach componentProperty in property.value.propertyIterator>
        <ice:column id="list${componentProperty.name}Id">
            <f:facet name="header">
<#assign propertyPath = property.name + '.' + componentProperty.name>
                <ui:include src="layout/sort.xhtml">
                    <ui:param name="entityList" value="${'#'}{${listName}}"/>
                    <ui:param name="propertyLabel" value="${label(componentProperty.name)}"/>
                    <ui:param name="propertyPath" value="${componentName}.${propertyPath}"/>
                </ui:include>
            </f:facet>
            ${'#'}{${componentName}.${property.name}.${componentProperty.name}}
        </ice:column>
</#foreach>
<#else>
        <ice:column id="list${property.name}Id">
            <f:facet name="header">
                <ui:include src="layout/sort.xhtml">
                    <ui:param name="entityList" value="${'#'}{${listName}}"/>
                    <ui:param name="propertyLabel" value="${label(property.name)}"/>
                    <ui:param name="propertyPath" value="${componentName}.${property.name}"/>
                </ui:include>
            </f:facet>
            ${'#'}{${componentName}.${property.name}}&amp;nbsp;
        </ice:column>
</#if>
</#if>
<#if isToOne(property)>
<#assign parentPojo = c2j.getPOJOClass(cfg.getClassMapping(property.value.referencedEntityName))>
<#if parentPojo.isComponent(parentPojo.identifierProperty)>
<#foreach componentProperty in parentPojo.identifierProperty.value.propertyIterator>
        <ice:column id="listColumn${componentProperty}${listName}Id">
            <f:facet name="header">
<#assign propertyPath = property.name + '.' + parentPojo.identifierProperty.name + '.' + componentProperty.name>
                <ui:include src="layout/sort.xhtml">
                    <ui:param name="entityList" value="${'#'}{${listName}}"/>
                    <ui:param name="propertyLabel" value="${label(property.name)} ${label(componentProperty.name)?uncap_first}"/>
                    <ui:param name="propertyPath" value="${componentName}.${propertyPath}"/>
                </ui:include>
            </f:facet>
            ${'#'}{${componentName}.${propertyPath}}&amp;nbsp;
        </ice:column>
</#foreach>
<#else>
        <ice:column id="listColumn${property.name}Id">
            <f:facet name="header">
<#assign propertyPath = property.name + '.' + parentPojo.identifierProperty.name>
                <ui:include src="layout/sort.xhtml">
                    <ui:param name="entityList" value="${'#'}{${listName}}"/>
                    <ui:param name="propertyLabel" value="${label(property.name)} ${label(parentPojo.identifierProperty.name)?uncap_first}"/>
                    <ui:param name="propertyPath" value="${componentName}.${propertyPath}"/>
                </ui:include>
            </f:facet>
            ${'#'}{${componentName}.${propertyPath}}
        </ice:column>
</#if>
</#if>
</#foreach>
        <ice:column id="listColumn${pageName}Id">
            <f:facet name="header">Action</f:facet>
            <s:link view="/${'#'}{empty from ? '${pageName}' : from}.xhtml" 
                   value="${'#'}{empty from ? 'View' : 'Select'}" 
                      id="${componentName}">
<#if pojo.isComponent(pojo.identifierProperty)>
<#foreach componentProperty in pojo.identifierProperty.value.propertyIterator>
                <f:param name="${componentName}${componentProperty.name?cap_first}" 
                        value="${'#'}{${componentName}.${pojo.identifierProperty.name}.${componentProperty.name}}"/>
</#foreach>
<#else>
                <f:param name="${componentName}${pojo.identifierProperty.name?cap_first}" 
                        value="${'#'}{${componentName}.${pojo.identifierProperty.name}}"/>
</#if>
            </s:link>
			${'#'}{' '}
            <s:link view="/${editPageName}.xhtml"
                   value="Edit" 
                      id="${componentName}Edit"
                      rendered="${'#'}{empty from}">
<#if pojo.isComponent(pojo.identifierProperty)>
<#foreach componentProperty in pojo.identifierProperty.value.propertyIterator>
                <f:param name="${componentName}${componentProperty.name?cap_first}" 
                        value="${'#'}{${componentName}.${pojo.identifierProperty.name}.${componentProperty.name}}"/>
</#foreach>
<#else>
                <f:param name="${componentName}${pojo.identifierProperty.name?cap_first}" 
                        value="${'#'}{${componentName}.${pojo.identifierProperty.name}}"/>
</#if>
            </s:link>
        </ice:column>
    </ice:dataTable>

    </div>
</ice:panelGroup>

    <div class="tableControl">
      
        <s:link view="/${listPageName}.xhtml" 
            rendered="${'#'}{${listName}.previousExists}" 
               value="${'#'}{messages.left}${'#'}{messages.left} First Page"
                  id="firstPage${listName}Id">
          <f:param name="firstResult" value="0"/>
        </s:link>
        
        <s:link view="/${listPageName}.xhtml" 
            rendered="${'#'}{${listName}.previousExists}" 
               value="${'#'}{messages.left} Previous Page"
                  id="previousPage${listName}Id">
            <f:param name="firstResult" 
                    value="${'#'}{${listName}.previousFirstResult}"/>
        </s:link>
        
        <s:link view="/${listPageName}.xhtml" 
            rendered="${'#'}{${listName}.nextExists}" 
               value="Next Page ${'#'}{messages.right}"
                  id="nextPage${listName}Id">
            <f:param name="firstResult" 
                    value="${'#'}{${listName}.nextFirstResult}"/>
        </s:link>
        
        <s:link view="/${listPageName}.xhtml" 
            rendered="${'#'}{${listName}.nextExists}" 
               value="Last Page ${'#'}{messages.right}${'#'}{messages.right}"
                  id="lastPage${listName}Id">
            <f:param name="firstResult" 
                    value="${'#'}{${listName}.lastFirstResult}"/>
        </s:link>
        
    </div>
    
    <s:div styleClass="actionButtons" rendered="${'#'}{empty from}">
        <s:button view="/${editPageName}.xhtml"
                    id="create" 
                 value="Create ${componentName}">
<#assign idName = componentName + pojo.identifierProperty.name?cap_first>
<#if c2j.isComponent(pojo.identifierProperty)>
<#foreach componentProperty in pojo.identifierProperty.value.propertyIterator>
<#assign cidName = componentName + componentProperty.name?cap_first>
            <f:param name="${cidName}"/>
</#foreach>
<#else>
            <f:param name="${idName}"/>
</#if>
        </s:button>
    </s:div>
    
</ui:define>

</ui:composition>

