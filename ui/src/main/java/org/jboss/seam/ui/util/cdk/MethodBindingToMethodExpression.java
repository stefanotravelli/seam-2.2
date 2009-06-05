/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.seam.ui.util.cdk;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.el.MethodNotFoundException;
import javax.el.PropertyNotFoundException;
import javax.faces.FacesException;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;

/**
 * Converts a MethodBinding to a MethodExpression
 * 
 * 
 * @author Stan Silvert
 * @author Pete Muir
 */
@SuppressWarnings("deprecation")
public class MethodBindingToMethodExpression extends MethodExpression implements StateHolder
{
    private static final Class[] EXPECTED_TYPES = new Class[] { MethodBinding.class, StateHolder.class };

    private MethodBinding methodBinding;

    private boolean _transientFlag;

    private transient MethodInfo methodInfo;

    /**
     * No-arg constructor used during restoreState
     */
    protected MethodBindingToMethodExpression()
    {
    }

    /** Creates a new instance of MethodBindingToMethodExpression */
    public MethodBindingToMethodExpression(MethodBinding methodBinding)
    {
        checkNullArgument(methodBinding, "methodBinding");
        this.methodBinding = methodBinding;
    }

    /**
     * Return the wrapped MethodBinding.
     */
    public MethodBinding getMethodBinding()
    {
        return methodBinding;
    }
    
    void setMethodBinding(MethodBinding methodBinding)
    {
        this.methodBinding = methodBinding;
    }

    /**
     * Note: MethodInfo.getParamTypes() may incorrectly return an empty class array if invoke() has not been called.
     * 
     * @throws IllegalStateException
     *             if expected params types have not been determined.
     */
    @Override
   public MethodInfo getMethodInfo(ELContext context) throws PropertyNotFoundException, MethodNotFoundException,
            ELException
    {
        checkNullArgument(context, "elcontext");
        checkNullState(methodBinding, "methodBinding");

        if (methodInfo == null)
        {
            final FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
            if (facesContext != null)
            {
                methodInfo = invoke(new Invoker<MethodInfo>()
                {
                    public MethodInfo invoke()
                    {
                        return new MethodInfo(null, methodBinding.getType(facesContext), null);
                    }
                });
            }
        }
        return methodInfo;
    }

    @Override
   public Object invoke(ELContext context, final Object[] params) throws PropertyNotFoundException,
            MethodNotFoundException, ELException
    {
        checkNullArgument(context, "elcontext");
        checkNullState(methodBinding, "methodBinding");
        final FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        if (facesContext != null)
        {
            return invoke(new Invoker<Object>()
            {
                public Object invoke()
                {
                    return methodBinding.invoke(facesContext, params);
                }
            });
        }
        return null;
    }

    @Override
   public boolean isLiteralText()
    {
        if (methodBinding == null)
            throw new IllegalStateException("methodBinding is null");
        String expr = methodBinding.getExpressionString();
        return !(expr.startsWith("#{") && expr.endsWith("}"));
    }

    @Override
   public String getExpressionString()
    {
        return methodBinding.getExpressionString();
    }

    public Object saveState(FacesContext context)
    {
        if (!isTransient())
        {
            if (methodBinding instanceof StateHolder)
            {
                Object[] state = new Object[2];
                state[0] = methodBinding.getClass().getName();
                state[1] = ((StateHolder) methodBinding).saveState(context);
                return state;
            }
            else
            {
                return methodBinding;
            }
        }
        return null;
    }

    public void restoreState(FacesContext context, Object state)
    {
        if (state instanceof MethodBinding)
        {
            methodBinding = (MethodBinding) state;
            methodInfo = null;
        }
        else if (state != null)
        {
            Object[] values = (Object[]) state;
            methodBinding = (MethodBinding) newInstance(values[0].toString(), EXPECTED_TYPES);
            ((StateHolder) methodBinding).restoreState(context, values[1]);
            methodInfo = null;
        }
    }

    public void setTransient(boolean transientFlag)
    {
        _transientFlag = transientFlag;
    }

    public boolean isTransient()
    {
        return _transientFlag;
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((methodBinding == null) ? 0 : methodBinding.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MethodBindingToMethodExpression other = (MethodBindingToMethodExpression) obj;
        if (methodBinding == null)
        {
            if (other.methodBinding != null)
                return false;
        }
        else if (!methodBinding.equals(other.methodBinding))
            return false;
        return true;
    }

    private void checkNullState(Object notNullInstance, String instanceName)
    {
        if (notNullInstance == null)
            throw new IllegalStateException(instanceName + " is null");
    }

    private void checkNullArgument(Object notNullInstance, String instanceName)
    {
        if (notNullInstance == null)
            throw new IllegalArgumentException(instanceName + " is null");
    }

    private <T> T invoke(Invoker<T> invoker)
    {
        try
        {
            return invoker.invoke();
        }
        catch (javax.faces.el.MethodNotFoundException e)
        {
            throw new MethodNotFoundException(e.getMessage(), e);
        }
        catch (EvaluationException e)
        {
            throw new ELException(e.getMessage(), e);
        }
    }

    private interface Invoker<T>
    {
        T invoke();
    }
    
    private static Object newInstance(String type, Class[] expectedTypes)
    {
        if (type == null)
            return null;        
        
        Class clazzForName = simpleClassForName(type);
        
        if(expectedTypes != null)
        {
            for (int i = 0, size = expectedTypes.length; i < size; i++)
            {
                if (!expectedTypes[i].isAssignableFrom(clazzForName))
                {
                    throw new FacesException("'" + type + "' does not implement expected type '" + expectedTypes[i]
                            + "'");
                }
            }
        }
        
        return newInstance(clazzForName);
    }

    private static Object newInstance(Class clazz)
        throws FacesException
    {
        try
        {
            return clazz.newInstance();
        }
        catch(NoClassDefFoundError e)
        {
            throw new FacesException(e);
        }
        catch (InstantiationException e)
        {
            throw new FacesException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new FacesException(e);
        }
    }
    
    /**
     * Tries a Class.loadClass with the context class loader of the current thread first and
     * automatically falls back to the ClassUtils class loader (i.e. the loader of the
     * myfaces.jar lib) if necessary.
     *
     * @param type fully qualified name of a non-primitive non-array class
     * @return the corresponding Class
     * @throws NullPointerException if type is null
     * @throws ClassNotFoundException
     */
    private static Class classForName(String type)
        throws ClassNotFoundException
    {
        if (type == null) throw new NullPointerException("type");
        try
        {
            // Try WebApp ClassLoader first
            return Class.forName(type,
                                 false, // do not initialize for faster startup
                                 Thread.currentThread().getContextClassLoader());
        }
        catch (ClassNotFoundException ignore)
        {
            // fallback: Try ClassLoader for ClassUtils (i.e. the myfaces.jar lib)
            return Class.forName(type,
                                 false, // do not initialize for faster startup
                                 MethodBindingToMethodExpression.class.getClassLoader());
        }
    }


    /**
     * Same as {@link #classForName(String)}, but throws a RuntimeException
     * (FacesException) instead of a ClassNotFoundException.
     *
     * @return the corresponding Class
     * @throws NullPointerException if type is null
     * @throws FacesException if class not found
     */
    private static Class simpleClassForName(String type)
    {
        try
        {
            return classForName(type);
        }
        catch (ClassNotFoundException e)
        {
            throw new FacesException(e);
        }
    }



}