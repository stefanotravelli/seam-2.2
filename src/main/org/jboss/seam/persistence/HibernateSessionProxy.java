package org.jboss.seam.persistence;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.Filter;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.EntityKey;
import org.hibernate.engine.PersistenceContext;
import org.hibernate.engine.QueryParameters;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.engine.ActionQueue;
import org.hibernate.engine.EntityEntry;
import org.hibernate.engine.query.sql.NativeSQLQuerySpecification;
import org.hibernate.event.EventListeners;
import org.hibernate.event.EventSource;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.jdbc.Batcher;
import org.hibernate.jdbc.JDBCContext;
import org.hibernate.loader.custom.CustomQuery;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.stat.SessionStatistics;
import org.hibernate.type.Type;

/**
 * Proxies the Session, and implements EL interpolation
 * in HQL. Needs to implement SessionImplementor because
 * DetachedCriteria casts the Session to SessionImplementor.
 * 
 * @author Gavin King
 * @author Emmanuel Bernard
 * FIXME: EventSource should not really be there, remove once HSearch is fixed
 *
 */
public class HibernateSessionProxy implements Session, SessionImplementor, EventSource
{
   private Session delegate;

   /**
    * Don't use that constructor directly, use HibernatePersistenceProvider.proxySession()
    */
   public HibernateSessionProxy(Session session)
   {
      delegate = session;
   }

   public Transaction beginTransaction() throws HibernateException
   {
      return delegate.beginTransaction();
   }

   public void cancelQuery() throws HibernateException
   {
      delegate.cancelQuery();
   }

   public void clear()
   {
      delegate.clear();
   }

   public Connection close() throws HibernateException
   {
      return delegate.close();
   }

   @SuppressWarnings("deprecation")
   public Connection connection() throws HibernateException
   {
      return delegate.connection();
   }

   public boolean contains(Object arg0)
   {
      return delegate.contains(arg0);
   }

   public Criteria createCriteria(Class arg0, String arg1)
   {
      return delegate.createCriteria(arg0, arg1);
   }

   public Criteria createCriteria(Class arg0)
   {
      return delegate.createCriteria(arg0);
   }

   public Criteria createCriteria(String arg0, String arg1)
   {
      return delegate.createCriteria(arg0, arg1);
   }

   public Criteria createCriteria(String arg0)
   {
      return delegate.createCriteria(arg0);
   }

   public Query createFilter(Object arg0, String arg1) throws HibernateException
   {
      return delegate.createFilter(arg0, arg1);
   }

   public Query createQuery(String hql) throws HibernateException
   {
      if ( hql.indexOf('#')>0 )
      {
         QueryParser qp = new QueryParser(hql);
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
         return delegate.createQuery(hql);
      }
   }

   public SQLQuery createSQLQuery(String arg0) throws HibernateException
   {
      return delegate.createSQLQuery(arg0);
   }

   public void delete(Object arg0) throws HibernateException
   {
      delegate.delete(arg0);
   }

   public void delete(String arg0, Object arg1) throws HibernateException
   {
      delegate.delete(arg0, arg1);
   }

   public void disableFilter(String arg0)
   {
      delegate.disableFilter(arg0);
   }

   public Connection disconnect() throws HibernateException
   {
      return delegate.disconnect();
   }

   public Filter enableFilter(String arg0)
   {
      return delegate.enableFilter(arg0);
   }

   public void evict(Object arg0) throws HibernateException
   {
      delegate.evict(arg0);
   }

   public void flush() throws HibernateException
   {
      delegate.flush();
   }

   public Object get(Class arg0, Serializable arg1, LockMode arg2) throws HibernateException
   {
      return delegate.get(arg0, arg1, arg2);
   }

   public Object get(Class arg0, Serializable arg1) throws HibernateException
   {
      return delegate.get(arg0, arg1);
   }

   public Object get(String arg0, Serializable arg1, LockMode arg2) throws HibernateException
   {
      return delegate.get(arg0, arg1, arg2);
   }

   public Object get(String arg0, Serializable arg1) throws HibernateException
   {
      return delegate.get(arg0, arg1);
   }

   public CacheMode getCacheMode()
   {
      return delegate.getCacheMode();
   }

