package org.jboss.seam.wicket.ioc;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.jboss.seam.wicket.WicketComponent;

public class InvocationContext<T>
{

   private Constructor<T> constructor;
   private Method method;
   private T bean;
   private WicketComponent<T> component;
   
   public InvocationContext(Method method, T bean, WicketComponent<T> component)
   {
      this.method = method;
      this.bean = bean;
      this.component = component;
   }
   
   public InvocationContext(Constructor<T> constructor, T bean, WicketComponent<T> component)
   {
      // TODO Write the constructor discovery code
      this.constructor = constructor;
      this.bean = bean;
      this.component = component;
   }
   
   public Constructor<T> getConstructor()
   {
      return constructor;
   }
   
   public Method getMethod()
   {
      return method;
   }
   
   public Member getMember()
   {
      if (method != null)
      {
         return method;
      }
      else if (constructor != null)
      {
         return constructor;
      }
      else
      {
         throw new IllegalStateException("No member");
      }
   }
   
   public AccessibleObject getAccessibleObject()
   {
      if (method != null)
      {
         return method;
      }
      else if (constructor != null)
      {
         return constructor;
      }
      else
      {
         throw new IllegalStateException("No member");
      }
   }
   
   public T getBean()
   {
      return bean;
   }
   
   public WicketComponent<T> getComponent()
   {
      return component;
   }
   
   public InstrumentedComponent getInstrumentedComponent()
   {
      return (InstrumentedComponent) bean;
   }
   
}