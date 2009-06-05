<#include "../util/TypeInfo.ftl">

<#if !property.equals(pojo.identifierProperty) || property.value.identifierGeneratorStrategy == "assigned">
<#if c2j.isComponent(property)>
<#foreach componentProperty in property.value.propertyIterator>

        <s:decorate id="${componentProperty.name}" template="layout/display.xhtml">
            <ui:define name="label">${label(componentProperty.name)}</ui:define>
<#if isDate(componentProperty)>
            <ice:outputText id="view${componentProperty.name}TextId"
	                 value="${'#'}{${homeName}.instance.${property.name}.${componentProperty.name}}">
                <s:convertDateTime type="date" dateStyle="short"/>
            </ice:outputText>
<#elseif isTime(componentProperty)>
             <ice:outputText id="view${componentProperty.name}TextId" 
	                  value="${'#'}{${homeName}.instance.${property.name}.${componentProperty.name}}">
                <s:convertDateTime type="time"/>
             </ice:outputText>
<#elseif isTimestamp(componentProperty)>
            <ice:outputText id="view${componentProperty.name}TextId"
	                 value="${'#'}{${homeName}.instance.${property.name}.${componentProperty.name}}">
                <s:convertDateTime type="both" dateStyle="short"/>
            </ice:outputText>
<#elseif isBigDecimal(componentProperty)>
            <ice:outputText id="view${componentProperty.name}TextId"
	                 value="${'#'}{${homeName}.instance.${property.name}.${componentProperty.name}}">
                <f:convertNumber/>
            </ice:outputText>
<#elseif isBigInteger(componentProperty)>
            <ice:outputText id="view${componentProperty.name}TextId"
	                 value="${'#'}{${homeName}.instance.${property.name}.${componentProperty.name}}">
                <f:convertNumber integerOnly="true"/>
            </ice:outputText>
<#else>
            ${'#'}{${homeName}.instance.${property.name}.${componentProperty.name}}&#160;
</#if>
        </s:decorate>
</#foreach>
<#else>

        <s:decorate id="${property.name}" template="layout/display.xhtml">
            <ui:define name="label">${label(property.name)}</ui:define>
<#if isDate(property)>
            <ice:outputText id="view${property.name}TextId"
	                 value="${'#'}{${homeName}.instance.${property.name}}">
                <s:convertDateTime type="date" dateStyle="short"/>
            </ice:outputText>
<#elseif isTime(property)>
            <ice:outputText id="view${property.name}TextId"
	                 value="${'#'}{${homeName}.instance.${property.name}}">
                <s:convertDateTime type="time"/>
            </ice:outputText>
<#elseif isTimestamp(property)>
            <ice:outputText id="view${property.name}TextId"
	                 value="${'#'}{${homeName}.instance.${property.name}}">
                <s:convertDateTime type="both" dateStyle="short"/>
            </ice:outputText>
<#elseif isBigDecimal(property)>
            <ice:outputText id="view${property.name}TextId"
	                 value="${'#'}{${homeName}.instance.${property.name}}">
                <f:convertNumber/>
            </ice:outputText>
<#elseif isBigInteger(property)>
            <ice:outputText id="view${property.name}TextId"
	                 value="${'#'}{${homeName}.instance.${property.name}}">
                <f:convertNumber integerOnly="true"/>
            </ice:outputText>
<#else>
            ${'#'}{${homeName}.instance.${property.name}}&#160;
</#if>
        </s:decorate>
</#if>
</#if>
