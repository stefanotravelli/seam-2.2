package org.jboss.seam.async;

import static org.jboss.seam.ComponentType.JAVA_BEAN;

import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.annotations.intercept.InterceptorType;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.intercept.InvocationContext;

/**
 * Dispatches method calls to @Asynchronous methods
 * asynchronously, and returns the "timer" object
 * if necessary.
 * 
 * @author Gavin King
 *
 */
@Interceptor(stateless=true, type=InterceptorType.CLIENT)
public class AsynchronousInterceptor extends AbstractInterceptor
{
   private static final long serialVersionUID = 9194177339867853303L;
   
   private static final String REENTRANT = "org.jboss.seam.async.AsynchronousIntercepter.REENTRANT";
   
   @AroundInvoke
   public Object aroundInvoke(InvocationContext invocation) throws Exception
   {
      boolean scheduleAsync = invocation.getMethod().isAnnotationPresent(Asynchronous.class) && 
            (!isExecutingAsynchronousCall() || Contexts.getEventContext().isSet(REENTRANT));
      if (scheduleAsync)
      {
         Dispatcher dispatcher = AbstractDispatcher.instance();
         if (dispatcher==null)
         {
            throw new IllegalStateException("org.jboss.seam.async.dispatcher is not installed in components.xml");
         }
         Object timer = dispatcher.scheduleInvocation( invocation, getComponent() );
         //if the method returns a Timer, return it to the client
         return timer!=null && invocation.getMethod().getReturnType().isAssignableFrom( timer.getClass() ) ? timer : null;
      } else {
          
            boolean setFlag = false;
            if (isExecutingAsynchronousCall()) {
                Contexts.getEventContext().set(REENTRANT, true);
                setFlag = true;
            }
            
            try {
                return invocation.proceed();
            } finally {
                if (setFlag) {
                    Contexts.getEventContext().remove(REENTRANT);
                }
            }
        }
   }
   
   private boolean isExecutingAsynchronousCall()
   {
       return Contexts.getEventContext().isSet(AbstractDispatcher.EXECUTING_ASYNCHRONOUS_CALL);
   }
   
   public boolean isInterceptorEnabled()
   {
      return ( getComponent().getType().isEjb() && getComponent().businessInterfaceHasAnnotation(Asynchronous.class) ) ||
      ( getComponent().getType() == JAVA_BEAN && getComponent().beanClassHasAnnotation(Asynchronous.class) );
   }
}
