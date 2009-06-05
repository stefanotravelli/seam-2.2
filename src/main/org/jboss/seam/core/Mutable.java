package org.jboss.seam.core;

import java.io.Serializable;

/**
 * Must be implemented by any SESSION or CONVERSATION scoped
 * mutable JavaBean component that will be used in a clustered
 * environment. If this interface is not implemented correctly,
 * changes may not be replicated across the cluster (depending
 * upon the servlet engine implementation).
 * 
 * @author Gavin King
 *
 */
public interface Mutable extends Serializable
{
   /**
    * Get and clear the dirty flag.
    * 
    * @return true if the instance is dirty and requires replication
    */
   public boolean clearDirty();
}
