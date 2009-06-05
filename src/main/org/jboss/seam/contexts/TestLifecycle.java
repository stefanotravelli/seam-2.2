/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.contexts;

import java.util.Map;

import javax.servlet.ServletContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.servlet.ServletApplicationMap;

/**
 * Methods for setup and teardown of Seam contexts at the
 * beginning and end of a test.
 * 
 * @author Gavin King
 */
public class TestLifecycle
{

   private static final LogProvider log = Logging.getLogProvider(TestLifecycle.class);

   public static void beginTest(ServletContext context, Map<String, Object> session)
   {
      log.debug( ">>> Begin test" );
      Contexts.applicationContext.set( new ApplicationContext( new ServletApplicationMap(context) ) );
      Contexts.eventContext.set( new BasicContext(ScopeType.EVENT) );
      Contexts.conversationContext.set( new BasicContext(ScopeType.CONVERSATION) );
      Contexts.businessProcessContext.set( new BusinessProcessContext() );
      Contexts.sessionContext.set( new SessionContext(session) );
   }

   public static void endTest()
   {
      Lifecycle.clearThreadlocals();
      log.debug( "<<< End test" );
   }

}
