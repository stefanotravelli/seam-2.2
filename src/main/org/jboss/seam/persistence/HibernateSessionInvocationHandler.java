package org.jboss.seam.persistence;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.hibernate.engine.ActionQueue;
import org.hibernate.engine.EntityEntry;
import org.hibernate.engine.EntityKey;
import org.hibernate.engine.PersistenceContext;
import org.hibernate.engine.QueryParameters;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.engine.query.sql.NativeSQLQuerySpecification;
import org.hibernate.event.EventListeners;
import org.hibernate.event.EventSource;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.jdbc.Batcher;
import org.hibernate.jdbc.JDBCContext;
import org.hibernate.jdbc.Work;
import org.hibernate.loader.custom.CustomQuery;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.stat.SessionStatistics;
import org.hibernate.type.Type;

/**
 * InvocationHandler that proxies the Session, and implements EL interpolation
 * in HQL. Needs to implement SessionImplementor because DetachedCriteria casts
 * the Session to SessionImplementor.
 * 
 * @author Gavin King
 * @author Emmanuel Bernard
 * @author Mike Youngstrom
 * @author Marek Novotny
 * 
 */
public class HibernateSessionInvocationHandler implements InvocationHandler, Serializable, EventSource
{
   
   private Session delegate;
   
   public HibernateSessionInvocationHandler(Session paramDelegate)
   {
      this.delegate = paramDelegate;
   }
     
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      try
      {
         if ("createQuery".equals(method.getName()) && method.getParameterTypes().length > 0 && method.getParameterTypes()[0].equals(String.class))
         {
            return handleCreateQueryWithString(method, args);
         }
         if ("reconnect".equals(method.getName()) && method.getParameterTypes().length == 0)
         {
            return handleReconnectNoArg(method);
         }
         return method.invoke(delegate, args);
      }
      catch (InvocationTargetException e)
      {
         throw e.getTargetException();
      }
   }
   
   protected Object handleCreateQueryWithString(Method method, Object[] args) throws Throwable
   {
      if (args[0] == null)
      {
         //return method.invoke(getDelegate(method), args);
         return method.invoke(delegate, args);
      }
      String ejbql = (String) args[0];
      if (ejbql.indexOf('#') > 0)
      {
         QueryParser qp = new QueryParser(ejbql);
         Object[] newArgs = args.clone();
         newArgs[0] = qp.getEjbql();
         //Query query = (Query) method.invoke(getDelegate(method), newArgs);
         Query query = (Query) method.invoke(delegate, newArgs);
         for (int i = 0; i < qp.getParameterValueBindings().size(); i++)
         {
            query.setParameter(QueryParser.getParameterName(i), qp.getParameterValueBindings().get(i).getValue());
         }
         return query;
      }
      else
      {
         return method.invoke(delegate, args);
      }
   }
   
   protected Object handleReconnectNoArg(Method method) throws Throwable
   {
      throw new UnsupportedOperationException("deprecated");
   }

   public Interceptor getInterceptor()
   {
      return ((SessionImplementor) delegate).getInterceptor();
   }

   public void setAutoClear(boolean paramBoolean)
   {
      ((SessionImplementor) delegate).setAutoClear(paramBoolean);
   }

   public boolean isTransactionInProgress()
   {
      return ((SessionImplementor) delegate).isTransactionInProgress();
   }

   public void initializeCollection(PersistentCollection paramPersistentCollection, boolean paramBoolean) throws HibernateException
   {
      ((SessionImplementor) delegate).initializeCollection(paramPersistentCollection, paramBoolean);
   }

   public Object internalLoad(String paramString, Serializable paramSerializable, boolean paramBoolean1, boolean paramBoolean2) throws HibernateException
   {
      return ((SessionImplementor) delegate).internalLoad(paramString, paramSerializable, paramBoolean1, paramBoolean2);
   }

   public Object immediateLoad(String paramString, Serializable paramSerializable) throws HibernateException
   {
      return ((SessionImplementor) delegate).immediateLoad(paramString, paramSerializable);
   }

   public long getTimestamp()
   {
      return ((SessionImplementor) delegate).getTimestamp();
   }

   public SessionFactoryImplementor getFactory()
   {
      return ((SessionImplementor) delegate).getFactory();
   }

   public Batcher getBatcher()
   {
      return ((SessionImplementor) delegate).getBatcher();
   }

   public List list(String paramString, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) delegate).list(paramString, paramQueryParameters);
   }

   public Iterator iterate(String paramString, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) delegate).iterate(paramString, paramQueryParameters);
   }

   public ScrollableResults scroll(String paramString, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) delegate).scroll(paramString, paramQueryParameters);
   }

   public ScrollableResults scroll(CriteriaImpl paramCriteriaImpl, ScrollMode paramScrollMode)
   {
      return ((SessionImplementor) delegate).scroll(paramCriteriaImpl, paramScrollMode);
   }

   public List list(CriteriaImpl paramCriteriaImpl)
   {
      return ((SessionImplementor) delegate).list(paramCriteriaImpl);
   }

   public List listFilter(Object paramObject, String paramString, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) delegate).listFilter(paramObject, paramString, paramQueryParameters);
   }

   public Iterator iterateFilter(Object paramObject, String paramString, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) delegate).iterateFilter(paramObject, paramString, paramQueryParameters);
   }

   public EntityPersister getEntityPersister(String paramString, Object paramObject) throws HibernateException
   {
      return ((SessionImplementor) delegate).getEntityPersister(paramString, paramObject);
   }

   public Object getEntityUsingInterceptor(EntityKey paramEntityKey) throws HibernateException
   {
      return ((SessionImplementor) delegate).getEntityUsingInterceptor(paramEntityKey);
   }

   public void afterTransactionCompletion(boolean paramBoolean, Transaction paramTransaction)
   {
      ((SessionImplementor) delegate).afterTransactionCompletion(paramBoolean, paramTransaction);      
   }

   public void beforeTransactionCompletion(Transaction paramTransaction)
   {
      ((SessionImplementor) delegate).beforeTransactionCompletion(paramTransaction)      ;
   }

   public Serializable getContextEntityIdentifier(Object paramObject)
   {
      return ((SessionImplementor) delegate).getContextEntityIdentifier(paramObject);
   }

   public String bestGuessEntityName(Object paramObject)
   {
      return ((SessionImplementor) delegate).bestGuessEntityName(paramObject);
   }

   public String guessEntityName(Object paramObject) throws HibernateException
   {
      return ((SessionImplementor) delegate).guessEntityName(paramObject);
   }

   public Object instantiate(String paramString, Serializable paramSerializable) throws HibernateException
   {
      return ((SessionImplementor) delegate).instantiate(paramString, paramSerializable);
   }

   public List listCustomQuery(CustomQuery paramCustomQuery, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) delegate).listCustomQuery(paramCustomQuery, paramQueryParameters);
   }

   public ScrollableResults scrollCustomQuery(CustomQuery paramCustomQuery, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) delegate).scrollCustomQuery(paramCustomQuery, paramQueryParameters);
   }

   public List list(NativeSQLQuerySpecification paramNativeSQLQuerySpecification, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) delegate).list(paramNativeSQLQuerySpecification, paramQueryParameters);
   }

   public ScrollableResults scroll(NativeSQLQuerySpecification paramNativeSQLQuerySpecification, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) delegate).scroll(paramNativeSQLQuerySpecification, paramQueryParameters);
   }

   public Object getFilterParameterValue(String paramString)
   {
      return ((SessionImplementor) delegate).getFilterParameterValue(paramString);
   }

   public Type getFilterParameterType(String paramString)
   {
      return ((SessionImplementor) delegate).getFilterParameterType(paramString);
   }

   public Map getEnabledFilters()
   {
      return ((SessionImplementor) delegate).getEnabledFilters();
   }

   public int getDontFlushFromFind()
   {
      return ((SessionImplementor) delegate).getDontFlushFromFind();
   }

   public EventListeners getListeners()
   {
      return ((SessionImplementor) delegate).getListeners();
   }

   public PersistenceContext getPersistenceContext()
   {
      return ((SessionImplementor) delegate).getPersistenceContext();
   }

   public int executeUpdate(String paramString, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) delegate).executeUpdate(paramString, paramQueryParameters);
   }

   public int executeNativeUpdate(NativeSQLQuerySpecification paramNativeSQLQuerySpecification, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) delegate).executeNativeUpdate(paramNativeSQLQuerySpecification, paramQueryParameters);
   }


   public EntityMode getEntityMode()
   {
      return ((SessionImplementor) delegate).getEntityMode();
   }

   public CacheMode getCacheMode()
   {
      return ((SessionImplementor) delegate).getCacheMode();
   }

   public void setCacheMode(CacheMode paramCacheMode)
   {
      ((SessionImplementor) delegate).setCacheMode(paramCacheMode);      
   }

   public boolean isOpen()
   {
      return ((SessionImplementor) delegate).isOpen();
   }

   public boolean isConnected()
   {
      return ((SessionImplementor) delegate).isConnected();
   }

   public FlushMode getFlushMode()
   {
      return ((SessionImplementor) delegate).getFlushMode();
   }

   public void setFlushMode(FlushMode paramFlushMode)
   {
      ((SessionImplementor) delegate).setFlushMode(paramFlushMode);      
   }

   public Connection connection()
   {
      return ((SessionImplementor) delegate).connection();
   }

   public void flush()
   {
      ((SessionImplementor) delegate).flush();   
   }

   public Query getNamedQuery(String paramString)
   {
      return ((SessionImplementor) delegate).getNamedQuery(paramString);
   }

   public Query getNamedSQLQuery(String paramString)
   {
      return ((SessionImplementor) delegate).getNamedSQLQuery(paramString);
   }

   public boolean isEventSource()
   {
      return ((SessionImplementor) delegate).isEventSource();
   }

   public void afterScrollOperation()
   {
      ((SessionImplementor) delegate).afterScrollOperation();      
   }

   public String getFetchProfile()
   {
      return ((SessionImplementor) delegate).getFetchProfile();
   }

   public void setFetchProfile(String paramString)
   {
      ((SessionImplementor) delegate).setFetchProfile(paramString);      
   }

   public JDBCContext getJDBCContext()
   {
      return ((SessionImplementor) delegate).getJDBCContext();
   }

   public boolean isClosed()
   {
      return ((SessionImplementor) delegate).isClosed();
   }

   public Session getSession(EntityMode paramEntityMode)
   {
      return delegate.getSession(paramEntityMode);
   }

   public SessionFactory getSessionFactory()
   {
      return delegate.getSessionFactory();
   }

   public Connection close() throws HibernateException
   {
      return delegate.close();
   }

   public void cancelQuery() throws HibernateException
   {
      delegate.cancelQuery();
   }

   public boolean isDirty() throws HibernateException
   {
      return delegate.isDirty();
   }

   public boolean isDefaultReadOnly()
   {
      return ((HibernateSessionInvocationHandler) delegate).isDefaultReadOnly();
   }

   public void setDefaultReadOnly(boolean paramBoolean)
   {
      ((HibernateSessionInvocationHandler) delegate).setDefaultReadOnly(paramBoolean);      
   }

   public Serializable getIdentifier(Object paramObject) throws HibernateException
   {
      return delegate.getIdentifier(paramObject);
   }

   public boolean contains(Object paramObject)
   {
      return delegate.contains(paramObject);
   }

   public void evict(Object paramObject) throws HibernateException
   {
      delegate.evict(paramObject);
   }

   public Object load(Class paramClass, Serializable paramSerializable, LockMode paramLockMode) throws HibernateException
   {
      return delegate.load(paramClass, paramSerializable, paramLockMode);
   }

   public Object load(String paramString, Serializable paramSerializable, LockMode paramLockMode) throws HibernateException
   {
      return delegate.load(paramString, paramSerializable, paramLockMode);
   }

   public Object load(Class paramClass, Serializable paramSerializable) throws HibernateException
   {
      return delegate.load(paramClass, paramSerializable);
   }

   public Object load(String paramString, Serializable paramSerializable) throws HibernateException
   {
      return delegate.load(paramString, paramSerializable);
   }

   public void load(Object paramObject, Serializable paramSerializable) throws HibernateException
   {
      delegate.load(paramObject, paramSerializable);      
   }

   public void replicate(Object paramObject, ReplicationMode paramReplicationMode) throws HibernateException
   {
      delegate.replicate(paramObject, paramReplicationMode);      
   }

   public void replicate(String paramString, Object paramObject, ReplicationMode paramReplicationMode) throws HibernateException
   {
      delegate.replicate(paramString, paramObject, paramReplicationMode);      
   }

   public Serializable save(Object paramObject) throws HibernateException
   {
      return delegate.save(paramObject);
   }

   public Serializable save(String paramString, Object paramObject) throws HibernateException
   {
      return delegate.save(paramString, paramObject);
   }

   public void saveOrUpdate(Object paramObject) throws HibernateException
   {
      delegate.saveOrUpdate(paramObject);      
   }

   public void saveOrUpdate(String paramString, Object paramObject) throws HibernateException
   {
      delegate.saveOrUpdate(paramString, paramObject);      
   }

   public void update(Object paramObject) throws HibernateException
   {
      delegate.update(paramObject);      
   }

   public void update(String paramString, Object paramObject) throws HibernateException
   {
      delegate.update(paramString, paramObject);      
   }

   public Object merge(Object paramObject) throws HibernateException
   {
      return delegate.merge(paramObject);
   }

   public Object merge(String paramString, Object paramObject) throws HibernateException
   {
      return delegate.merge(paramString, paramObject);
   }

   public void persist(Object paramObject) throws HibernateException
   {
      delegate.persist(paramObject);
   }

   public void persist(String paramString, Object paramObject) throws HibernateException
   {
      delegate.persist(paramString, paramObject);
   }

   public void delete(Object paramObject) throws HibernateException
   {
      delegate.delete(paramObject);
   }

   public void delete(String paramString, Object paramObject) throws HibernateException
   {
      ((EventSource) delegate).delete(paramString, paramObject);
   }

   public void lock(Object paramObject, LockMode paramLockMode) throws HibernateException
   {
      delegate.lock(paramObject, paramLockMode);      
   }

   public void lock(String paramString, Object paramObject, LockMode paramLockMode) throws HibernateException
   {
      delegate.lock(paramString, paramObject, paramLockMode);      
   }

   public void refresh(Object paramObject) throws HibernateException
   {
      delegate.refresh(paramObject);
   }

   public void refresh(Object paramObject, LockMode paramLockMode) throws HibernateException
   {
      delegate.refresh(paramObject, paramLockMode);
   }

   public LockMode getCurrentLockMode(Object paramObject) throws HibernateException
   {
      return delegate.getCurrentLockMode(paramObject);
   }

   public Transaction beginTransaction() throws HibernateException
   {
      return delegate.beginTransaction();
   }

   public Transaction getTransaction()
   {
      return delegate.getTransaction();
   }

   public Criteria createCriteria(Class paramClass)
   {
      return delegate.createCriteria(paramClass);
   }

   public Criteria createCriteria(Class paramClass, String paramString)
   {
      return delegate.createCriteria(paramClass, paramString);
   }

   public Criteria createCriteria(String paramString)
   {
      return delegate.createCriteria(paramString);
   }

   public Criteria createCriteria(String paramString1, String paramString2)
   {
      return delegate.createCriteria(paramString1, paramString2);
   }

   public Query createQuery(String paramString) throws HibernateException
   {
      return delegate.createQuery(paramString);
   }

   public SQLQuery createSQLQuery(String paramString) throws HibernateException
   {
      return delegate.createSQLQuery(paramString);
   }

   public Query createFilter(Object paramObject, String paramString) throws HibernateException
   {
      return delegate.createFilter(paramObject, paramString);
   }

   public void clear()
   {
      delegate.clear();      
   }

   public Object get(Class paramClass, Serializable paramSerializable) throws HibernateException
   {
      return delegate.get(paramClass, paramSerializable);
   }

   public Object get(Class paramClass, Serializable paramSerializable, LockMode paramLockMode) throws HibernateException
   {
      return delegate.get(paramClass, paramSerializable, paramLockMode);
   }

   public Object get(String paramString, Serializable paramSerializable) throws HibernateException
   {
      return delegate.get(paramString, paramSerializable);
   }

   public Object get(String paramString, Serializable paramSerializable, LockMode paramLockMode) throws HibernateException
   {
      return delegate.get(paramString, paramSerializable, paramLockMode);
   }

   public String getEntityName(Object paramObject) throws HibernateException
   {
      return delegate.getEntityName(paramObject);
   }

   public Filter enableFilter(String paramString)
   {
      return delegate.enableFilter(paramString);
   }

   public Filter getEnabledFilter(String paramString)
   {
      return delegate.getEnabledFilter(paramString);
   }

   public void disableFilter(String paramString)
   {
      delegate.disableFilter(paramString);      
   }

   public SessionStatistics getStatistics()
   {
      return delegate.getStatistics();
   }

   public boolean isReadOnly(Object paramObject)
   {
      return ((HibernateSessionInvocationHandler) delegate).isReadOnly(paramObject);
   }

   public void setReadOnly(Object paramObject, boolean paramBoolean)
   {
      delegate.setReadOnly(paramObject, paramBoolean);
   }

   public void doWork(Work paramWork) throws HibernateException
   {
      delegate.doWork(paramWork);
   }

   public Connection disconnect() throws HibernateException
   {
      return delegate.disconnect();
   }

   @SuppressWarnings("deprecation")
   public void reconnect() throws HibernateException
   {
      delegate.reconnect();
   }

   public void reconnect(Connection paramConnection) throws HibernateException
   {
      delegate.reconnect(paramConnection);
   }

   public boolean isFetchProfileEnabled(String paramString)
   {
      return ((HibernateSessionInvocationHandler) delegate).isFetchProfileEnabled(paramString);
   }

   public void enableFetchProfile(String paramString)
   {
      ((HibernateSessionInvocationHandler) delegate).enableFetchProfile(paramString);
   }

   public void disableFetchProfile(String paramString)
   {
      ((HibernateSessionInvocationHandler) delegate).disableFetchProfile(paramString);
   }

   public ActionQueue getActionQueue()
   {
      return ((EventSource) delegate).getActionQueue();
   }

   public Object instantiate(EntityPersister paramEntityPersister, Serializable paramSerializable) throws HibernateException
   {
      return ((EventSource) delegate).instantiate(paramEntityPersister, paramSerializable);
   }

   public void forceFlush(EntityEntry paramEntityEntry) throws HibernateException
   {
      ((EventSource) delegate).forceFlush(paramEntityEntry);
   }

   public void merge(String paramString, Object paramObject, Map paramMap) throws HibernateException
   {
      ((EventSource) delegate).merge(paramString, paramObject, paramMap);
   }

   public void persist(String paramString, Object paramObject, Map paramMap) throws HibernateException
   {
      ((EventSource) delegate).persist(paramString, paramObject, paramMap);
   }

   public void persistOnFlush(String paramString, Object paramObject, Map paramMap)
   {
      ((EventSource) delegate).persistOnFlush(paramString, paramObject, paramMap);
   }

   public void refresh(Object paramObject, Map paramMap) throws HibernateException
   {
      ((EventSource) delegate).refresh(paramObject, paramMap);
   }

   public void saveOrUpdateCopy(String paramString, Object paramObject, Map paramMap) throws HibernateException
   {
      ((EventSource) delegate).saveOrUpdateCopy(paramString, paramObject, paramMap);      
   }

   public void delete(String paramString, Object paramObject, boolean paramBoolean, Set paramSet)
   {
      ((EventSource) delegate).delete(paramString, paramObject, paramBoolean, paramSet);
   }

}
