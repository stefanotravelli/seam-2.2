package org.jboss.seam.navigation;

import java.util.Map;

import org.jboss.seam.core.ConversationPropagation;
import org.jboss.seam.core.Manager;
import org.jboss.seam.util.Id;

/**
 * 
 * Seam's default strategy for propagating conversations.
 *
 */
public class SyntheticConversationIdParameter implements ConversationIdParameter
{
   public String getName()
   {
      return null;
   }
   
   public String getParameterName()
   {
      return Manager.instance().getConversationIdParameter();
   }
   
   public String getParameterValue()
   {
      return Manager.instance().getCurrentConversationId();
   }
   
   public String getParameterValue(String value)
   {
      return value;
   }
   
   public String getInitialConversationId(Map parameters)
   {
      return Id.nextId();  
   }
   
   public String getConversationId()
   {
      return Id.nextId();
   }
   
   public String getRequestConversationId(Map parameters)
   {
      return ConversationPropagation.getRequestParameterValue( parameters, getParameterName() );      
   }
}
