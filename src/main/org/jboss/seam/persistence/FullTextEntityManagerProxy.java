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
 */
public class FullTextEntityManagerProxy extends EntityManagerProxy implements FullTextEntityManager
{
   private FullTextEntityManager fullTextEntityManager;

   public FullTextEntityManagerProxy(FullTextEntityManager entityManager)
   {
      super(entityManager);
      this.fullTextEntityManager = entityManager;
   }

   public FullTextQuery createFullTextQuery(Query query, Class... classes)
   {
      return fullTextEntityManager.createFullTextQuery(query, classes);
   }

   public void index(Object object)
   {
      fullTextEntityManager.index(object);
   }

   public SearchFactory getSearchFactory()
   {
      return fullTextEntityManager.getSearchFactory();
   }

   public void purge(Class aClass, Serializable serializable)
   {
      fullTextEntityManager.purge(aClass, serializable);
   }

   public void purgeAll(Class aClass)
   {
      fullTextEntityManager.purgeAll(aClass);
   }
}
