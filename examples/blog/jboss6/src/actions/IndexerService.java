//$Id$
package actions;

import javax.ejb.Remove;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

/**
 * Index Blog entry at startup
 *
 * @author Emmanuel Bernard
 * @author Sanne Grinovero
 */
@Name("indexerService")
@Scope(ScopeType.APPLICATION)
@Startup
public class IndexerService
{
   @In
   private FullTextEntityManager entityManager;

   @Create
   public void index() {
      entityManager.createIndexer().start();
   }

   @Remove
   @Destroy
   public void stop() {}
}
