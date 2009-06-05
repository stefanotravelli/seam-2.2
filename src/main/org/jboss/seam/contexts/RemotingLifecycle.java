package org.jboss.seam.contexts;

import org.jboss.seam.ScopeType;

/**
 * Lifecycle management for Seam Remoting requests
 * 
 * @author Shane Bryzak
 */
public class RemotingLifecycle
{
   public static void restorePageContext()
   {
      Contexts.pageContext.set( new BasicContext(ScopeType.PAGE) );
   }
}
