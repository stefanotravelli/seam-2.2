<#include "../util/TypeInfo.ftl">

<#if !c2h.isCollection(property) && !isToOne(property) && property != pojo.versionProperty!>
<#assign propertyIsId = property.equals(pojo.identifierProperty)>
<#if !propertyIsId || property.value.identifierGeneratorStrategy == "assigned">
<#if pojo.isComponent(property)>
<#foreach componentProperty in property.value.propertyIterator>
<#assign column = componentProperty.columnIterator.next()>

            <s:decorate id="${componentProperty.name}Field" template="layout/edit.xhtml">
                <ui:define name="label">${label(componentProperty.name)}</ui:define>
<#if isDate(componentProperty)>
                        <ice:selectInputDate id="${componentProperty.name}" 
                              renderAsPopup="true"
<#if propertyIsId>
                              disabled="${'#'}{${homeName}.managed}"
</#if>
<#if !column.nullable>
                       required="true"
</#if>
                                value="${'#'}{${homeName}.instance.${property.name}.${componentProperty.name}}"> 
                               <s:convertDateTime type="date"/>                                                          
                        </ice:selectInputDate>
<#elseif isTime(componentProperty)>
                        <ice:selectInputDate id="${componentProperty.name}" 
<#if !column.nullable>
                            required="true"
</#if>
                            value="${'#'}{${homeName}.instance.${property.name}.${componentProperty.name}}">
                           <s:convertDateTime type="time"/>
                        </ice:selectInputDate>
<#elseif isTimestamp(componentProperty)>
                        <ice:selectInputDate id="${componentProperty.name}" 
<#if !column.nullable>
                            required="true"
</#if>
                            value="${'#'}{${homeName}.instance.${property.name}.${componentProperty.name}}">
                            <s:convertDateTime type="both" dateStyle="short"/>
                        </ice:inputText>
<#elseif isBigDecimal(componentProperty)>
                        <ice:inputText id="${componentProperty.name}" 
                              partialSubmit="true"
<#if !column.nullable>
                       required="true"
</#if>
                          value="${'#'}{${homeName}.instance.${property.name}.${componentProperty.name}}"
                           size="${column.precision+7}">
                        </ice:inputText>
<#elseif isBigInteger(componentProperty)>
                        <ice:inputText id="${componentProperty.name}" 
					partialSubmit="true"
<#if propertyIsId>
                       disabled="${'#'}{${homeName}.managed}"
</#if>
<#if !column.nullable>
                       required="true"
</#if>
                          value="${'#'}{${homeName}.instance.${property.name}.${componentProperty.name}}"
                           size="${column.precision+6}">
                        </ice:inputText>
<#elseif isBoolean(componentProperty)>
                 <h:selectBooleanCheckbox id="${componentProperty.name}"
<#if !column.nullable>
                                    required="true"
</#if>
<#if propertyIsId>
                                    disabled="${'#'}{${homeName}.managed}"
</#if>
                                       value="${'#'}{${homeName}.instance.${property.name}.${componentProperty.name}}"/>
<#elseif isString(componentProperty)>
<#if column.length gt 160>
<#if column.length gt 800>
<#assign rows = 10>
<#else>
<#assign rows = (column.length/80)?int>
</#if>
                <ice:inputTextarea id="${componentProperty.name}"
                               cols="80"
                               rows="${rows}"
<#if propertyIsId>
                           disabled="${'#'}{${homeName}.managed}"
</#if>
<#if !column.nullable>
                           required="true"
</#if>
                              value="${'#'}{${homeName}.instance.${property.name}.${componentProperty.name}}"/>
<#else>
<#if column.length gt 100>
<#assign size = 100>
<#else>
<#assign size = column.length>
</#if>
                <ice:inputText id="${componentProperty.name}" 
<#if propertyIsId>
                       disabled="${'#'}{${homeName}.managed}"
</#if>
<#if !column.nullable>
                      required="true"
</#if>
                          size="${size}"
                     maxlength="${column.length}"
                         partialSubmit="true"
                         value="${'#'}{${homeName}.instance.${property.name}.${componentProperty.name}}">
                 </ice:inputText>
</#if>
<#else>
                <ice:inputText id="${componentProperty.name}"
