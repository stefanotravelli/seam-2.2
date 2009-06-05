package org.jboss.seam.wicket.ioc;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javassist.util.proxy.ProxyObject;

import org.jboss.seam.util.ProxyFactory;

/**
 * Utilitilies relating to a MetaModel
 * @author pmuir
 *
 */
public class MetaModelUtils
{
   
   public static String toName(String name, Method method)
   {
      //TODO: does not handle "isFoo"
      if (name==null || name.length() == 0)
      {
         name = method.getName().substring(3, 4).toLowerCase()
               + method.getName().substring(4);
      }
      return name;
   }

   public static String toName(String name, Field field)
   {
      if (name==null || name.length() == 0)
      {
         name = field.getName();
      }
      return name;
   }
   
   public static Class<ProxyObject> createProxyFactory(final Class beanClass)
   {
      ProxyFactory factory = new ProxyFactory();
      if (beanClass.isInterface())
      {
         factory.setInterfaces(new Class[] {beanClass, Serializable.class});
      }
      else
      {
         factory.setSuperclass( beanClass );
      }
      return factory.createClass();
   }

}
