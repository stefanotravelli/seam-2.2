/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.seam.el;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;

import org.jboss.el.lang.EvaluationContext;
import org.jboss.seam.util.JSF;

/**
 * Allows JSF action listener methods to not declare the
 * totally useless ActionEvent parameter if they don't
 * want to. 
 *
 * @author Gavin King
 */
public class SeamExpressionFactory extends ExpressionFactory 
{
   public static final ExpressionFactory INSTANCE = new SeamExpressionFactory(EL.EXPRESSION_FACTORY);
   
    private static final Class[] NO_CLASSES = {};
    
    private final ExpressionFactory expressionFactory;
    
    SeamExpressionFactory(ExpressionFactory expressionFactory) 
    {
       this.expressionFactory = expressionFactory;
    }
    
    /**
     * Wrap the base ELContext, adding Seam's FunctionMapper.
     * 
     * Thus, any expressions with s:hasRole, s:hasPermission 
     * must be evaluated either via Facelets/JSP (since they
     * are declared in the tld/taglib.xml or via the 
     * Expressions component.
     * 
     * @param context the JSF ELContext
     */
    private static EvaluationContext decorateELContext(ELContext context)
    {
       return new EvaluationContext( context, new SeamFunctionMapper( context.getFunctionMapper() ), context.getVariableMapper() );
    }
    
    @Override
    public Object coerceToType(Object obj, Class targetType) 
    {
        return expressionFactory.coerceToType(obj, targetType);
    }

    @Override
    public MethodExpression createMethodExpression(ELContext elContext, String expression, Class returnType, Class[] paramTypes) 
    {
        if ( paramTypes.length==1 && JSF.FACES_EVENT.isAssignableFrom( paramTypes[0] ) )
        {
         return new OptionalParameterMethodExpression(
                 expressionFactory.createMethodExpression( decorateELContext(elContext), expression, returnType, paramTypes ),
                 expressionFactory.createMethodExpression( decorateELContext(elContext), expression, returnType, NO_CLASSES )
              );
        }
        else
        {
           return expressionFactory.createMethodExpression( decorateELContext(elContext), expression, returnType, paramTypes );
        }
    }
    
    @Override
    public ValueExpression createValueExpression(Object instance, Class expectedType) 
    {
        return expressionFactory.createValueExpression(instance, expectedType);
    }

    @Override
    public ValueExpression createValueExpression(ELContext elContext, String expression, Class expectedType) 
    {   
        return expressionFactory.createValueExpression( decorateELContext(elContext), expression, expectedType );
    }
    
}