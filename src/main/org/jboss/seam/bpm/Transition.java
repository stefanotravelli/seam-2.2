package org.jboss.seam.bpm;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.AbstractMutable;

/**
 * Allows the application to set the jBPM transition to be used when
 * an <tt>@EndTask</tt> method is encountered.
 * 
 * @see org.jboss.seam.annotations.bpm.EndTask
 * 
 * @author Gavin King
 */
@Name("org.jboss.seam.bpm.transition")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
@Install(precedence=BUILT_IN, dependencies="org.jboss.seam.bpm.jbpm")
public class Transition extends AbstractMutable implements Serializable 
{
   private static final long serialVersionUID = -3054558654376670239L;
   
   private String name;
   
   public String getName() 
   {
      return name;
   }
   
   /**
    * Set the jBPM transition name
    */
   public void setName(String name) 
   {
      setDirty(this.name, name);
      this.name = name;
   }
   
   public static Transition instance()
   {
      if ( !Contexts.isApplicationContextActive() )
      {
         throw new IllegalStateException("No active application context");
      }
      return (Transition) Component.getInstance(Transition.class, ScopeType.CONVERSATION);
   }
   
   @Override
   public String toString()
   {
      return "Transition(" + name + ")";
   }
   
}
