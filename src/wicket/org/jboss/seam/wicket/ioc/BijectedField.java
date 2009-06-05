package org.jboss.seam.wicket.ioc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.jboss.seam.util.Reflections;

/**
 * Implementation of BijectedAttribute for a field
 * @author Pete Muir
 *
 */
public abstract class BijectedField<T extends Annotation> extends InjectedField<T> implements BijectedAttribute<T>
{
   private String contextVariableName;
   
   public BijectedField(Field field, T annotation)
   {
      super(field, annotation);
      contextVariableName = getSpecifiedContextVariableName();
      if (contextVariableName == null || "".equals(contextVariableName))
      {
         contextVariableName = field.getName();
      }
   }
   
   public Object get(Object bean)
   {
      field.setAccessible(true);
      return Reflections.getAndWrap(field, bean);
   }
   
   public String getContextVariableName()
   {
      return contextVariableName;
   }
   
   protected abstract String getSpecifiedContextVariableName();
   
   @Override
   public String toString()
   {
      return "BijectedField(" + Reflections.toString(field) + ')';
   }
}
