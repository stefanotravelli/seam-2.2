package org.jboss.seam.wicket.ioc;

import static org.jboss.seam.util.Reflections.invokeAndWrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.jboss.seam.util.Reflections;

/**
 * Implementation of BijectedAttribute for a method
 * @author Pete Muir
 *
 */
public abstract class BijectedMethod<T extends Annotation> implements BijectedAttribute<T>
{
   private Method method;
   private T annotation;
   private String contextVariableName;
   
   public BijectedMethod(Method method, T annotation)
   {
      this.method = method;
      this.annotation = annotation;
      contextVariableName = getSpecifiedContextVariableName();
      if (contextVariableName == null || "".equals(contextVariableName))
      {
         if ( method.getName().matches("^(get|set).*") && method.getParameterTypes().length==0 )
         {
            contextVariableName = method.getName().substring(3);
         }
         else if ( method.getName().matches("^(is).*") && method.getParameterTypes().length==0 )
         {
            contextVariableName = method.getName().substring(2);
         }
      }
   }
   
   public Method getMember()
   {
      return method;
   }
   
   public T getAnnotation()
   {
      return annotation;
   }
   
   public void set(Object bean, Object value)
   {
      method.setAccessible(true);
      invokeAndWrap( method, bean, value );
   }
   
   public Object get(Object bean)
   {
      method.setAccessible(true);
      return invokeAndWrap(method, bean);
   }
   
   public Class getType()
   {
      return method.getParameterTypes()[0];
   }
   
   @Override
   public String toString()
   {
      return "BijectedMethod(" + Reflections.toString(method) + ')';
   }
   
   protected abstract String getSpecifiedContextVariableName();
   
   public String getContextVariableName()
   {
      return contextVariableName;
   }
   
}
