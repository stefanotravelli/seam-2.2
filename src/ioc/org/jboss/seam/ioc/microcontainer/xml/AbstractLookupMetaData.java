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
package org.jboss.seam.ioc.microcontainer.xml;

import org.jboss.beans.metadata.plugins.AbstractDependencyValueMetaData;
import org.jboss.beans.metadata.spi.MetaDataVisitor;
import org.jboss.dependency.plugins.AbstractDependencyItem;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.reflect.spi.TypeInfo;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.util.JBossStringBuilder;

/**
 * Injecting Seam components into MC beans.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class AbstractLookupMetaData extends AbstractDependencyValueMetaData
{
    protected ScopeType scope;
    protected Boolean create = Boolean.TRUE;

    @Override
    public void initialVisit(MetaDataVisitor visitor)
    {
       // update - context is member field
       ControllerContext context = visitor.getControllerContext();
       Object name = context.getName();
       Object iDependOn = getUnderlyingValue();
       ControllerState whenRequired = null; //whenRequiredState; // update
       if (whenRequired == null)
       {
          whenRequired = visitor.getContextState();
       }

       DependencyItem item = new ComponentDependencyItem(name, iDependOn, whenRequired);
       visitor.addDependency(item);

//       visitor.initialVisit(this);
    }

    @Override
    public Object getValue(TypeInfo typeInfo, ClassLoader classLoader) throws Throwable
    {
        Object component;
        if (scope != null)
        {
           component = Component.getInstance((String)value, scope, create);
        }
        else
        {
           component = Component.getInstance((String)value, create);
        }
        if (property != null && component != null)
        {
/*
            // uncomment once we update MC
            KernelController controller = ((KernelController)context.getController());
            KernelConfigurator configurator = controller.getKernel().getConfigurator();
            BeanInfo beanInfo = configurator.getBeanInfo(component.getClass());
            component = beanInfo.getProperty(component, property);
*/
        }
        if (component != null && typeInfo != null)
        {
            // change to new TypeInfo.isAssignable after update
            if (typeInfo.getType().isAssignableFrom(component.getClass()) == false)
                throw new IllegalArgumentException("Illegal component class type: " + this + ", expected: " + typeInfo);
        }
        return component;
    }

    public ScopeType getScope()
    {
        return scope;
    }

    public void setScope(ScopeType scope)
    {
        this.scope = scope;
    }

    public Boolean getCreate()
    {
        return create;
    }

    public void setCreate(Boolean create)
    {
        this.create = create;
    }

    @Override
    public void toString(JBossStringBuilder buffer)
    {
       super.toString(buffer);
       if (scope != null)
          buffer.append(" scope=").append(scope);
       if (create != null)
          buffer.append(" create=").append(create);
    }

    private class ComponentDependencyItem extends AbstractDependencyItem
    {
        public ComponentDependencyItem(Object name, Object iDependOn, ControllerState whenRequied)
        {
            super(name, iDependOn, whenRequied, dependentState);
        }

        @Override
        public boolean resolve(Controller controller)
        {
            setResolved(Component.getInstance((String)getIDependOn()) != null);
            return isResolved();
        }
    }

}
