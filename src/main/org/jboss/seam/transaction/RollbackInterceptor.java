//$Id$
package org.jboss.seam.transaction;

import static org.jboss.seam.ComponentType.JAVA_BEAN;
import static org.jboss.seam.util.Work.isRollbackRequired;

import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.intercept.InvocationContext;

/**
 * Automatically sets the current transaction to rollback 
 * only when an exception is thrown.
 * 
 * @author Gavin King
 */
@Interceptor(stateless=true)
public class RollbackInterceptor extends AbstractInterceptor 
{
   private static final long serialVersionUID = 5551801508325093417L;
   
   @AroundInvoke
   public Object aroundInvoke(InvocationContext invocation) throws Exception 
   {
      try
      {
         return invocation.proceed();
      }
      catch (Exception e)
      {
         if ( isRollbackRequired(e, getComponent().getType() == JAVA_BEAN) )
         {
            try
            {
               Transaction.instance().setRollbackOnly();
            }
            catch (Exception te) {} //swallow
         }
         throw e;
      }
   }
   
   public boolean isInterceptorEnabled()
   {
      // Just here for consistency
      return true;
   }
   
}
