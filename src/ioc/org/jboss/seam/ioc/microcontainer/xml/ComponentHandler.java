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

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.jboss.beans.metadata.plugins.AbstractAnnotationMetaData;
import org.jboss.beans.metadata.spi.AnnotationMetaData;
import org.jboss.kernel.plugins.deployment.xml.BeanHandler;
import org.jboss.seam.ScopeType;
import org.jboss.xb.binding.sunday.unmarshalling.ElementBinding;
import org.xml.sax.Attributes;

/**
 * Component handler.
 * Adding Seam annotations - @Name and @Scope.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class ComponentHandler extends BeanHandler
{
    public static final ComponentHandler HANDLER = new ComponentHandler();

    @Override
    public Object startElement(Object object, QName qName, ElementBinding elementBinding)
    {
        return new AbstractComponentMetaData();
    }

    @Override
    public void attributes(Object object, QName qName, ElementBinding elementBinding, Attributes attrs, NamespaceContext namespaceContext)
    {
        super.attributes(object, qName, elementBinding, attrs, namespaceContext);
        AbstractComponentMetaData component = (AbstractComponentMetaData)object;
        for (int i = 0; i < attrs.getLength(); ++i)
        {
            String localName = attrs.getLocalName(i);
            if ("scope".equals(localName))
                component.setScope(ScopeType.valueOf(attrs.getValue(i)));
        }
    }

    @Override
    public Object endElement(Object object, QName qName, ElementBinding elementBinding)
    {
        AbstractComponentMetaData component = (AbstractComponentMetaData)super.endElement(object, qName, elementBinding);
        Set<AnnotationMetaData> annotations = component.getAnnotations();
        if (annotations == null)
        {
            annotations = new HashSet<AnnotationMetaData>();
            component.setAnnotations(annotations);
        }
        AbstractAnnotationMetaData nameAnnotation = new AbstractAnnotationMetaData();
        // update nameAnnotation.setAnnotation("@" + Name.class + "(" + component.getBean() + ")");
        annotations.add(nameAnnotation);
        if (component.getScope() != null)
        {
            AbstractAnnotationMetaData scopeAnnotation = new AbstractAnnotationMetaData();
            // update scopeAnnotation.setAnnotation("@" + Scope.class + "(" + component.getScope() + ")");
            annotations.add(scopeAnnotation);
        }
        return component;
    }
}
