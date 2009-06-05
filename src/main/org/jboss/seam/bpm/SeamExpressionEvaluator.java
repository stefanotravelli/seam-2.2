package org.jboss.seam.bpm;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.MethodNotFoundException;
import javax.el.PropertyNotFoundException;
import javax.el.ValueExpression;

import org.jboss.seam.el.EL;
import org.jboss.seam.el.SeamFunctionMapper;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jbpm.jpdl.el.ELException;
import org.jbpm.jpdl.el.Expression;
import org.jbpm.jpdl.el.ExpressionEvaluator;
import org.jbpm.jpdl.el.FunctionMapper;
import org.jbpm.jpdl.el.VariableResolver;

/**
 * Plugs the JBoss EL expression language and Seam
 * EL resolvers into jBPM. Note that this current 
 * implementation does not allow jBPM to see stuff
 * defined only by the JSF ELResolvers.
 * 
 * @author Gavin King
 * @author Pete Muir
 *
 */
public class SeamExpressionEvaluator 
    extends ExpressionEvaluator
{
   
    private static LogProvider log = Logging.getLogProvider(SeamExpressionEvaluator.class);

    @Override
    public Object evaluate(String expression, Class returnType, final VariableResolver resolver, FunctionMapper mapper)
        throws ELException
    {
        return createExpression(expression, returnType, mapper).evaluate(resolver);
    }
    
    @Override
    public Expression parseExpression(final String expression, final Class returnType, FunctionMapper mapper)
        throws ELException
    {
        return createExpression(expression, returnType, mapper);
    }
    
    private static Expression createExpression(final String expression, final Class returnType, final FunctionMapper mapper)
    {
        
        return new Expression() 
        {
            private ELContext elContext = EL.createELContext();

            private MethodExpression me;
            private ValueExpression ve;
                
            private void initMethodExpression() 
            {
                if (me == null || ve == null)
                {
                    me = EL.EXPRESSION_FACTORY.createMethodExpression(elContext, expression, returnType, new Class[0]);
                }
            }
                
            private void initValueExpression() 
            {
                if (me == null || ve == null)
                {
                    ve = EL.EXPRESSION_FACTORY.createValueExpression(elContext, expression, returnType);
                }
            }
                
            @Override
            public Object evaluate(VariableResolver resolver) throws ELException
            {
                List<javax.el.ELException> exceptions = new ArrayList<javax.el.ELException>();
                try
                {
                    initMethodExpression(); 
                    if (me != null)
                    {
                        try
                        {
                            return me.invoke(createELContext(resolver, mapper), new Object[0]);
                        }
                        catch (MethodNotFoundException e)
                        {
                            exceptions.add(e);
                        }
                    }
                }
                catch (javax.el.ELException e) 
                {
                    exceptions.add(e);
                }
                 
                try
                {
                    initValueExpression();
                    if (ve != null)
                    {
                        try
                        {
                            return ve.getValue(createELContext(resolver, mapper));
                        }
                        catch (PropertyNotFoundException e)
                        {
                            exceptions.add(e);
                        }
                    }
                }
                catch (javax.el.ELException e)
                {
                    exceptions.add(e);
                }
                
                if (exceptions.size() == 1)
                {
                   throw new ELException("Error evaluating " + expression, exceptions.get(0));
                }
                else if (exceptions.size() > 1)
                {
                   log.debug("Exceptions occurred when parsing " + expression);
                   for (javax.el.ELException e : exceptions)
                   {
                      log.debug("Possible cause", e);
                   }
                }
                if (me == null && ve ==  null)
                {
                   log.debug("Error parsing " + expression);
                   throw new ELException("Error parsing " + expression + "; not a valid EL expression");
                }
                throw new ELException("Error evaluating " + expression + "; possible causes are logged at debug level");
            }
        };
    }
   
    private static javax.el.FunctionMapper decorateFunctionMapper(final FunctionMapper functionMapper)
    {
        return new SeamFunctionMapper( new javax.el.FunctionMapper() 
        {
            @Override
            public Method resolveFunction(String prefix, String localName)
            {
                return functionMapper.resolveFunction(prefix, localName);
            }
        });
    }
    
    private static ELContext createELContext(VariableResolver resolver, FunctionMapper functionMapper)
    {
        CompositeELResolver composite = new CompositeELResolver();
        composite.add(EL.EL_RESOLVER);
        composite.add( new JbpmELResolver(resolver) );
        return EL.createELContext(composite, decorateFunctionMapper(functionMapper));
    }
    
}
