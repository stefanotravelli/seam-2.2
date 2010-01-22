package org.jboss.seam.wicket.international;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;

@Scope(ScopeType.CONVERSATION)
@Name(StatusMessages.COMPONENT_NAME)
@Install(precedence=FRAMEWORK, classDependencies="org.apache.wicket.Application")
@BypassInterceptors
public class WicketStatusMessages extends StatusMessages
{
   
   public void onBeforeRender()
   {
      doRunTasks();
   }
   
   public List<StatusMessage> getKeyedMessages(String id)
   {
      return instance().getKeyedMessages().get(id);
   }
   
   public List<StatusMessage> getGlobalMessages()
   {
      return instance().getMessages();
   } 
   
   public static WicketStatusMessages instance()
   {
      Component component = Component.forName(StatusMessages.COMPONENT_NAME);
      if(component != null && !component.getScope().isContextActive())
      {
         throw new IllegalStateException("No active "+component.getScope().name()+" context");
      }
      //Attempting to get the instance anyway for backwards compatibility with some potential hack situations.
      return (WicketStatusMessages) Component.getInstance(StatusMessages.COMPONENT_NAME);
   }

}
