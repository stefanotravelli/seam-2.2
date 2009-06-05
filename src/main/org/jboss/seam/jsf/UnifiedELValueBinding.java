package org.jboss.seam.jsf;

import java.io.Serializable;

import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.ValueBinding;

/**
 * Nobody should be using ValueBinding anymore, but if they 
 * are, we need this.
 * 
 * @author Gavin King
 *
 */
@SuppressWarnings("deprecation")
@Deprecated
public class UnifiedELValueBinding extends ValueBinding implements Serializable
{
   private transient ValueExpression valueExpression;
   
   private String expressionString;

   public UnifiedELValueBinding(String expressionString)
   {
      this.expressionString = expressionString;
   }

   public UnifiedELValueBinding() {}
   
   @Override
   public String getExpressionString()
   {
      return expressionString;
   }

   @Override
   public Class getType(FacesContext ctx) throws EvaluationException, PropertyNotFoundException {
      return getValueExpression(ctx).getType( ctx.getELContext() );
   }

   @Override
   public Object getValue(FacesContext ctx) throws EvaluationException, PropertyNotFoundException {
   	return getValueExpression(ctx).getValue( ctx.getELContext() );
   }

   @Override
   public boolean isReadOnly(FacesContext ctx) throws EvaluationException, PropertyNotFoundException {
   	return getValueExpression(ctx).isReadOnly( ctx.getELContext() );
   }

   @Override
   public void setValue(FacesContext ctx, Object value) throws EvaluationException, PropertyNotFoundException {
      getValueExpression(ctx).setValue( ctx.getELContext(), value);
   }
   
   @Override
   public String toString()
   {
      return getExpressionString();
   }

   private ValueExpression getValueExpression(FacesContext ctx)
   {
      if (valueExpression==null)
      {
         valueExpression = ctx.getApplication().getExpressionFactory()
                  .createValueExpression( ctx.getELContext(), expressionString, Object.class );
      }
      return valueExpression;
   }
}