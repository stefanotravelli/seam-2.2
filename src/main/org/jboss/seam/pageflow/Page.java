package org.jboss.seam.pageflow;

import org.dom4j.Element;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.core.Interpolator;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.jbpm.jpdl.xml.Parsable;

/**
 * A page node in a jPDL pageflow
 * 
 * @author Tom Baeyens
 * @author Gavin King
 *
 */
public class Page extends Node implements Parsable 
{
   
   // This classname is configured in the jbpm configuration 
   // file : org/jbpm/graph/node/node.types.xml inside 
   // the jbpm-{version}.jar
   
   // In case it would be necessary, that file, can be customized
   // by updating the reference to it in the central jbpm configuration 
   // file 'jbpm.cfg.xml'

   private static final long serialVersionUID = 1L;
   
   private String viewId;
   private boolean isConversationEnd = false;
   private boolean isConversationEndBeforeRedirect = false;
   private boolean isTaskEnd = false;
   private String transition;
   private String processToCreate;
   private boolean redirect;
   private String description;
   private Integer timeout;
   private boolean backEnabled;
   private boolean switchEnabled;
   private String noConversationViewId;

   /**
    * parses the dom4j element that corresponds to this page.
    */
   @Override
   public void read(Element pageElement, JpdlXmlReader jpdlXmlReader) 
   {
      super.read(pageElement, jpdlXmlReader);
      viewId = pageElement.attributeValue("view-id");
      if (viewId==null)
      {
         throw new IllegalStateException("must specify view-id for <page/> node: " + pageElement.attributeValue("name"));
      }
      noConversationViewId = pageElement.attributeValue("no-conversation-view-id");
      backEnabled = "enabled".equals( pageElement.attributeValue("back") );
      switchEnabled = !"disabled".equals( pageElement.attributeValue("switch") );
      Element endConversationElement = pageElement.element("end-conversation");
      if ( endConversationElement!=null )
      {
         isConversationEnd = true;
         isConversationEndBeforeRedirect = Boolean.parseBoolean( endConversationElement.attributeValue("before-redirect") );
         processToCreate = endConversationElement.attributeValue("create-process");
      }
      Element endTaskElement = pageElement.element("end-task");
      if (endTaskElement!=null) 
      {
         isTaskEnd = true;
         transition = endTaskElement.attributeValue("transition");
      }
      redirect = Boolean.parseBoolean( pageElement.attributeValue("redirect") );
      if ( pageElement.element("redirect")!=null )
      {
         redirect = true;
      }
      Element descriptionElement = pageElement.element("description");
      if (descriptionElement!=null)
      {
         description = descriptionElement.getTextTrim();
      }
      String timeoutString = pageElement.attributeValue("timeout");
      if ( timeoutString!=null )
      {
         timeout = Integer.valueOf(timeoutString);
      }
   }

   /**
    * is executed when execution arrives in this page at runtime.
    */
   @Override
   public void execute(ExecutionContext executionContext) 
   {
      if ( isConversationEnd && processToCreate!=null )
      {
         BusinessProcess.instance().createProcess(processToCreate);
      }
      
      if ( isTaskEnd ) 
      {
         BusinessProcess.instance().endTask(transition);         
      }

      if (isConversationEnd || isTaskEnd ) 
      {
         if (isConversationEndBeforeRedirect)
         {
            Conversation.instance().endBeforeRedirect();
         }
         else
         {
            Conversation.instance().end();
         }
      }
      if (getAction() != null)
      {
         try
         {
            getAction().execute(executionContext);
         }
         catch (Exception e)
         {
            raiseException(e, executionContext);
         }
      }
   }

   public boolean isConversationEnd() 
   {
      return isConversationEnd;
   }
   
   public String getTransition() 
   {    
      return transition;
   }
   
   public String getViewId() 
   {
      return Interpolator.instance().interpolate(viewId);
   }
   
   public boolean isRedirect()
   {
      return redirect;
   }
   
   public boolean hasDescription()
   {
      return description!=null;
   }

   @Override
   public String getDescription() {
      return Interpolator.instance().interpolate(description);
   }

   public Integer getTimeout() {
      return timeout;
   }

   public boolean isBackEnabled()
   {
      return backEnabled;
   }

   public boolean isSwitchEnabled()
   {
      return switchEnabled;
   }

   public String getNoConversationViewId()
   {
      return noConversationViewId;
   }

   protected boolean isTaskEnd()
   {
      return isTaskEnd;
   }

   protected String getProcessToCreate()
   {
      return processToCreate;
   }

   public boolean isConversationEndBeforeRedirect()
   {
      return isConversationEndBeforeRedirect;
   }
}
