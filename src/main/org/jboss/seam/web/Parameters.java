package org.jboss.seam.web;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * Access to request parameters.
 * 
 * @author Gavin King
 *
 */
@Name("org.jboss.seam.web.parameters")
@BypassInterceptors
@Scope(ScopeType.STATELESS)
@Install(precedence=BUILT_IN)
public class Parameters
{

   protected Object convertRequestParameter(String requestParameter, Class type)
   {
      if ( String.class.equals(type) ) return requestParameter;
      throw new IllegalArgumentException("No converters available");
   }

   public Map<String, String[]> getRequestParameters()
   {
      ServletContexts servletContexts = ServletContexts.getInstance();
      if ( servletContexts!=null )
      {
         ServletRequest servletRequest = servletContexts.getRequest();
         if ( servletRequest!=null )
         {
            return servletRequest.getParameterMap();
         }
      }
      return Collections.EMPTY_MAP;
   }

   public Object convertMultiValueRequestParameter(Map<String, String[]> requestParameters, String name, Class<?> type)
   {
      String[] array = requestParameters.get(name);
      if (array==null || array.length==0)
      {
         return null;
      }
      else
      {
         if ( type.isArray() )
         {
               int length = Array.getLength(array);
               Class<?> elementType = type.getComponentType();
               Object newInstance = Array.newInstance(elementType, length);
               for ( int i=0; i<length; i++ )
               {
                  Object element = convertRequestParameter( (String) Array.get(array, i), elementType );
                  Array.set( newInstance, i, element );
               }
               return newInstance;
         }
         else
         {
            return convertRequestParameter( array[0], type );
         }
      }
   }

   public static Parameters instance()
   {
      return (Parameters) Component.getInstance(Parameters.class, ScopeType.STATELESS);
   }
   
}
