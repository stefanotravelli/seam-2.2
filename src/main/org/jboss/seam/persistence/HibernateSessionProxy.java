package org.jboss.seam.persistence;

import org.hibernate.Session;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.event.EventSource;

/**
 * Marker interface that signifies a proxy is using the
 * HibernateSessionInvocationHandler. Also here for backwards compatibility with
 * previous HibernateSessionProxy.
 * 
 * @author Gavin King
 * @author Emmanuel Bernard
 * @author Mike Youngstrom
 * 
 */
public interface HibernateSessionProxy extends Session, SessionImplementor, EventSource
{
}
