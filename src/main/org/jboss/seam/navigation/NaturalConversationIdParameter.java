package org.jboss.seam.navigation;

import java.util.Map;

import org.jboss.seam.core.ConversationPropagation;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;

/**
 * A conversation parameter strategy for "natural" conversation ids.
 * Natural conversation ids are defined using &lt;conversation/&gt; in 
 * pages.xml. 
 *  
 * @author Shane Bryzak
 */
public class NaturalConversationIdParameter implements ConversationIdParameter
{
   private String name;
   private String parameterName;
   private ValueExpression vb;
   
   public NaturalConversationIdParameter(String name, String paramName, String expression)
   {
      this.name = name;
      this.parameterName = paramName;
      
      this.vb = expression != null ? 
               Expressions.instance().createValueExpression(expression) : null;
   }
   
   public String getName()
   {
      return name;
   }
   
   public String getParameterName()
   {
      return parameterName;
   }
   
   public String getInitialConversationId(Map parameters)
   {
      String id = getRequestConversationId(parameters);
      return id==null ? getConversationId() : id;
   }
   
   public String getRequestConversationId(Map parameters)
   {
      String value = ConversationPropagation.getRequestParameterValue(parameters, parameterName);
      if (value==null)
      {
         return null;
      }
      else
      {
         return name + ':' + value;
      }
   }
   
   public String getConversationId()
   {
      return name + ':' + getParameterValue();
   }

   public String getParameterValue()
   {
      Object value = vb.getValue();
      if (value==null)
      {
         throw new IllegalStateException("conversation id evaluated to null: " + name);
      }
      else
      {
         //TODO: use a JSF converter!
         return vb.getValue().toString();
      }
   }
   
   public String getParameterValue(String value)
   {
      return value.startsWith(name + ":") ? value.substring(name.length() + 1) : value;
   }
   
}
