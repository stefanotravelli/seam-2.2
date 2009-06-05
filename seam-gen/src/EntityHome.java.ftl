<#include "../util/TypeInfo.ftl">
package ${actionPackage};
<#assign classbody>
<#assign entityName = pojo.shortName>
<#assign componentName = entityName?uncap_first>
<#assign homeName = componentName + "Home">
@${pojo.importType("org.jboss.seam.annotations.Name")}("${homeName}")
public class ${entityName}Home extends ${pojo.importType("org.jboss.seam.framework.EntityHome")}<${entityName}>
{

<#assign parentHomeNames = []>
<#foreach property in pojo.allPropertiesIterator>
<#if isToOne(property)>
<#assign parentPojo = c2j.getPOJOClass(cfg.getClassMapping(property.value.referencedEntityName))>
<#assign parentHomeName = parentPojo.shortName?uncap_first + "Home">
<#if !parentHomeNames?seq_contains(parentHomeName)><#-- This doesn't fix the functionality, just allows compilation to work -->
<#assign parentHomeNames = parentHomeNames + [parentHomeName]>
    @${pojo.importType("org.jboss.seam.annotations.In")}(create=true)
    <#if parentPojo.packageName!="">${pojo.importType("${parentPojo.packageName}.${parentPojo.shortName}")}<#else>${parentPojo.shortName}</#if>Home ${parentHomeName};
</#if>
</#if>
</#foreach>

<#assign idName = entityName + pojo.identifierProperty.name?cap_first>
<#if c2j.isComponent(pojo.identifierProperty)>
<#assign idType = entityName + "Id">
<#else>
<#assign idType = pojo.importType(pojo.identifierProperty.type.returnedClass.name)>
</#if>
    public void set${idName}(${idType} id)
    {
        setId(id);
    }

    public ${idType} get${idName}()
    {
        return (${idType}) getId();
    }

<#if pojo.isComponent(pojo.identifierProperty)>
    public ${entityName}Home()
    {
        set${idName}( new ${entityName}Id() );
    }

    @Override
    public boolean isIdDefined()
    {
<#foreach property in pojo.identifierProperty.value.propertyIterator>
<#assign getter = pojo.getGetterSignature(property)>
<#if property.value.typeName == "string" || property.value.typeName == "java.lang.String" >
        if ( get${idName}().${getter}()==null || "".equals( get${idName}().${getter}() ) ) return false;
<#elseif !c2j.isPrimitive( pojo.getJavaTypeName(property, true) )>
        if ( get${idName}().${getter}()==null ) return false;
<#else>
        if ( get${idName}().${getter}()==0 ) return false;
</#if>
</#foreach>
        return true;
    }

</#if>
    @Override
    protected ${entityName} createInstance()
    {
        ${entityName} ${componentName} = new ${entityName}();
<#if pojo.isComponent(pojo.identifierProperty)>
        ${componentName}.setId( new ${entityName}Id() );
</#if>
        return ${componentName};
    }

    public void load()
    {
        if (isIdDefined())
        {
            wire();
        }
    }

    public void wire()
    {
        getInstance();
<#foreach property in pojo.allPropertiesIterator>
<#if isToOne(property)>
<#assign parentPojo = c2j.getPOJOClass(cfg.getClassMapping(property.value.referencedEntityName))>
<#if parentPojo.shortName!=pojo.shortName>
<#assign parentHomeName = parentPojo.shortName?uncap_first + "Home">
<#assign setter = "set" + pojo.getPropertyName(property)>
        ${parentPojo.shortName} ${property.name}=${parentHomeName}.getDefinedInstance();
        if ( ${property.name}!=null )
        {
           getInstance().${setter}(${property.name});
        }
</#if>
</#if>
</#foreach>
    }

    public boolean isWired()
    {
<#foreach property in pojo.allPropertiesIterator>
<#if (isToOne(property) && !property.optional)>
<#assign getter = pojo.getGetterSignature(property)>
        if ( getInstance().${getter}()==null ) return false;
</#if>
</#foreach>
        return true;
    }

    public ${entityName} getDefinedInstance()
    {
        return isIdDefined() ? getInstance() : null;
    }

<#foreach property in pojo.allPropertiesIterator>
<#assign getter = pojo.getGetterSignature(property)>
<#if c2h.isOneToManyCollection(property)>
<#assign childPojo = c2j.getPOJOClass(property.value.element.associatedClass)>
    public ${pojo.importType("java.util.List")}<<#if childPojo.packageName!="">${pojo.importType("${childPojo.packageName}.${childPojo.shortName}")}<#else>${childPojo.shortName}</#if>> ${getter}() {
        return getInstance() == null ?
            null : new ${pojo.importType("java.util.ArrayList")}<${childPojo.shortName}>( getInstance().${getter}() );
    }
</#if>
</#foreach>

}
</#assign>

<#if pojo.packageName != "">
import ${pojo.packageName}.*;<#-- This import is necessary because we're using a different package than Hibernate Tools expects -->
</#if>
${pojo.generateImports()}
${classbody}
