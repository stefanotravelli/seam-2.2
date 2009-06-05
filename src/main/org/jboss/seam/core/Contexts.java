/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.core;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Context;

/**
 * Provides access to the current contexts associated with the thread.
 * 
 * @author Gavin King
 */
@Name("org.jboss.seam.core.contexts")
@BypassInterceptors
@Install(precedence=BUILT_IN)
@Scope(ScopeType.STATELESS)
public class Contexts 
{

   @Factory(value="org.jboss.seam.core.eventContext", autoCreate=true)
   public Context getEventContext() 
   {
      return org.jboss.seam.contexts.Contexts.getEventContext();
   }

   @Factory(value="org.jboss.seam.core.methodContext", autoCreate=true)
   public Context getMethodContext() 
   {
      return org.jboss.seam.contexts.Contexts.getMethodContext();
   }

   @Factory(value="org.jboss.seam.core.pageContext", autoCreate=true)
   public Context getPageContext() 
   {
      return org.jboss.seam.contexts.Contexts.getPageContext();
   }

   @Factory(value="org.jboss.seam.core.sessionContext", autoCreate=true)
   public Context getSessionContext() 
   {
      return org.jboss.seam.contexts.Contexts.getSessionContext();
   }

   @Factory(value="org.jboss.seam.core.applicationContext", autoCreate=true)
   public Context getApplicationContext() 
   {
      return org.jboss.seam.contexts.Contexts.getApplicationContext();
   }

   @Factory(value="org.jboss.seam.core.conversationContext", autoCreate=true)
   public Context getConversationContext() 
   {
      return org.jboss.seam.contexts.Contexts.getConversationContext();
   }

   @Factory(value="org.jboss.seam.core.businessProcessContext", autoCreate=true)
   public Context getBusinessProcessContext() 
   {
      return org.jboss.seam.contexts.Contexts.getBusinessProcessContext();
   }

}
