<?xml version="1.0" encoding="UTF-8"?>

<page xmlns="http://jboss.com/products/seam/pages"

      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"

      xsi:schemaLocation="http://jboss.com/products/seam/pages http://jboss.com/products/seam/pages-2.1.xsd">

      
<#include "../util/TypeInfo.ftl">

<#assign entityName = pojo.shortName>
<#assign componentName = entityName?uncap_first>
<#assign listName = componentName + "List">

   <param name="firstResult" value="${'#'}{${listName}.firstResult}"/>
   <param name="sort" value="${'#'}{${listName}.orderColumn}"/>
   <param name="dir" value="${'#'}{${listName}.orderDirection}"/>
   <param name="logic" value="${'#'}{${listName}.restrictionLogicOperator}"/>
   <param name="from"/>
<#foreach property in pojo.allPropertiesIterator>
<#if !c2h.isCollection(property) && !isToOne(property) && property != pojo.versionProperty!>
<#if c2j.isComponent(property)>
<#foreach componentProperty in property.value.propertyIterator>
<#if isString(componentProperty)>
   <param name="${componentProperty.name}" value="${'#'}{${listName}.${componentName}.${property.name}.${componentProperty.name}}"/>
</#if>
</#foreach>
<#else>
<#if isString(property)>
   <param name="${property.name}" value="${'#'}{${listName}.${componentName}.${property.name}}"/>
</#if>
</#if>
</#if>
</#foreach>
</page>
