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

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.jboss.kernel.plugins.deployment.xml.DependencyHandler;
import org.jboss.seam.ScopeType;
import org.jboss.xb.binding.sunday.unmarshalling.ElementBinding;
import org.xml.sax.Attributes;

/**
 * Seam component lookup handler.
 * Handling scope and create attribute.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class LookupHandler extends DependencyHandler
{
    public static final LookupHandler HANDLER = new LookupHandler();

    @Override
    public Object startElement(Object object, QName qName, ElementBinding elementBinding)
    {
        return new AbstractLookupMetaData();
    }

    @Override
    public void attributes(Object object, QName qName, ElementBinding elementBinding, Attributes attrs, NamespaceContext namespaceContext)
    {
        super.attributes(object, qName, elementBinding, attrs, namespaceContext);
        AbstractLookupMetaData lookup = (AbstractLookupMetaData) object;
        for (int i = 0; i < attrs.getLength(); ++i)
        {
           String localName = attrs.getLocalName(i);
           if ("scope".equals(localName))
              lookup.setScope(ScopeType.valueOf(attrs.getValue(i)));
           else if ("create".equals(localName))
              lookup.setCreate(Boolean.parseBoolean(attrs.getValue(i)));
        }
    }
}
