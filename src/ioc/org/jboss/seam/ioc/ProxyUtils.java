/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.seam.ioc;

import java.lang.reflect.Proxy;
import java.util.Set;

import javassist.util.proxy.ProxyObject;

import org.jboss.seam.Component;
import org.jboss.seam.ComponentType;
import org.jboss.seam.intercept.JavaBeanInterceptor;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class ProxyUtils
{
   private static final LogProvider log = Logging.getLogProvider(ProxyUtils.class);

   public static boolean isProxy(Class clazz)
   {
      return Proxy.isProxyClass(clazz);
   }

   public static Object enhance(Object bean, Set<Class> interfaces, IoCComponent component) throws Exception
   {
      Class beanClass = bean.getClass();
      if (isProxy(beanClass))
      {
         throw new RuntimeException("Seam cannot wrap JDK proxied IoC beans. Please use CGLib or Javassist proxying instead");
      }
      //
      if (isCglibProxyClass(beanClass) || isJavassistProxyClass(beanClass))
      {
         beanClass = beanClass.getSuperclass();
      }
      if (log.isDebugEnabled())
      {
         log.debug("Creating proxy for " + component.getIoCName() + " Seam component '"
               + component.getName() + "' using class: " + beanClass.getName());
      }
      // create pojo proxy
      JavaBeanInterceptor interceptor = new JavaBeanInterceptor(bean, component);
      // Should probably create a Factory but required a lot of duplicated
      // code and there is potential for a spring bean to provide
      // different interfaces at different times in an application. If
      // need is great I can create a Factory and assume the same
      // interfaces all the time.
      ProxyObject po = Component.createProxyFactory(ComponentType.JAVA_BEAN, beanClass, interfaces).newInstance();
      po.setHandler(interceptor);
      interceptor.postConstruct();
      return po;
   }

   public static boolean isCglibProxyClass(Class clazz)
   {
      return clazz != null && clazz.getName().contains("$$");
   }

   public static boolean isJavassistProxyClass(Class clazz)
   {
      return false; // todo
   }

}
