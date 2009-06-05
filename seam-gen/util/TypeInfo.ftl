<#-- FIXME: This file should be renamed since it now has auxiliary functions and macros -->
<#function isTimestamp property>
	<#return property.value.typeName == "timestamp"/>
</#function>

<#function isTime property>
	<#return property.value.typeName == "time"/>
</#function>

<#function isDate property>
	<#return property.value.typeName == "date" || property.value.typeName =="java.util.Date"/>
</#function>

<#function isBigDecimal property>
	<#return property.value.typeName == "big_decimal" || property.value.typeName =="java.math.BigDecimal"/>
</#function>

<#function isBigInteger property>
	<#return property.value.typeName == "big_integer" || property.value.typeName =="java.util.BigInteger"/>
</#function>

<#function isBoolean property>
	<#return property.value.typeName == "boolean" || property.value.typeName =="yes_no" || property.value.typeName =="true_false" || property.value.typeName == "java.lang.Boolean"/>
</#function>

<#function isString property>
	<#return property.value.typeName == "string" || property.value.typeName =="java.lang.String"/>
</#function>

<#function isToOne property>
    <#return property.value.class.name.matches("org.hibernate.mapping.(One|Many)ToOne")/>
</#function>

<#function label property>
    <#return property?replace("([^A-Z]*)([A-Z]|$)", "$1 $2", "r")?trim?lower_case?cap_first/>
</#function>

<#macro outputValue property expression indent>
    <#assign padding = ""?left_pad(indent)/>
    <#if isDate(property)>
${padding}<h:outputText value="${expression}">
${padding}    <s:convertDateTime type="date" dateStyle="short"/>
${padding}</h:outputText>
    <#elseif isTime(property)>
${padding}<h:outputText value="${expression}">
${padding}    <s:convertDateTime type="time"/>
${padding}</h:outputText>
    <#elseif isTimestamp(property)>
${padding}<h:outputText value="${expression}">
${padding}    <s:convertDateTime type="both" dateStyle="short"/>
${padding}</h:outputText>
    <#elseif isBigDecimal(property)>
${padding}<h:outputText value="${expression}">
${padding}    <f:convertNumber/>
${padding}</h:outputText>
    <#elseif isBigInteger(property)>
${padding}<h:outputText value="${expression}">
${padding}    <f:convertNumber integerOnly="true"/>
${padding}</h:outputText>
    <#else>
${padding}<h:outputText value="${expression}"/>
    </#if>
</#macro>
