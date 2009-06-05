package org.jboss.seam.faces;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Init;
import org.jboss.seam.core.Manager;
import org.jboss.seam.pageflow.Pageflow;
import org.jboss.seam.web.Session;

/**
 * Book-keeping component that persists information
 * about the conversation associated with the current
 * page.
 * 
 * @author Gavin King
 *
 */
@Name("org.jboss.seam.faces.facesPage")
@BypassInterceptors
@Install(precedence=BUILT_IN, classDependencies="javax.faces.context.FacesContext")
@Scope(ScopeType.PAGE)
public class FacesPage implements Serializable
{
   private static final long serialVersionUID = 4807114041808347239L;
   private String pageflowName;
   private Integer pageflowCounter;
   private String pageflowNodeName;
   
   private String conversationId;
   private boolean conversationIsLongRunning;
   
   //private Map<String, Object> pageParameters;
   
   public String getConversationId()
   {
      return conversationId;
   }
   
   public void discardTemporaryConversation()
   {
      conversationId = null;
      conversationIsLongRunning = false;
   }
   
   public void discardNestedConversation(String outerConversationId)
   {
      conversationId = outerConversationId;
      conversationIsLongRunning = true;
   }
   
   public void storeConversation(String conversationId)
   {
      this.conversationId = conversationId;
      conversationIsLongRunning = true;
   }
   
   public void storePageflow()
   {
      if ( Init.instance().isJbpmInstalled() )
      {
         Pageflow pageflow = Pageflow.instance();
         if ( pageflow.isInProcess() /*&& !pageflow.getProcessInstance().hasEnded()*/ && Manager.instance().isLongRunningConversation() )
         {
            pageflowName = pageflow.getSubProcessInstance().getProcessDefinition().getName();
            pageflowNodeName = pageflow.getNode().getName();
            pageflowCounter = pageflow.getPageflowCounter();
         }
         else
         {
            pageflowName = null;
            pageflowNodeName = null;
            pageflowCounter = null;
         }
      }
   }

   public static FacesPage instance()
   {
      if ( !Contexts.isPageContextActive() )
      {
         throw new IllegalStateException("No page context active");
      }
      return (FacesPage) Component.getInstance(FacesPage.class, ScopeType.PAGE);
   }

   public boolean isConversationLongRunning()
   {
      return conversationIsLongRunning;
   }

   public Integer getPageflowCounter()
   {
      return pageflowCounter;
   }

   public String getPageflowName()
   {
      return pageflowName;
   }

   public String getPageflowNodeName()
   {
      return pageflowNodeName;
   }

   public void storeConversation()
   {
      Manager manager = Manager.instance();
      
      //we only need to execute this code when we are in the 
      //RENDER_RESPONSE phase, ie. not before redirects
   
      Session session = Session.getInstance();
      boolean sessionInvalid = session!=null && session.isInvalid();
      if ( !sessionInvalid && manager.isLongRunningConversation() )
      {
         storeConversation( manager.getCurrentConversationId() );
      }
      else if ( !sessionInvalid && manager.isNestedConversation() )
      {
         discardNestedConversation( manager.getParentConversationId() );
      }
      else
      {
         discardTemporaryConversation();
      }

      /*if ( !sessionInvalid && Init.instance().isClientSideConversations()  )
      {
         // if we are using client-side conversations, put the
         // map containing the conversation context variables 
         // into the view root (or remove it for a temp 
         // conversation context)
         Contexts.getConversationContext().flush();
      }*/

   }

   /*public Map<String, Object> getPageParameters()
   {
      return pageParameters==null ? Collections.EMPTY_MAP : pageParameters;
   }

   public void setPageParameters(Map<String, Object> pageParameters)
   {
      this.pageParameters = pageParameters.isEmpty() ? null : pageParameters;
   }
   
   /**
    * Used by test harness
    * 
    * @param name the page parameter name
    * @param value the value
    */
   /*public void setPageParameter(String name, Object value)
   {
      if (pageParameters==null)
      {
         pageParameters = new HashMap<String, Object>();
      }
      pageParameters.put(name, value);
   }*/

}
