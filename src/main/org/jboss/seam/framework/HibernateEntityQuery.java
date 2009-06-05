package org.jboss.seam.framework;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.persistence.QueryParser;

/**
 * A Query object for Hibernate.
 * 
 * @author Gavin King
 *
 */
public class HibernateEntityQuery<E> extends Query<Session, E>
{

   private List<E> resultList;
   private E singleResult;
   private Long resultCount;
   
   private Boolean cacheable;
   private String cacheRegion;
   private Integer fetchSize;
   
   @Override
   public void validate()
   {
      super.validate();
      if ( getSession()==null )
      {
         throw new IllegalStateException("hibernateSession is null");
      }
   }

   @Transactional
   @Override
   public List<E> getResultList()
   {
      if ( isAnyParameterDirty() )
      {
         refresh();
      }
      initResultList();
      return truncResultList(resultList);
   }

   private void initResultList()
   {
      if (resultList==null)
      {
         org.hibernate.Query query = createQuery();
         resultList = query==null ? null : query.list();
      }
   }
   
   @Override
   @Transactional
   public boolean isNextExists()
   {
      return resultList!=null && getMaxResults()!=null &&
            resultList.size() > getMaxResults();
   }
   
   @Transactional
   @Override
   public E getSingleResult()
   {
      if (isAnyParameterDirty())
      {
         refresh();
      }
      initSingleResult();
      return singleResult;
   }

   private void initSingleResult()
   {
      if (singleResult==null)
      {
         org.hibernate.Query query = createQuery();
         singleResult = (E) (query==null ? 
               null : query.uniqueResult());
      }
   }

   @Transactional
   @Override
   public Long getResultCount()
   {
      if (isAnyParameterDirty())
      {
         refresh();
      }
      initResultCount();
      return resultCount;
   }

   private void initResultCount()
   {
      if (resultCount==null)
      {
         org.hibernate.Query query = createCountQuery();
         resultCount = query==null ? 
               null : (Long) query.uniqueResult();
      }
   }

   @Override
   public void refresh()
   {
      super.refresh();
      resultCount = null;
      resultList = null;
      singleResult = null;
   }
   
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
   
   protected org.hibernate.Query createQuery()
   {
      parseEjbql();
      
      evaluateAllParameters();
      
      org.hibernate.Query query = getSession().createQuery( getRenderedEjbql() );
      setParameters( query, getQueryParameterValues(), 0 );
      setParameters( query, getRestrictionParameterValues(), getQueryParameterValues().size() );
      if ( getFirstResult()!=null) query.setFirstResult( getFirstResult() );
      if ( getMaxResults()!=null) query.setMaxResults( getMaxResults()+1 ); //add one, so we can tell if there is another page
      if ( getCacheable()!=null ) query.setCacheable( getCacheable() );
      if ( getCacheRegion()!=null ) query.setCacheRegion( getCacheRegion() );
      if ( getFetchSize()!=null ) query.setFetchSize( getFetchSize() );
      return query;
   }
   
   protected org.hibernate.Query createCountQuery()
   {
      parseEjbql();
      
      evaluateAllParameters();
      
      org.hibernate.Query query = getSession().createQuery( getCountEjbql() );
      setParameters( query, getQueryParameterValues(), 0 );
      setParameters( query, getRestrictionParameterValues(), getQueryParameterValues().size() );
      return query;
   }

   private void setParameters(org.hibernate.Query query, List<Object> parameters, int start)
   {
      for (int i=0; i<parameters.size(); i++)
      {
         Object parameterValue = parameters.get(i);
         if ( isRestrictionParameterSet(parameterValue) )
         {
            if(parameterValue instanceof Collection){
               query.setParameterList(QueryParser.getParameterName(start + i), (Collection) parameterValue);
            }else{
               query.setParameter( QueryParser.getParameterName(start + i), parameterValue );
            }
         }
      }
   }

   protected Boolean getCacheable()
   {
      return cacheable;
   }

   protected void setCacheable(Boolean cacheable)
   {
      this.cacheable = cacheable;
   }

   protected String getCacheRegion()
   {
      return cacheRegion;
   }

   protected void setCacheRegion(String cacheRegion)
   {
      this.cacheRegion = cacheRegion;
   }

   protected Integer getFetchSize()
   {
      return fetchSize;
   }

   protected void setFetchSize(Integer fetchSize)
   {
      this.fetchSize = fetchSize;
   }

}
