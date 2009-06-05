package org.jboss.seam.ui.component;

import javax.faces.component.UIComponentBase;


public abstract class UIStyle extends UIComponentBase
{

   public abstract String getStyleClass();

   public abstract String getStyle();
   
   public abstract void setStyleClass(String styleClass);
   
   public abstract void setStyle(String style);

}
