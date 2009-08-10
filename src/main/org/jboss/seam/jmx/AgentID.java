package org.jboss.seam.jmx;

import java.net.InetAddress;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.util.Random;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;

import org.jboss.mx.server.ServerConstants;

/**
 * Utility class for creating JMX agent identifiers. Also contains the
 * helper method for retrieving the <tt>AgentID</tt> of an existing MBean server
 * instance.
 *
 * @see javax.management.MBeanServerDelegateMBean
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81019 $
 *   
 */
public class AgentID 
   implements ServerConstants
{
   // Static ----------------------------------------------------
   private static SynchronizedLong id = new SynchronizedLong(0);

   private static final Random rand = new Random(System.currentTimeMillis());

   /**
    * Creates a new agent ID string. The identifier is of the form
    * <tt>&lt;ip.address&gt;/&lt;creation time in ms&gt;/&lt;VMID+(random int 0-100)&gt;/&lt;sequence #&gt;</tt>.<P>
    *
    * This AgentID string is globally unique.
    *
    * @return Agent ID string
    */
   public static String create()
   {
      String ipAddress = null;

      try
      {
         ipAddress = (String) AccessController.doPrivileged(
            new PrivilegedExceptionAction()
            {
               public Object run() throws Exception
               {
                  return InetAddress.getLocalHost().getHostAddress();
               }
            }
         );
      }
      catch(PrivilegedActionException e)
      {
         ipAddress = "127.0.0.1";
      }
      // use the VMID to create a more unique ID that can be used to guarantee that this
      // MBeanServerID is unique across multiple JVMs, even on the same host
      String vmid = new java.rmi.dgc.VMID().toString().replace(':','x').replace('-','X') + rand.nextInt(100);

      return ipAddress + "/" + System.currentTimeMillis() + "/" + vmid + "/"+ (id.increment());
   }
    /**
     * test
     *
     * @param args
     */
   public static void main (String args[])
   {
       for (int c=0;c<10;c++)
        System.out.println(AgentID.create());
   }

   /**
    * Returns the agent identifier string of a given MBean server instance.
    *
    * @return <tt>MBeanServerId</tt> attribute of the MBean server delegate.
    */
   public static String get(MBeanServer server)
   {
      try 
      {
         ObjectName name = new ObjectName(MBEAN_SERVER_DELEGATE);
         String agentID = (String)server.getAttribute(name, "MBeanServerId");   
      
         return agentID;
      }
      catch (Throwable t)
      {
         throw new Error("Cannot find the MBean server delegate: " + t.toString());
      }
   }
}
      



