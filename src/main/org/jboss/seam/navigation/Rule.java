package org.jboss.seam.navigation;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import org.jboss.seam.core.Events;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.util.Strings;

/**
 * Metadata for an &lt;rule/&gt; in pages.xml
 * 
 * @author Gavin King
 *
 */
public final class Rule
{
   private String outcomeValue;
   private ValueExpression condition;
   private List<Output> outputs = new ArrayList<Output>();
   private ConversationControl conversationControl = new ConversationControl();
   private TaskControl taskControl = new TaskControl();
   private ProcessControl processControl = new ProcessControl();
   private List<NavigationHandler> navigationHandlers = new ArrayList<NavigationHandler>();
   private List<String> eventTypes = new ArrayList<String>();

   public boolean matches(String actualValue)
   {
      return ( actualValue!=null || condition!=null ) &&
            ( outcomeValue==null || outcomeValue.equals(actualValue) ) &&
            ( condition==null || Boolean.TRUE.equals( condition.getValue() ) );
   }
   
   public List<NavigationHandler> getNavigationHandlers()
   {
      return navigationHandlers;
   }

   public void addNavigationHandler(NavigationHandler navigationHandler)
   {
      this.navigationHandlers.add(navigationHandler);
   }

   public ConversationControl getConversationControl()
   {
      return conversationControl;
   }
   
   public TaskControl getTaskControl()
   {
      return taskControl;
   }
   
   public ProcessControl getProcessControl()
   {
      return processControl;
   }

   public ValueExpression getCondition()
   {
      return condition;
   }

   public void setCondition(ValueExpression expression)
   {
      this.condition = expression;
   }

   public String getOutcomeValue()
   {
      return outcomeValue;
   }

   public void setOutcomeValue(String value)
   {
      this.outcomeValue = value;
   }

   public List<Output> getOutputs()
   {
      return outputs;
   }

   public boolean execute(FacesContext context)
   {
      getConversationControl().beginOrEndConversation();
      getTaskControl().beginOrEndTask();
      getProcessControl().createOrResumeProcess();
      for ( Output output: getOutputs() ) 
      {
         output.out();
      }
      for (String eventType : eventTypes)
      {
         Events.instance().raiseEvent(eventType);
      }
      for ( NavigationHandler nh: getNavigationHandlers() )
      {
         if ( nh.navigate(context) ) return true;
      }
      return false;
   }

   public List<String> getEventTypes()
   {
      return eventTypes;
   }

   public void addEventType(String eventType)
   {
      if (!Strings.isEmpty(eventType))
      {
         eventTypes.add(eventType);
      }
   }
}