   public LockMode getCurrentLockMode(Object arg0) throws HibernateException
   {
      return delegate.getCurrentLockMode(arg0);
   }

   public Filter getEnabledFilter(String arg0)
   {
      return delegate.getEnabledFilter(arg0);
   }

   public EntityMode getEntityMode()
   {
      return delegate.getEntityMode();
   }

   public String getEntityName(Object arg0) throws HibernateException
   {
      return delegate.getEntityName(arg0);
   }

   public FlushMode getFlushMode()
   {
      return delegate.getFlushMode();
   }

   public Serializable getIdentifier(Object arg0) throws HibernateException
   {
      return delegate.getIdentifier(arg0);
   }

   public Query getNamedQuery(String arg0) throws HibernateException
   {
      return delegate.getNamedQuery(arg0);
   }

   public Session getSession(EntityMode arg0)
   {
      return delegate.getSession(arg0);
   }

   public SessionFactory getSessionFactory()
   {
      return delegate.getSessionFactory();
   }

   public SessionStatistics getStatistics()
   {
      return delegate.getStatistics();
   }

   public Transaction getTransaction()
   {
      return delegate.getTransaction();
   }

   public boolean isConnected()
   {
      return delegate.isConnected();
   }

   public boolean isDirty() throws HibernateException
   {
      return delegate.isDirty();
   }

   public boolean isOpen()
   {
      return delegate.isOpen();
   }

   public Object load(Class arg0, Serializable arg1, LockMode arg2) throws HibernateException
   {
      return delegate.load(arg0, arg1, arg2);
   }

   public Object load(Class arg0, Serializable arg1) throws HibernateException
   {
      return delegate.load(arg0, arg1);
   }

   public void load(Object arg0, Serializable arg1) throws HibernateException
   {
      delegate.load(arg0, arg1);
   }

   public Object load(String arg0, Serializable arg1, LockMode arg2) throws HibernateException
   {
      return delegate.load(arg0, arg1, arg2);
   }

   public Object load(String arg0, Serializable arg1) throws HibernateException
   {
      return delegate.load(arg0, arg1);
   }

   public void lock(Object arg0, LockMode arg1) throws HibernateException
   {
      delegate.lock(arg0, arg1);
   }

   public void lock(String arg0, Object arg1, LockMode arg2) throws HibernateException
   {
      delegate.lock(arg0, arg1, arg2);
   }

   public Object merge(Object arg0) throws HibernateException
   {
      return delegate.merge(arg0);
   }

   public Object merge(String arg0, Object arg1) throws HibernateException
   {
      return delegate.merge(arg0, arg1);
   }

   public void persist(Object arg0) throws HibernateException
   {
      delegate.persist(arg0);
   }

   public void persist(String arg0, Object arg1) throws HibernateException
   {
      delegate.persist(arg0, arg1);
   }

   public void reconnect() throws HibernateException
   {
      throw new UnsupportedOperationException("deprecated");
   }

   public void reconnect(Connection arg0) throws HibernateException
   {
      delegate.reconnect(arg0);
   }

   public void refresh(Object arg0, LockMode arg1) throws HibernateException
   {
      delegate.refresh(arg0, arg1);
   }

   public void refresh(Object arg0) throws HibernateException
   {
      delegate.refresh(arg0);
   }

   public void replicate(Object arg0, ReplicationMode arg1) throws HibernateException
   {
      delegate.replicate(arg0, arg1);
   }

   public void replicate(String arg0, Object arg1, ReplicationMode arg2) throws HibernateException
   {
      delegate.replicate(arg0, arg1, arg2);
   }

   public Serializable save(Object arg0) throws HibernateException
   {
      return delegate.save(arg0);
   }

   public Serializable save(String arg0, Object arg1) throws HibernateException
   {
      return delegate.save(arg0, arg1);
   }

   public void saveOrUpdate(Object arg0) throws HibernateException
   {
      delegate.saveOrUpdate(arg0);
   }

   public void saveOrUpdate(String arg0, Object arg1) throws HibernateException
   {
      delegate.saveOrUpdate(arg0, arg1);
   }

   public void setCacheMode(CacheMode arg0)
   {
      delegate.setCacheMode(arg0);
   }

