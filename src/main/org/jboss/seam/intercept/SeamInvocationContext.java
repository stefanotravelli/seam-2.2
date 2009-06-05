//$Id$
package org.jboss.seam.intercept;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;



/**
 * Adapts from EJB interception to Seam component interceptors
 * 
 * @author Gavin King
 */
class SeamInvocationContext implements InvocationContext
{
   
   private final EventType eventType;
   private final InvocationContext context;
   private final List<Interceptor> interceptors;
   private final List<Object> userInterceptors;
   int location = 0;

   public SeamInvocationContext(InvocationContext context, EventType type, List<Object> userInterceptors, List<Interceptor> interceptors)
   {
      this.context = context;
      this.interceptors = interceptors;
      this.userInterceptors = userInterceptors;
      this.eventType = type;
   }
   
   public Object getTarget()
   {
      return context.getTarget();
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

   public Object proceed() throws Exception
   {
      if ( location==interceptors.size() )
      {
         return context.proceed();
      }
      else
      {
         Object userInterceptor = userInterceptors.get(location);
         Interceptor interceptor = interceptors.get(location);
         location++;
         switch (eventType)
         {
            case AROUND_INVOKE:
               if ( interceptor.isOptimized() )
               {
                  return ( (OptimizedInterceptor) userInterceptor ).aroundInvoke(this);
               }
               else
               {
                  return interceptor.aroundInvoke(this, userInterceptor);
               }
            case POST_CONSTRUCT: return interceptor.postConstruct(this, userInterceptor);
            case PRE_DESTORY: return interceptor.preDestroy(this, userInterceptor);
            case PRE_PASSIVATE: return interceptor.prePassivate(this, userInterceptor);
            case POST_ACTIVATE: return interceptor.postActivate(this, userInterceptor);
            default: throw new IllegalArgumentException("no InvocationType");
         }
      }
   }

   public void setParameters(Object[] params)
   {
      context.setParameters(params);
   }

}
