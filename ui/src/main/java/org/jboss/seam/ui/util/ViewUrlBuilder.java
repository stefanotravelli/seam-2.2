
package org.jboss.seam.ui.util;

import java.io.UnsupportedEncodingException;

import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;

import org.jboss.seam.navigation.Page;
import org.jboss.seam.navigation.Pages;

public class ViewUrlBuilder extends UrlBuilder
{

   private Page page;

   public ViewUrlBuilder(String viewId, String fragment, boolean urlEncodeParameters)
   {
      super(fragment, FacesContext.getCurrentInstance().getResponseWriter().getCharacterEncoding(), urlEncodeParameters);
      if (viewId == null)
      {
         throw new NullPointerException("viewId must not be null");
      }
      FacesContext facesContext = FacesContext.getCurrentInstance();
      String url = facesContext.getApplication().getViewHandler().getActionURL(facesContext,
               viewId);
      url = Pages.instance().encodeScheme(viewId, facesContext, url);
      setUrl(url);
      
      page = Pages.instance().getPage(viewId);
   }
   
   public ViewUrlBuilder(String viewId, String fragment)
   {
      this(viewId, fragment, true);
      
   }

   @Override
   public void addParameter(UIParameter parameter) throws UnsupportedEncodingException
   {
      String name = parameter.getName();
      if (parameter.getValue() != null && !(name.equals(page.getConversationIdParameter().getParameterName())
               && getParameters().containsKey(name)))
      {
        super.addParameter(parameter);
      }
   }

    @Override
    public String getEncodedUrl() {
        return FacesContext.getCurrentInstance().getExternalContext().encodeActionURL(super.getEncodedUrl());
    }
}
