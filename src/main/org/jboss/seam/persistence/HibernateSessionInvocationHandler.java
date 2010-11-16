package org.jboss.seam.persistence;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
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
import org.hibernate.search.FullTextSession;
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
   
   private FullTextSession ftDelegate;
   private Session delegate;
   
   private Map<String, Method> eventSourceMethods = new HashMap<String, Method>();
   
   public HibernateSessionInvocationHandler(Session paramDelegate, FullTextSession searchDelegate)
   {
      this.ftDelegate = searchDelegate;
      this.delegate = paramDelegate;
      buildEventSourceMethodMetadata();
   }
   
   private void buildEventSourceMethodMetadata(){
      Method[] methods = EventSource.class.getDeclaredMethods();
      for (Method declaredMethod : methods)
      {
         eventSourceMethods.put(declaredMethod.getName(), declaredMethod);
      }
   }
   
   /**
    * Get the proper delegate based on {@link org.hibernate.event.EventSource}
    * or {@link org.hibernate.search.FullTextSession}
    * 
    * @param method
    * @return proper delegate based on {@link org.hibernate.event.EventSource}
    *         or {@link org.hibernate.search.FullTextSession}
    */
   Object getDelegate(Method method)
   {
      if (isPureEventSourceMethod(method))
      {
         return delegate;
      }
      else
      {
         return ftDelegate;
      }
   }
   
   /**
    * Detects if Method is hosted on EventSource and *not* any of its interfaces
    * if true returns true otherwise return false
    * 
    * @param method
    * @return true if it is on declared on
    *         {@link org.hibernate.event.EventSource} otherwise return false
    */
   boolean isPureEventSourceMethod(Method method)
   {
      if ( eventSourceMethods.containsKey(method.getName()))
      {
         Method declMethod = eventSourceMethods.get(method);
         if (declMethod != null)
         {
            TypeVariable<Method>[] pars = declMethod.getTypeParameters();
            if (Arrays.equals(method.getTypeParameters(), pars))
            {
               return true;
            }
         }
      }
      
      return false;
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
         return method.invoke(getDelegate(method), args);
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
         return method.invoke(getDelegate(method), args);
      }
      String ejbql = (String) args[0];
      if (ejbql.indexOf('#') > 0)
      {
         QueryParser qp = new QueryParser(ejbql);
         Object[] newArgs = args.clone();
         newArgs[0] = qp.getEjbql();
         Query query = (Query) method.invoke(getDelegate(method), newArgs);
         for (int i = 0; i < qp.getParameterValueBindings().size(); i++)
         {
            query.setParameter(QueryParser.getParameterName(i), qp.getParameterValueBindings().get(i).getValue());
         }
         return query;
      }
      else
      {
         return method.invoke(getDelegate(method), args);
      }
   }
   
   protected Object handleReconnectNoArg(Method method) throws Throwable
   {
      throw new UnsupportedOperationException("deprecated");
   }

   public Interceptor getInterceptor()
   {
      return ((SessionImplementor) ftDelegate).getInterceptor();
   }

   public void setAutoClear(boolean paramBoolean)
   {
      ((SessionImplementor) ftDelegate).setAutoClear(paramBoolean);
   }

   public boolean isTransactionInProgress()
   {
      return ((SessionImplementor) ftDelegate).isTransactionInProgress();
   }

   public void initializeCollection(PersistentCollection paramPersistentCollection, boolean paramBoolean) throws HibernateException
   {
      ((SessionImplementor) ftDelegate).initializeCollection(paramPersistentCollection, paramBoolean);
   }

   public Object internalLoad(String paramString, Serializable paramSerializable, boolean paramBoolean1, boolean paramBoolean2) throws HibernateException
   {
      return ((SessionImplementor) ftDelegate).internalLoad(paramString, paramSerializable, paramBoolean1, paramBoolean2);
   }

   public Object immediateLoad(String paramString, Serializable paramSerializable) throws HibernateException
   {
      return ((SessionImplementor) ftDelegate).immediateLoad(paramString, paramSerializable);
   }

   public long getTimestamp()
   {
      return ((SessionImplementor) ftDelegate).getTimestamp();
   }

   public SessionFactoryImplementor getFactory()
   {
      return ((SessionImplementor) ftDelegate).getFactory();
   }

   public Batcher getBatcher()
   {
      return ((SessionImplementor) ftDelegate).getBatcher();
   }

   public List list(String paramString, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) ftDelegate).list(paramString, paramQueryParameters);
   }

   public Iterator iterate(String paramString, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) ftDelegate).iterate(paramString, paramQueryParameters);
   }

   public ScrollableResults scroll(String paramString, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) ftDelegate).scroll(paramString, paramQueryParameters);
   }

   public ScrollableResults scroll(CriteriaImpl paramCriteriaImpl, ScrollMode paramScrollMode)
   {
      return ((SessionImplementor) ftDelegate).scroll(paramCriteriaImpl, paramScrollMode);
   }

   public List list(CriteriaImpl paramCriteriaImpl)
   {
      return ((SessionImplementor) ftDelegate).list(paramCriteriaImpl);
   }

   public List listFilter(Object paramObject, String paramString, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) ftDelegate).listFilter(paramObject, paramString, paramQueryParameters);
   }

   public Iterator iterateFilter(Object paramObject, String paramString, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) ftDelegate).iterateFilter(paramObject, paramString, paramQueryParameters);
   }

   public EntityPersister getEntityPersister(String paramString, Object paramObject) throws HibernateException
   {
      return ((SessionImplementor) ftDelegate).getEntityPersister(paramString, paramObject);
   }

   public Object getEntityUsingInterceptor(EntityKey paramEntityKey) throws HibernateException
   {
      return ((SessionImplementor) ftDelegate).getEntityUsingInterceptor(paramEntityKey);
   }

   public void afterTransactionCompletion(boolean paramBoolean, Transaction paramTransaction)
   {
      ((SessionImplementor) ftDelegate).afterTransactionCompletion(paramBoolean, paramTransaction);      
   }

   public void beforeTransactionCompletion(Transaction paramTransaction)
   {
      ((SessionImplementor) ftDelegate).beforeTransactionCompletion(paramTransaction)      ;
   }

   public Serializable getContextEntityIdentifier(Object paramObject)
   {
      return ((SessionImplementor) ftDelegate).getContextEntityIdentifier(paramObject);
   }

   public String bestGuessEntityName(Object paramObject)
   {
      return ((SessionImplementor) ftDelegate).bestGuessEntityName(paramObject);
   }

   public String guessEntityName(Object paramObject) throws HibernateException
   {
      return ((SessionImplementor) ftDelegate).guessEntityName(paramObject);
   }

   public Object instantiate(String paramString, Serializable paramSerializable) throws HibernateException
   {
      return ((SessionImplementor) ftDelegate).instantiate(paramString, paramSerializable);
   }

   public List listCustomQuery(CustomQuery paramCustomQuery, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) ftDelegate).listCustomQuery(paramCustomQuery, paramQueryParameters);
   }

   public ScrollableResults scrollCustomQuery(CustomQuery paramCustomQuery, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) ftDelegate).scrollCustomQuery(paramCustomQuery, paramQueryParameters);
   }

   public List list(NativeSQLQuerySpecification paramNativeSQLQuerySpecification, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) ftDelegate).list(paramNativeSQLQuerySpecification, paramQueryParameters);
   }

   public ScrollableResults scroll(NativeSQLQuerySpecification paramNativeSQLQuerySpecification, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) ftDelegate).scroll(paramNativeSQLQuerySpecification, paramQueryParameters);
   }

   public Object getFilterParameterValue(String paramString)
   {
      return ((SessionImplementor) ftDelegate).getFilterParameterValue(paramString);
   }

   public Type getFilterParameterType(String paramString)
   {
      return ((SessionImplementor) ftDelegate).getFilterParameterType(paramString);
   }

   public Map getEnabledFilters()
   {
      return ((SessionImplementor) ftDelegate).getEnabledFilters();
   }

   public int getDontFlushFromFind()
   {
      return ((SessionImplementor) ftDelegate).getDontFlushFromFind();
   }

   public EventListeners getListeners()
   {
      return ((SessionImplementor) ftDelegate).getListeners();
   }

   public PersistenceContext getPersistenceContext()
   {
      return ((SessionImplementor) ftDelegate).getPersistenceContext();
   }

   public int executeUpdate(String paramString, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) ftDelegate).executeUpdate(paramString, paramQueryParameters);
   }

   public int executeNativeUpdate(NativeSQLQuerySpecification paramNativeSQLQuerySpecification, QueryParameters paramQueryParameters) throws HibernateException
   {
      return ((SessionImplementor) ftDelegate).executeNativeUpdate(paramNativeSQLQuerySpecification, paramQueryParameters);
   }


   public EntityMode getEntityMode()
   {
      return ((SessionImplementor) ftDelegate).getEntityMode();
   }

   public CacheMode getCacheMode()
   {
      return ((SessionImplementor) ftDelegate).getCacheMode();
   }

   public void setCacheMode(CacheMode paramCacheMode)
   {
      ((SessionImplementor) ftDelegate).setCacheMode(paramCacheMode);      
   }

   public boolean isOpen()
   {
      return ((SessionImplementor) ftDelegate).isOpen();
   }

   public boolean isConnected()
   {
      return ((SessionImplementor) ftDelegate).isConnected();
   }

   public FlushMode getFlushMode()
   {
      return ((SessionImplementor) ftDelegate).getFlushMode();
   }

   public void setFlushMode(FlushMode paramFlushMode)
   {
      ((SessionImplementor) ftDelegate).setFlushMode(paramFlushMode);      
   }

   public Connection connection()
   {
      return ((SessionImplementor) ftDelegate).connection();
   }

   public void flush()
   {
      ((SessionImplementor) ftDelegate).flush();   
   }

   public Query getNamedQuery(String paramString)
   {
      return ((SessionImplementor) ftDelegate).getNamedQuery(paramString);
   }

   public Query getNamedSQLQuery(String paramString)
   {
      return ((SessionImplementor) ftDelegate).getNamedSQLQuery(paramString);
   }

   public boolean isEventSource()
   {
      return ((SessionImplementor) ftDelegate).isEventSource();
   }

   public void afterScrollOperation()
   {
      ((SessionImplementor) ftDelegate).afterScrollOperation();      
   }

   public String getFetchProfile()
   {
      return ((SessionImplementor) ftDelegate).getFetchProfile();
   }

   public void setFetchProfile(String paramString)
   {
      ((SessionImplementor) ftDelegate).setFetchProfile(paramString);      
   }

   public JDBCContext getJDBCContext()
   {
      return ((SessionImplementor) ftDelegate).getJDBCContext();
   }

   public boolean isClosed()
   {
      return ((SessionImplementor) ftDelegate).isClosed();
   }

   public Session getSession(EntityMode paramEntityMode)
   {
      return ftDelegate.getSession(paramEntityMode);
   }

   public SessionFactory getSessionFactory()
   {
      return ftDelegate.getSessionFactory();
   }

   public Connection close() throws HibernateException
   {
      return ftDelegate.close();
   }

   public void cancelQuery() throws HibernateException
   {
      ftDelegate.cancelQuery();
   }

   public boolean isDirty() throws HibernateException
   {
      return ftDelegate.isDirty();
   }

   public boolean isDefaultReadOnly()
   {
      return ((HibernateSessionInvocationHandler) ftDelegate).isDefaultReadOnly();
   }

   public void setDefaultReadOnly(boolean paramBoolean)
   {
      ((HibernateSessionInvocationHandler) ftDelegate).setDefaultReadOnly(paramBoolean);      
   }

   public Serializable getIdentifier(Object paramObject) throws HibernateException
   {
      return ftDelegate.getIdentifier(paramObject);
   }

   public boolean contains(Object paramObject)
   {
      return ftDelegate.contains(paramObject);
   }

   public void evict(Object paramObject) throws HibernateException
   {
      ftDelegate.evict(paramObject);
   }

   public Object load(Class paramClass, Serializable paramSerializable, LockMode paramLockMode) throws HibernateException
   {
      return ftDelegate.load(paramClass, paramSerializable, paramLockMode);
   }

   public Object load(String paramString, Serializable paramSerializable, LockMode paramLockMode) throws HibernateException
   {
      return ftDelegate.load(paramString, paramSerializable, paramLockMode);
   }

   public Object load(Class paramClass, Serializable paramSerializable) throws HibernateException
   {
      return ftDelegate.load(paramClass, paramSerializable);
   }

   public Object load(String paramString, Serializable paramSerializable) throws HibernateException
   {
      return ftDelegate.load(paramString, paramSerializable);
   }

   public void load(Object paramObject, Serializable paramSerializable) throws HibernateException
   {
      ftDelegate.load(paramObject, paramSerializable);      
   }

   public void replicate(Object paramObject, ReplicationMode paramReplicationMode) throws HibernateException
   {
      ftDelegate.replicate(paramObject, paramReplicationMode);      
   }

   public void replicate(String paramString, Object paramObject, ReplicationMode paramReplicationMode) throws HibernateException
   {
      ftDelegate.replicate(paramString, paramObject, paramReplicationMode);      
   }

   public Serializable save(Object paramObject) throws HibernateException
   {
      return ftDelegate.save(paramObject);
   }

   public Serializable save(String paramString, Object paramObject) throws HibernateException
   {
      return ftDelegate.save(paramString, paramObject);
   }

   public void saveOrUpdate(Object paramObject) throws HibernateException
   {
      ftDelegate.saveOrUpdate(paramObject);      
   }

   public void saveOrUpdate(String paramString, Object paramObject) throws HibernateException
   {
      ftDelegate.saveOrUpdate(paramString, paramObject);      
   }

   public void update(Object paramObject) throws HibernateException
   {
      ftDelegate.update(paramObject);      
   }

   public void update(String paramString, Object paramObject) throws HibernateException
   {
      ftDelegate.update(paramString, paramObject);      
   }

   public Object merge(Object paramObject) throws HibernateException
   {
      return ftDelegate.merge(paramObject);
   }

   public Object merge(String paramString, Object paramObject) throws HibernateException
   {
      return ftDelegate.merge(paramString, paramObject);
   }

   public void persist(Object paramObject) throws HibernateException
   {
      ftDelegate.persist(paramObject);
   }

   public void persist(String paramString, Object paramObject) throws HibernateException
   {
      ftDelegate.persist(paramString, paramObject);
   }

   public void delete(Object paramObject) throws HibernateException
   {
      ftDelegate.delete(paramObject);
   }

   public void delete(String paramString, Object paramObject) throws HibernateException
   {
      ((EventSource) delegate).delete(paramString, paramObject);
   }

   public void lock(Object paramObject, LockMode paramLockMode) throws HibernateException
   {
      ftDelegate.lock(paramObject, paramLockMode);      
   }

   public void lock(String paramString, Object paramObject, LockMode paramLockMode) throws HibernateException
   {
      ftDelegate.lock(paramString, paramObject, paramLockMode);      
   }

   public void refresh(Object paramObject) throws HibernateException
   {
      ftDelegate.refresh(paramObject);
   }

   public void refresh(Object paramObject, LockMode paramLockMode) throws HibernateException
   {
      ftDelegate.refresh(paramObject, paramLockMode);
   }

   public LockMode getCurrentLockMode(Object paramObject) throws HibernateException
   {
      return ftDelegate.getCurrentLockMode(paramObject);
   }

   public Transaction beginTransaction() throws HibernateException
   {
      return ftDelegate.beginTransaction();
   }

   public Transaction getTransaction()
   {
      return ftDelegate.getTransaction();
   }

   public Criteria createCriteria(Class paramClass)
   {
      return ftDelegate.createCriteria(paramClass);
   }

   public Criteria createCriteria(Class paramClass, String paramString)
   {
      return ftDelegate.createCriteria(paramClass, paramString);
   }

   public Criteria createCriteria(String paramString)
   {
      return ftDelegate.createCriteria(paramString);
   }

   public Criteria createCriteria(String paramString1, String paramString2)
   {
      return ftDelegate.createCriteria(paramString1, paramString2);
   }

   public Query createQuery(String paramString) throws HibernateException
   {
      return ftDelegate.createQuery(paramString);
   }

   public SQLQuery createSQLQuery(String paramString) throws HibernateException
   {
      return ftDelegate.createSQLQuery(paramString);
   }

   public Query createFilter(Object paramObject, String paramString) throws HibernateException
   {
      return ftDelegate.createFilter(paramObject, paramString);
   }

   public void clear()
   {
      ftDelegate.clear();      
   }

   public Object get(Class paramClass, Serializable paramSerializable) throws HibernateException
   {
      return ftDelegate.get(paramClass, paramSerializable);
   }

   public Object get(Class paramClass, Serializable paramSerializable, LockMode paramLockMode) throws HibernateException
   {
      return ftDelegate.get(paramClass, paramSerializable, paramLockMode);
   }

   public Object get(String paramString, Serializable paramSerializable) throws HibernateException
   {
      return ftDelegate.get(paramString, paramSerializable);
   }

   public Object get(String paramString, Serializable paramSerializable, LockMode paramLockMode) throws HibernateException
   {
      return ftDelegate.get(paramString, paramSerializable, paramLockMode);
   }

   public String getEntityName(Object paramObject) throws HibernateException
   {
      return ftDelegate.getEntityName(paramObject);
   }

   public Filter enableFilter(String paramString)
   {
      return ftDelegate.enableFilter(paramString);
   }

   public Filter getEnabledFilter(String paramString)
   {
      return ftDelegate.getEnabledFilter(paramString);
   }

   public void disableFilter(String paramString)
   {
      ftDelegate.disableFilter(paramString);      
   }

   public SessionStatistics getStatistics()
   {
      return ftDelegate.getStatistics();
   }

   public boolean isReadOnly(Object paramObject)
   {
      return ((HibernateSessionInvocationHandler) ftDelegate).isReadOnly(paramObject);
   }

   public void setReadOnly(Object paramObject, boolean paramBoolean)
   {
      ftDelegate.setReadOnly(paramObject, paramBoolean);
   }

   public void doWork(Work paramWork) throws HibernateException
   {
      ftDelegate.doWork(paramWork);
   }

   public Connection disconnect() throws HibernateException
   {
      return ftDelegate.disconnect();
   }

   @SuppressWarnings("deprecation")
   public void reconnect() throws HibernateException
   {
      ftDelegate.reconnect();
   }

   public void reconnect(Connection paramConnection) throws HibernateException
   {
      ftDelegate.reconnect(paramConnection);
   }

   public boolean isFetchProfileEnabled(String paramString)
   {
      return ((HibernateSessionInvocationHandler) ftDelegate).isFetchProfileEnabled(paramString);
   }

   public void enableFetchProfile(String paramString)
   {
      ((HibernateSessionInvocationHandler) ftDelegate).enableFetchProfile(paramString);
   }

   public void disableFetchProfile(String paramString)
   {
      ((HibernateSessionInvocationHandler) ftDelegate).disableFetchProfile(paramString);
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
