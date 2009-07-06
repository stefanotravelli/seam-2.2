//$Id$
package actions;

import java.util.List;

import javax.ejb.Remove;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Transactional;
import domain.BlogEntry;

/**
 * Index Blog entry at startup
 *
 * @author Emmanuel Bernard
 */
@Name("indexerService")
@Scope(ScopeType.APPLICATION)
@Startup
public class IndexerService
{
   @In
   private FullTextEntityManager entityManager;

   @Create
   @Transactional
   public void index() {
      entityManager.purgeAll( BlogEntry.class );
      List blogEntries = entityManager.createQuery("select be from BlogEntry be").getResultList();
      for (Object be : blogEntries) {
         entityManager.index(be);
      }
   }

   @Remove
   @Destroy
   public void stop() {}
}
