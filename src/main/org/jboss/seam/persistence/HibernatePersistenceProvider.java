package org.jboss.seam.persistence;
import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.transaction.Synchronization;

import org.hibernate.EntityMode;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.StaleStateException;
import org.hibernate.TransientObjectException;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.VersionType;
import org.jboss.seam.Component;
import org.jboss.seam.Entity;
import org.jboss.seam.ScopeType;
import org.jboss.seam.Entity.NotEntityException;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
/**
 * Support for non-standardized features of Hibernate, when
 * used as the JPA persistence provider.
 * 
 * @author Gavin King
 * @author Pete Muir
 *
 */
@Name("org.jboss.seam.persistence.persistenceProvider")
@Scope(ScopeType.STATELESS)
@BypassInterceptors
@Install(precedence=FRAMEWORK, classDependencies={"org.hibernate.Session", "javax.persistence.EntityManager"})
public class HibernatePersistenceProvider extends PersistenceProvider
{
   
   private static Log log = Logging.getLog(HibernatePersistenceProvider.class);
   private static Class FULL_TEXT_SESSION_PROXY_CLASS;
   private static Method FULL_TEXT_SESSION_CONSTRUCTOR;
   private static Class FULL_TEXT_ENTITYMANAGER_PROXY_CLASS;
   private static Method FULL_TEXT_ENTITYMANAGER_CONSTRUCTOR;
   static
   {
      try
      {
         String version = null;
         try {
            Class searchVersionClass = Class.forName("org.hibernate.search.Version");
            Field versionField = searchVersionClass.getDeclaredField("VERSION");
            version = (String) versionField.get(null);
         }
         catch (Exception e)
         {
            log.debug("no Hibernate Search, sorry :-(", e);
         }
         if (version != null) {
            Class searchClass = Class.forName("org.hibernate.search.Search");
            try {
               FULL_TEXT_SESSION_CONSTRUCTOR = searchClass.getDeclaredMethod("getFullTextSession", Session.class);
            }
            catch (NoSuchMethodException noSuchMethod) {
               log.debug("org.hibernate.search.Search.getFullTextSession(Session) not found, trying deprecated method name createFullTextSession");
               FULL_TEXT_SESSION_CONSTRUCTOR = searchClass.getDeclaredMethod("createFullTextSession", Session.class);
            }
            FULL_TEXT_SESSION_PROXY_CLASS = Class.forName("org.jboss.seam.persistence.FullTextHibernateSessionProxy");
            Class jpaSearchClass = Class.forName("org.hibernate.search.jpa.Search");
            try {
               FULL_TEXT_ENTITYMANAGER_CONSTRUCTOR = jpaSearchClass.getDeclaredMethod("getFullTextEntityManager", EntityManager.class);   
            }
            catch (NoSuchMethodException noSuchMethod) {
               log.debug("org.hibernate.search.jpa.getFullTextSession(EntityManager) not found, trying deprecated method name createFullTextEntityManager");
               FULL_TEXT_ENTITYMANAGER_CONSTRUCTOR = jpaSearchClass.getDeclaredMethod("createFullTextEntityManager", EntityManager.class);
            }
            FULL_TEXT_ENTITYMANAGER_PROXY_CLASS = Class.forName("org.jboss.seam.persistence.FullTextEntityManagerProxy");
            log.debug("Hibernate Search is available :-)");
         }
      }
      catch (Exception e)
      {
         log.debug("no Hibernate Search, sorry :-(", e);
      }
   }

   @Override
   public void init()
   {
      super.init();
      featureSet.add(Feature.WILDCARD_AS_COUNT_QUERY_SUBJECT);
   }
   
