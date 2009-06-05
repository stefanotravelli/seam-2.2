package org.jboss.seam.persistence;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.AbstractMutable;
import org.jboss.seam.core.Manager;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * Maintains the set of persistence contexts that have been touched in a
 * conversation. Also controls the flush mode used by the persistence contexts
 * during the render phase.
 * 
 * @author Gavin King
 */
@Name("org.jboss.seam.persistence.persistenceContexts")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
@Install(precedence=BUILT_IN)
public class PersistenceContexts extends AbstractMutable implements Serializable
{
   private static final long serialVersionUID = -4897350516435283182L;
   private static final LogProvider log = Logging.getLogProvider(PersistenceContexts.class);
   private Set<String> set = new HashSet<String>();
   private FlushModeType flushMode;
   // the real flush mode is a backup of the flush mode when doing a temporary switch (such as during render)
   private FlushModeType realFlushMode;
 
   @Create
   public void create()
   {
      FlushModeType defaultFlushMode = Manager.instance().getDefaultFlushMode(); 
      if (defaultFlushMode != null)
      {
         flushMode = defaultFlushMode;
      }
      else
      {
         flushMode = FlushModeType.AUTO;
      }
   }
   
   public FlushModeType getFlushMode()
   {
      return flushMode;
   }
   
   public Set<String> getTouchedContexts()
   {
      return Collections.unmodifiableSet(set);
   }
   
   public void touch(String context)
   {
      if ( set.add(context) ) setDirty();
   }
   
   public void untouch(String context)
   {
      if ( set.remove(context) ) setDirty();
   }
   
   public static PersistenceContexts instance()
   {
      if ( Contexts.isConversationContextActive() )
      {
         return (PersistenceContexts) Component.getInstance(PersistenceContexts.class);
      }
      else
      {
         return null;
      }
   }
   
   public void changeFlushMode(FlushModeType flushMode)
   {
      changeFlushMode(flushMode, false);   
   }

   public void changeFlushMode(FlushModeType flushMode, boolean temporary)
   {
      if (temporary) {
         realFlushMode = this.flushMode;
      }
      this.flushMode = flushMode;
      changeFlushModes();
   }

   /**
    * Restore the previous flush mode if the current flush mode is marked 
    * as temporary.
    */
   public void restoreFlushMode() {
      if (realFlushMode != null && realFlushMode != flushMode) {
         flushMode = realFlushMode;
         realFlushMode = null;
         changeFlushModes();
      }
   }

   private void changeFlushModes()
   {
      for (String name: set)
      {
         PersistenceContextManager pcm = (PersistenceContextManager) Contexts.getConversationContext().get(name);
         if (pcm!=null)
         {
            try
            {
               pcm.changeFlushMode(flushMode);
            }
            catch (UnsupportedOperationException uoe)
            {
               // we won't be nasty and throw and exception, but we'll log a warning to the developer
               log.warn(uoe.getMessage());
            }
         }
      }
   }
   
   public void beforeRender()
   {
      // some JPA providers may not support MANUAL flushing
      // defer the decision to the provider manager component
      PersistenceProvider.instance().setRenderFlushMode();
   }
   
   public void afterRender()
   {
      restoreFlushMode();
   }
   
}
