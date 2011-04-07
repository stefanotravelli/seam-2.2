package org.jboss.seam.framework;

import java.io.Serializable;

import org.hibernate.Criteria;
import org.hibernate.Filter;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

/**
 * Base class for controller objects that perform
 * persistence operations using Hibernate. Adds
 * convenience methods for access to the Hibernate
 * Session object.
 * 
 * @author Gavin King
 *
 */
public class HibernateEntityController extends PersistenceController<Session>
{
   
   public Session getSession()
   {
      return getPersistenceContext();
   }
   
   public void setSession(Session session)
   {
      setPersistenceContext(session);
   }

   @Override
   protected String getPersistenceContextName()
   {
      return "hibernateSession";
   }
   
   protected Criteria createCriteria(Class clazz)
   {
      return getSession().createCriteria(clazz);
   }

   protected Query createQuery(String hql) throws HibernateException
   {
      return getSession().createQuery(hql);
   }

   protected SQLQuery createSQLQuery(String sql) throws HibernateException
   {
      return getSession().createSQLQuery(sql);
   }

   protected void delete(Object entity) throws HibernateException
   {
      getSession().delete(entity);
   }

   protected Filter enableFilter(String name)
   {
      return getSession().enableFilter(name);
   }

   protected void flush() throws HibernateException
   {
      getSession().flush();
   }

   @SuppressWarnings("deprecation")
   protected <T> T get(Class<T> clazz, Serializable id, LockMode lockMode) throws HibernateException
   {
      return (T) getSession().get(clazz, id, lockMode);
   }

   protected <T> T get(Class<T> clazz, Serializable id) throws HibernateException
   {
      return (T) getSession().get(clazz, id);
   }

   protected Query getNamedQuery(String name) throws HibernateException
   {
      return getSession().getNamedQuery(name);
   }

   @SuppressWarnings("deprecation")
   protected <T> T load(Class<T> clazz, Serializable id, LockMode lockMode) throws HibernateException
   {
      return (T) getSession().load(clazz, id, lockMode);
   }

   protected <T> T load(Class<T> clazz, Serializable id) throws HibernateException
   {
      return (T) getSession().load(clazz, id);
   }

   @SuppressWarnings("deprecation")
   protected void lock(Object entity, LockMode lockMode) throws HibernateException
   {
      getSession().lock(entity, lockMode);
   }

   protected <T> T merge(T entity) throws HibernateException
   {
      return (T) getSession().merge(entity);
   }

   protected void persist(Object entity) throws HibernateException
   {
      getSession().persist(entity);
   }

   @SuppressWarnings("deprecation")
   protected void refresh(Object entity, LockMode lockMode) throws HibernateException
   {
      getSession().refresh(entity, lockMode);
   }

   protected void refresh(Object entity) throws HibernateException
   {
      getSession().refresh(entity);
   }
   
}
