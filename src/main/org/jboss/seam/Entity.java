package org.jboss.seam;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Version;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.init.EjbDescriptor;
import org.jboss.seam.init.EjbEntityDescriptor;
import org.jboss.seam.util.Reflections;

/**
 * Metamodel class for entity classes.
 * 
 * A class will be identified as an entity class if it has an @Entity annotation.
 * 
 * @author Gavin King
 * 
 */
public class Entity extends Model
{

   private Method preRemoveMethod;
   private Method prePersistMethod;
   private Method preUpdateMethod;
   private Method postLoadMethod;
   private Method identifierGetter;
   private Field identifierField;
   private Method versionGetter;
   private Field versionField;
   private String name;

   /**
    * 
    * @param beanClass
    *
    * Use Entity.forBean() or Entity.forClass
    */
   @Deprecated
   public Entity(Class<?> beanClass)
   {
      super(beanClass);
      EjbDescriptor descriptor = Seam.getEjbDescriptor(beanClass);
      if (descriptor instanceof EjbEntityDescriptor)
      {
         mergeAnnotationAndOrmXml((EjbEntityDescriptor) descriptor);
      }
      else
      {
         mergeAnnotationAndOrmXml(null);
      }
   }

   public Method getPostLoadMethod()
   {
      return postLoadMethod;
   }

   public Method getPrePersistMethod()
   {
      return prePersistMethod;
   }

   public Method getPreRemoveMethod()
   {
      return preRemoveMethod;
   }

   public Method getPreUpdateMethod()
   {
      return preUpdateMethod;
   }

   @Deprecated
   public Field getIdentifierField()
   {
      return identifierField;
   }

   @Deprecated
   public Method getIdentifierGetter()
   {
      return identifierGetter;
   }

   @Deprecated
   public Field getVersionField()
   {
      return versionField;
   }

   @Deprecated
   public Method getVersionGetter()
   {
      return versionGetter;
   }

   public Object getIdentifier(Object entity)
   {
      if (identifierGetter != null)
      {
         return Reflections.invokeAndWrap(identifierGetter, entity);
      }
      else if (identifierField != null)
      {
         return Reflections.getAndWrap(identifierField, entity);
      }
      else
      {
         throw new IllegalStateException("@Id attribute not found for entity class: " + getBeanClass().getName());
      }
   }

   public Object getVersion(Object entity)
   {
      if (versionGetter != null)
      {
         return Reflections.invokeAndWrap(versionGetter, entity);
      }
      else if (versionField != null)
      {
         return Reflections.getAndWrap(versionField, entity);
      }
      else
      {
         return null;
      }
   }

   public String getName()
   {
      return name;
   }
   
   public static Entity forBean(Object bean)
   {
      return forClass(bean.getClass());
   }
   
   public static Entity forClass(Class clazz)
   {
      if (!Contexts.isApplicationContextActive())
      {
         throw new IllegalStateException("No application context active");
      }

      Class entityClass = Seam.getEntityClass(clazz);
      
      if (entityClass == null)
      {
         throw new NotEntityException("Not an entity class: " + clazz.getName());
      }
      String name = getModelName(entityClass);
      Model model = (Model) Contexts.getApplicationContext().get(name);
      if (model == null || !(model instanceof Entity))
      {
         Entity entity = new Entity(entityClass);
         Contexts.getApplicationContext().set(name, entity);
         return entity;
      }
      else
      {
         return (Entity) model;
      }
   }

