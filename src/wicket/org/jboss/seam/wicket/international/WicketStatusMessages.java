package org.jboss.seam.wicket.international;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
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
      if ( !Contexts.isConversationContextActive() )
      {
         throw new IllegalStateException("No active conversation context");
      }
      return (WicketStatusMessages) Component.getInstance(StatusMessages.COMPONENT_NAME, ScopeType.CONVERSATION);
   }

}
