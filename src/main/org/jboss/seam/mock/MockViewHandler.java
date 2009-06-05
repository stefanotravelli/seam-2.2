package org.jboss.seam.mock;

import java.io.IOException;
import java.util.Locale;

import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.jboss.seam.util.Strings;

public class MockViewHandler extends ViewHandler {

	@Override
	public Locale calculateLocale(FacesContext ctx) {
		return Locale.getDefault();
	}

	@Override
	public String calculateRenderKitId(FacesContext ctx) {
		return null;
	}

	@Override
	public UIViewRoot createView(FacesContext ctx, String viewId) {
		UIViewRoot viewRoot = new UIViewRoot();
      viewRoot.setViewId(viewId);
      //TODO: set locale?
      return viewRoot;
	}

	@Override
	public String getActionURL(FacesContext ctx, String viewId)
   {
      String contextPath = ctx.getExternalContext().getRequestContextPath();
      String pathInfo = ctx.getExternalContext().getRequestPathInfo();
      String servletPath = ctx.getExternalContext().getRequestServletPath(); 
      
      if (Strings.isEmpty(pathInfo))
      {
         int sploc = servletPath.lastIndexOf('.');
         if (sploc < 0)
         {
            throw new IllegalArgumentException("no file extension in servlet path: " + servletPath);
         }
         return contextPath + getViewIdSansSuffix(viewId) + servletPath.substring(sploc);

      }
      else
      {
         return contextPath + (servletPath!=null?servletPath : "") + viewId;
      }
   }

	private static String getViewIdSansSuffix(String viewId)
	{
	   int loc = viewId.lastIndexOf('.');
      if (loc < 0)
      {
         throw new IllegalArgumentException("no file extension in view id: " + viewId);
      }
      return viewId.substring(0, loc);
	}

	@Override
	public String getResourceURL(FacesContext ctx, String url) 
   {
		return url;
	}

	@Override
	public void renderView(FacesContext ctx, UIViewRoot viewRoot)
			throws IOException, FacesException {}

	@Override
	public UIViewRoot restoreView(FacesContext ctx, String id) 
   {
		return null;
	}

	@Override
	public void writeState(FacesContext ctx) throws IOException {}

}
