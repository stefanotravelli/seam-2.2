package org.jboss.seam.jsf;

import java.io.IOException;

import javax.faces.application.StateManager;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.navigation.Pages;

/**
 * A wrapper for the JSF implementation's StateManager that allows
 * us to intercept saving of the serialized component tree. This
 * is quite ugly but was needed in order to allow conversations to
 * be started and manipulated during the RENDER_RESPONSE phase.
 * 
 * @author Gavin King
 */
@SuppressWarnings("deprecation")
public class SeamStateManager extends StateManager 
{
   private final StateManager stateManager;

   public SeamStateManager(StateManager sm) 
   {
      this.stateManager = sm;
   }

   @Override
   protected Object getComponentStateToSave(FacesContext ctx) 
   {
      throw new UnsupportedOperationException();
   }

   @Override
   protected Object getTreeStructureToSave(FacesContext ctx) 
   {
      throw new UnsupportedOperationException();
   }

   @Override
   protected void restoreComponentState(FacesContext ctx, UIViewRoot viewRoot, String str) 
   {
      throw new UnsupportedOperationException();
   }

   @Override
   protected UIViewRoot restoreTreeStructure(FacesContext ctx, String str1, String str2) 
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public SerializedView saveSerializedView(FacesContext facesContext) 
   {
      
      if ( Contexts.isPageContextActive() )
      {
         //store the page parameters in the view root
         Pages.instance().updateStringValuesInPageContextUsingModel(facesContext);
      }

      return stateManager.saveSerializedView(facesContext);
   }

   @Override
   public void writeState(FacesContext ctx, SerializedView sv) throws IOException 
   {
      stateManager.writeState(ctx, sv);
   }

   @Override
   public UIViewRoot restoreView(FacesContext ctx, String str1, String str2) 
   {
      return stateManager.restoreView(ctx, str1, str2);
   }

   @Override
   public Object saveView(FacesContext facesContext) 
   {
      
      if ( Contexts.isPageContextActive() )
      {
         //store the page parameters in the view root
         Pages.instance().updateStringValuesInPageContextUsingModel(facesContext);
      }

      return stateManager.saveView(facesContext);
   }

   @Override
   public void writeState(FacesContext ctx, Object sv) throws IOException 
   {
      stateManager.writeState(ctx, sv);
   }

   @Override
   public boolean isSavingStateInClient(FacesContext ctx) 
   {
      return stateManager.isSavingStateInClient(ctx);
   }
   
}