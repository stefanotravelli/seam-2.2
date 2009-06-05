package org.jboss.seam.debug;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.Contexts;

@Name("org.jboss.seam.debug.introspector")
@Install(debug=true, precedence=BUILT_IN)
public class Introspector {
   
   @RequestParameter
   private String name;
   
   public Attribute[] getAttributes() throws Exception
   {
      if (name==null) return null;
      Object component = Contexts.lookupInStatefulContexts(name);
      if (component==null) 
      {
         return null;
      }
      else if (component instanceof Map)
      {
         return getMapAttributes( (Map) component );
      }
      else if (component instanceof List)
      {
         return getListAttributes( (List) component );
      }
      else
      {
         return getComponentAttributes(component);
      }
   }
   
   public Attribute[] getMapAttributes(Map<Object, Object> map)
   {
      Attribute[] attributes = new Attribute[map.size()];
      int i=0;
      for( Map.Entry me: map.entrySet() )
      {
         attributes[i++] = new Attribute( me.getKey().toString(), me.getValue() );
      }
      return attributes;
   }

   public Attribute[] getListAttributes(List list)
   {
      Attribute[] attributes = new Attribute[list.size()];
      for(int i=0; i<list.size(); i++ )
      {
         attributes[i] = new Attribute( Integer.toString(i), list.get(i) );
      }
      return attributes;
   }

   private Attribute[] getComponentAttributes(Object component) throws IntrospectionException, IllegalAccessException {
      BeanInfo bi = java.beans.Introspector.getBeanInfo( component.getClass() );
      //MethodDescriptor[] methods = bi.getMethodDescriptors();
      PropertyDescriptor[] properties = bi.getPropertyDescriptors();
      Attribute[] attributes = new Attribute[properties.length+1];
      for (int i=0; i<properties.length; i++)
      {
         Object value;
         try
         {
            Method readMethod = properties[i].getReadMethod();
            if (readMethod==null) continue;
            value = readMethod.invoke(component);
         }
         catch (InvocationTargetException ite)
         {
            Throwable e = ite.getCause();
            value = toString(e);
         }
         
         boolean convertArrayToList = value!=null && 
            value.getClass().isArray() && 
            !value.getClass().getComponentType().isPrimitive();
         if ( convertArrayToList )
         {
            value = Arrays.asList( (Object[]) value );
         }
         
         attributes[i] = new Attribute( properties[i].getDisplayName(), value );
      }
      
      String toString;
      try
      {
          toString = component.toString();
      }
      catch (Exception e)
      {
          toString = e.getClass().getName() + '[' + e.getMessage() + ']';
      }
      attributes[properties.length] = new Attribute("toString()", toString);
      
      return attributes;
   }

   private static String toString(Throwable e)
   {
      return e.getClass().getName() + '[' + e.getMessage() + ']';
   }
   
   public static class Attribute
   {
      private String name;
      private Object value;
      
      public Attribute(String name, Object value)
      {
         this.name = name;
         this.value = value;
      }
      
      public String getName() 
      {
         return name;
      }
      
      public Object getValue() 
      {
         return value;
      }
      
      public String getStringValue() 
      {
         try
         {
            return value==null ? null : value.toString();
         }
         catch (Throwable e)
         {
            return Introspector.toString(e);
         }
      }
      
   }
}
