package org.jboss.seam.deployment;

/**
 * An accelerated version of the underlying strategy that uses the SimpleURLScanner
 * to determine the timestamp of the latest file.
 * 
 * @author Dan Allen
 */
public abstract class TimestampCheckForwardingDeploymentStrategy extends ForwardingDeploymentStrategy
{
   private Scanner scanner;

   public boolean changedSince(long mark)
   {
      scan();
      return getTimestamp() > mark;
   }

   @Override
   protected void initScanner()
   {
      if (getScanner() instanceof AbstractScanner)
      {
         final AbstractScanner delegate = (AbstractScanner) getScanner();
         this.scanner = new TimestampScanner(getServletContext())
         {

            @Override
            protected AbstractScanner delegate()
            {
               return delegate;
            }
            
         };
      }
      
   }
   
   @Override
   protected void postScan()
   {
      // No-op
   }

}