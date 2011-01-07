package com.jboss.dvd.seam;

import java.util.Date;

import javax.ejb.Remove;
import javax.ejb.Stateful;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

/**
 * At startup we need to reindex all entities from the database.
 * This is an optional step in a real application, we need it in
 * the demo as the database is volatile and was just filled
 * with a SQL dump (see import.sql).
 *  
 * @author Sanne Grinovero
 */
@Name("indexer")
@Stateful
@Scope(ScopeType.APPLICATION)
@Startup
public class IndexerAction implements Indexer
{
   
   private Date lastIndexingTime;
   
   @In
   private FullTextEntityManager entityManager;
   
   public Date getLastIndexingTime()
   {
      return lastIndexingTime;
   }
   
   @Create
   public void index()
   {
      // Re-build the index for the whole database:
      entityManager
         .createIndexer()
         .start();
      lastIndexingTime = new Date();
   }
   
   @Remove
   @Destroy
   public void stop() {}
   
}
