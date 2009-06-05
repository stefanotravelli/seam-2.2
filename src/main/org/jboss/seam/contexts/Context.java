/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.contexts;

import org.jboss.seam.ScopeType;

/**
 * API for accessing named components and named values that
 * are currently associated with a particular seam scope.
 * 
 * @author Gavin King
 */
public interface Context 
{
   /**
    * Get a value by name.
    */
   public Object get(String name);
   /**
    * Get a component instance, by its component name,
    * as determined by the @Name annotation value.
    */
   public Object get(Class clazz);
   /**
    * Set a value.
    */
   public void set(String name, Object value);
   /**
    * Unset a value.
    */
   public void remove(String name);
   /**
    * Is the value set?
    */
   public boolean isSet(String name);
   /**
    * Get all names defined in the context. 
    */
   public String[] getNames();
   /**
    * Force synchronization to the underlying state store.
    *
    * Some implementations of Context "persist" state back to
    * the underlying store synchronously, others asynchronously.
    * This method is usually called by Seam, when necessary.
    */
   public void flush();
   /**
    * Get the scope that this context object is associated with,
    */
   public ScopeType getType();
}
