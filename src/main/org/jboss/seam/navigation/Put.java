package org.jboss.seam.navigation;

import org.jboss.seam.ScopeType;
import org.jboss.seam.core.Expressions.ValueExpression;

public class Put
{
   private String name;
   private ScopeType scope;
   private ValueExpression value;

   public String getName()
   {
      return name;
   }
   public void setName(String name)
   {
      this.name = name;
   }
   public ScopeType getScope()
   {
      return scope;
   }
   public void setScope(ScopeType scope)
   {
      this.scope = scope;
   }
   public ValueExpression getValue()
   {
      return value;
   }
   public void setValue(ValueExpression value)
   {
      this.value = value;
   }
   
}
