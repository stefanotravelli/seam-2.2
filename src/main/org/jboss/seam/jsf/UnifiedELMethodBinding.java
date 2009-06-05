package org.jboss.seam.jsf;

import java.io.Serializable;

import javax.el.MethodExpression;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.MethodNotFoundException;

/**
 * Nobody should be using MethodBinding anymore, but if they 
 * are, we need this.
 * 
 * @author Gavin King
 *
 */
@SuppressWarnings("deprecation")
@Deprecated
public class UnifiedELMethodBinding extends MethodBinding implements Serializable
{
   private transient MethodExpression methodExpression;
   
   private String expressionString;
   private Class[] argTypes;

   public UnifiedELMethodBinding() {}
   
   public UnifiedELMethodBinding(String expressionString, Class[] argTypes)
   {
      this.expressionString = expressionString;
      this.argTypes = argTypes;
   }

   @Override
   public String getExpressionString()
   {
      return expressionString;
   }

   @Override
   public Class getType(FacesContext ctx) throws MethodNotFoundException
   {
      return getMethodExpression(ctx).getMethodInfo( ctx.getELContext() ).getReturnType();
   }

   @Override
   public Object invoke(FacesContext ctx, Object[] args) throws EvaluationException, MethodNotFoundException
   {
      return getMethodExpression(ctx).invoke( ctx.getELContext(), args);
   }

   @Override
   public String toString()
   {
      return getExpressionString();
   }

   private MethodExpression getMethodExpression(FacesContext ctx)
   {
      if (methodExpression==null)
      {
         // In JSF 1.1 EL (argTypes = null) == (argTypes = new Class[0]), but not in Unified EL
         methodExpression = ctx.getApplication().getExpressionFactory()
                  .createMethodExpression( ctx.getELContext(), expressionString, Object.class, argTypes == null ? new Class[0] : argTypes );
      }
      return methodExpression;
   }
}