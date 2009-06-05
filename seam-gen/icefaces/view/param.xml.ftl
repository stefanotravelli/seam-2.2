<#include "../util/TypeInfo.ftl">
<#foreach property in pojo.allPropertiesIterator>
<#if isToOne(property)>
<#assign parentPojo = c2j.getPOJOClass(cfg.getClassMapping(property.value.referencedEntityName))>
<#if entities.add(parentPojo.shortName)>
<#assign parentComponentName = parentPojo.shortName?uncap_first>
<#assign parentHomeName = parentComponentName + "Home">
   <param name="${parentComponentName}From"/>
<#assign parentIdName = parentComponentName + parentPojo.identifierProperty.name?cap_first>
<#if c2j.isComponent(parentPojo.identifierProperty)>
<#foreach parentComponentProperty in parentPojo.identifierProperty.value.propertyIterator>
<#assign parentCidName = parentComponentName + parentComponentProperty.name?cap_first>
   <param name="${parentCidName}" value="${'#'}{${parentHomeName}.${parentIdName}.${parentComponentProperty.name}}"/>
</#foreach>
<#else>
   <param name="${parentIdName}" value="${'#'}{${parentHomeName}.${parentIdName}}"/>
</#if>
<#assign p = pojo>
<#assign pojo = parentPojo>
<#include "param.xml.ftl">
<#assign pojo = p>
</#if>
</#if>
</#foreach>