   /**
    * Wrap the Hibernate Session in a proxy that supports HQL
    * EL interpolation and implements FullTextSession if Hibernate
    * Search is available in the classpath.
    */
   static Session proxySession(Session session)
   {
      if (FULL_TEXT_SESSION_PROXY_CLASS==null)
      {
         return (Session) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
               new Class[] { HibernateSessionProxy.class },
               new HibernateSessionInvocationHandler(session));
      }
      else
      {
         try {
            return (Session) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                  new Class[] { FULL_TEXT_SESSION_PROXY_CLASS },
                  new HibernateSessionInvocationHandler((Session) FULL_TEXT_SESSION_CONSTRUCTOR.invoke(null, session)));
         }
         catch(Exception e) {
            log.warn("Unable to wrap into a FullTextSessionProxy, regular SessionProxy returned", e);
            return (Session) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                  new Class[] { HibernateSessionProxy.class },
                  new HibernateSessionInvocationHandler(session));
         }
      }
   }
   
   /**
    * Wrap the delegate Hibernate Session in a proxy that supports HQL
    * EL interpolation and implements FullTextSession if Hibernate
    * Search is available in the classpath.
    */
   @Override
   public Object proxyDelegate(Object delegate)
   {
      try
      {
         return proxySession( (Session) delegate );
      }
      catch (NotHibernateException nhe)
      {
         return super.proxyDelegate(delegate);
      }
      catch (Exception e)
      {
         throw new RuntimeException("could not proxy delegate", e);
      }
   }
   
   @Override
   public void setFlushModeManual(EntityManager entityManager)
   {
       try
       {
          getSession(entityManager).setFlushMode(FlushMode.MANUAL);
       }
       catch (NotHibernateException nhe)
       {
          super.setFlushModeManual(entityManager);
       }
   }
   
   @Override
   public void setRenderFlushMode()
   {
      PersistenceContexts.instance().changeFlushMode(FlushModeType.MANUAL, true);
   }

   @Override
   public boolean isDirty(EntityManager entityManager)
   {
       try
       {
          return getSession(entityManager).isDirty();
       }
       catch (NotHibernateException nhe)
       {
          return super.isDirty(entityManager);
       }
   }
   
   @Override
   public Object getId(Object bean, EntityManager entityManager) 
   {
       try
       {
          return getSession(entityManager).getIdentifier(bean);
       }
       catch (NotHibernateException nhe)
       {
          return super.getId(bean, entityManager);
       }
       catch (TransientObjectException e) 
       {
          if (bean instanceof HibernateProxy)
          {
             return super.getId(((HibernateProxy) bean).getHibernateLazyInitializer().getImplementation(), entityManager);
          }
          else
          {
             return super.getId(bean, entityManager);
          }
       }
   }
   
   @Override
   public Object getVersion(Object bean, EntityManager entityManager) 
   {
       try
       {
          return getVersion( bean, getSession(entityManager) );
       }
       catch (NotHibernateException nhe)
       {
          return super.getVersion(bean, entityManager);
       }
   }
   
   @Override
   public void checkVersion(Object bean, EntityManager entityManager, Object oldVersion, Object version)
   {
       try
       {
          checkVersion(bean, getSession(entityManager), oldVersion, version);
       }
       catch (NotHibernateException nhe)
       {
          super.checkVersion(bean, entityManager, oldVersion, version);
       }
   }
   
   @Override
   public void enableFilter(Filter f, EntityManager entityManager)
   {
      try
      {
         org.hibernate.Filter filter = getSession(entityManager).enableFilter( f.getName() );
         for ( Map.Entry<String, ValueExpression> me: f.getParameters().entrySet() )
         {
		   Object filterValue = me.getValue().getValue();
		   if ( filterValue instanceof Collection ) {
		      filter.setParameterList(me.getKey(), (Collection) filterValue);
		   } else {
			  filter.setParameter(me.getKey(), filterValue);
		   }
         }
         filter.validate();
      }
      catch (NotHibernateException nhe)
      {
         super.enableFilter(f, entityManager);
      }

   }
   
   @Override
   public boolean registerSynchronization(Synchronization sync, EntityManager entityManager)
   {
      try
      {
         //TODO: just make sure that a Hibernate JPA EntityTransaction
         //      delegates to the Hibernate Session transaction
         getSession(entityManager).getTransaction().registerSynchronization(sync);
         return true;
      }
      catch (NotHibernateException nhe)
      {
         return super.registerSynchronization(sync, entityManager);
      }

   }

   @Override
   public String getName(Object bean, EntityManager entityManager) throws IllegalArgumentException
   {
      try 
      {
         return getSession(entityManager).getEntityName(bean);
      } 
      catch (NotHibernateException nhe)
      {
         return super.getName(bean, entityManager);
      }
      catch (TransientObjectException e) 
      {
         return super.getName(bean, entityManager);
      }
   }
   
   @Override
   public EntityManager proxyEntityManager(EntityManager entityManager)
   {
      if (FULL_TEXT_ENTITYMANAGER_PROXY_CLASS==null)
      {
         return super.proxyEntityManager(entityManager);
      }
      else
      {
         try
         {
            return (EntityManager) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                  new Class[] { FULL_TEXT_ENTITYMANAGER_PROXY_CLASS },
                  new EntityManagerInvocationHandler(
                        (EntityManager) FULL_TEXT_ENTITYMANAGER_CONSTRUCTOR.invoke(null,
                              super.proxyEntityManager(entityManager))));
         }
         catch (Exception e)
         {
            //throw new RuntimeException("could not proxy FullTextEntityManager", e);
            return super.proxyEntityManager(entityManager);
         }
      }
   }
   
   public static void checkVersion(Object value, Session session, Object oldVersion, Object version)
   {
      ClassMetadata classMetadata = getClassMetadata(value, session);
      VersionType versionType = (VersionType) classMetadata.getPropertyTypes()[ classMetadata.getVersionProperty() ];
      if ( !versionType.isEqual(oldVersion, version) )
      {
         throw new StaleStateException("current database version number does not match passivated version number");
      }
   }
   
   public static Object getVersion(Object value, Session session)
   {
      ClassMetadata classMetadata = getClassMetadata(value, session);
      return classMetadata!=null && classMetadata.isVersioned() ? 
               classMetadata.getVersion(value, EntityMode.POJO) : null;
   }
   
   private static ClassMetadata getClassMetadata(Object value, Session session)
   {
      Class entityClass = getEntityClass(value);
      ClassMetadata classMetadata = null;
      if (entityClass!=null)
      {
         classMetadata = session.getSessionFactory().getClassMetadata(entityClass);
         if (classMetadata==null)
         {
            throw new IllegalArgumentException( 
                     "Could not find ClassMetadata object for entity class: " + 
                     entityClass.getName() 
                  );
         }
      }
      return classMetadata;
   }
   
   /**
    * Returns the class of the specified Hibernate entity
    */
   @Override
   public Class getBeanClass(Object bean)
   {
      return getEntityClass(bean);
   }
   
   public static Class getEntityClass(Object bean)
   {
      Class clazz = null;
      try
      {
         clazz = Entity.forBean(bean).getBeanClass();
      }
      catch (NotEntityException e) {
         // It's ok, try some other methods
      }
      
      if (clazz == null)
      {
         clazz = Hibernate.getClass(bean);
      }
      
      return clazz;
   }
   
   private Session getSession(EntityManager entityManager)
   {
      Object delegate = entityManager.getDelegate();
      if ( delegate instanceof Session )
      {
         return (Session) delegate;
      }
      else
      {
         throw new NotHibernateException();
      }
   }
   
   /**
    * Occurs when Hibernate is in the classpath, but this particular
    * EntityManager is not from Hibernate
    * 
    * @author Gavin King
    *
    */
   static class NotHibernateException extends IllegalArgumentException {}
   
   public static HibernatePersistenceProvider instance()
   {
       return (HibernatePersistenceProvider) Component.getInstance(HibernatePersistenceProvider.class, ScopeType.STATELESS);
   }
}
