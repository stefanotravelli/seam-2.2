package org.jboss.seam.wicket.ioc;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.seam.wicket.WicketComponent;


public class WicketHandler implements Serializable
{
   
   public static WicketHandler create(Object bean)
   {
      WicketHandler handler = new WicketHandler(bean.getClass());
      return handler;
   }
   
   public WicketHandler(Class<?> type)
   {
      this.type = type;
   }
   
   private Class<?> type;
   private transient WicketComponent component;
   private int reentrant = 0;
   
   private WicketComponent getComponent()
   {
      if (component == null)
      {
         component = WicketComponent.getInstance(type);
      }
      return component;
   }
   
   public void beforeInvoke(Object target, Method calledMethod)
   {
      doBeforeInvoke(new InvocationContext(calledMethod, target, getComponent()));
   }
   
   public Object afterInvoke(Object target, Method calledMethod, Object result)
   {
      return doAfterInvoke(new InvocationContext(calledMethod, target, getComponent()), result);
   }
   
   public void beforeInvoke(Object target, Constructor constructor)
   {
      getComponent().initialize(target);
      doBeforeInvoke(new InvocationContext(constructor, target, getComponent()));
   }
   
   public void afterInvoke(Object target, Constructor constructor)
   {
      doAfterInvoke(new InvocationContext(constructor, target, getComponent()), null);
   }
   
   private void doBeforeInvoke(InvocationContext invocationContext)
   {
      if (reentrant == 0)
      {
         for (StatelessInterceptor interceptor : (List<StatelessInterceptor>) getComponent().getInterceptors())
         {
            interceptor.beforeInvoke(invocationContext);
         }
      }
      reentrant++;
      InstrumentedComponent enclosing = getEnclosingInstance(invocationContext.getBean());
      if (enclosing != null && enclosing.getHandler() != null)
      {
         enclosing.getHandler().injectEnclosingInstance(enclosing);
      }
   }
   
   protected void injectEnclosingInstance(InstrumentedComponent instance)
   {
      if (reentrant == 0)
      {
         getComponent().inject(instance);
      }
      reentrant++;
      instance = instance.getEnclosingInstance();
      if (instance != null)
      {
         instance.getHandler().injectEnclosingInstance(instance);
      }
   }
   
   
   public Exception handleException(Object target, Method method, Exception exception)
   {
      return doHandleException(new InvocationContext(method, target, getComponent()), exception);
   }
   
   public Exception handleException(Object target, Constructor constructor, Exception exception)
   {
      return doHandleException(new InvocationContext(constructor, target, getComponent()), exception);
   }
   
   private Exception doHandleException(InvocationContext invocationContext, Exception exception)
   {
      reentrant--;
      if (reentrant == 0)
      {
         for (StatelessInterceptor interceptor : (List<StatelessInterceptor>)  getComponent().getInterceptors())
         {
            exception = interceptor.handleException(invocationContext, exception);
         }
      }
      return exception;
   }
   
   private Object doAfterInvoke(InvocationContext invocationContext, Object result)
   {
      
      reentrant--;
      if (reentrant == 0)
      {
         for (int i = getComponent().getInterceptors().size() - 1; i >= 0; i--)
         {
            result = ((StatelessInterceptor) getComponent().getInterceptors().get(i)).afterInvoke(invocationContext, result);
         }
      }
      InstrumentedComponent enclosing = getEnclosingInstance(invocationContext.getBean());
      if (enclosing != null && enclosing.getHandler() != null)
      {
         enclosing.getHandler().disinjectEnclosingInstance(enclosing);
      }
      
      return result;
   }
   
   protected void disinjectEnclosingInstance(InstrumentedComponent instance)
   {
      reentrant--;
      if (reentrant == 0)
      {
         getComponent().disinject(instance);
      }
      instance = instance.getEnclosingInstance();
      if (instance != null)
      {
         instance.getHandler().disinjectEnclosingInstance(instance);
      }
   }


   public boolean isReentrant()
   {
      return reentrant > 0;
   }
   
   
   public InstrumentedComponent getEnclosingInstance(Object bean)
   {
      Class enclosingType = getComponent().getClass();
      if (enclosingType != null)
      {
         try 
         {
            Field enclosingField = bean.getClass().getDeclaredField(getComponent().getEnclosingInstanceVariableName());
            enclosingField.setAccessible(true);
            Object enclosingInstance = enclosingField.get(bean);
            if (enclosingInstance instanceof InstrumentedComponent)
            {
               return (InstrumentedComponent) enclosingInstance;
            }
         }
         catch (Exception e) {}
      }
      return null;
   }
   
}
