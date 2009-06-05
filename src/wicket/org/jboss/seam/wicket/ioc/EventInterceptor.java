package org.jboss.seam.wicket.ioc;

import org.jboss.seam.annotations.RaiseEvent;
import org.jboss.seam.core.Events;

public class EventInterceptor<T> implements StatelessInterceptor<T>
{

   public Object afterInvoke(InvocationContext<T> invocationContext, Object result)
   {
      if ( result!=null || invocationContext.getConstructor() != null || (invocationContext.getMethod() != null && invocationContext.getMethod().getReturnType().equals(void.class)) )
      {
         if ( invocationContext.getAccessibleObject().isAnnotationPresent(RaiseEvent.class) )
         {
            String[] types = invocationContext.getAccessibleObject().getAnnotation(RaiseEvent.class).value();
            if ( types.length==0 )
            {
               Events.instance().raiseEvent( invocationContext.getMember().getName() );
            }
            else
            {
               for (String type: types )
               {
                  Events.instance().raiseEvent(type);
               }
            }
         }
      }
      return result;
   }

   public void beforeInvoke(InvocationContext<T> invocationContext)
   {
      
   }

   public Exception handleException(InvocationContext<T> invocationContext, Exception exception)
   {
      return exception;
   }
}