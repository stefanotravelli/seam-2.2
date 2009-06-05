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

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.io.Serializable;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.system.ServiceControllerMBean;

/**
 * Notifies Seam components in current underlying Microcontainer Controller.
 * It adds new MBean into the underlying MBeanServer.
 * MC components need to depend on the actual ObjectName.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
@BypassInterceptors
@Startup
@Install(value = false, precedence = FRAMEWORK)
public class JMXNotificationComponent extends ControllerNotificationComponent implements JMXNotificationComponentMBean, Serializable
{
    /**
     * The serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    private ObjectName objectName;

    protected ObjectName createObjectName(Component component) throws Exception
    {
        return new ObjectName("seam:service=" + getClass().getSimpleName() + "." + component.getName());
    }

    @Override
    protected void notifyController(Component component) throws Throwable
    {
        objectName = createObjectName(component);
        handleJMXRegistration(true);
    }
    
    @Override
    protected void clearNotification() throws Throwable
    {
        handleJMXRegistration(false);
        objectName = null;
    }

    public void removeComponents() throws Throwable
    {
        clearNotification();
    }

    protected void handleJMXRegistration(boolean register) throws Exception
    {
        MBeanServer server = MBeanServerLocator.locateJBoss();
        if (server == null)
            throw new IllegalArgumentException("Not running in JBoss app. server [currently only supporting]");
        ServiceControllerMBean serviceController = (ServiceControllerMBean)MBeanProxyExt.create(ServiceControllerMBean.class, ServiceControllerMBean.OBJECT_NAME);
        if (register)
        {
            server.registerMBean(this, objectName);
            // we want it to be installed
            serviceController.start(objectName);
        }
        else if (objectName != null)
        {
            // destroy it
            serviceController.destroy(objectName);
            serviceController.remove(objectName);
        }
    }

}
