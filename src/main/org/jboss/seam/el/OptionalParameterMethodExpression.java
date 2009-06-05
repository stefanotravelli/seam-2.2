/**
 * 
 */
package org.jboss.seam.el;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.el.MethodNotFoundException;

/**
 * 
 * @author Gavin King
 *
 */
class OptionalParameterMethodExpression extends MethodExpression
{
    
    private MethodExpression withParam;
    private MethodExpression withNoParam;
    
   public OptionalParameterMethodExpression(MethodExpression withParam, MethodExpression withNoParam)
   {
      this.withParam = withParam;
      this.withNoParam = withNoParam;
   }

   @Override
   public MethodInfo getMethodInfo(ELContext ctx)
   {
      return withParam.getMethodInfo(ctx);
   }

   @Override
   public Object invoke(ELContext ctx, Object[] args)
   {
      try
      {
         return withParam.invoke(ctx, args);
      }
      catch (MethodNotFoundException mnfe)
      {
         try
         {
            return withNoParam.invoke(ctx, new Object[0]);
         }
         catch (MethodNotFoundException mnfe2)
         {
            throw mnfe;
         }
      }
   }

   @Override
   public String getExpressionString()
   {
      return withParam.getExpressionString();
   }

   @Override
   public boolean isLiteralText()
   {
      return withParam.isLiteralText();
   }

   @Override
   public boolean equals(Object object)
   {
      if ( !(object instanceof OptionalParameterMethodExpression) ) return false;
      OptionalParameterMethodExpression other = (OptionalParameterMethodExpression) object;
      return withParam.equals(other.withParam);
   }

   @Override
   public int hashCode()
   {
      return withParam.hashCode();
   }
    
 }