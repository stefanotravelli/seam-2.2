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
    xmlns:rich="http://richfaces.org/rich"
    template="layout/template.xhtml">

<ui:define name="body">

    <rich:panel>
        <f:facet name="header">${label(entityName)} Details</f:facet>
<#foreach property in pojo.allPropertiesIterator>
<#if !c2h.isCollection(property) && !isToOne(property) && property != pojo.versionProperty!>
<#include "viewproperty.xhtml.ftl">
</#if>
</#foreach>

        <div style="clear:both"/>

    </rich:panel>

    <div class="actionButtons">

        <s:button view="/${editPageName}.xhtml"
                    id="edit"
                 value="Edit"/>

        <s:button view="/${'#'}{empty ${componentName}From ? '${masterPageName}' : ${componentName}From}.xhtml"
                    id="done"
                 value="Done"/>

    </div>
<#assign hasAssociations=false>
<#foreach property in pojo.allPropertiesIterator>
<#if isToOne(property) || c2h.isOneToManyCollection(property)>
<#assign hasAssociations=true>
</#if>
</#foreach>

<#if hasAssociations>
    <rich:tabPanel switchType="ajax">
</#if>
<#foreach property in pojo.allPropertiesIterator>
<#if isToOne(property)>
<#assign parentPojo = c2j.getPOJOClass(cfg.getClassMapping(property.value.referencedEntityName))>
<#assign parentPageName = parentPojo.shortName>
<#assign parentName = parentPojo.shortName?uncap_first>

    <rich:tab>
        <f:facet name="label">
            <h:panelGroup><h:graphicImage value="/img/manytoone.gif" style="vertical-align: middle; padding-right: 4px;"/>${label(property.name)}</h:panelGroup>
        </f:facet>
    <div class="association" id="${property.name}Parent">

        <h:outputText value="There is no ${property.name} associated with this ${componentName}."
                   rendered="${'#'}{${homeName}.instance.${property.name} == null}"/>

        <rich:dataTable var="_${parentName}"
                   value="${'#'}{${homeName}.instance.${property.name}}"
                rendered="${'#'}{${homeName}.instance.${property.name} != null}"
              rowClasses="rvgRowOne,rvgRowTwo"
                      id="${property.name}Table">
<#foreach parentProperty in parentPojo.allPropertiesIterator>
<#if !c2h.isCollection(parentProperty) && !isToOne(parentProperty) && parentProperty != parentPojo.versionProperty!>
<#if parentPojo.isComponent(parentProperty)>
<#foreach componentProperty in parentProperty.value.propertyIterator>
            <h:column>
                <f:facet name="header">${label(componentProperty.name)}</f:facet>
                <@outputValue property=componentProperty expression="${'#'}{_${parentName}.${parentProperty.name}.${componentProperty.name}}" indent=16/>
            </h:column>
</#foreach>
<#else>
            <h:column>
                <f:facet name="header">${label(parentProperty.name)}</f:facet>
                <@outputValue property=parentProperty expression="${'#'}{_${parentName}.${parentProperty.name}}" indent=16/>
            </h:column>
</#if>
</#if>
<#if isToOne(parentProperty)>
<#assign grandparentPojo = c2j.getPOJOClass(cfg.getClassMapping(parentProperty.value.referencedEntityName))>
<#if grandparentPojo.isComponent(grandparentPojo.identifierProperty)>
<#foreach componentProperty in grandparentPojo.identifierProperty.value.propertyIterator>
            <h:column>
                <f:facet name="header">${label(parentProperty.name)} ${label(componentProperty.name)?uncap_first}</f:facet>
                <@outputValue property=componentProperty expression="${'#'}{_${parentName}.${parentProperty.name}.${grandparentPojo.identifierProperty.name}.${componentProperty.name}}" indent=16/>
            </h:column>
</#foreach>
<#else>
            <h:column>
                <f:facet name="header">${label(parentProperty.name)} ${label(grandparentPojo.identifierProperty.name)?uncap_first}</f:facet>
                <@outputValue property=grandparentPojo.identifierProperty expression="${'#'}{_${parentName}.${parentProperty.name}.${grandparentPojo.identifierProperty.name}}" indent=16/>
            </h:column>
</#if>
</#if>
</#foreach>
            <h:column styleClass="action">
                <f:facet name="header">Action</f:facet>
                <s:link id="view${parentName}"
                     value="View"
                      view="/${parentPageName}.xhtml">
