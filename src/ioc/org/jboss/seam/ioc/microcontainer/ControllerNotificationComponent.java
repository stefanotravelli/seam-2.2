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

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

/**
 * Notifies Seam components in current underlying Microcontainer Controller.
 * Meaning that ServletContext is available to register MC beans as Seam components
 * and MC beans can lookup Seam components.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
@Scope(ScopeType.APPLICATION)
public abstract class ControllerNotificationComponent implements Serializable
{
    /** The serialVersionUID */
    private static final long serialVersionUID = 1L;

    @Logger
    protected Log log;

    @Create
    public void create(Component component) throws Throwable
    {
        if (log.isDebugEnabled())
        {
            log.debug("Creating notification MC component ...");
        }
        try
        {
            notifyController(component);
        }
        catch (Throwable t)
        {
            throw new IllegalArgumentException("Exception installing ControllerNotificationComponent: " + t, t);
        }
    }

    /**
     * Creates notification to the underlying controller
     *
     * @param component this component instance
     * @throws Throwable throw throwable if unable to notify underlying controller
     */
    protected abstract void notifyController(Component component) throws Throwable;

    /**
     * Remove 'work' with creating notification
     *
     * @throws Throwable exception during notification cleanup
     */
    protected abstract void clearNotification() throws Throwable;

    @Destroy
    public void destroy()
    {
        try
        {
            clearNotification();
            if (log.isDebugEnabled())
            {
                log.debug("Notification MC component destroyed ...");
            }
        }
        catch (Throwable t)
        {
            log.warn("Exception while clearing previous notification: " + t);
        }
    }

}
