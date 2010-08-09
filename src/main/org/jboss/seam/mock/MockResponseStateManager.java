package org.jboss.seam.mock;

import java.io.IOException;

import javax.faces.application.StateManager.SerializedView;
import javax.faces.context.FacesContext;
import javax.faces.render.ResponseStateManager;

@SuppressWarnings("deprecation")
public class MockResponseStateManager extends ResponseStateManager 
{

   @Override
   public Object getComponentStateToRestore(FacesContext ctx) 
   {
      return new Object();
   }

   @Override
   public Object getTreeStructureToRestore(FacesContext ctx, String x) 
   {
      return new Object();
   }

   @Override
   public void writeState(FacesContext ctx, SerializedView viewState) throws IOException 
   {
      
   }
   
   @Override
   public boolean isPostback(FacesContext context)
   {
      return context.getExternalContext().getRequestParameterMap().containsKey(ResponseStateManager.VIEW_STATE_PARAM);
   }

}
