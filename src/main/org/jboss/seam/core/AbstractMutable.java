package org.jboss.seam.core;

/**
 * Base helper implementation of Mutable
 * 
 * @author Gavin King
 *
 */
public abstract class AbstractMutable implements Mutable
{
   private transient boolean dirty;

   public boolean clearDirty()
   {
      boolean result = dirty;
      dirty = false;
      return result;
   }
   
   /**
    * Set the dirty flag if the value has changed.
    * Call whenever a subclass attribute is updated.
    * 
    * @param oldValue the old value of an attribute
    * @param newValue the new value of an attribute
    */
   protected <T> void setDirty(T oldValue, T newValue)
   {
      dirty = dirty || (oldValue!=newValue && (
            oldValue==null || 
            !oldValue.equals(newValue) 
         ));
   }
   
   /**
    * Set the dirty flag.
    */
   protected void setDirty()
   {
      dirty = true;
   }

}
