package org.jboss.seam.navigation;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import org.jboss.seam.core.Expressions.ValueExpression;

public final class Navigation
{
   private ValueExpression<Object> outcome;
   private List<Rule> rules = new ArrayList<Rule>();
   private Rule rule;
   
   public List<Rule> getRules()
   {
      return rules;
   }
   
   public void setOutcome(ValueExpression<Object> outcomeValueExpression)
   {
      this.outcome = outcomeValueExpression;
   }
   
   public ValueExpression<Object> getOutcome()
   {
      return outcome;
   }

   public Rule getRule()
   {
      return rule;
   }

   public void setRule(Rule rule)
   {
      this.rule = rule;
   }

   public boolean navigate(FacesContext context, final String actionOutcomeValue)
   {
      String outcomeValue;
      if ( getOutcome()==null )
      {
         outcomeValue = actionOutcomeValue;
      }
      else
      {
         Object value = getOutcome().getValue();
         outcomeValue = value==null ? null : value.toString();
      }
      
      for ( Rule rule: getRules() )
      {
         if ( rule.matches(outcomeValue) )
         {
            return rule.execute(context);
         }
      }
      
      return getRule().execute(context);
   }

}