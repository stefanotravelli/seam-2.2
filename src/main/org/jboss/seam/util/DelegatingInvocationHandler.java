package org.jboss.seam.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

/**
 * An InvocationHandler implementation that delegates method invocations to a specified object,
 * optionally allowing the method to be overridden locally. 
 * 
 * @author Shane Bryzak
 */
public class DelegatingInvocationHandler<T> implements InvocationHandler
{   
   private static Log log = Logging.getLog(DelegatingInvocationHandler.class);     
   
   private class MethodTarget
   {
      public Method method;
      public Object target;
      
      public MethodTarget(Object target, Method method)
      {
         this.target = target;
         this.method = method;
      }
   }   
   
   private Map<Method,MethodTarget> methodCache = new HashMap<Method,MethodTarget>();
   private T delegate;
   
   public DelegatingInvocationHandler(T delegate)
   {
      this.delegate = delegate;
   }
   
   public T getDelegate()
   {
      return delegate;
   }
   
   public Object invoke(Object proxy, Method method, Object[] args)
         throws Throwable
   {  
      MethodTarget target = methodCache.get(method);      
      
      if (target == null && !methodCache.containsKey(method))
      {
         synchronized(methodCache)
         {
            if (!methodCache.containsKey(method))
            {
               try
               {
                  target = new MethodTarget(this, getClass().getMethod(method.getName(), method.getParameterTypes()));                  
               }
               catch (NoSuchMethodException ex)
               {
                  // Swallow this, we'll try to find a matching method on the delegate
               }
               
               if (target == null)
               {
                  try
                  {
                     target = new MethodTarget(delegate, delegate.getClass().getMethod(method.getName(), method.getParameterTypes()));
                  }
                  catch (NoSuchMethodException ex)
                  {
                     // Swallow this, put a null entry in methodCache
                  }
               }
               
               methodCache.put(method, target);
            }
            else
            {
               target = methodCache.get(method);               
            }
         }                  
      }
      
      if (target == null)
      {
         throw new IllegalStateException("Proxied session does not implement method " + method.getName() +
               " with args [" + (args == null ? null : Arrays.asList(args)) + "]");
      }
      
      if (log.isTraceEnabled())
      {
         log.trace("Delegating method " + method.getName() + " with args " + 
                 (args == null ? null : Arrays.asList(args)));
      }
      
      return target.method.invoke(target.target, args);      
   }  
}
