package org.jboss.seam.persistence;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.transaction.Synchronization;

import org.jboss.seam.Component;
import org.jboss.seam.Entity;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.intercept.PostConstruct;

/**
 * Abstraction layer for persistence providers (JPA implementations). This class
 * provides a working base implementation that can be optimized for performance
 * and non-standardized features by extending and overriding the methods.
 * 
 * The methods on this class are a great todo list for the next rev of the JPA
 * spec ;-)
 * 
 * @author Gavin King
 * @author Pete Muir
 * 
 */
@Name("org.jboss.seam.persistence.persistenceProvider")
@Scope(ScopeType.STATELESS)
@BypassInterceptors
@Install(precedence = BUILT_IN, classDependencies = "javax.persistence.EntityManager")
public class PersistenceProvider
{
   public enum Feature
   {
      /**
       * Identifies whether this JPA provider supports using a wildcard as the
       * subject of a count query.
       * 
       * <p>
       * Here's a count query that uses a wildcard as the subject.
       * </p>
       * 
       * <pre>
       * select count(*) from Vehicle v
       * </pre>
       * <p>
       * Per the JPA 1.0 spec, using a wildcard as a subject of a count query is
       * not permitted. Instead, the subject must be the entity or the alias, as
       * in this count query:
       * </p>
       * 
       * <pre>
       * select count(v) from Vehicle v
       * </pre>
       * <p>
       * Hibernate supports the wildcard syntax as an vendor extension.
       * Furthermore, Hibernate produces an invalid SQL query when using the
       * compliant subject if the entity has a composite primary key. Therefore,
       * we prefer to use the wildcard syntax if it is supported.
       * </p>
       */
      WILDCARD_AS_COUNT_QUERY_SUBJECT
   }
   
   protected Set<Feature> featureSet = new HashSet<Feature>();
   
   @PostConstruct
   // @Create method not called on stateless components
   public void init()
   {
   }
   
   /**
    * Indicate whether this JPA provider supports the feature defined by the
    * provided Feature enum value.
    */
   public boolean supportsFeature(Feature feature)
   {
      return featureSet.contains(feature);
   }
   
   /**
    * Set the flush mode to manual-only flushing. Called when an atomic
    * persistence context is required.
    */
   public void setFlushModeManual(EntityManager entityManager)
   {
      throw new UnsupportedOperationException("Use of FlushMode.MANUAL requires Hibernate as the persistence provider. Please use Hibernate, a custom persistenceProvider, or remove the MANUAL flush mode setting.");
   }
   
   /**
    * <p>
    * Set the FlushMode the persistence contexts should use during rendering by
    * calling {@link PersistenceContexts#changeFlushMode(FlushModeType, true)}.
    * The actual changing of the flush mode is handled by the
    * {@link PersistenceContexts} instance. The boolean argument should be true
    * to indicate that this is a temporary change and that the old flush mode
    * should be restored after render.
    * </p>
    * <p>
    * Ideally, this should be MANUAL since changes should never flush to the
    * database while in render response and the cost of a dirty check can be
    * avoided. However, since the MANUAL mode is not officially part of the JPA
    * specification, the default implementation will perform no operation.
    * </p>
    */
   public void setRenderFlushMode()
   {
      // no-op in default implementation
   }
   
   /**
    * Does the persistence context have unflushed changes? If it does not,
    * persistence context replication can be optimized.
    * 
    * @return true to indicate that there are unflushed changes
    */
   public boolean isDirty(EntityManager entityManager)
   {
      return true; // best we can do!
   }
   
   /**
    * Get the value of the entity identifier attribute.
    * 
    * @param bean
    *           a managed entity instance
    */
   public Object getId(Object bean, EntityManager entityManager)
   {
      return Entity.forBean(bean).getIdentifier(bean);
   }
   
   /**
    * Get the name of the entity
    * 
    * @param bean
    * @param entityManager
    * 
    * @throws IllegalArgumentException
    *            if the passed object is not an entity
    */
   public String getName(Object bean, EntityManager entityManager) throws IllegalArgumentException
   {
      return Entity.forBean(bean).getName();
   }
   
   /**
    * Get the value of the entity version attribute.
    * 
    * @param bean
    *           a managed entity instance
    */
   public Object getVersion(Object bean, EntityManager entityManager)
   {
      return Entity.forBean(bean).getVersion(bean);
   }
   
   public void checkVersion(Object bean, EntityManager entityManager, Object oldVersion, Object version)
   {
      boolean equal;
      if (oldVersion instanceof Date)
      {
         equal = ((Date) oldVersion).getTime() == ((Date) version).getTime();
      }
      else
      {
         equal = oldVersion.equals(version);
      }
      if (!equal)
      {
         throw new OptimisticLockException("Current database version number does not match passivated version number");
      }
   }
   
   /**
    * Enable a Filter. This is here just especially for Hibernate, since we well
    * know that other products don't have such cool features.
    */
   public void enableFilter(Filter filter, EntityManager entityManager)
   {
      throw new UnsupportedOperationException("Use of filters requires Hibernate as the persistence provider. Please use Hibernate or remove the filters configuration.");
   }
   
   /**
    * Register a Synchronization with the current transaction.
    */
   public boolean registerSynchronization(Synchronization sync, EntityManager entityManager)
   {
      return false; // best we can do!
   }
   
   public static PersistenceProvider instance()
   {
      return (PersistenceProvider) Component.getInstance(PersistenceProvider.class, ScopeType.STATELESS);
   }
   
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
    * @param bean
    *           The entity bean instance
    * @return The class of the entity bean
    */
   public Class getBeanClass(Object bean)
   {
      return Entity.forBean(bean).getBeanClass();
   }
   
   public Method getPostLoadMethod(Object bean, EntityManager entityManager)
   {
      return Entity.forBean(bean).getPostLoadMethod();
   }
   
   public Method getPrePersistMethod(Object bean, EntityManager entityManager)
   {
      return Entity.forBean(bean).getPrePersistMethod();
   }
   
   public Method getPreUpdateMethod(Object bean, EntityManager entityManager)
   {
      return Entity.forBean(bean).getPreUpdateMethod();
   }
   
   public Method getPreRemoveMethod(Object bean, EntityManager entityManager)
   {
      return Entity.forBean(bean).getPreRemoveMethod();
   }
   
   @Deprecated
   public Method getPreRemoveMethod(Class beanClass)
   {
      return Entity.forClass(beanClass).getPreRemoveMethod();
   }
   
   @Deprecated
   public Method getPostLoadMethod(Class beanClass)
   {
      return Entity.forClass(beanClass).getPostLoadMethod();
   }
   
   @Deprecated
   public Method getPrePersistMethod(Class beanClass)
   {
      return Entity.forClass(beanClass).getPrePersistMethod();
   }
   
   @Deprecated
   public Method getPreUpdateMethod(Class beanClass)
   {
      return Entity.forClass(beanClass).getPreUpdateMethod();
   }
   
}
