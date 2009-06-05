<#include "../util/TypeInfo.ftl">

<#if !property.equals(pojo.identifierProperty) || property.value.identifierGeneratorStrategy == "assigned">
<#if c2j.isComponent(property)>
<#foreach componentProperty in property.value.propertyIterator>
        <s:decorate id="${componentProperty.name}" template="layout/display.xhtml">
            <ui:define name="label">${label(componentProperty.name)}</ui:define>
            <@outputValue property=componentProperty expression="${'#'}{${homeName}.instance.${property.name}.${componentProperty.name}}" indent=12/>
        </s:decorate>
</#foreach>
<#else>
        <s:decorate id="${property.name}" template="layout/display.xhtml">
            <ui:define name="label">${label(property.name)}</ui:define>
            <@outputValue property=property expression="${'#'}{${homeName}.instance.${property.name}}" indent=12/>
        </s:decorate>
</#if>
</#if>