<#if parentPojo.isComponent(parentPojo.identifierProperty)>
<#foreach componentProperty in parentPojo.identifierProperty.value.propertyIterator>
                    <f:param name="${parentName}${componentProperty.name?cap_first}"
                            value="${'#'}{_${parentName}.${parentPojo.identifierProperty.name}.${componentProperty.name}}"/>
</#foreach>
<#else>
                    <f:param name="${parentName}${parentPojo.identifierProperty.name?cap_first}"
                           value="${'#'}{_${parentName}.${parentPojo.identifierProperty.name}}"/>
</#if>
                </s:link>
            </h:column>
        </rich:dataTable>

    </div>
    </rich:tab>
</#if>
<#if c2h.isOneToManyCollection(property)>

    <rich:tab>
        <f:facet name="label">
            <h:panelGroup><h:graphicImage value="/img/onetomany.gif" style="vertical-align: middle; padding-right: 4px;"/>${label(property.name)}</h:panelGroup>
        </f:facet>
    <h:form styleClass="association" id="${property.name}Children">

<#assign childPojo = c2j.getPOJOClass(property.value.element.associatedClass)>
<#assign childPageName = childPojo.shortName>
<#assign childEditPageName = childPojo.shortName + "Edit">
<#assign childName = childPojo.shortName?uncap_first>
<#assign childHomeName = childName + "Home">
        <h:outputText value="There are no ${property.name} associated with this ${componentName}."
                   rendered="${'#'}{empty ${homeName}.${property.name}}"/>

        <rich:dataTable value="${'#'}{${homeName}.${property.name}}"
                       var="_${childName}"
                  rendered="${'#'}{not empty ${homeName}.${property.name}}"
                rowClasses="rvgRowOne,rvgRowTwo"
                        id="${property.name}Table">
<#foreach childProperty in childPojo.allPropertiesIterator>
<#if !c2h.isCollection(childProperty) && !isToOne(childProperty) && childProperty != childPojo.versionProperty!>
<#if childPojo.isComponent(childProperty)>
<#foreach componentProperty in childProperty.value.propertyIterator>
            <rich:column sortBy="${'#'}{_${childName}.${childProperty.name}.${componentProperty.name}}">
                <f:facet name="header">${label(componentProperty.name)}</f:facet>
                <@outputValue property=componentProperty expression="${'#'}{_${childName}.${childProperty.name}.${componentProperty.name}}" indent=16/>
            </rich:column>
</#foreach>
<#else>
            <rich:column sortBy="${'#'}{_${childName}.${childProperty.name}}">
                <f:facet name="header">${label(childProperty.name)}</f:facet>
                <@outputValue property=childProperty expression="${'#'}{_${childName}.${childProperty.name}}" indent=16/>
            </rich:column>
</#if>
</#if>
</#foreach>
            <h:column>
                <f:facet name="header">Action</f:facet>
                <s:link id="select${childName}"
                     value="Select"
                      view="/${childPageName}.xhtml">
<#if childPojo.isComponent(childPojo.identifierProperty)>
<#foreach componentProperty in childPojo.identifierProperty.value.propertyIterator>
                    <f:param name="${childName}${componentProperty.name?cap_first}"
                            value="${'#'}{_${childName}.${childPojo.identifierProperty.name}.${componentProperty.name}}"/>
</#foreach>
<#else>
                    <f:param name="${childName}${childPojo.identifierProperty.name?cap_first}"
                            value="${'#'}{_${childName}.${childPojo.identifierProperty.name}}"/>
</#if>
                    <f:param name="${childName}From" value="${entityName}"/>
                </s:link>
            </h:column>
        </rich:dataTable>

    </h:form>

    <div class="actionButtons">
        <s:button
               value="Add ${childName}"
                view="/${childEditPageName}.xhtml">
            <f:param name="${componentName}${pojo.identifierProperty.name?cap_first}"
                    value="${'#'}{${homeName}.instance.${pojo.identifierProperty.name}}"/>
            <f:param name="${childName}From" value="${entityName}"/>
        </s:button>
    </div>
    </rich:tab>
</#if>
</#foreach>
<#if hasAssociations>
</rich:tabPanel>
</#if>
</ui:define>

</ui:composition>
