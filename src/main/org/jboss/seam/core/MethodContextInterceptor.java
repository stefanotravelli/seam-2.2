package org.jboss.seam.core;

import static org.jboss.seam.ComponentType.ENTITY_BEAN;

import java.lang.reflect.Method;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.security.SecurityInterceptor;

/**
 * Sets up the METHOD context and unproxies the SFSB 
 * for the duration of the call.
 * 
 * @author Gavin King
 *
 */
@Interceptor(stateless=true, around={BijectionInterceptor.class, EventInterceptor.class, SecurityInterceptor.class})
public class MethodContextInterceptor extends AbstractInterceptor
{
   private static final long serialVersionUID = 6833040683938889232L;
   @AroundInvoke
   public Object aroundInvoke(InvocationContext ctx) throws Exception
   {
      Component comp = getComponent();
      String name = comp.getName();
      Object target = ctx.getTarget();
      Method method = ctx.getMethod();
      Object[] parameters = ctx.getParameters();
      Context outerMethodContext = Lifecycle.beginMethod();
      try
      {
         Contexts.getMethodContext().set(name, target);
         Contexts.getMethodContext().set("org.jboss.seam.this", target);
         Contexts.getMethodContext().set("org.jboss.seam.method", method);
         Contexts.getMethodContext().set("org.jboss.seam.parameters", parameters);
         Contexts.getMethodContext().set("org.jboss.seam.component", comp);
         return ctx.proceed();
      }
      finally
      {
         Lifecycle.endMethod(outerMethodContext);
      }
   }
   
   public boolean isInterceptorEnabled()
   {
      return getComponent().getType()!=ENTITY_BEAN;
   }
   
}