<#if !column.nullable>
                       required="true"
</#if>
<#if propertyIsId>
                       disabled="${'#'}{${homeName}.managed}"
</#if>                        
                         partialSubmit="true"
                         value="${'#'}{${homeName}.instance.${property.name}.${componentProperty.name}}">
                 </ice:inputText>
</#if>
               </s:decorate>
</#foreach>
<#else>
<#assign column = property.columnIterator.next()>

            <s:decorate id="${property.name}Field" template="layout/edit.xhtml">
                <ui:define name="label">${label(property.name)}</ui:define>
<#if isDate(property)>
                        <ice:selectInputDate id="${property.name}" 
                              renderAsPopup="true"

<#if propertyIsId>
                             disabled="${'#'}{${homeName}.managed}"
</#if>
<#if !column.nullable>
                             required="true"
</#if>
                                  value="${'#'}{${homeName}.instance.${property.name}}">
                                    <s:convertDateTime type="date"/> 
                       </ice:selectInputDate>
<#elseif isTime(property)>
                       <ice:selectInputDate id="${property.name}" 
					       partialSubmit="true"
<#if !column.nullable>
                               required="true"
</#if>
                                  value="${'#'}{${homeName}.instance.${property.name}}">
                            <s:convertDateTime type="time"/>
                        </ice:selectInputDate>
<#elseif isTimestamp(property)>
                        <ice:selectInputDate id="${property.name}" 
                                     size="16"
			                partialSubmit="true"
<#if !column.nullable>
                                 required="true"
</#if>
                                   value="${'#'}{${homeName}.instance.${property.name}}">
                            <s:convertDateTime type="both" dateStyle="short"/>
                        </ice:selectInputDate>
<#elseif isBigDecimal(property)>
                        <ice:inputText id="${property.name}" 
			                partialSubmit="true"
<#if !column.nullable>
                                 required="true"
</#if>
                                 value="${'#'}{${homeName}.instance.${property.name}}"
                                 size="${column.precision+7}">
                        </ice:inputText>
<#elseif isBigInteger(property)>
                        <ice:inputText id="${property.name}"
			                partialSubmit="true"
<#if propertyIsId>
                            disabled="${'#'}{${homeName}.managed}"
</#if>
<#if !column.nullable>
                            required="true"
</#if>
                          value="${'#'}{${homeName}.instance.${property.name}}"
                           size="${column.precision+6}"/>
<#elseif isBoolean(property)>
                        <ice:selectBooleanCheckbox id="${property.name}"
					           partialSubmit="true"
<#if !column.nullable>
                                   required="true"
</#if>
<#if propertyIsId>
                                   disabled="${'#'}{${homeName}.managed}"
</#if>
                                      value="${'#'}{${homeName}.instance.${property.name}}"/>
<#elseif isString(property)>
<#if column.length gt 160>
<#if column.length gt 800>
<#assign rows = 10>
<#else>
<#assign rows = (column.length/80)?int>
</#if>
                <ice:inputTextarea id="${property.name}"
                               cols="80"
                               rows="${rows}"
<#if propertyIsId>
                           disabled="${'#'}{${homeName}.managed}"
</#if>
<#if !column.nullable>
                           required="true"
</#if>
                              value="${'#'}{${homeName}.instance.${property.name}}"/>
<#else>
<#if column.length gt 100>
<#assign size = 100>
<#else>
<#assign size = column.length>
</#if>
                        <ice:inputText id="${property.name}" 
			                partialSubmit="true"
<#if propertyIsId>
                                 disabled="${'#'}{${homeName}.managed}"
</#if>
<#if !column.nullable>
                                 required="true"
</#if>
                                   size="${size}"
                              maxlength="${column.length}"
                                  value="${'#'}{${homeName}.instance.${property.name}}">
                        </ice:inputText>
</#if>
<#else>
                        <ice:inputText id="${property.name}"
			                partialSubmit="true"
<#if !column.nullable>
                       required="true"
</#if>
<#if propertyIsId>
                       disabled="${'#'}{${homeName}.managed}"
</#if>
                                    value="${'#'}{${homeName}.instance.${property.name}}">
                        </ice:inputText>
</#if>
            </s:decorate>
</#if>
</#if>
</#if>
