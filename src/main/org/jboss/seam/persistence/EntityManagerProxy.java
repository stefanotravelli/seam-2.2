package org.jboss.seam.persistence;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.jboss.seam.security.permission.PermissionManager;

/**
 * Proxies the EntityManager, and implements EL interpolation
 * in JPA-QL
 * 
 * @author Gavin King
 *
 */
public class EntityManagerProxy implements EntityManager, Serializable
{
   private EntityManager delegate;

   public EntityManagerProxy(EntityManager entityManager)
   {
      delegate = entityManager;
   }

   public void clear()
   {
      delegate.clear();
   }

   public void close()
   {
      delegate.close();
   }

   public boolean contains(Object entity)
   {
      return delegate.contains(entity);
   }

   public Query createNamedQuery(String name)
   {
      return delegate.createNamedQuery(name);
   }

   public Query createNativeQuery(String sql, Class clazz)
   {
      return delegate.createNativeQuery(sql, clazz);
   }

   public Query createNativeQuery(String sql, String lang)
   {
      return delegate.createNativeQuery(sql, lang);
   }

   public Query createNativeQuery(String sql)
   {
      return delegate.createNativeQuery(sql);
   }

   public Query createQuery(String ejbql)
   {
      if ( ejbql.indexOf('#')>0 )
      {
         QueryParser qp = new QueryParser(ejbql);
         Query query = delegate.createQuery( qp.getEjbql() );
         for (int i=0; i<qp.getParameterValueBindings().size(); i++)
         {
            query.setParameter( 
                     QueryParser.getParameterName(i), 
                     qp.getParameterValueBindings().get(i).getValue() 
                  );
         }
         return query;
      }
      else
      {
         return delegate.createQuery(ejbql);
      }
   }

   public <T> T find(Class<T> clazz, Object id)
   {
      return delegate.find(clazz, id);
   }

   public void flush()
   {
      delegate.flush();
   }

   public Object getDelegate()
   {
      return PersistenceProvider.instance().proxyDelegate( delegate.getDelegate() );
   }

   public FlushModeType getFlushMode()
   {
      return delegate.getFlushMode();
   }

   public <T> T getReference(Class<T> clazz, Object id)
   {
      return delegate.getReference(clazz, id);
   }

   public EntityTransaction getTransaction()
   {
      return delegate.getTransaction();
   }

   public boolean isOpen()
   {
      return delegate.isOpen();
   }

   public void joinTransaction()
   {
      delegate.joinTransaction();
   }

   public void lock(Object entity, LockModeType lm)
   {
      delegate.lock(entity, lm);
   }

   public <T> T merge(T entity)
   {
      return delegate.merge(entity);
   }

   public void persist(Object entity)
   {
      delegate.persist(entity);
   }

   public void refresh(Object entity)
   {
      delegate.refresh(entity);
   }

   public void remove(Object entity)
   {
      delegate.remove(entity);
      PermissionManager.instance().clearPermissions(entity);
   }

   public void setFlushMode(FlushModeType fm)
   {
      delegate.setFlushMode(fm);
   }
}
