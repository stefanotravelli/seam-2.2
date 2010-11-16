package org.jboss.seam.jmx;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.Vector;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletContext;

import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

/**
 * The purpose of this component is to detect a clustered environment and
 * to inform the HttpSessionListener whether the origin of a session destroyed
 * event is the failover of a session from one node to the next. If a node
 * is failing over, we don't want the SFSBs referenced by the session to be
 * destroyed.
 * 
 * @author Dan Allen
 */
@Name("org.jboss.seam.jmx.jbossClusterMonitor")
@BypassInterceptors
@Scope(APPLICATION)
@Startup
@Install(precedence=BUILT_IN, classDependencies="org.jgroups.MembershipListener")
public class JBossClusterMonitor
{
   private static Log log = Logging.getLog(JBossClusterMonitor.class);
   
   private MBeanServer jbossMBeanServer;
   
   private boolean clustered;
   
   private ObjectName clusteringCacheObjectName;
   
   private ObjectName serverObjectName;
   
   @Create
   public void create()
   {
      jbossMBeanServer = locateJBoss();
      
      if (!isJBoss())
      {
         return;
      }
      
      try
      {
         clusteringCacheObjectName = new ObjectName("jboss.cache:service=TomcatClusteringCache");
         serverObjectName = new ObjectName("jboss.system:type=Server");
      }
      catch (MalformedObjectNameException e)
      {
         log.warn("Invalid JMX name: " + e.getMessage());
      }
      
      try
      {
         jbossMBeanServer.getMBeanInfo(clusteringCacheObjectName);
         clustered = true;
         log.info("JBoss cluster detected");
      }
      catch (Exception e) {}
   }
   
   public boolean isClustered()
   {
      return clustered;
   }
   
   /**
    * Consults the jboss.system:type=Server MBean to determine if this instance
    * of JBoss AS is currently being shutdown. Note that the flag only returns
    * true if the shutdown() method on this MBean is used. It does not detect a
    * force halt via a process signal (i.e., CTRL-C).
    */
   public boolean nodeIsShuttingDown()
   {
      if (!isJBoss())
      {
         return false;
      }
      
      try
      {
         return (Boolean) jbossMBeanServer.getAttribute(serverObjectName, "InShutdown");
      }
      catch (Exception e)
      {
         return false;
      }
   }
   
   public boolean isLastNode()
   {
      if (!clustered)
      {
         return true;
      }
      
      // other options
      // object name => jboss.jgroups:cluster=DefaultPartition,type=channel
      // object name => jboss.jgroups:cluster=Tomcat-Cluster,type=channel
      // attribute => NumberOfTasksInTimer
      
      try
      {
         return ((Vector) jbossMBeanServer.getAttribute(clusteringCacheObjectName, "Members")).size() == 1;
      }
      catch (Exception e) {
         log.warn("Could not determine number of members in cluster", e);
         return true;
      }
   }
   
   public boolean failover()
   {
      return isClustered() && nodeIsShuttingDown() && !isLastNode();
   }
   
   public boolean isJBoss()
   {
      return jbossMBeanServer != null;
   }
   
   protected MBeanServer locateJBoss()
   {
      for (Object o: MBeanServerFactory.findMBeanServer(null)) {
         MBeanServer server = (MBeanServer) o;
         if ("jboss".equals(server.getDefaultDomain())) {
            return server;
         }
      }
      return null;
   }
   
   // FIXME my sense is that this could lookup could be more elegant or conforming
   public static JBossClusterMonitor getInstance(ServletContext ctx)
   {
      return (JBossClusterMonitor) ctx.getAttribute(Seam.getComponentName(JBossClusterMonitor.class));
   }
}