   public void setFlushMode(FlushMode arg0)
   {
      delegate.setFlushMode(arg0);
   }

   public void setReadOnly(Object arg0, boolean arg1)
   {
      delegate.setReadOnly(arg0, arg1);
   }

   public void update(Object arg0) throws HibernateException
   {
      delegate.update(arg0);
   }

   public void update(String arg0, Object arg1) throws HibernateException
   {
      delegate.update(arg0, arg1);
   }
   
   private SessionImplementor getDelegateSessionImplementor()
   {
      return (SessionImplementor) delegate;
   }

   private EventSource getDelegateEventSource()
   {
      return (EventSource) delegate;
   }

   public void afterScrollOperation()
   {
      getDelegateSessionImplementor().afterScrollOperation();
   }

   public void afterTransactionCompletion(boolean arg0, Transaction arg1)
   {
      getDelegateSessionImplementor().afterTransactionCompletion(arg0, arg1);
   }

   public void beforeTransactionCompletion(Transaction arg0)
   {
      getDelegateSessionImplementor().beforeTransactionCompletion(arg0);
   }

   public String bestGuessEntityName(Object arg0)
   {
      return getDelegateSessionImplementor().bestGuessEntityName(arg0);
   }

   public int executeNativeUpdate(NativeSQLQuerySpecification arg0, QueryParameters arg1) throws HibernateException
   {
      return getDelegateSessionImplementor().executeNativeUpdate(arg0, arg1);
   }

   public int executeUpdate(String arg0, QueryParameters arg1) throws HibernateException
   {
      return getDelegateSessionImplementor().executeUpdate(arg0, arg1);
   }

   public Batcher getBatcher()
   {
      return getDelegateSessionImplementor().getBatcher();
   }

   public Serializable getContextEntityIdentifier(Object arg0)
   {
      return getDelegateSessionImplementor().getContextEntityIdentifier(arg0);
   }

   public int getDontFlushFromFind()
   {
      return getDelegateSessionImplementor().getDontFlushFromFind();
   }

   public Map getEnabledFilters()
   {
      return getDelegateSessionImplementor().getEnabledFilters();
   }

   public EntityPersister getEntityPersister(String arg0, Object arg1) throws HibernateException
   {
      return getDelegateSessionImplementor().getEntityPersister(arg0, arg1);
   }

   public Object getEntityUsingInterceptor(EntityKey arg0) throws HibernateException
   {
      return getDelegateSessionImplementor().getEntityUsingInterceptor(arg0);
   }

   public SessionFactoryImplementor getFactory()
   {
      return getDelegateSessionImplementor().getFactory();
   }

   public String getFetchProfile()
   {
      return getDelegateSessionImplementor().getFetchProfile();
   }

   public Type getFilterParameterType(String arg0)
   {
      return getDelegateSessionImplementor().getFilterParameterType(arg0);
   }

   public Object getFilterParameterValue(String arg0)
   {
      return getDelegateSessionImplementor().getFilterParameterValue(arg0);
   }

   public Interceptor getInterceptor()
   {
      return getDelegateSessionImplementor().getInterceptor();
   }

   public JDBCContext getJDBCContext()
   {
      return getDelegateSessionImplementor().getJDBCContext();
   }

   public EventListeners getListeners()
   {
      return getDelegateSessionImplementor().getListeners();
   }

   public Query getNamedSQLQuery(String arg0)
   {
      return getDelegateSessionImplementor().getNamedSQLQuery(arg0);
   }

   public PersistenceContext getPersistenceContext()
   {
      return getDelegateSessionImplementor().getPersistenceContext();
   }

   public long getTimestamp()
   {
      return getDelegateSessionImplementor().getTimestamp();
   }

   public String guessEntityName(Object arg0) throws HibernateException
   {
      return getDelegateSessionImplementor().guessEntityName(arg0);
   }

   public Object immediateLoad(String arg0, Serializable arg1) throws HibernateException
   {
      return getDelegateSessionImplementor().immediateLoad(arg0, arg1);
   }

   public void initializeCollection(PersistentCollection arg0, boolean arg1) throws HibernateException
   {
      getDelegateSessionImplementor().initializeCollection(arg0, arg1);
   }

