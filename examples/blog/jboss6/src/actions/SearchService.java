//$Id$
package actions;

import java.util.List;

import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import domain.BlogEntry;

/**
 * Pulls the search results
 *
 * @author Gavin King
 * @author Sanne Grinovero
 */
@Name("searchService")
public class SearchService 
{
   
   @In
   private FullTextEntityManager entityManager;
   
   private String searchPattern;
   
   @Factory("searchResults")
   public List<BlogEntry> getSearchResults()
   {
      if (searchPattern==null || "".equals(searchPattern) ) {
         searchPattern = null;
         return getLatestBlogEntries(100);
      }
      else
      {
         Query luceneQuery = getFullTextQuery();
         return entityManager.createFullTextQuery(luceneQuery, BlogEntry.class)
               .setMaxResults(100)
               .getResultList();
      }
   }

   private Query getFullTextQuery()
   {
      //Create a QueryBuilder targeting BlogEntry
      QueryBuilder queryBuilder = entityManager
         .getSearchFactory()
         .buildQueryBuilder()
         .forEntity(BlogEntry.class)
         .get();
      
      //A fulltext query using English Analyzer
      Query queryUsingEnglishStemmer = queryBuilder.keyword()
         .onFields("title:en").boostedTo(4f)
         .andField("body:en")
         .matching(searchPattern)
         .createQuery();
      
      //A fulltext query using ngrams
      Query queryUsingNGrams = queryBuilder.keyword()
         .onFields("title:ngrams").boostedTo(2f)
         .andField("body:ngrams").boostedTo(0.4f)
         .matching(searchPattern)
         .createQuery();
      
      //Combine them for best results:
      Query fullTextQuery = queryBuilder.bool()
         .should(queryUsingEnglishStemmer)
         .should(queryUsingNGrams)
         .createQuery();
      
      return fullTextQuery;
   }

   private List<BlogEntry> getLatestBlogEntries(int limit)
   {
      return entityManager
         .createQuery("select be from BlogEntry be order by date desc")
         .setHint("org.hibernate.cacheable", true)
         .setMaxResults(limit)
         .getResultList();
   }

   public String getSearchPattern()
   {
      return searchPattern;
   }

   public void setSearchPattern(String searchPattern)
   {
      this.searchPattern = searchPattern;
   }

}
