package org.jboss.seam.util;

import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.context.FacesContext;

public class Faces
{

   public static String getDefaultSuffix(FacesContext context) throws FacesException 
   {
      String viewSuffix = context.getExternalContext().getInitParameter(ViewHandler.DEFAULT_SUFFIX_PARAM_NAME);
      return (viewSuffix != null) ? viewSuffix : ViewHandler.DEFAULT_SUFFIX;
   }
   
}
