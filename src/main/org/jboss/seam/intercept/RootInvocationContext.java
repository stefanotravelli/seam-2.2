package org.jboss.seam.intercept;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.util.Reflections;

/**
 * InvocationContext for use with CGLIB-based interceptors.
 * 
 * @author Gavin King
 *
 */
class RootInvocationContext implements InvocationContext
{
   private final Object bean;
   private final Method method;
   private Object[] params;
   private final Map contextData = new HashMap();

   public RootInvocationContext(Object bean, Method method, Object[] params)
   {
      this.bean = bean;
      this.method = method;
      this.params = params;
   }
   
   public Object proceed() throws Exception
   {     
      method.setAccessible(true);
      return Reflections.invoke(method, bean, params);
   }

   public Object getTarget()
   {
      return bean;
   }

   public Map getContextData()
   {
      return contextData;
   }

   public Method getMethod()
   {
      return method;
   }

   public Object[] getParameters()
   {
      return params;
   }

   public void setParameters(Object[] newParams)
   {
      params = newParams;
   }
}