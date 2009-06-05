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

import java.io.Serializable;

import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * Notifies Seam components in current underlying Microcontainer Controller.
 * It adds component to the underlying controller with PojoNotificationComponent.<component_name> name.
 * Other Seam components need to depend on this name.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
@BypassInterceptors
@Startup
@Install(value = false, precedence = Install.FRAMEWORK)
public class PojoNotificationComponent extends ControllerNotificationComponent implements Serializable
{
    /**
     * The serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    private String componentName;

    protected Kernel getKernel()
    {
        Kernel kernel = KernelLocator.getInstance().getKernel();
        if (kernel == null)
            throw new IllegalArgumentException("Kernel instance is null, add KernelLocator to -beans.xml!");
        return kernel;
    }

    @Override
    protected void notifyController(Component component) throws Throwable
    {
        Class<PojoNotificationComponent> clazz = PojoNotificationComponent.class;
        componentName = clazz.getSimpleName() + "." + component.getName();
        KernelController controller = getKernel().getController();
        AbstractBeanMetaData beanMetaData = new AbstractBeanMetaData(componentName, clazz.getName());
        controller.install(beanMetaData, this);
    }

    @Override
    protected void clearNotification() throws Throwable
    {
        KernelController controller = getKernel().getController();
        controller.uninstall(componentName);
        componentName = null;
    }

}
