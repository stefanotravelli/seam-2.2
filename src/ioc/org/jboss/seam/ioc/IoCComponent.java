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
package org.jboss.seam.ioc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpSessionActivationListener;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.core.Mutable;
import org.jboss.seam.intercept.Proxy;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * An extension of Component that allows external IoC
 * to provide the base instance for a Seam component.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class IoCComponent extends Component
{
   protected final LogProvider log = Logging.getLogProvider(getClass());

   /**
    * Creates a Seam Component from other IoC container
    *
    * @param clazz   class
    * @param name    component name
    * @param scope   component scope
    */
   public IoCComponent(Class clazz, String name, ScopeType scope)
   {
      super(clazz, name, scope, false, new String[0], null);
   }

   protected abstract String getIoCName();

   protected abstract Object instantiateIoCBean() throws Exception;

   /**
    * Instantiates a IoC bean and provides it as a
    * java bean to be wrapped by Seam.
    *
    * @see org.jboss.seam.Component#instantiateJavaBean()
    */
   @Override
   protected Object instantiateJavaBean() throws Exception
   {
      Object bean = instantiateIoCBean();
       // initialize the bean following Component.instantiateJavaBean()'s
       // pattern.
       if ( !isInterceptionEnabled() )
       {
          initialize(bean);
           // Only call postConstruct if the bean is not stateless otherwise in the case of a singleton it wowuld be
           // called every time seam request the bean not just when it is created.
           if (getScope() != ScopeType.STATELESS)
           {
               callPostConstructMethod(bean);
           }
       }
       else if ( !(bean instanceof Proxy) )
       {
           // Add all of the interfaces of the bean instance into the Seam
           // proxy bean because spring's proxies add a bunch of interfaces too
           // that should be accessible.
           Set<Class> interfaces = new HashSet<Class>(Arrays.asList(bean.getClass().getInterfaces()));
           interfaces.add(HttpSessionActivationListener.class);
           interfaces.add(Mutable.class);
           interfaces.add(Proxy.class);
           // enhance bean
           bean = ProxyUtils.enhance(bean, interfaces, this);
       }
       return bean;
   }

}
