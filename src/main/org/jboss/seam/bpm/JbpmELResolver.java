package org.jboss.seam.bpm;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;

import org.jbpm.jpdl.el.VariableResolver;

/**
 * Resolves jBPM variables for Unified EL
 * 
 * @author Gavin King
 *
 */
final class JbpmELResolver extends ELResolver
{
   private final VariableResolver resolver;

   JbpmELResolver(VariableResolver resolver)
   {
      this.resolver = resolver;
   }

   @Override
   public Object getValue(ELContext context, Object base, Object property) 
   {
      if ( base==null && property!=null )
      {         
         context.setPropertyResolved(true); 
         return resolver.resolveVariable( (String) property );
      }
      else
      {
         return null;
      }
   }

   @Override
   public boolean isReadOnly(ELContext context, Object base, Object property) 
   {
      return true;
   }

   @Override
   public Class<?> getCommonPropertyType(ELContext context, Object base)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public Class<?> getType(ELContext context, Object base, Object property) 
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setValue(ELContext context, Object base, Object property, Object value)
   {
      throw new UnsupportedOperationException();
   }
   
}