package org.jboss.seam.ui.component;

import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;

import org.jboss.seam.navigation.ConversationIdParameter;
import org.jboss.seam.navigation.Pages;

/**
 * Embeds the natural conversation ID into the request.
 *
 * @author Shane Bryzak
 */
public abstract class UIConversationName extends UIParameter {
	
	private static final String COMPONENT_TYPE = "org.jboss.seam.ui.ConversationName";
   
   @Override
   public String getName()
   {
      return "conversationName";
   }
   
   @Override
   public Object getValue()
   {
      ConversationIdParameter param = Pages.instance().getConversationIdParameter(super.getValue().toString());      
      return param != null ? param.getConversationId() : null;
   }
   
   public static UIConversationName newInstance() {
      return (UIConversationName) FacesContext.getCurrentInstance().getApplication().createComponent(COMPONENT_TYPE);
   }
   
}
