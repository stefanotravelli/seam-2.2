package org.jboss.seam.ioc.guice;

import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.util.Reflections;
import org.jboss.seam.util.Strings;
import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import com.google.inject.Injector;
import com.google.inject.Inject;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Triggers Guice injection on a Seam component.
 *
 * @author Pawel Wrzeszcz (pwrzeszcz [at] jboss . org)
 * @author Tomasz Szymanski (tszymanski [at] jboss.org)
 */
@Interceptor
public class GuiceInterceptor extends AbstractInterceptor
{
   private static final Log log = Logging.getLog(GuiceInterceptor.class);

   private static final long serialVersionUID = -6716553117162905303L;

   private static final String GUICE_COMPONENT_FIELDS_MAP = "org.jboss.seam.GuiceComponentFieldsMap";

   private transient Injector defaultInjector = null;

   @AroundInvoke
   public Object aroundInvoke(InvocationContext invocationContext) throws Exception
   {
      inject(invocationContext.getTarget());

      Object result = invocationContext.proceed();

      disinject(invocationContext.getTarget());

      return result;
   }

   private void inject(Object target)
   {
      if (log.isTraceEnabled())
      {
         log.trace("Injecting members of component '#0'", getComponent().getName());
      }

      getGuiceInjector().injectMembers(target);
    }

   private void disinject(Object target) throws Exception
   {
      for (Field guiceField : getGuiceAnnotatedFields())
      {
         if (!guiceField.isAccessible())
         {
            guiceField.setAccessible(true);
         }
         Reflections.set(guiceField, target, null);
      }
   }

   /**
    * @return a Guice injector for the current component
    */
   private Injector getGuiceInjector()
   {
      final String expr;
      Guice guice = getComponent().getBeanClass().getAnnotation(Guice.class);
      if (guice != null) {
         expr = guice.value();
      }
      else {
         expr = null;
      }

      // Optimize lookups for default injector
      return Strings.isEmpty(expr)
         ? getCachedDefaultInjector() : getInjectorByName(expr);
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

   private static Injector getInjectorByName(final String expr)
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

   /**
    * @return a collection of all component fields injected by Guice (annotated with the @Inject annotation)
    */
   private Collection<Field> getGuiceAnnotatedFields()
   {
      final Map<Class,Collection<Field>> fieldsMap = getGuiceComponentFieldsMap();
      Collection<Field> annotatedFields = fieldsMap.get(getComponent().getBeanClass());
      if (annotatedFields == null)
      {
         annotatedFields = Reflections.getFields(getComponent().getBeanClass(), Inject.class);
         fieldsMap.put(getComponent().getBeanClass(), annotatedFields);
      }
      return annotatedFields;
   }

   /**
    * @return a cache that stores fields annotated with the @Inject annotation for the Guice component classes
    */
   @SuppressWarnings("unchecked")
   private Map<Class,Collection<Field>> getGuiceComponentFieldsMap()
   {
      if (Contexts.getApplicationContext().get(GUICE_COMPONENT_FIELDS_MAP) == null)
      {
         Contexts.getApplicationContext().set(GUICE_COMPONENT_FIELDS_MAP, new HashMap<Class, Collection<Field>>());
      }
      return (Map<Class, Collection<Field>>) Contexts.getApplicationContext().get(GUICE_COMPONENT_FIELDS_MAP);
   }

   public boolean isInterceptorEnabled()
   {
      return true;
   }
}
