package org.jboss.seam.jmx;

import javax.management.JMRuntimeException;

/**
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81019 $  
 */
public class RuntimeProxyException
   extends JMRuntimeException
{
   private static final long serialVersionUID = -1166909485463779459L;

   // Constructors --------------------------------------------------
   public RuntimeProxyException() 
   {
      super();
   }
   
   public RuntimeProxyException(String msg)
   {
      super(msg);
   }
}
      



