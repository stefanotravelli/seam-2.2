/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.jboss.seam.Seam;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.init.Initialization;
import org.jboss.seam.jmx.JBossClusterMonitor;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * Drives certain Seam functionality such as initialization and cleanup
 * of application and session contexts from the web application lifecycle.
 * 
 * @author Gavin King
 */
public class SeamListener implements ServletContextListener, HttpSessionListener
{
   private static final LogProvider log = Logging.getLogProvider(ServletContextListener.class);
   
   public void contextInitialized(ServletContextEvent event) 
   {
      log.info( "Welcome to Seam " + Seam.getVersion() );
      event.getServletContext().setAttribute( Seam.VERSION, Seam.getVersion() );
      ServletLifecycle.beginApplication( event.getServletContext() );
      new Initialization( event.getServletContext() ).create().init();
   }
   
   public void contextDestroyed(ServletContextEvent event) 
   {
      ServletLifecycle.endApplication(event.getServletContext());
   }
   
   public void sessionCreated(HttpSessionEvent event) 
   {
      ServletLifecycle.beginSession( event.getSession() );
   }
   
   public void sessionDestroyed(HttpSessionEvent event) 
   {
      JBossClusterMonitor monitor = JBossClusterMonitor.getInstance(event.getSession().getServletContext());
      if (monitor != null && monitor.failover())
      {
         // If application is unfarmed or all nodes shutdown simultaneously, cluster cache may still fail to retrieve SFSBs to destroy
         log.debug("Detected fail-over, not destroying session context");
      }
      else
      {
         ServletLifecycle.endSession( event.getSession() );
      }
   }
   
}
