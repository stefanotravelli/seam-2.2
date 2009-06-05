//$Id$
package org.jboss.seam.core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.annotations.intercept.InterceptorType;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.intercept.InvocationContext;

/**
 * Serializes calls to a component.
 * 
 * @author Gavin King
 */
@Interceptor(type=InterceptorType.CLIENT)
public class SynchronizationInterceptor extends AbstractInterceptor
{
   private static final long serialVersionUID = -4173880108889358566L;
   
   private ReentrantLock lock = new ReentrantLock(true);
   
   @AroundInvoke
   public Object aroundInvoke(InvocationContext invocation) throws Exception
   {
      if ( lock.tryLock( getComponent().getTimeout(), TimeUnit.MILLISECONDS ) )
      {
         try
         {
            return invocation.proceed();
         }
         finally
         {
            lock.unlock();
         }
      }
      else
      {
         throw new LockTimeoutException("could not acquire lock on @Synchronized component: " + 
               getComponent().getName());
      }
   }
   
   public boolean isInterceptorEnabled()
   {
      return getComponent().isSynchronize();
   }

}
