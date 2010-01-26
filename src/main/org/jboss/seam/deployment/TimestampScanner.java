package org.jboss.seam.deployment;

import javax.servlet.ServletContext;

/**
 * A no-op version of the URLScanner that merely returns whether the deployment
 * handler would in fact handle this file. It does not process the file
 * in any way. This allows us to use this scanner for timestamp checking.
 * 
 * @author Dan Allen
 */
public abstract class TimestampScanner extends ForwardingAbstractScanner
{

   @Override
   protected boolean handle(String name)
   {
      for (DeploymentHandler handler : getDeploymentStrategy().getDeploymentHandlers().values())
      {
         if (handler instanceof ClassDeploymentHandler)
         {
            if (name.endsWith(".class"))
            {
               return true;
            }
         }
         else
         {
            if (name.endsWith(handler.getMetadata().getFileNameSuffix()))
            {
               return true;
            }
         }
      }
      return false;
   }
   
   @Deprecated
   public TimestampScanner()
   {
      
   }
   
   
   public TimestampScanner(ServletContext servletContext)
   {
      super(servletContext);
   }
  
}
