package org.jboss.seam.persistence;

import static org.jboss.seam.util.JSF.DATA_MODEL;
import static org.jboss.seam.util.JSF.getWrappedData;
import static org.jboss.seam.util.JSF.setWrappedData;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.seam.Component;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.In;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Manager;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Reflections;

/**
 * @author Gavin King
 * @author Pete Muir
 * @author Norman Richards
 * @author Dan Allen
 */
public class ManagedEntityWrapper
{

   private static LogProvider log = Logging.getLogProvider(ManagedEntityWrapper.class);
   
   public void wrap(Object target, Component component) throws Exception
   {
      if ( !touchedContextsExist() )
      {
         log.trace("No touched persistence contexts. Therefore, there are no entities in this conversation whose identities need to be preserved.");
         return;
      }
      
      String oldCid = switchToConversationContextOfComponent(component);
      Class beanClass = target.getClass();
      for (; beanClass!=Object.class; beanClass=beanClass.getSuperclass())
      {
         log.trace("Examining fields on " + beanClass);
         for ( Field field: beanClass.getDeclaredFields() )
         {
            if ( !ignore(field) )
            {
               Object value = getFieldValue(target, field);
               if (value!=null)
               {
                  Object dataModel = null;
                  if ( DATA_MODEL.isInstance(value) )
                  {
                     dataModel = value;
                     value = getWrappedData(dataModel);
                  }
                  if ( containsReferenceToEntityInstance(value) )
                  {
                     log.trace("Attempting to save wrapper for " + field + " (" + value + ")");
                     saveWrapper(target, component, field, dataModel, value);
                  }
                  else
                  {
                     log.trace("Clearing wrapper for " + field + " (" + value + ") as it isn't a entity reference");
                     clearWrapper(component, field);
                  }
               }
               else
               {
                  log.trace("Clearing wrapper for " + field + " as it is null");
                  clearWrapper(component, field);
               }
            }
            else
            {
               log.trace("Ignoring field " + field + " as it is static, transient or annotated with @In");
            }
         }
      }
      restorePreviousConversationContextIfNecessary(oldCid);
   }

   public void deserialize(Object controllerBean, Component component) throws Exception
   {
      if ( !touchedContextsExist() )
      {
         log.trace("No touched persistence contexts. Therefore, there are no entities in this conversation whose identities need to be restored.");
         return;
      }
      
      Class beanClass = controllerBean.getClass();
      for (; beanClass!=Object.class; beanClass=beanClass.getSuperclass())
      {
         log.trace("Examining fields on " + beanClass);
         for ( Field field: beanClass.getDeclaredFields() )
         {
            if ( !ignore(field) )
            {
               Object value = getFieldValue(controllerBean, field);
               Object dataModel = null;
               if (value!=null && DATA_MODEL.isInstance(value) )
               {
                  dataModel = value;
               }
               log.trace("Attempting to restore wrapper for " + field + " (" + value + ")");
               //TODO: be more selective
               getFromWrapper(controllerBean, component, field, dataModel);
            }
            else
            {
               log.trace("Ignoring field " + field + " as it is static, transient or annotated with @In");
            }
         }
      }
   }

   private boolean containsReferenceToEntityInstance(Object value)
   {
      if (value == null)
      {
         return false;
      }
      else if (value instanceof Collection)
      {
         // Do a lazy man's generic check by scanning the collection until an entity is found (nested objects not considered).
         for (Iterator iter = ((Collection) value).iterator(); iter.hasNext();)
         {
            Object v = iter.next();
            if (v != null && Seam.getEntityClass(v.getClass()) != null)
            {
               return true;
            }
         }
         return false;
      }
      else if (value instanceof Map)
      {
         // Do a lazy man's generic check by scanning the collection until an entity is found (nested objects not considered).
         for (Iterator iter = ((Map) value).entrySet().iterator(); iter.hasNext();)
         {
            Entry e = (Entry) iter.next();
            if ((e.getKey() != null && Seam.getEntityClass(e.getKey().getClass()) != null) ||
                  (e.getValue() != null && Seam.getEntityClass(e.getValue().getClass()) != null))
            {
               return true;
            }
         }
         return false;
      }
      else if (Seam.getEntityClass(value.getClass()) != null)
      {
         return true;
      }
      
      return false;
   }

   private Object getFieldValue(Object bean, Field field) throws Exception
   {
      if ( !field.isAccessible() ) field.setAccessible(true);
      Object value = Reflections.get(field, bean);
      return value;
   }

   private boolean ignore(Field field)
   {
      return Modifier.isTransient( field.getModifiers() ) || 
            Modifier.isStatic( field.getModifiers() )
            || field.isAnnotationPresent(In.class);
   }

   private boolean touchedContextsExist()
   {
       PersistenceContexts touchedContexts = PersistenceContexts.instance();
       return touchedContexts!=null && touchedContexts.getTouchedContexts().size()>0;
   }

   private String getFieldId(Component component, Field field)
   {
      return component.getName() + '.' + field.getName();
   }

   private void saveWrapper(Object bean, Component component, Field field, Object dataModel, Object value) throws Exception
   {
      Contexts.getConversationContext().set( getFieldId(component, field), value );
      if (dataModel==null)
      {
         Reflections.set(field, bean, null);
      }
      else
      {
         // JBSEAM-1814, JBPAPP-1616 Clearing the wrapped data is simply unnecessary. Either we leave it alone, or we set the field to null.
         //setWrappedData(dataModel, null);
      }
   }

   private void clearWrapper(Component component, Field field) throws Exception
   {
      Contexts.getConversationContext().remove( getFieldId(component, field) );
   }

   private void getFromWrapper(Object bean, Component component, Field field, Object dataModel) throws Exception
   {
      Object value =Contexts.getConversationContext().get( getFieldId(component, field) );
      if (value!=null)
      {
         if (dataModel==null)
         {
            Reflections.set(field, bean, value);
         }
         else
         {
            setWrappedData(dataModel, value);
         }
      }
   }
   
   /**
    * Changes the thread's current conversation context to the one that holds a reference to this
    * component. This is necessary if a nested conversation is making a call to a component in
    * a parent conversation.
    */
   private String switchToConversationContextOfComponent(Component component)
   {
      Manager manager = Manager.instance();
      if (manager.isNestedConversation())
      {
         String currentCid = manager.getCurrentConversationId();
         String residentCid = manager.getCurrentConversationEntry().findPositionInConversationStack(component);
         if (!currentCid.equals(residentCid))
         {
            Contexts.getConversationContext().flush();
            Manager.instance().switchConversation(residentCid, false);
            return currentCid;
         }
      }
      
      return null;
   }
   
   private void restorePreviousConversationContextIfNecessary(String oldCid)
   {
      if (oldCid != null)
      {
         Contexts.getConversationContext().flush();
         Manager.instance().switchConversation(oldCid, false);
      }
   }
   
}
