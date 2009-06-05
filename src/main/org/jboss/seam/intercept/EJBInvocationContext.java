package org.jboss.seam.intercept;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Wraps the invocation context coming from EJB3, and
 * adapts it to the Seam version of the API.
 * 
 * @author Gavin King
 *
 */
class EJBInvocationContext implements InvocationContext, javax.interceptor.InvocationContext
{
   private javax.interceptor.InvocationContext context;

   public EJBInvocationContext(javax.interceptor.InvocationContext context)
   {
      this.context = context;
   }

   public Map getContextData()
   {
      return context.getContextData();
   }

   public Method getMethod()
   {
      return context.getMethod();
   }

   public Object[] getParameters()
   {
      return context.getParameters();
   }

   public Object getTarget()
   {
      return context.getTarget();
   }

   public Object proceed() throws Exception
   {
      return context.proceed();
   }

   public void setParameters(Object[] params)
   {
      context.setParameters(params);
   }
}
