package org.jboss.seam.ui.component;

import javax.faces.component.UIComponentBase;

/**
 * UIComponent for validator 
 * 
 * @author Daniel Roth
 */
public abstract class UIEqualityValidator extends UIComponentBase
{

   public abstract String getFor();

   public abstract void setFor(String forId);

   public abstract String getMessage();

   public abstract void setMessage(String message);

   public abstract String getMessageId();

   public abstract void setMessageId(String messageId);
   
   public abstract void setOperator(String operator);
   
   public abstract String getOperator();

}
