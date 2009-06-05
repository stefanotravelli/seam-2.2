//$Id$
package org.jboss.seam.core;

import java.lang.reflect.Method;

import org.jboss.seam.NoConversationException;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Conversational;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.bpm.BeginTask;
import org.jboss.seam.annotations.bpm.StartTask;
import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.bpm.BusinessProcessInterceptor;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.intercept.InvocationContext;

/**
 * Check that a conversational bean is not being invoked
 * outside the scope of a long-running conversation. If
 * it is, throw an exception.
 * 
 * @author Gavin King
 */
@Interceptor(stateless=true,
             around={BijectionInterceptor.class, BusinessProcessInterceptor.class})
public class ConversationalInterceptor extends AbstractInterceptor
{
   private static final long serialVersionUID = 1127583515811479385L;

   @AroundInvoke
   public Object aroundInvoke(InvocationContext invocation) throws Exception
   {
      Method method = invocation.getMethod();

      if ( isNoConversationForConversationalBean(method) )
      {
         Events.instance().raiseEvent("org.jboss.seam.noConversation");
         throw new NoConversationException( "no long-running conversation for @Conversational bean: " + getComponent().getName() );         
      }

      return invocation.proceed();
   
   }
   
   private boolean isNoConversationForConversationalBean(Method method)
   {
      boolean classlevelViolation = componentIsConversational() && 
            !Manager.instance().isLongRunningOrNestedConversation()  &&
            !method.isAnnotationPresent(Begin.class) &&
            !method.isAnnotationPresent(StartTask.class) &&
            !method.isAnnotationPresent(BeginTask.class) &&
            !method.isAnnotationPresent(Destroy.class) && 
            !method.isAnnotationPresent(Create.class); //probably superfluous
      
      if (classlevelViolation) return true;
      
      boolean methodlevelViolation = methodIsConversational(method) &&
            !Manager.instance().isLongRunningOrNestedConversation();
      
      return methodlevelViolation;
      
   }

   private boolean methodIsConversational(Method method) 
   {
      return method.isAnnotationPresent(Conversational.class);
   }

   private boolean componentIsConversational() 
   {
      return getComponent().getBeanClass().isAnnotationPresent(Conversational.class);
   }
   
   public boolean isInterceptorEnabled()
   {
      return getComponent().beanClassHasAnnotation(Conversational.class);
   }

}
