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
    xmlns:rich="http://richfaces.org/rich"
    template="layout/template.xhtml">

<ui:define name="body">

    <h:form id="${componentName}Search" styleClass="edit">

        <rich:simpleTogglePanel label="${entityName} Search Filter" switchType="ajax">

<#assign searchParamNames = []/>
<#foreach property in pojo.allPropertiesIterator>
<#if !c2h.isCollection(property) && !isToOne(property) && property != pojo.versionProperty!>
<#if c2j.isComponent(property)>
<#foreach componentProperty in property.value.propertyIterator>
<#if isString(componentProperty)>
<#assign searchParamNames = searchParamNames + [componentProperty.name]/>
            <s:decorate template="layout/display.xhtml">
                <ui:define name="label">${label(componentProperty.name)}</ui:define>
                <h:inputText id="${componentProperty.name}" value="${'#'}{${listName}.${componentName}.${property.name}.${componentProperty.name}}"/>
            </s:decorate>

</#if>
</#foreach>
<#else>
<#if isString(property)>
<#assign searchParamNames = searchParamNames + [property.name]/>
            <s:decorate template="layout/display.xhtml">
                <ui:define name="label">${label(property.name)}</ui:define>
                <h:inputText id="${property.name}" value="${'#'}{${listName}.${componentName}.${property.name}}"/>
            </s:decorate>

</#if>
</#if>
</#if>
</#foreach>
            <s:decorate template="layout/display.xhtml">
                <ui:define name="label">Match</ui:define>
                <h:selectOneRadio id="logic" value="${'#'}{${listName}.restrictionLogicOperator}" styleClass="radio">
                    <f:selectItem itemLabel="All" itemValue="and"/>
                    <f:selectItem itemLabel="Any" itemValue="or"/>
                </h:selectOneRadio>
            </s:decorate>

        </rich:simpleTogglePanel>

        <div class="actionButtons">
            <h:commandButton id="search" value="Search" action="/${listPageName}.xhtml"/>
            <s:button id="reset" value="Reset" includePageParams="false"/>
        </div>

    </h:form>

    <rich:panel>
        <f:facet name="header">${entityName} Search Results (${'#'}{empty ${listName}.resultList ? 0 : (${listName}.paginated ? ${listName}.resultCount : ${listName}.resultList.size)})</f:facet>
    <div class="results" id="${componentName}List">

    <h:outputText value="The ${componentName} search returned no results."
               rendered="${'#'}{empty ${listName}.resultList}"/>

    <rich:dataTable id="${listName}"
                var="_${componentName}"
              value="${'#'}{${listName}.resultList}"
           rendered="${'#'}{not empty ${listName}.resultList}">
<#foreach property in pojo.allPropertiesIterator>
<#if !c2h.isCollection(property) && !isToOne(property) && property != pojo.versionProperty!>
<#if pojo.isComponent(property)>
<#foreach componentProperty in property.value.propertyIterator>
        <h:column>
            <f:facet name="header">
<#assign propertyPath = property.name + '.' + componentProperty.name>
                <ui:include src="layout/sort.xhtml">
                    <ui:param name="entityList" value="${'#'}{${listName}}"/>
                    <ui:param name="propertyLabel" value="${label(componentProperty.name)}"/>
                    <ui:param name="propertyPath" value="${componentName}.${propertyPath}"/>
                </ui:include>
            </f:facet>
            <@outputValue property=componentProperty expression="${'#'}{_${componentName}.${property.name}.${componentProperty.name}}" indent=12/>
        </h:column>
</#foreach>
<#else>
        <h:column>
            <f:facet name="header">
                <ui:include src="layout/sort.xhtml">
                    <ui:param name="entityList" value="${'#'}{${listName}}"/>
                    <ui:param name="propertyLabel" value="${label(property.name)}"/>
                    <ui:param name="propertyPath" value="${componentName}.${property.name}"/>
                </ui:include>
            </f:facet>
            <@outputValue property=property expression="${'#'}{_${componentName}.${property.name}}" indent=12/>
        </h:column>
</#if>
</#if>
<#if isToOne(property)>
<#assign parentPojo = c2j.getPOJOClass(cfg.getClassMapping(property.value.referencedEntityName))>
<#if parentPojo.isComponent(parentPojo.identifierProperty)>
<#foreach componentProperty in parentPojo.identifierProperty.value.propertyIterator>
        <h:column>
            <f:facet name="header">
