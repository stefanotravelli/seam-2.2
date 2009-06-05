package org.jboss.seam.jsf;

import java.io.IOException;
import java.util.Locale;

import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.LocaleSelector;

/**
 * Allows the JSF view locale to be integrated with
 * the locale coming from Seam internationalization.
 * 
 * @see org.jboss.seam.international.LocaleSelector
 * 
 * @author Gavin King
 *
 */
public class SeamViewHandler extends ViewHandler 
{
   
   private ViewHandler viewHandler;
   
   public SeamViewHandler(ViewHandler viewHandler)
   {
      this.viewHandler = viewHandler;
   }
   
   @Override
   public String calculateCharacterEncoding(FacesContext context)
   {
      return viewHandler.calculateCharacterEncoding(context);
   }

   @Override
   public void initView(FacesContext context) throws FacesException
   {
      viewHandler.initView(context);
   }
   
   @Override
   public Locale calculateLocale(FacesContext facesContext) 
   {
      Locale jsfLocale = viewHandler.calculateLocale(facesContext);
      if ( !Contexts.isSessionContextActive() )
      {
         return jsfLocale;
      }
      else
      {
         return LocaleSelector.instance().calculateLocale(jsfLocale);
      }
   }

   @Override
   public String calculateRenderKitId(FacesContext ctx) 
   {
      return viewHandler.calculateRenderKitId(ctx);
   }

   @Override
   public UIViewRoot createView(FacesContext ctx, String viewId) 
   {
      return viewHandler.createView(ctx, viewId);
   }

   @Override
   public String getActionURL(FacesContext ctx, String viewId) 
   {
      return viewHandler.getActionURL(ctx, viewId);
   }

   @Override
   public String getResourceURL(FacesContext ctx, String path) 
   {
      return viewHandler.getResourceURL(ctx, path);
   }

   @Override
   public void renderView(FacesContext ctx, UIViewRoot viewRoot)
         throws IOException, FacesException 
   {
      viewHandler.renderView(ctx, viewRoot);
   }

   @Override
   public UIViewRoot restoreView(FacesContext ctx, String viewId) 
   {
      return viewHandler.restoreView(ctx, viewId);
   }

   @Override
   public void writeState(FacesContext ctx) throws IOException 
   {
      viewHandler.writeState(ctx);
   }

}