   private void mergeAnnotationAndOrmXml(EjbEntityDescriptor descriptor)
   {
      // Lookup the name of the Entity from XML, annotation or default
      this.name = lookupName(getBeanClass(), descriptor);
      if (this.name == null)
      {
         throw new NotEntityException("Unable to establish name of entity " + getBeanClass());
      }
      
      if (descriptor != null)
      {
         // Set any methods and fields we need metadata for from the XML
         // descriptor. These take priority over annotations
         
         this.preRemoveMethod = getEntityCallbackMethod(getBeanClass(), descriptor.getPreRemoveMethodName());
         this.prePersistMethod = getEntityCallbackMethod(getBeanClass(), descriptor.getPrePersistMethodName());
         this.preUpdateMethod = getEntityCallbackMethod(getBeanClass(), descriptor.getPreUpdateMethodName());
         this.postLoadMethod = getEntityCallbackMethod(getBeanClass(), descriptor.getPostLoadMethodName());
         
         this.identifierField = descriptor.getIdentifierFieldName() != null ? Reflections.getField(getBeanClass(), descriptor.getIdentifierFieldName()) : null;
         this.identifierGetter = descriptor.getIdentifierPropertyName() != null ? Reflections.getGetterMethod(getBeanClass(), descriptor.getIdentifierPropertyName()) : null;
         
         this.versionField = descriptor.getVersionFieldName() != null ? Reflections.getField(getBeanClass(), descriptor.getVersionFieldName()) : null;
         this.versionGetter = descriptor.getVersionPropertyName() != null ? Reflections.getGetterMethod(getBeanClass(), descriptor.getVersionPropertyName()) : null;
      }
      
      if (descriptor == null || !descriptor.isMetaDataComplete())
      {
         for ( Class<?> clazz=getBeanClass(); clazz!=Object.class; clazz = clazz.getSuperclass() )
         {

            for ( Method method: clazz.getDeclaredMethods() )
            {
               //TODO: does the spec allow multiple lifecycle method
               //      in the entity class heirarchy?
               if (this.preRemoveMethod == null && method.isAnnotationPresent(PreRemove.class))
               {
                  this.preRemoveMethod = method;
               }
               if (this.prePersistMethod == null && method.isAnnotationPresent(PrePersist.class) )
               {
                  this.prePersistMethod = method;
               }
               if (preUpdateMethod == null && method.isAnnotationPresent(PreUpdate.class) )
               {
                  preUpdateMethod = method;
               }
               if (postLoadMethod == null && method.isAnnotationPresent(PostLoad.class) )
               {
                  postLoadMethod = method;
               }
               if (identifierField == null && identifierGetter == null && method.isAnnotationPresent(Id.class) || method.isAnnotationPresent(EmbeddedId.class))
               {
                  identifierGetter = method;
               }
               if (versionField == null && versionGetter == null && method.isAnnotationPresent(Version.class) )
               {
                  versionGetter = method;
               }
            }
            
            if ( ( identifierGetter == null && identifierField == null ) || ( versionField == null && versionGetter == null ) )
            {
               for ( Field field: clazz.getDeclaredFields() )
               {
                  if ( identifierGetter == null && identifierField == null && (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)))
                  {
                     identifierField = field;
                  }
                  if ( versionGetter == null && versionField == null && field.isAnnotationPresent(Version.class) )
                  {
                     versionField = field;
                  }
               }
            }
         }
      }
      
      setAccessible(this.preRemoveMethod);
      setAccessible(this.prePersistMethod);
      setAccessible(this.preUpdateMethod);
      setAccessible(this.postLoadMethod);
      setAccessible(this.identifierField);
      setAccessible(this.identifierGetter);
      setAccessible(this.versionField);
      setAccessible(this.versionGetter);
   }
   
   private void setAccessible(AccessibleObject accessibleObject)
   {
      if (accessibleObject != null)
      {
         accessibleObject.setAccessible(true);
      }
   }

   private static String lookupName(Class<?> beanClass, EjbEntityDescriptor descriptor)
   {
      if (descriptor != null && descriptor.getEjbName() != null)
      {
         // XML overrides annotations
         return descriptor.getEjbName();
      }
      else if ( (descriptor == null || !descriptor.isMetaDataComplete()) && beanClass.isAnnotationPresent(javax.persistence.Entity.class) && !"".equals(beanClass.getAnnotation(javax.persistence.Entity.class).name()))
      {
         // Is a name specified?
         return beanClass.getAnnotation(javax.persistence.Entity.class).name();
      }
      else if (descriptor != null || beanClass.isAnnotationPresent(javax.persistence.Entity.class))
      {
         // Use the default name if either a descriptor is specified or the
         // annotation is present
         return beanClass.getName();
      }
      else
      {
         return null;
      }
   }

   private static Method getEntityCallbackMethod(Class beanClass, String callbackMethodName)
   {
      try
      {
         if (callbackMethodName != null)
         {
            return Reflections.getMethod(beanClass, callbackMethodName);
         }
         else
         {
            return null;
         }
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException("Unable to find Entity callback method specified in orm.xml", e);
      }
   }
   
   public static class NotEntityException extends IllegalArgumentException 
   {
      
      public NotEntityException(String string)
      {
         super(string);
      }
      
   }

}
