<?xml version="1.0" encoding="UTF-8"?>
<#assign entityName = pojo.shortName>
<#assign componentName = entityName?uncap_first>
<#assign homeName = componentName + "Home">
<#assign masterPageName = entityName + "List">
<#assign pageName = entityName>
<page xmlns="http://jboss.com/products/seam/pages"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://jboss.com/products/seam/pages http://jboss.com/products/seam/pages-2.2.xsd"
      no-conversation-view-id="/${masterPageName}.xhtml"
      login-required="true">

   <begin-conversation join="true" flush-mode="MANUAL"/>

   <action execute="${'#'}{${homeName}.wire}"/>

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

   <navigation from-action="${'#'}{${homeName}.persist}">
      <rule if-outcome="persisted">
         <end-conversation/>
         <redirect view-id="/${pageName}.xhtml"/>
      </rule>
   </navigation>

   <navigation from-action="${'#'}{${homeName}.update}">
      <rule if-outcome="updated">
         <end-conversation/>
         <redirect view-id="/${pageName}.xhtml"/>
      </rule>
   </navigation>

   <navigation from-action="${'#'}{${homeName}.remove}">
      <rule if-outcome="removed">
         <end-conversation/>
         <redirect view-id="/${masterPageName}.xhtml"/>
      </rule>
   </navigation>

</page>