   public Object instantiate(String arg0, Serializable arg1) throws HibernateException
   {
      return getDelegateSessionImplementor().instantiate(arg0, arg1);
   }

   public Object internalLoad(String arg0, Serializable arg1, boolean arg2, boolean arg3) throws HibernateException
   {
      return getDelegateSessionImplementor().internalLoad(arg0, arg1, arg2, arg3);
   }

   public boolean isClosed()
   {
      return getDelegateSessionImplementor().isClosed();
   }

   public boolean isEventSource()
   {
      return getDelegateSessionImplementor().isEventSource();
   }

   public boolean isTransactionInProgress()
   {
      return getDelegateSessionImplementor().isTransactionInProgress();
   }

   public Iterator iterate(String arg0, QueryParameters arg1) throws HibernateException
   {
      return getDelegateSessionImplementor().iterate(arg0, arg1);
   }

   public Iterator iterateFilter(Object arg0, String arg1, QueryParameters arg2) throws HibernateException
   {
      return getDelegateSessionImplementor().iterateFilter(arg0, arg1, arg2);
   }

   public List list(CriteriaImpl arg0)
   {
      return getDelegateSessionImplementor().list(arg0);
   }

   public List list(NativeSQLQuerySpecification arg0, QueryParameters arg1) throws HibernateException
   {
      return getDelegateSessionImplementor().list(arg0, arg1);
   }

   public List list(String arg0, QueryParameters arg1) throws HibernateException
   {
      return getDelegateSessionImplementor().list(arg0, arg1);
   }

   public List listCustomQuery(CustomQuery arg0, QueryParameters arg1) throws HibernateException
   {
      return getDelegateSessionImplementor().listCustomQuery(arg0, arg1);
   }

   public List listFilter(Object arg0, String arg1, QueryParameters arg2) throws HibernateException
   {
      return getDelegateSessionImplementor().listFilter(arg0, arg1, arg2);
   }

   public ScrollableResults scroll(CriteriaImpl arg0, ScrollMode arg1)
   {
      return getDelegateSessionImplementor().scroll(arg0, arg1);
   }

   public ScrollableResults scroll(NativeSQLQuerySpecification arg0, QueryParameters arg1) throws HibernateException
   {
      return getDelegateSessionImplementor().scroll(arg0, arg1);
   }

   public ScrollableResults scroll(String arg0, QueryParameters arg1) throws HibernateException
   {
      return getDelegateSessionImplementor().scroll(arg0, arg1);
   }

   public ScrollableResults scrollCustomQuery(CustomQuery arg0, QueryParameters arg1) throws HibernateException
   {
      return getDelegateSessionImplementor().scrollCustomQuery(arg0, arg1);
   }

   public void setAutoClear(boolean arg0)
   {
      getDelegateSessionImplementor().setAutoClear(arg0);
   }

   public void setFetchProfile(String arg0)
   {
      getDelegateSessionImplementor().setFetchProfile(arg0);
   }

	public ActionQueue getActionQueue() {
		return getDelegateEventSource().getActionQueue();
	}

	public Object instantiate(EntityPersister entityPersister, Serializable serializable) throws HibernateException {
		return getDelegateEventSource().instantiate( entityPersister, serializable );
	}

	public void forceFlush(EntityEntry entityEntry) throws HibernateException {
		getDelegateEventSource().forceFlush( entityEntry );
	}

	public void merge(String s, Object o, Map map) throws HibernateException {
		getDelegateEventSource().merge( s, o, map );
	}

	public void persist(String s, Object o, Map map) throws HibernateException {
		getDelegateEventSource().persist( s, o, map );
	}

	public void persistOnFlush(String s, Object o, Map map) {
		getDelegateEventSource().persistOnFlush( s, o, map );
	}

	public void refresh(Object o, Map map) throws HibernateException {
		getDelegateEventSource().refresh( o, map );
	}

	public void saveOrUpdateCopy(String s, Object o, Map map) throws HibernateException {
		getDelegateEventSource().saveOrUpdateCopy( s, o , map );
	}

	public void delete(String s, Object o, boolean b, Set set) {
		getDelegateEventSource().delete( s, o, b, set );
	}
}
