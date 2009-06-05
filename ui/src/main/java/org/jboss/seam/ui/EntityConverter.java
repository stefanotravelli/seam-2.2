package org.jboss.seam.ui;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.faces.Converter;

/**
 * Allows conversion of an entity to/from a key which can be written to a page.
 * 
 * Support is provided for JPA (by default) and Hibernate (with the session 
 * specified in components.xml)
 */
@Name("org.jboss.seam.ui.EntityConverter")
@Scope(CONVERSATION)
@Install(precedence = BUILT_IN)
@Converter
@BypassInterceptors
public class EntityConverter implements
         javax.faces.convert.Converter, Serializable
{
   
   private AbstractEntityLoader entityLoader;

   public AbstractEntityLoader getEntityLoader()
   {
      if (entityLoader == null)
      {
         return AbstractEntityLoader.instance();
      }
      else
      {
         return entityLoader;
      }
   }
   
   public void setEntityLoader(AbstractEntityLoader entityLoader)
   {
      this.entityLoader = entityLoader;
   }
   
   @SuppressWarnings("unchecked")
   @Transactional
   public String getAsString(FacesContext facesContext, UIComponent cmp, Object value) throws ConverterException
   {
      if (value == null)
      {
         return null;
      }
      if (value instanceof String) 
      {
         return (String) value;
      }
      return getEntityLoader().put(value);
   }
   

   @Transactional
   public Object getAsObject(FacesContext facesContext, UIComponent cmp, String value) throws ConverterException
   {
      if (value == null)
      {
         return null;
      }
      return getEntityLoader().get(value);
   }
   
}
