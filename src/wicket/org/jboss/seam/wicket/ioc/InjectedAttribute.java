package org.jboss.seam.wicket.ioc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;

public interface InjectedAttribute<T extends Annotation>
{

   public abstract T getAnnotation();

   public abstract Class getType();

   public abstract void set(Object bean, Object value);

   public abstract Member getMember();

}