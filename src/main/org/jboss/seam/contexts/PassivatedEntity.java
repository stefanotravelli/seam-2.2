package org.jboss.seam.contexts;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.jboss.seam.Component;
import org.jboss.seam.Seam;
import org.jboss.seam.persistence.HibernatePersistenceProvider;
import org.jboss.seam.persistence.PersistenceContexts;
import org.jboss.seam.persistence.PersistenceProvider;
import org.jboss.seam.transaction.Transaction;

/**
 * A swizzled entity reference, consisting of the class,
 * id and persistence context name.
 * 
 * @see EntityBean
 * @see org.jboss.seam.persistence.ManagedEntityInterceptor
 * 
 * @author Gavin King
 *
 */
class PassivatedEntity implements Serializable
{
   private static final long serialVersionUID = 6565440294007267788L;
   
   private Object id;
   private Object version;
   private String persistenceContext;
   private Class<?> entityClass; //TODO: make this transient, and serialize only the class name..
   
   private PassivatedEntity(Object id, Object version, Class<?> entityClass, String persistenceContext)
   {
      this.id = id;
      this.persistenceContext = persistenceContext;
      this.entityClass = entityClass;
      this.version = version;
   }
   
   private String getPersistenceContext()
   {
      return persistenceContext;
   }
   
   private Object getId()
   {
      return id;
   }
   
   private Class<?> getEntityClass()
   {
      return entityClass;
   }
   
   public boolean isVersioned()
   {
      return version!=null;
   }

   public Object toEntityReference(boolean checkVersion)
   {
      Object persistenceContext = Component.getInstance( getPersistenceContext() );
      if ( persistenceContext==null )
      {
         return null;
      }
      else
      {
         if (persistenceContext instanceof EntityManager)
         {
            return getEntityFromEntityManager(persistenceContext, checkVersion);
         }
         else
         {
            return getEntityFromHibernate(persistenceContext, checkVersion);
         }
      }
   }

   private Object getEntityFromHibernate(Object persistenceContext, boolean checkVersion)
   {
      //TODO: split this out to somewhere to isolate the Hibernate dependency!!
      Session session = (Session) persistenceContext;
      if ( session.isOpen() )
      {
         Object result = session.load( getEntityClass(), (Serializable) getId() );
         if (result!=null && checkVersion)
         {
            checkVersion(session, result);
         }
         return result;
      }
      else
      {
         return null;
      }
   }

   private void checkVersion(Session session, Object result)
   {
      Object version = HibernatePersistenceProvider.getVersion(result, session);
      if (version!=null) 
      {
         HibernatePersistenceProvider.checkVersion(result, session, this.version, version);
      }
   }

   private Object getEntityFromEntityManager(Object persistenceContext, boolean checkVersion)
   {
      EntityManager em = (EntityManager) persistenceContext;
      if ( em.isOpen() )
      {
         Object result = em.getReference( getEntityClass(), getId() );
         if (result!=null && checkVersion) 
         {
            checkVersion(em, result);
         }
         return result;
      }
      else
      {
         return null;
      }
   }

   private void checkVersion(EntityManager em, Object result)
   {
      Object version = PersistenceProvider.instance().getVersion(result, em);
      if (version!=null) 
      {
         PersistenceProvider.instance().checkVersion(result, em, this.version, version);
      }
   }
   
   /*public static Object unpassivateEntityAndCheckVersion(String key)
   {
      return unpassivateEntity(key, true);
   }

   public static Object unpassivateEntity(String key)
   {
      return unpassivateEntity(key, false);
   }

   private static Object unpassivateEntity(String key, boolean checkVersion)
   {
      PassivatedEntity passivatedEntity = (PassivatedEntity) Contexts.getConversationContext().get(key);
      return passivatedEntity==null ? null : passivatedEntity.toEntityReference(checkVersion);
   }*/

   public static PassivatedEntity passivateEntity(Object value)
   {
      Class entityClass = Seam.getEntityClass( value.getClass() );
      if (entityClass!=null)
      {
         for ( String persistenceContextName: PersistenceContexts.instance().getTouchedContexts() )
         {
            Object persistenceContext = Component.getInstance(persistenceContextName);
            return createPassivatedEntity(value, entityClass, persistenceContextName, persistenceContext);
         }
      }
      return null;
   }

   private static PassivatedEntity createPassivatedEntity(Object value, Class entityClass, String persistenceContextName, Object persistenceContext)
   {
      if (persistenceContext instanceof EntityManager)
      {
         return createUsingEntityManager(value, entityClass, persistenceContextName, persistenceContext);
      }
      else
      {
         return createUsingHibernate(value, entityClass, persistenceContextName, persistenceContext);
      }
   }

   /*private static String storeConversationContext(PassivatedEntity result)
   {
      if (result==null)
      {
         return null;
      }
      else
      {
         String key = result.getKey();
         Contexts.getConversationContext().set( key, result );
         return key;
      }
   }
   
   private String getKey()
   {
      return "org.jboss.seam.passivatedEntity." + entityClass.getName() + '#' + getId();
   }*/

   private static PassivatedEntity createUsingHibernate(Object value, Class entityClass, String persistenceContextName, Object persistenceContext)
   {
      //TODO: split this out to somewhere to isolate the Hibernate dependency!!
      Session session = (Session) persistenceContext;
      if ( isManaged(value, session) )
      {
         Object id = session.getIdentifier(value);
         Object version = HibernatePersistenceProvider.getVersion(value, session);
         return create(entityClass, persistenceContextName, id, version);
      }
      else
      {
         return null;
      }
   }

   private static boolean isManaged(Object value, Session session)
   {
      boolean managed;
      try
      {
         managed = session.isOpen() && session.contains(value);
      }
      catch (RuntimeException re) 
      {
         //just in case! //TODO; deleteme
         managed = false;
      }
      return managed;
   }

   private static PassivatedEntity createUsingEntityManager(Object value, Class entityClass, String persistenceContextName, Object persistenceContext)
   {
      EntityManager em = (EntityManager) persistenceContext;
      if ( isManaged(value, em) )
      {
         Object id = PersistenceProvider.instance().getId(value, em);
         Object version = PersistenceProvider.instance().getVersion(value, em);
         return create(entityClass, persistenceContextName, id, version);
      }
      else
      {
         return null;
      }
   }

   private static boolean isManaged(Object value, EntityManager em)
   {
      boolean managed;
      try
      {
         managed = em.isOpen() && em.contains(value);
      }
      catch (RuntimeException re) 
      {
         //workaround for bug in HEM! //TODO; deleteme
         managed = false;
      }
      return managed;
   }

   private static PassivatedEntity create(Class entityClass, String persistenceContextName, Object id, Object version)
   {
      if (id==null)
      {
         //this can happen if persist() fails in Hibernate
         return null;
      }
      else
      {
         return new PassivatedEntity(id, version, entityClass, persistenceContextName);
      }
   }

   static boolean isTransactionRolledBackOrMarkedRollback()
   {
      try
      {
         return Transaction.instance().isRolledBackOrMarkedRollback();
      }
      catch (Exception e)
      {
         return false;
      }
   }
   
   @Override
   public String toString()
   {
      return entityClass + "#" + id;
   }
   
}