<#assign propertyPath = property.name + '.' + parentPojo.identifierProperty.name + '.' + componentProperty.name>
                <ui:include src="layout/sort.xhtml">
                    <ui:param name="entityList" value="${'#'}{${listName}}"/>
                    <ui:param name="propertyLabel" value="${label(property.name)} ${label(componentProperty.name)?uncap_first}"/>
                    <ui:param name="propertyPath" value="${componentName}.${propertyPath}"/>
                </ui:include>
            </f:facet>
            <@outputValue property=componentProperty expression="${'#'}{_${componentName}.${propertyPath}}" indent=12/>
        </h:column>
</#foreach>
<#else>
        <h:column>
            <f:facet name="header">
<#assign propertyPath = property.name + '.' + parentPojo.identifierProperty.name>
                <ui:include src="layout/sort.xhtml">
                    <ui:param name="entityList" value="${'#'}{${listName}}"/>
                    <ui:param name="propertyLabel" value="${label(property.name)} ${label(parentPojo.identifierProperty.name)?uncap_first}"/>
                    <ui:param name="propertyPath" value="${componentName}.${propertyPath}"/>
                </ui:include>
            </f:facet>
            <@outputValue property=parentPojo.identifierProperty expression="${'#'}{_${componentName}.${propertyPath}}" indent=12/>
        </h:column>
</#if>
</#if>
</#foreach>
        <rich:column styleClass="action">
            <f:facet name="header">Action</f:facet>
            <s:link view="/${'#'}{empty from ? '${pageName}' : from}.xhtml"
                   value="${'#'}{empty from ? 'View' : 'Select'}"
             propagation="${'#'}{empty from ? 'none' : 'default'}"
                      id="${componentName}ViewId">
<#if pojo.isComponent(pojo.identifierProperty)>
<#foreach componentProperty in pojo.identifierProperty.value.propertyIterator>
                <f:param name="${componentName}${componentProperty.name?cap_first}"
                        value="${'#'}{_${componentName}.${pojo.identifierProperty.name}.${componentProperty.name}}"/>
</#foreach>
<#else>
                <f:param name="${componentName}${pojo.identifierProperty.name?cap_first}"
                        value="${'#'}{_${componentName}.${pojo.identifierProperty.name}}"/>
</#if>
            </s:link>
            ${'#'}{' '}
            <s:link view="/${editPageName}.xhtml"
                   value="Edit"
             propagation="none"
                      id="${componentName}Edit"
                rendered="${'#'}{empty from}">
<#if pojo.isComponent(pojo.identifierProperty)>
<#foreach componentProperty in pojo.identifierProperty.value.propertyIterator>
                <f:param name="${componentName}${componentProperty.name?cap_first}"
                        value="${'#'}{_${componentName}.${pojo.identifierProperty.name}.${componentProperty.name}}"/>
</#foreach>
<#else>
                <f:param name="${componentName}${pojo.identifierProperty.name?cap_first}"
                        value="${'#'}{_${componentName}.${pojo.identifierProperty.name}}"/>
</#if>
            </s:link>
        </rich:column>
    </rich:dataTable>

    </div>
    </rich:panel>

    <div class="tableControl">

        <s:link view="/${listPageName}.xhtml"
            rendered="${'#'}{${listName}.previousExists}"
               value="${'#'}{messages.left}${'#'}{messages.left} First Page"
                  id="firstPage">
          <f:param name="firstResult" value="0"/>
        </s:link>

        <s:link view="/${listPageName}.xhtml"
            rendered="${'#'}{${listName}.previousExists}"
               value="${'#'}{messages.left} Previous Page"
                  id="previousPage">
            <f:param name="firstResult"
                    value="${'#'}{${listName}.previousFirstResult}"/>
        </s:link>

        <s:link view="/${listPageName}.xhtml"
            rendered="${'#'}{${listName}.nextExists}"
               value="Next Page ${'#'}{messages.right}"
                  id="nextPage">
            <f:param name="firstResult"
                    value="${'#'}{${listName}.nextFirstResult}"/>
        </s:link>

        <s:link view="/${listPageName}.xhtml"
            rendered="${'#'}{${listName}.nextExists}"
               value="Last Page ${'#'}{messages.right}${'#'}{messages.right}"
                  id="lastPage">
            <f:param name="firstResult"
                    value="${'#'}{${listName}.lastFirstResult}"/>
        </s:link>

    </div>

    <s:div styleClass="actionButtons" rendered="${'#'}{empty from}">
        <s:button view="/${editPageName}.xhtml"
                    id="create"
           propagation="none"
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
