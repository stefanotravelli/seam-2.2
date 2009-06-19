package org.jboss.seam.drools;

import java.io.Serializable;

import org.drools.RuleBase;
import org.drools.StatefulSession;
import org.drools.spi.GlobalResolver;
import org.drools.event.AgendaEventListener;
import org.drools.event.RuleFlowEventListener;
import org.drools.event.WorkingMemoryEventListener;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Mutable;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * A conversation-scoped Drools WorkingMemory for a named RuleBase
 * 
 * @author Gavin King
 * @author Tihomir Surdilovic
 *
 */
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class ManagedWorkingMemory implements Mutable, Serializable
{
   private static final long serialVersionUID = -1746942080571374743L;
   
   private static final LogProvider log = Logging.getLogProvider(ManagedWorkingMemory.class);
   
   private String ruleBaseName;
   private String[] eventListeners;
   private StatefulSession statefulSession;
   private ValueExpression<RuleBase> ruleBase;
   
   public boolean clearDirty()
   {
      return true;
   }
   
   /**
    * The name of a Seam context variable holding an
    * instance of org.drools.RuleBase
    * 
    * @return a context variable name
    * @deprecated
    */
   public String getRuleBaseName()
   {
      return ruleBaseName;
   }
   
   /**
    * The name of a Seam context variable holding an
    * instance of org.drools.RuleBase
    * 
    * @param ruleBaseName a context variable name
    * @deprecated
    */
   public void setRuleBaseName(String ruleBaseName)
   {
      this.ruleBaseName = ruleBaseName;
   }
   
   @Unwrap
   public StatefulSession getStatefulSession()
   {
      if (statefulSession==null)
      {
         statefulSession = getRuleBaseFromValueBinding().newStatefulSession();
         statefulSession.setGlobalResolver( createGlobalResolver( statefulSession.getGlobalResolver() ) );
         if(eventListeners != null) {
            setEventListeners(statefulSession);
         }
      }
      return statefulSession;
   }
   
   private void setEventListeners(StatefulSession statefulSession) 
   {
      if(eventListeners != null) {
         for(String eventListener : eventListeners) 
         {
            log.debug("adding eventListener: " + eventListener);
            try
            {
               Class eventListenerClass = Class.forName(eventListener);
               Object eventListenerObject = eventListenerClass.newInstance();
               if(eventListenerObject instanceof WorkingMemoryEventListener) 
               {
                  statefulSession.addEventListener((WorkingMemoryEventListener) eventListenerObject);
               } 
               else if(eventListenerObject instanceof AgendaEventListener) 
               {
                  statefulSession.addEventListener((AgendaEventListener) eventListenerObject);
               } 
               else if(eventListenerObject instanceof RuleFlowEventListener) 
               {
                  statefulSession.addEventListener((RuleFlowEventListener) eventListenerObject);
               } 
               else 
               {
                  log.debug("event Listener " + eventListener + " is not of valid type - bypassing.");
               }
            }
            catch (Exception e)
            {
               log.error("error adding event listener " + eventListener + " - bypassing.");
            }
         }
      }
   }

   protected RuleBase getRuleBaseFromValueBinding()
   {
      RuleBase ruleBase;
      if (this.ruleBase!=null)
      {
         ruleBase = this.ruleBase.getValue();
      }
      else if (ruleBaseName!=null)
      {
         //deprecated stuff
         ruleBase = (RuleBase) Component.getInstance(ruleBaseName, true);
      }
      else
      {
         throw new IllegalStateException("No RuleBase");
      }
             
      if (ruleBase==null)
      {
         throw new IllegalStateException("RuleBase not found: " + ruleBaseName);
      }
      return ruleBase;
   }

   protected GlobalResolver createGlobalResolver(GlobalResolver delegate)
   {
      return new SeamGlobalResolver(delegate);
   }
   
   @Destroy
   public void destroy()
   {
      statefulSession.dispose();
   }
   
   public ValueExpression<RuleBase> getRuleBase()
   {
      return ruleBase;
   }
   
   public void setRuleBase(ValueExpression<RuleBase> ruleBase)
   {
      this.ruleBase = ruleBase;
   }
   
   public String[] getEventListeners()
   {
      return eventListeners;
   }

   public void setEventListeners(String[] eventListeners)
   {
      this.eventListeners = eventListeners;
   }
}
