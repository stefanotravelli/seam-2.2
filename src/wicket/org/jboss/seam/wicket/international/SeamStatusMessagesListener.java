package org.jboss.seam.wicket.international;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.application.IComponentOnBeforeRenderListener;
import org.jboss.seam.international.StatusMessage;


public class SeamStatusMessagesListener implements IComponentOnBeforeRenderListener
{

   public void onBeforeRender(Component component)
   {
      
      WicketStatusMessages wicketStatusMessages = WicketStatusMessages.instance();
      wicketStatusMessages.onBeforeRender();
      
      if (component instanceof Page)
      {
         // If the component is the page, then we also add global messages
         List<StatusMessage> statusMessages = wicketStatusMessages.getGlobalMessages();
         if (statusMessages != null)
         {   
            for (StatusMessage statusMessage: statusMessages)
            {
               addFeedbackMessage(statusMessage, component);
            }
         }
         wicketStatusMessages.clearGlobalMessages();
      }
      
      List<StatusMessage> statusMessages = wicketStatusMessages.getKeyedMessages(component.getId());
      if (statusMessages != null)
      {
         for (StatusMessage statusMessage: statusMessages)
         {
            addFeedbackMessage(statusMessage, component);
         }
         wicketStatusMessages.clearKeyedMessages(component.getId());
      }
   }
   
   private void addFeedbackMessage(StatusMessage statusMessage, Component component)
   {
      switch (statusMessage.getSeverity())
      {
      case ERROR:
         component.error(statusMessage.getSummary());
         break;
      case FATAL:
         component.fatal(statusMessage.getSummary());
         break;
      case INFO:
         component.info(statusMessage.getSummary());
         break;
      case WARN:
         component.warn(statusMessage.getSummary());
         break;
      }
   }

}
