package org.jboss.seam.faces;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * Access to request parameters in the JSF environment.
 * 
 * @author Gavin King
 *
 */
@Name("org.jboss.seam.web.parameters")
@BypassInterceptors
@Scope(ScopeType.STATELESS)
@Install(precedence=FRAMEWORK, classDependencies="javax.faces.context.FacesContext")
public class Parameters extends org.jboss.seam.web.Parameters
{

   @Override
   protected Object convertRequestParameter(String requestParameter, Class type)
   {
      if ( String.class.equals(type) ) return requestParameter;
   
      FacesContext facesContext = FacesContext.getCurrentInstance();
      if (facesContext==null)
      {
         throw new IllegalStateException("No FacesContext associated with current thread, cannot convert request parameter type");
      }
      else
      {
         Converter converter = facesContext.getApplication().createConverter(type);
         if (converter==null)
         {
            throw new IllegalArgumentException("no converter for type: " + type);
         }
         UIViewRoot viewRoot = facesContext.getViewRoot();
         return converter.getAsObject( 
                  facesContext, 
                  viewRoot==null ? new UIViewRoot() : viewRoot, //have to pass something here, or get a totally useless NPE from JSF 
                  requestParameter );
      }
   }

   @Override
   public Map<String, String[]> getRequestParameters()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      if ( facesContext != null )
      {
         return facesContext.getExternalContext().getRequestParameterValuesMap();
      }
      
      return super.getRequestParameters();
   }

}
