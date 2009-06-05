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

import org.jboss.kernel.Kernel;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * Kernel instance holder.
 * 
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class KernelLocator
{
    private static KernelLocator instance;
    private static LogProvider log = Logging.getLogProvider(KernelLocator.class);

    private Kernel kernel;

    private KernelLocator() {}

    public static KernelLocator getInstance()
    {
        if (instance == null)
        {
            if (log.isTraceEnabled())
            {
                log.trace("Instantiating new KernelLocator.");
            }
            instance = new KernelLocator();
        }
        return instance;
    }

    public Kernel getKernel()
    {
        return kernel;
    }

    public void setKernel(Kernel kernel)
    {
        if (log.isTraceEnabled())
        {
            log.trace("Setting the current Kernel instance: " + kernel);
        }
        this.kernel = kernel;
    }
}
