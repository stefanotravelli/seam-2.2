package org.jboss.seam.jmx;

import javax.management.JMException;

public class MBeanProxyCreationException
   extends JMException
{
   private static final long serialVersionUID = 1008637966352433381L;

   // Constructors --------------------------------------------------
   public MBeanProxyCreationException() 
   {
      super();
   }
   
   public MBeanProxyCreationException(String msg)
   {
      super(msg);
   }
}
      



