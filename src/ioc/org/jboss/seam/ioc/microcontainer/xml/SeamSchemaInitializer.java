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

import javax.xml.namespace.QName;

import org.jboss.kernel.plugins.deployment.xml.BeanAnnotationInterceptor;
import org.jboss.kernel.plugins.deployment.xml.BeanClassLoaderInterceptor;
import org.jboss.kernel.plugins.deployment.xml.BeanConstructorInterceptor;
import org.jboss.kernel.plugins.deployment.xml.BeanCreateInterceptor;
import org.jboss.kernel.plugins.deployment.xml.BeanDemandsInterceptor;
import org.jboss.kernel.plugins.deployment.xml.BeanDependsInterceptor;
import org.jboss.kernel.plugins.deployment.xml.BeanDestroyInterceptor;
import org.jboss.kernel.plugins.deployment.xml.BeanInstallInterceptor;
import org.jboss.kernel.plugins.deployment.xml.BeanPropertyInterceptor;
import org.jboss.kernel.plugins.deployment.xml.BeanSchemaBinding20;
import org.jboss.kernel.plugins.deployment.xml.BeanStartInterceptor;
import org.jboss.kernel.plugins.deployment.xml.BeanStopInterceptor;
import org.jboss.kernel.plugins.deployment.xml.BeanSuppliesInterceptor;
import org.jboss.kernel.plugins.deployment.xml.BeanUninstallInterceptor;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingInitializer;
import org.jboss.xb.binding.sunday.unmarshalling.TypeBinding;

/**
 * Seam schema initializer.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class SeamSchemaInitializer implements SchemaBindingInitializer
{
    /** The namespace */
    public static final String SEAM_COMPONENT_NS = "urn:jboss:seam-components:1.0";

    /** The bean binding */
    public static final QName componentTypeQName = new QName(SEAM_COMPONENT_NS, "componentType");

    /** The bean binding */
    public static final QName lookupTypeQName = new QName(SEAM_COMPONENT_NS, "lookupType");

    public SchemaBinding init(SchemaBinding schema)
    {
        // remove replace in xml binding
        schema.setReplacePropertyRefs(false);
        // init component
        initComponent(schema);
        // init lookup
        initLookup(schema);
        return schema;
    }

    protected void initComponent(SchemaBinding schema)
    {
        TypeBinding componentType = schema.getType(componentTypeQName);
        // handler
        componentType.setHandler(ComponentHandler.HANDLER);
        // bean has a classloader
        componentType.pushInterceptor(BeanSchemaBinding20.classloaderQName, BeanClassLoaderInterceptor.INTERCEPTOR);
        // bean has a constructor
        componentType.pushInterceptor(BeanSchemaBinding20.constructorQName, BeanConstructorInterceptor.INTERCEPTOR);
        // bean has properties
        componentType.pushInterceptor(BeanSchemaBinding20.propertyQName, BeanPropertyInterceptor.INTERCEPTOR);
        // bean has a create
        componentType.pushInterceptor(BeanSchemaBinding20.createQName, BeanCreateInterceptor.INTERCEPTOR);
        // bean has a start
        componentType.pushInterceptor(BeanSchemaBinding20.startQName, BeanStartInterceptor.INTERCEPTOR);
        // bean has a stop
        componentType.pushInterceptor(BeanSchemaBinding20.stopQName, BeanStopInterceptor.INTERCEPTOR);
        // bean has a destroy
        componentType.pushInterceptor(BeanSchemaBinding20.destroyQName, BeanDestroyInterceptor.INTERCEPTOR);
        // bean has annotations
        componentType.pushInterceptor(BeanSchemaBinding20.annotationQName, BeanAnnotationInterceptor.INTERCEPTOR);
        // bean has installs
        componentType.pushInterceptor(BeanSchemaBinding20.installQName, BeanInstallInterceptor.INTERCEPTOR);
        // bean has uninstalls
        componentType.pushInterceptor(BeanSchemaBinding20.uninstallQName, BeanUninstallInterceptor.INTERCEPTOR);
        // bean has depends
        componentType.pushInterceptor(BeanSchemaBinding20.dependsQName, BeanDependsInterceptor.INTERCEPTOR);
        // bean has demands
        componentType.pushInterceptor(BeanSchemaBinding20.demandQName, BeanDemandsInterceptor.INTERCEPTOR);
        // bean has supplies
        componentType.pushInterceptor(BeanSchemaBinding20.supplyQName, BeanSuppliesInterceptor.INTERCEPTOR);
    }

    protected void initLookup(SchemaBinding schema)
    {
        TypeBinding lookuptype = schema.getType(lookupTypeQName);
        // handler
        lookuptype.setHandler(LookupHandler.HANDLER);
    }

}
