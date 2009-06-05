package org.jboss.seam.mock;

import java.io.IOException;

import javax.faces.application.StateManager;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

@SuppressWarnings("deprecation")
public class MockStateManager extends StateManager 
{
   
   @Override
   public Object saveView(FacesContext ctx) {
      return null;
   }
   
   @Override
   public void writeState(FacesContext ctx, Object state) throws IOException {}
   
   @Override
   public SerializedView saveSerializedView(FacesContext ctx) 
   {
      return null;
   }

   @Override
   protected Object getTreeStructureToSave(FacesContext ctx) 
   {
      return null;
   }

   @Override
   protected Object getComponentStateToSave(FacesContext ctx) 
   {
      return null;
   }

   @Override
   public void writeState(FacesContext ctx, SerializedView sv)
         throws IOException {}

   @Override
   public UIViewRoot restoreView(FacesContext ctx, String x, String y) 
   {
      return null;
   }

   @Override
   protected UIViewRoot restoreTreeStructure(FacesContext ctx, String x, String y) 
   {
      return null;
   }

   @Override
   protected void restoreComponentState(FacesContext ctx, UIViewRoot viewRoot, String x) {}

}
