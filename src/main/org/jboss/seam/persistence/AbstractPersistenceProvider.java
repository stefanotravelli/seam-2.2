package org.jboss.seam.persistence;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.transaction.Synchronization;

import org.jboss.seam.Entity;

/**
 * Provides a default implementation of PersistenceProvider methods where possible
 * 
 * Other methods must be implemented
 * 
 * @author Pete Muir
 *
 */
public abstract class AbstractPersistenceProvider
{

   /**
    *  Set the flush mode to manual-only flushing. Called when
    *  an atomic persistence context is required.
    */
   public abstract void setFlushModeManual(EntityManager entityManager);

   /**
    * Does the persistence context have unflushed changes? If
    * it does not, persistence context replication can be
    * optimized.
    * 
    * @return true to indicate that there are unflushed changes
    */
   public abstract boolean isDirty(EntityManager entityManager);

   /**
    * Get the value of the entity identifier attribute.
    * 
    * @param bean a managed entity instance
    */
   public Object getId(Object bean, EntityManager entityManager)
   {
      return Entity.forClass( bean.getClass() ).getIdentifier(bean);
   }

   /**
    * Get the name of the entity
    * 
    * @param bean
    * @param entityManager
    * 
    * @throws IllegalArgumentException if the passed object is not an entity
    */
   public String getName(Object bean, EntityManager entityManager) throws IllegalArgumentException
   {
      return Entity.forClass(bean.getClass()).getName();
   }

   /**
    * Get the value of the entity version attribute.
    * 
    * @param bean a managed entity instance
    */
   public Object getVersion(Object bean, EntityManager entityManager)
   {
      return Entity.forClass( bean.getClass() ).getVersion(bean);
   }

   public void checkVersion(Object bean, EntityManager entityManager, Object oldVersion, Object version)
   {
      boolean equal;
      if (oldVersion instanceof Date)
      {
         equal = ( (Date) oldVersion ).getTime() == ( (Date) version ).getTime();
      }
      else
      {
         equal = oldVersion.equals(version);
      }
      if ( !equal )
      {
         throw new OptimisticLockException("current database version number does not match passivated version number");
      }
   }

   /**
    * Enable a Filter. This is here just especially for Hibernate,
    * since we well know that other products don't have such cool
    * features. 
    */
   public abstract void enableFilter(Filter filter, EntityManager entityManager);

   /**
    * Register a Synchronization with the current transaction.
    */
   public abstract boolean registerSynchronization(Synchronization sync, EntityManager entityManager);

   /**
    * Wrap the delegate before returning it to the application
    */
   public Object proxyDelegate(Object delegate)
   {
      return delegate;
   }

   /**
    * Wrap the entityManager before returning it to the application
    */
   public EntityManager proxyEntityManager(EntityManager entityManager)
   {
      return (EntityManager) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
            new Class[] { EntityManagerProxy.class },
            new EntityManagerInvocationHandler(entityManager));
   }

   /**
    * Returns the class of an entity bean instance
    * 
    * @param bean The entity bean instance
    * @return The class of the entity bean
    */
   public Class getBeanClass(Object bean)
   {
      return Entity.forClass(bean.getClass()).getBeanClass();
   }

   public Method getPostLoadMethod(Class beanClass, EntityManager entityManager)
   {
      return Entity.forClass(beanClass).getPostLoadMethod();      
   }

   public Method getPrePersistMethod(Class beanClass, EntityManager entityManager)
   {
      return Entity.forClass(beanClass).getPrePersistMethod();
   }

   public Method getPreUpdateMethod(Class beanClass, EntityManager entityManager)
   {
      return Entity.forClass(beanClass).getPreUpdateMethod();
   }

   public Method getPreRemoveMethod(Class beanClass, EntityManager entityManager)
   {
      return Entity.forClass(beanClass).getPreRemoveMethod();
   }

}