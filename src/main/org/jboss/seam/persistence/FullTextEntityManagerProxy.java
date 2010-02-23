//$Id$
package org.jboss.seam.persistence;

import org.hibernate.search.jpa.FullTextEntityManager;

/**
 * Marker Interface here to show that a given EntityManager is doing EL
 * manipulation and for backwards compatibility with previous non proxy
 * solution.
 * 
 * @author Emmanuel Bernard
 * @author Sanne Grinovero
 * @author Mike Youngstrom
 */
public interface FullTextEntityManagerProxy extends EntityManagerProxy, FullTextEntityManager
{
}
