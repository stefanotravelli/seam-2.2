package org.jboss.seam.el;

import org.jboss.seam.util.JSF;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;

import org.jboss.seam.Namespace;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Init;

/**
 * Resolves Seam components and namespaces. Also
 * allows the use of #{dataModel.size}, #{dataModel.empty},
 * #{collection.size}, #{map.size}, #{map.values}, #{map.keySet},
 * and #{map.entrySet}. Also allows #{sessionContext['name']}.
 * 
 * @author Gavin King
 *
 */
public class SeamELResolver extends ELResolver
{

   @Override
   public Class getCommonPropertyType(ELContext context, Object base)
   {
      return null;
   }

   @Override
   public Iterator getFeatureDescriptors(ELContext context, Object base)
   {
      return null;
   }

   @Override
   public Class getType(ELContext context, Object base, Object property)
   {
      return null;
   }

   @Override
   public Object getValue(ELContext context, Object base, Object property) 
   {
        if (base == null) {
            return resolveBase(context, property);
            
        } else if (base instanceof Namespace) {
            return resolveInNamespace(context, (Namespace) base, property);
            
        } else if (JSF.DATA_MODEL.isInstance(base)) {
            return resolveInDataModel(context,  base, property);
            
        } else if (base instanceof Collection) {
            return resolveInCollection(context, (Collection) base, property);
            
        } else if (base instanceof Map) {
            return resolveInMap(context, (Map) base, property);
            
        } else if (base instanceof Context) {
            return resolveInContextObject(context, (Context) base, property);
            
        } else {
            return null;
        }
    }

   private Object resolveInContextObject(ELContext context, Context seamContext, Object property)
   {
        if (seamContext.isSet((String) property)) {
            context.setPropertyResolved(true);
            return seamContext.get((String) property);
        } else {
            return null;
        }
    }

   private boolean containsKey(Map map, String key) {
      try {
         return map.containsKey(key);   
      } catch (UnsupportedOperationException e) {
         // eat it
         return false;
      }
   }
   
   private Object resolveInMap(ELContext context, Map map, Object property) {         
        if ("size".equals(property) && !containsKey(map,"size")) {
            context.setPropertyResolved(true);
            return map.size();
            
        } else if ("values".equals(property) && !containsKey(map,"values")) {
            context.setPropertyResolved(true);
            return map.values();
        
        } else if ("keySet".equals(property) && !containsKey(map,"keySet")) {
            context.setPropertyResolved(true);
            return map.keySet();
        
        } else if ("entrySet".equals(property) && !containsKey(map,"entrySet")) {
            context.setPropertyResolved(true);
            return map.entrySet();
        
        } else {
            return null;
        }
    }

   private Object resolveInCollection(ELContext context, Collection collection, Object property)
   {
        if ("size".equals(property)) {
            context.setPropertyResolved(true);
            return collection.size();
        } else {
            return null;
        }
    }

   private Object resolveInDataModel(ELContext context, Object base, Object property)
   {
        if ("size".equals(property)) {
            context.setPropertyResolved(true);
            return JSF.getRowCount(base);
        } else if ("empty".equals(property)) {
            context.setPropertyResolved(true);
            return JSF.getRowCount(base) == 0;
        } else {
            return null;
        }
    }

   private Object resolveBase(ELContext context, Object property)
   {
      if (!Contexts.isApplicationContextActive()) {
            // if no Seam contexts, bypass straight through to JSF
            return null;
        }

        String key = (String) property;
        Init init = Init.instance();

        // look for a component in the root namespace
        Object result = init.getRootNamespace().getComponentInstance(key);
        if (result != null) {
            context.setPropertyResolved(true);
            return result;
        } else {
            // look for a component in the imported namespaces
            for (Namespace ns : init.getGlobalImports()) {
                result = ns.getComponentInstance(key);
                if (result != null) {
                    context.setPropertyResolved(true);
                    return result;
                }
            }
        }

        // look for a namespace
        Namespace namespace = init.getRootNamespace().getChild(key);
        if (namespace != null) {
            context.setPropertyResolved(true);
        }
        return namespace;
    }

    private Object resolveInNamespace(ELContext context, Namespace namespace, Object property) {
        Object result = namespace.get((String) property);
        // JBSEAM-3077 if the result is null, it means that there is no component instance bound to this qualified name
        context.setPropertyResolved(true);
        return result;
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) 
    {
        return base != null
                && (JSF.DATA_MODEL.isInstance(base) || (base instanceof Collection) || (base instanceof Map));
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) 
    {
    }

}
