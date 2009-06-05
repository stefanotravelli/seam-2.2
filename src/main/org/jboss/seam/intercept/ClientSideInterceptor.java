//$Id$
package org.jboss.seam.intercept;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;

import org.jboss.seam.Component;
import org.jboss.seam.ComponentType;
import org.jboss.seam.annotations.intercept.InterceptorType;
import org.jboss.seam.ejb.SeamInterceptor;

/**
 * Controller interceptor for client-side interceptors of
 * EJB3 session bean components
 * 
 * @author Gavin King
 */
public class ClientSideInterceptor extends RootInterceptor 
      implements MethodHandler
{
   private static final long serialVersionUID = -1578313703571846699L;
   
   private final Object bean;
   private final Class beanClass;

   public ClientSideInterceptor(Object bean, Component component)
   {
      super(InterceptorType.CLIENT);
      this.bean = bean;
      this.beanClass = component.getBeanClass();
      init(component);
   }
   
   public Object invoke(final Object proxy, final Method method, final Method proceed, final Object[] params) throws Throwable
   {
      String methodName = method.getName();
      if ( params!=null && params.length==0 )
      {
         if ( "finalize".equals(methodName) )
         {
            return proceed.invoke(proxy, params);
         }
         else if ( "writeReplace".equals(methodName) )
         {
            return this;
         }
         else if ( "getComponent".equals(methodName) )
         {
            return getComponent();
         }
      }
      Object result = invoke( createInvocationContext(method, params), EventType.AROUND_INVOKE );
      return sessionBeanReturnedThis(result) ? proxy : result;
   }

   private boolean sessionBeanReturnedThis(Object result)
   {
      return result==bean || (
            result!=null && getComponent().getBeanClass().isAssignableFrom( result.getClass() )
         );
   }

   private RootInvocationContext createInvocationContext(final Method method, final Object[] params)
   {
      return new RootInvocationContext(bean, method, params)
      {
         @Override
         public Object proceed() throws Exception
         {
            Component old = SessionBeanInterceptor.COMPONENT.get();
            SeamInterceptor.COMPONENT.set( getComponent() );
            try
            {
               return super.proceed();
            }
            finally
            {
               SeamInterceptor.COMPONENT.set(old);
            }
         }
      };
   }
   
   //TODO: copy/paste from JavaBean interceptor
   Object readResolve()
   {
      Component comp = null;
      try
      {
         comp = getComponent();
      }
      catch (IllegalStateException ise) {
         //this can occur when tomcat deserializes persistent sessions
      }
      
      try
      {
         if (comp==null)
         {
            ProxyObject proxy = Component.createProxyFactory( 
                  ComponentType.STATEFUL_SESSION_BEAN, 
                  beanClass, 
                  Component.getBusinessInterfaces(beanClass)
               ).newInstance();
            proxy.setHandler(this);
            return proxy;
         }
         else
         {
            return comp.wrap(bean, this);
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

}
