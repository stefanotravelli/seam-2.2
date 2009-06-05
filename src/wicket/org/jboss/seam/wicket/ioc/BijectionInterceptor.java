package org.jboss.seam.wicket.ioc;



public class BijectionInterceptor<T> implements StatelessInterceptor<T>
{

   public Object afterInvoke(InvocationContext<T> invocationContext, Object result)
   {
      invocationContext.getComponent().outject(invocationContext.getBean());
      invocationContext.getComponent().disinject(invocationContext.getBean());
      return result;
   }

   public void beforeInvoke(InvocationContext<T> invocationContext)
   {
      try
      {
         invocationContext.getComponent().inject(invocationContext.getBean());
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public Exception handleException(InvocationContext<T> invocationContext, Exception exception)
   {
      return exception;
   }

}
