package org.jboss.seam.core;

import static org.jboss.seam.ScopeType.STATELESS;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.text.MessageFormat;
import java.util.StringTokenizer;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * Interpolates EL expressions in Strings
 * 
 * @author Gavin King
 */
@BypassInterceptors
@Scope(STATELESS)
@Name("org.jboss.seam.core.interpolator")
@Install(precedence = BUILT_IN)
public class Interpolator
{
   
   private static final LogProvider log = Logging.getLogProvider(Interpolator.class);
   
   public static Interpolator instance()
   {
      if (Contexts.isApplicationContextActive())
      {
         return (Interpolator) Component.getInstance(Interpolator.class, ScopeType.APPLICATION);
      }
      else
      {
         return new Interpolator(); // for unit testing
      }
   }
   
   /**
    * Replace all EL expressions in the form #{...} with their evaluated values.
    * 
    * @param string
    *           a template
    * @return the interpolated string
    */
   public String interpolate(String string, Object... params)
   {
      if (params == null)
      {
         params = new Object[0];
      }
      
      if (params.length > 10)
      {
         throw new IllegalArgumentException("more than 10 parameters");
      }
      
      if (string.indexOf('#') >= 0 || string.indexOf('{') >= 0)
      {
         string = interpolateExpressions(string, params);
      }
      
      return string;
   }
   
   private String interpolateExpressions(String string, Object... params)
   {
      StringTokenizer tokens = new StringTokenizer(string, "#{}", true);
      StringBuilder builder = new StringBuilder(string.length());
      
      while (tokens.hasMoreTokens())
      {
         String tok = tokens.nextToken();
         
         if ("#".equals(tok) && tokens.hasMoreTokens())
         {
            String nextTok = tokens.nextToken();
            
            while (nextTok.equals("#") && tokens.hasMoreTokens())
            {
               builder.append(tok);
               nextTok = tokens.nextToken();
            }
            
            if ("{".equals(nextTok))
            {
               String expression = "#{" + tokens.nextToken() + "}";
               try
               {
                  Object value = Expressions.instance().createValueExpression(expression).getValue();
                  if (value != null)
                     builder.append(value);
               }
               catch (Exception e)
               {
                  log.debug("exception interpolating string: " + string, e);
               }
               tokens.nextToken(); // the trailing "}"
               
            }
            else if (nextTok.equals("#"))
            {
               // could be trailing #
               builder.append("#");
               
            }
            else
            {
               int index;
               try
               {
                  index = Integer.parseInt(nextTok.substring(0, 1));
                  if (index >= params.length)
                  {
                     // log.warn("parameter index out of bounds: " + index +
                     // " in: " + string);
                     builder.append("#").append(nextTok);
                  }
                  else
                  {
                     builder.append(params[index]).append(nextTok.substring(1));
                  }
               }
               catch (NumberFormatException nfe)
               {
                  builder.append("#").append(nextTok);
               }
            }
         }
         else if ("{".equals(tok))
         {
            StringBuilder expr = new StringBuilder();
            
            expr.append(tok);
            int level = 1;
            
            while (tokens.hasMoreTokens())
            {
               String nextTok = tokens.nextToken();
               expr.append(nextTok);
               
               if (nextTok.equals("{"))
               {
                  ++level;
               }
               else if (nextTok.equals("}"))
               {
                  if (--level == 0)
                  {
                     try
                     {
                        if (params.length == 0)
                        {
                           builder.append(expr.toString());
                        }
                        else
                        {
                           String value = new MessageFormat(expr.toString(), Locale.instance()).format(params);
                           builder.append(value);
                        }
                     }
                     catch (Exception e)
                     {
                        // if it is a bad message, use the expression itself
                        builder.append(expr);
                     }
                     expr = null;
                     break;
                  }
               }
            }
            
            if (expr != null)
            {
               builder.append(expr);
            }
         }
         else
         {
            builder.append(tok);
         }
      }
      
      return builder.toString();
   }
   
}
