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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.kernel.plugins.registry.AbstractKernelRegistryEntry;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;
import org.jboss.kernel.spi.registry.KernelRegistryPlugin;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;

/**
 * Do a Seam component lookup for MC beans.
 * Lookup name can be a class or simple string.
 * With string you can determine scopeType and create param:
 * * name=somename;create=false;scope=SESSION
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 * @see org.jboss.seam.ioc.microcontainer.xml.LookupHandler
 * @see org.jboss.seam.ioc.microcontainer.xml.AbstractLookupMetaData
 */
public class SeamComponentRegistryPlugin implements KernelRegistryPlugin
{
    private static Pattern SCOPE = Pattern.compile(";scope=([a-zA-Z]+)");
    private static Pattern CREATE = Pattern.compile(";create=(true|false)");

    public KernelRegistryEntry getEntry(Object object)
    {
        String componentName = object.toString();
        Object component;
        if (object instanceof Class)
        {
            component = Component.getInstance((Class<?>)object);
        }
        else
        {
            // scope and create
            Boolean create = parseCreate(componentName);
            ScopeType scopeType = parseScopeType(componentName);
            // shorter name?
            int p = componentName.indexOf(";");
            if (p > 0)
                componentName = componentName.substring(p);
            // register component
            if (scopeType != null)
            {
                component = Component.getInstance(componentName, scopeType, create);
            }
            else
            {
                component = Component.getInstance(componentName, create);
            }
        }
        return new AbstractKernelRegistryEntry(componentName, component);
    }

    protected static Boolean parseCreate(String name)
    {
        Boolean create = Boolean.TRUE;
        Matcher createMatcher = CREATE.matcher(name);
        if (createMatcher.find())
        {
            create = Boolean.parseBoolean(createMatcher.group(1));
        }
        return create;
    }

    protected static ScopeType parseScopeType(String name)
    {
        ScopeType scopeType = null;
        Matcher scopeMatcher = SCOPE.matcher(name);
        if (scopeMatcher.find())
        {
            scopeType = ScopeType.valueOf(scopeMatcher.group(1));
        }
        return scopeType;
    }

}
