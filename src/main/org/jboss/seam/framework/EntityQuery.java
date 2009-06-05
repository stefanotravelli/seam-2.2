package org.jboss.seam.framework;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.transaction.SystemException;

import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.persistence.PersistenceProvider;
import org.jboss.seam.persistence.QueryParser;
import org.jboss.seam.persistence.PersistenceProvider.Feature;
import org.jboss.seam.transaction.Transaction;

/**
 * A Query object for JPA.
 * 
 * @author Gavin King
 *
 */
public class EntityQuery<E> extends Query<EntityManager, E>
{

   private List<E> resultList;
   private E singleResult;
   private Long resultCount;
   private Map<String, String> hints;

   /**
    * Validate the query
    * 
    * @throws IllegalStateException if the query is not valid
    */
   @Override
   public void validate()
   {
      super.validate();
      if ( getEntityManager()==null )
      {
         throw new IllegalStateException("entityManager is null");
      }
      
      if (!PersistenceProvider.instance().supportsFeature(Feature.WILDCARD_AS_COUNT_QUERY_SUBJECT)) {
         setUseWildcardAsCountQuerySubject(false);
      }
   }

   @Override
   @Transactional
   public boolean isNextExists()
   {
      return resultList!=null && getMaxResults()!=null &&
             resultList.size() > getMaxResults();
   }


   /**
    * Get the list of results this query returns
    * 
    * Any changed restriction values will be applied
    */
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
         javax.persistence.Query query = createQuery();
         resultList = query==null ? null : query.getResultList();
      }
   }
   
   /**
    * Get a single result from the query
    * 
    * Any changed restriction values will be applied
    * 
    * @throws NonUniqueResultException if there is more than one result
    */
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
      if ( singleResult==null)
      {
         javax.persistence.Query query = createQuery();
         singleResult = (E) (query==null ? 
               null : query.getSingleResult());
      }
   }

   /**
    * Get the number of results this query returns
    * 
    * Any changed restriction values will be applied
    */
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
      if ( resultCount==null )
      {
         javax.persistence.Query query = createCountQuery();
         resultCount = query==null ? 
               null : (Long) query.getSingleResult();
      }
   }

   /**
    * The refresh method will cause the result to be cleared.  The next access
    * to the result set will cause the query to be executed.
    * 
    * This method <b>does not</b> cause the ejbql or restrictions to reread.
    * If you want to update the ejbql or restrictions you must call 
    * {@link #setEjbql(String)} or {@link #setRestrictions(List)}
    */
   @Override
   public void refresh()
   {
      super.refresh();
      resultCount = null;
      resultList = null;
      singleResult = null;
   }
   
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
   
   protected javax.persistence.Query createQuery()
   {
      parseEjbql();
      
      evaluateAllParameters();
      
      joinTransaction();
      
      javax.persistence.Query query = getEntityManager().createQuery( getRenderedEjbql() );
      setParameters( query, getQueryParameterValues(), 0 );
      setParameters( query, getRestrictionParameterValues(), getQueryParameterValues().size() );
      if ( getFirstResult()!=null) query.setFirstResult( getFirstResult() );
      if ( getMaxResults()!=null) query.setMaxResults( getMaxResults()+1 ); //add one, so we can tell if there is another page
      if ( getHints()!=null )
      {
         for ( Map.Entry<String, String> me: getHints().entrySet() )
         {
            query.setHint(me.getKey(), me.getValue());
         }
      }
      return query;
   }
   
   protected javax.persistence.Query createCountQuery()
   {
      parseEjbql();

      evaluateAllParameters();

      joinTransaction();
      
      javax.persistence.Query query = getEntityManager().createQuery( getCountEjbql() );
      setParameters( query, getQueryParameterValues(), 0 );
      setParameters( query, getRestrictionParameterValues(), getQueryParameterValues().size() );
      return query;
   }

   private void setParameters(javax.persistence.Query query, List<Object> parameters, int start)
   {
      for (int i=0; i<parameters.size(); i++)
      {
         Object parameterValue = parameters.get(i);
         if ( isRestrictionParameterSet(parameterValue) )
         {
            query.setParameter( QueryParser.getParameterName(start + i), parameterValue );
         }
      }
   }

   public Map<String, String> getHints()
   {
      return hints;
   }

   public void setHints(Map<String, String> hints)
   {
      this.hints = hints;
   }
   
   protected void joinTransaction()
   {
      try
      {
         Transaction.instance().enlist( getEntityManager() );
      }
      catch (SystemException se)
      {
         throw new RuntimeException("could not join transaction", se);
      }
   }
   
}
