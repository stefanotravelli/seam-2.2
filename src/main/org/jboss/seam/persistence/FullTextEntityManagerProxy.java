//$Id$
package org.jboss.seam.persistence;

import java.io.Serializable;

import org.apache.lucene.search.Query;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;

/**
 * Wrap a FullTextEntityManager
 * 
 * @author Emmanuel Bernard
 * @author Sanne Grinovero
 */
public class FullTextEntityManagerProxy extends EntityManagerProxy implements FullTextEntityManager
{
   
   private final FullTextEntityManager fullTextEntityManager;
   
   public FullTextEntityManagerProxy(FullTextEntityManager entityManager)
   {
      super(entityManager);
      this.fullTextEntityManager = entityManager;
   }
   
   public FullTextQuery createFullTextQuery(Query query, Class<?>... classes)
   {
      return fullTextEntityManager.createFullTextQuery(query, classes);
   }
   
   public void flushToIndexes()
   {
      fullTextEntityManager.flushToIndexes();
   }
   
   public <T> void index(T entity)
   {
      fullTextEntityManager.index(entity);
   }
   
   public SearchFactory getSearchFactory()
   {
      return fullTextEntityManager.getSearchFactory();
   }
   
   public <T> void purge(Class<T> aClass, Serializable id)
   {
      fullTextEntityManager.purge(aClass, id);
   }
   
   public <T> void purgeAll(Class<T> entityType)
   {
      fullTextEntityManager.purgeAll(entityType);
   }
}
