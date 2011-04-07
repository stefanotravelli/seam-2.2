package org.jboss.seam.wicket;

import org.apache.wicket.AbortException;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.application.IComponentOnBeforeRenderListener;
import org.jboss.seam.core.Manager;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;

/**
 * A listener registered by SeamWebApplication which ensures that a long-running conversation exists when a page 
 * annotated with NoConversationPage is rendered 
 *
 */
public class SeamEnforceConversationListener implements IComponentOnBeforeRenderListener
{
   public void onBeforeRender(Component component)
   {
      if (!Manager.instance().isLongRunningConversation())
      {
         WicketComponent<?> wicketComponent = WicketComponent.getInstance(component.getClass());
         if (wicketComponent != null)
         {
            Class<? extends Page> noConversationPage = wicketComponent.getNoConversationPage();
            if (noConversationPage != null)
            {
               final RequestCycle cycle = RequestCycle.get();
               StatusMessages.instance().addFromResourceBundleOrDefault( 
                     StatusMessage.Severity.WARN, 
                     "org.jboss.seam.NoConversation", 
                     "The conversation ended or timed" 
                  );
               cycle.redirectTo(cycle.getSession().getPageFactory().newPage(noConversationPage));
               throw new AbortException();
            }
         }
      }
   }
}
