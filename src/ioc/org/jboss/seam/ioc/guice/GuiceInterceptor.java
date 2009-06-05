package org.jboss.seam.ioc.guice;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import com.google.inject.Injector;

/**
 * Triggers Guice injection on a Seam component.
 *
 * @author Pawel Wrzeszcz (pwrzeszcz [at] jboss . org)
 * @author Tomasz Szymanski (tszymanski [at] jboss.org)
 */
@Interceptor
public class GuiceInterceptor
{
   private static final Log log = Logging.getLog(GuiceInterceptor.class);

   private static final long serialVersionUID = -6716553117162905303L;

   private transient Injector defaultInjector = null;

   @AroundInvoke
   public Object aroundInvoke(InvocationContext invocationContext) throws Exception
   {
      final Injector injector = getInjectorForClass(invocationContext.getMethod().getDeclaringClass());

      if (log.isTraceEnabled())
      {
         log.trace("Injecting members of: #0", invocationContext.getTarget().getClass().getName());
      }

      injector.injectMembers(invocationContext.getTarget());

      return invocationContext.proceed();
   }

   private Injector getInjectorForClass(final Class<?> declaringClass)
   {
      final String expr;
      Guice guice = declaringClass.getAnnotation(Guice.class);
      if (guice != null) {
         expr = guice.value();
      }
      else {
         expr = null;
      }

      // Optimize lookups for default injector
      return (expr != null && expr.length() > 0)
         ? getInjectorByName(expr) : getCachedDefaultInjector();
   }

   private Injector getCachedDefaultInjector()
   {
      if (defaultInjector == null)
      {
         GuiceInit init = (GuiceInit) Component.getInstance(GuiceInit.class);

         if (init != null)
         {
            defaultInjector = init.getInjector();
         }    

         if (defaultInjector == null)
         {
            throw new IllegalStateException("Default Guice injector not specified.");
         }
      }

      return defaultInjector;
   }

   public static Injector getInjectorByName(final String expr)
   {
      Object result;

      if (expr.startsWith("#"))
      {
         result = Expressions.instance().createValueExpression(expr).getValue();
      }
      else
      {
         result = Component.getInstance(expr);
      }

      if (!(result instanceof Injector))
      {
         throw new IllegalArgumentException("Expression '" + expr + "' does not evaluate to a Guice injector.");
      }

      return (Injector) result;
   }
}
