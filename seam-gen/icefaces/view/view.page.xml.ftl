<?xml version="1.0" encoding="UTF-8"?>

<page xmlns="http://jboss.com/products/seam/pages"

      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"

      xsi:schemaLocation="http://jboss.com/products/seam/pages http://jboss.com/products/seam/pages-2.2.xsd">

      

<#assign entityName = pojo.shortName>
<#assign componentName = entityName?uncap_first>
<#assign homeName = componentName + "Home">

   <param name="${componentName}From"/>
<#assign idName = componentName + pojo.identifierProperty.name?cap_first>
<#if c2j.isComponent(pojo.identifierProperty)>
<#foreach componentProperty in pojo.identifierProperty.value.propertyIterator>
<#assign cidName = componentName + componentProperty.name?cap_first>
   <param name="${cidName}" value="${'#'}{${homeName}.${idName}.${componentProperty.name}}"/>
</#foreach>
<#else>
   <param name="${idName}" value="${'#'}{${homeName}.${idName}}"/>
</#if>
<#assign entities=util.set()>
<#if entities.add(pojo.shortName)>
<#include "param.xml.ftl">
</#if>
</page>
