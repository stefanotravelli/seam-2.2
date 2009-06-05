package org.jboss.seam.core;

import java.lang.reflect.Method;

import org.jboss.seam.annotations.RaiseEvent;
import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.bpm.BusinessProcessInterceptor;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.transaction.RollbackInterceptor;
import org.jboss.seam.transaction.TransactionInterceptor;

/**
 * Raises Seam events connected with a bean lifecycle.
 * 
 * @author Gavin King
 *
 */
@Interceptor(stateless=true,
             around={BijectionInterceptor.class, ConversationInterceptor.class, 
                     TransactionInterceptor.class, BusinessProcessInterceptor.class, 
                     RollbackInterceptor.class})
public class EventInterceptor extends AbstractInterceptor
{
   private static final long serialVersionUID = -136300200838134612L;
   
   /*@PostConstruct
   public void postConstruct(InvocationContext ctx)
   {
      Events.instance().raiseEvent("org.jboss.seam.postConstruct." + component.getName());
   }
   @PreDestroy
   public void preDestroy(InvocationContext ctx)
   {
      Events.instance().raiseEvent("org.jboss.seam.preDestroy." + component.getName());
   }
   @PrePassivate
   public void prePassivate(InvocationContext ctx)
   {
      Events.instance().raiseEvent("org.jboss.seam.prePassivate." + component.getName());
   }
   
   @PostActivate
   public void postActivate(InvocationContext ctx)
   {
      Events.instance().raiseEvent("org.jboss.seam.postActivate." + component.getName());
   }*/
   @AroundInvoke
   public Object aroundInvoke(InvocationContext ctx) throws Exception
   {
      Object result = ctx.proceed();
      Method method = ctx.getMethod();
      if ( result!=null || method.getReturnType().equals(void.class) )
      {
         if ( method.isAnnotationPresent(RaiseEvent.class) )
         {
            String[] types = method.getAnnotation(RaiseEvent.class).value();
            if ( types.length==0 )
            {
               Events.instance().raiseEvent( method.getName() );
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
   
   public boolean isInterceptorEnabled()
   {
      return getComponent().beanClassHasAnnotation(RaiseEvent.class); 
   }
   
}
