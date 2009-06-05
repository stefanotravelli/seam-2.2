package org.jboss.seam.test.unit;

import javax.faces.context.FacesContext;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.FacesLifecycle;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.core.ConversationEntries;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Manager;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.navigation.Pages;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests that validate the ConversationControl component as initialized for a Page instance.
 * 
 * @author Jacob Orshalick
 */
public class PageConversationControlTest extends AbstractPageTest
{
   /**
    * Override the base setup to add the required conversation components included in testing.
    */
   @BeforeMethod
   @Override
   public void setup()
   {
      super.setup();
      
      installComponent(Contexts.getApplicationContext(), ConversationEntries.class);
      installComponent(Contexts.getApplicationContext(), Conversation.class);
      installComponent(Contexts.getApplicationContext(), FacesMessages.class);
      installComponent(Contexts.getApplicationContext(), Manager.class);
      installComponent(Contexts.getApplicationContext(), Events.class);
      
      Manager.instance().initializeTemporaryConversation();
      FacesLifecycle.resumeConversation(FacesContext.getCurrentInstance().getExternalContext());
   }

   /**
    * Tests that when a nested conversation encounters an end-conversation tag the nested conversation is
    * demoted to temporary.
    */
   @Test
   public void testBeginNestedAndEndConversation() {
      Manager.instance().beginConversation();
      Manager.instance().beginNestedConversation();
      
      assert Manager.instance().isLongRunningConversation();
      assert Manager.instance().isNestedConversation();
      
      FacesContext facesContext = FacesContext.getCurrentInstance();

      facesContext.getViewRoot().setViewId("/end-conversation-test.xhtml");
      Pages.instance().preRender(facesContext);
      
      // nested conversation should be demoted to temporary
      assert !Manager.instance().isLongRunningConversation();
      assert Manager.instance().isNestedConversation();
   }
   
   /**
    * Tests that when a non-nested conversation encounters an end-conversation tag specifying that the root 
    * should be ended the current conversation is simply demoted to temporary.
    */
   @Test
   public void testBeginAndEndRootConversation() {
      Manager.instance().beginConversation();
      
      assert Manager.instance().isLongRunningConversation();
      
      FacesContext facesContext = FacesContext.getCurrentInstance();

      facesContext.getViewRoot().setViewId("/end-root-conversation-test.xhtml");
      Pages.instance().preRender(facesContext);
      
      assert !Manager.instance().isLongRunningConversation();
   }
   
   /**
    * Tests that when a nested conversation encounters an end-conversation tag specifying that the root 
    * should be ended the root is ended thereby destroying the conversation stack.
    */
   @Test
   public void testBeginNestedAndEndRootConversation() {
      Manager.instance().beginConversation();
      String rootConversationId = Manager.instance().getCurrentConversationId();
      
      Manager.instance().beginNestedConversation();

      assert !Manager.instance().getCurrentConversationId().equals(rootConversationId);
      assert Manager.instance().isLongRunningConversation();
      assert Manager.instance().isNestedConversation();
      
      FacesContext facesContext = FacesContext.getCurrentInstance();

      facesContext.getViewRoot().setViewId("/end-root-conversation-test.xhtml");
      Pages.instance().preRender(facesContext);

      assert Manager.instance().getCurrentConversationId().equals(rootConversationId);
      assert !Manager.instance().isLongRunningConversation();
      assert !Manager.instance().isNestedConversation();
   }
}
