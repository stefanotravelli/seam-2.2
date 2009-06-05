package org.jboss.seam.persistence;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Expressions.ValueExpression;

/**
 * Support for declarative application of
 * Hibernate filters to persistence contexts.
 * 
 * @see org.hibernate.Filter
 * @see ManagedHibernateSession
 * @see ManagedPersistenceContext
 * @author Gavin King
 */
@BypassInterceptors
@Scope(ScopeType.APPLICATION)
public class Filter implements Serializable
{
   private String name;
   // default to no parameters
   private Map<String, ValueExpression> parameters = new HashMap<String, ValueExpression>();
   private ValueExpression enabled;
   
   @Create
   public void create(Component component)
   {
      //default the filter name to the component name
      if (name==null)
      {
         name = component.getName();
      }
   }
   
   /**
    * The filter parameters.
    * 
    * @see org.hibernate.Filter#setParameter(String, Object)
    */
   public Map<String, ValueExpression> getParameters()
   {
      return parameters;
   }
   public void setParameters(Map<String, ValueExpression> parameters)
   {
      this.parameters = parameters;
   }
   
   /**
    * The Hibernate filter name.
    * 
    * @see org.hibernate.Session#enableFilter(String)
    */
   public String getName()
   {
      return name;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }
   
   public boolean isFilterEnabled()
   {
      ValueExpression enabledValueBinding = getEnabled();
      if (enabledValueBinding==null)
      {
         return true;
      }
      else
      {
         Boolean enabled = (Boolean) enabledValueBinding.getValue();
         return enabled!=null && enabled;
      }
   }

   @Override
   public String toString()
   {
      return "Filter(" + name + ")";
   }

   public ValueExpression getEnabled()
   {
      return enabled;
   }

   public void setEnabled(ValueExpression enabled)
   {
      this.enabled = enabled;
   }
}