package org.jboss.seam.jms;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.jms.Message;

/**
 * <p>
 * An interceptor which is intented to be applied to message-driven beans to
 * setup a Seam request life cycle within the boundaries of the bean's method
 * calls so Seam components can be instantiated and invoked.
 * </p>
 * 
 * <p>
 * TODO It would be nice to bake in an exception callback like the async
 * integration supports
 * </P>
 * 
 * @author Dan Allen
 */
public class ContextualMessageHandlerRequestInterceptor
{
   @AroundInvoke
   public Object aroundInvoke(final InvocationContext invocation) throws Exception
   {
      String methodName = invocation.getMethod().getName();
      Object[] args = invocation.getParameters();
      if (!"onMessage".equals(methodName) || args.length != 1 || !(args[0] instanceof Message))
      {
         return invocation.proceed();
      }
      
      ContextualMessageHandlerRequest contextualRequest = new ContextualMessageHandlerRequest((Message) invocation.getParameters()[0])
      {
         @Override
         public void process() throws Exception
         {
            setResult(invocation.proceed());
         }
      };
      contextualRequest.run();
      return contextualRequest.getResult();
   }
}
