package org.jboss.seam.persistence;

import org.jboss.seam.annotations.FlushModeType;

/**
 * Support for changing flushmodes for an existing
 * persistence context.
 * 
 * @author Gavin King
 *
 */
public interface PersistenceContextManager
{
   public void changeFlushMode(FlushModeType flushMode);
}
