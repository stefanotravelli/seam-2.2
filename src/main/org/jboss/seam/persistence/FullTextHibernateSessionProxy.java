package org.jboss.seam.persistence;

import org.hibernate.search.FullTextSession;

/**
 * Marker Interface here to show that a given EntityManager is doing EL
 * manipulation and for backwards compatibility with previous non proxy
 * solution.
 * 
 * @author Gavin King
 * @author Sanne Grinovero
 * @author Mike Youngstrom
 */
public interface FullTextHibernateSessionProxy extends HibernateSessionProxy, FullTextSession
{
}
