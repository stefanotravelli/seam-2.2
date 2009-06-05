package org.jboss.seam.drools;

import org.drools.spi.GlobalResolver;
import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Init;

/**
 * Resolves Seam context variables as Drools globals
 * 
 * @author Gavin King
 *
 */
public class SeamGlobalResolver implements GlobalResolver
{
   private GlobalResolver delegate;
   
   public SeamGlobalResolver(GlobalResolver delegate)
   {
      this.delegate = delegate;
   }

   public void setGlobal(String name, Object value)
   {
      //TODO: is this the right thing to do??
      //or: Contexts.getConversationContext().set(name, value);
      delegate.setGlobal(name, value);
   }
   
   public Object resolveGlobal(String name)
   {
      if ( !Contexts.isApplicationContextActive() )
      {
         return delegate.resolveGlobal(name);
      }
      else
      {
         Object instance = Component.getInstance(name);
         if (instance==null)
         {
            instance = delegate.resolveGlobal(name);
            return instance==null ?
                  Init.instance().getRootNamespace().getChild(name) :
                  instance;
         }
         else
         {
            return instance;
         }
      }
   }
}