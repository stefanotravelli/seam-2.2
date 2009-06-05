package org.jboss.seam.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jboss.seam.core.Expressions.ValueExpression;

import org.jboss.seam.core.Expressions;

public class QueryParser
{
   private List<ValueExpression> parameterValueBindings = new ArrayList<ValueExpression>();
   private StringBuilder ejbqlBuilder;
   
   public static String getParameterName(int loc)
   {
      return "el" + (loc+1);
   }
   
   public String getEjbql()
   {
      return ejbqlBuilder.toString();
   }
   
   public List<ValueExpression> getParameterValueBindings()
   {
      return parameterValueBindings;
   }
   
   public QueryParser(String ejbql)
   {
      this(ejbql, 0);
   }
   
   public QueryParser(String ejbql, int startingParameterNumber)
   {
       StringTokenizer tokens = new StringTokenizer(ejbql, "#}", true);
       ejbqlBuilder = new StringBuilder(ejbql.length());
       while (tokens.hasMoreTokens()) {
           String token = tokens.nextToken();
           if ("#".equals(token) && tokens.hasMoreTokens()) {
               String expressionToken = tokens.nextToken();

               if (!expressionToken.startsWith("{") || !tokens.hasMoreTokens()) {
                   ejbqlBuilder.append(token).append(expressionToken);
               } else {
                   String expression = token + expressionToken + tokens.nextToken();
                   ejbqlBuilder.append(':').append( getParameterName( startingParameterNumber + parameterValueBindings.size() ) );
                   parameterValueBindings.add( Expressions.instance().createValueExpression(expression) );
               }    
           } else {
               ejbqlBuilder.append(token);
           }
       }
   }
   
}
