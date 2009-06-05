<#include "Ejb3PropertyGetAnnotation.ftl"/>
<#if !property.equals(pojo.identifierProperty) && property.type.name=='yes_no'><#-- Set column type to yes_no in seam-gen.reveng.xml to activate this annotation (or tune this condition) -->
    @${pojo.importType("org.hibernate.annotations.Type")}(type = "yes_no")
</#if>
<#if !property.optional && !c2j.isPrimitive(pojo.getJavaTypeName(property, jdk5)) && (!property.equals(pojo.identifierProperty) || property.value.identifierGeneratorStrategy == "assigned")>
    @${pojo.importType("org.hibernate.validator.NotNull")}
</#if>
<#if property.columnSpan==1>
<#assign column = property.getColumnIterator().next()/>
<#if !c2h.isManyToOne(property) && !c2h.isTemporalValue(property) && column.length!=255 && property.type.name!="character" && pojo.getJavaTypeName(property, jdk5)?lower_case!="boolean">
    @${pojo.importType("org.hibernate.validator.Length")}(max=${column.length?c})
</#if>
</#if>
