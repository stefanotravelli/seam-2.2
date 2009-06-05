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
package org.jboss.seam.ioc.microcontainer;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.init.Initialization;

/**
 * A Seam binding aspect that creates bindings on interception of the kernel setKernelControllerContext
 * callback, and removes them on any other method. The expectation is that this is applied to the
 * org.jboss.kernel.spi.dependency.KernelControllerContextAware interface so that unbinding occurs on
 * the unsetKernelControllerContext method.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class SeamIntroduction implements Interceptor
{
    public String getName()
    {
        return getClass().getName();
    }

    public Object invoke(Invocation invocation) throws Throwable
    {
        MethodInvocation mi = (MethodInvocation)invocation;
        KernelControllerContext context = (KernelControllerContext)mi.getArguments()[0];
        Name name = (Name)invocation.resolveClassAnnotation(Name.class);
        Scope scope = (Scope)invocation.resolveClassAnnotation(Scope.class);
        ScopeType scopeType = scope != null ? scope.value() : null;
        if ("setKernelControllerContext".equals(mi.getMethod().getName()) && name != null)
        {
            Object target = context.getTarget();
            boolean unmockApplication = false;
            if (!Contexts.isApplicationContextActive())
            {
                Lifecycle.mockApplication();
                unmockApplication = true;
            }
            try
            {
                Contexts.getApplicationContext().set(
                        name + Initialization.COMPONENT_SUFFIX,
                        new MicrocontainerComponent(target.getClass(), name.value(), scopeType, context.getController())
                );
            }
            finally
            {
                if (unmockApplication)
                {
                    Lifecycle.unmockApplication();
                }
            }
        }
        else if (name != null && scopeType != null)
        {
            scopeType.getContext().remove(name.value());
        }
        return null;
    }

}
