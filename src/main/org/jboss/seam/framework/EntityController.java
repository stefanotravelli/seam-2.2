package org.jboss.seam.framework;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

/**
 * Base class for controller objects that perform
 * persistence operations using JPA. Adds
 * convenience methods for access to the JPA
 * EntityManager.
 * 
 * @author Gavin King
 *
 */
public class EntityController extends PersistenceController<EntityManager>
{
   
   public EntityManager getEntityManager()
   {
      return getPersistenceContext();
   }
   
   public void setEntityManager(EntityManager entityManager)
   {
      setPersistenceContext(entityManager);
   }

   @Override
   protected String getPersistenceContextName()
   {
      return "entityManager";
   }
   
   protected Query createNamedQuery(String name)
   {
      return getEntityManager().createNamedQuery(name);
   }

   protected Query createQuery(String ejbql)
   {
      return getEntityManager().createQuery(ejbql);
   }

   protected <T> T find(Class<T> clazz, Object id)
   {
      return getEntityManager().find(clazz, id);
   }

   protected void flush()
   {
      getEntityManager().flush();
   }

   protected <T> T getReference(Class<T> clazz, Object id)
   {
      return getEntityManager().getReference(clazz, id);
   }

   protected void lock(Object entity, LockModeType lockMode)
   {
      getEntityManager().lock(entity, lockMode);
   }

   protected <T> T merge(T entity)
   {
      return getEntityManager().merge(entity);
   }

   protected void persist(Object entity)
   {
      getEntityManager().persist(entity);
   }

   protected void refresh(Object entity)
   {
      getEntityManager().refresh(entity);
   }

   protected void remove(Object entity)
   {
      getEntityManager().remove(entity);
   }
   
}
