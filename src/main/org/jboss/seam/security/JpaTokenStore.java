package org.jboss.seam.security;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.security.TokenUsername;
import org.jboss.seam.annotations.security.TokenValue;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.security.management.IdentityManagementException;
import org.jboss.seam.util.AnnotatedBeanProperty;

/**
 * A TokenStore implementation, stores tokens inside a database table.
 * 
 * @author Shane Bryzak
 */
@Name("org.jboss.seam.security.tokenStore")
@Install(precedence = BUILT_IN, value=false) 
@Scope(APPLICATION)
@BypassInterceptors
public class JpaTokenStore implements TokenStore, Serializable
{
   private Class tokenClass;
   
   private ValueExpression<EntityManager> entityManager;    
   
   private AnnotatedBeanProperty<TokenUsername> tokenUsernameProperty;
   private AnnotatedBeanProperty<TokenValue> tokenValueProperty;
   
   @Create
   public void create()
   {
      if (entityManager == null)
      {
         entityManager = Expressions.instance().createValueExpression("#{entityManager}", EntityManager.class);
      }       
      
      tokenUsernameProperty = new AnnotatedBeanProperty<TokenUsername>(tokenClass, TokenUsername.class);
      tokenValueProperty = new AnnotatedBeanProperty<TokenValue>(tokenClass, TokenValue.class);
      
      if (!tokenUsernameProperty.isSet()) 
      {
         throw new IllegalStateException("Invalid tokenClass " + tokenClass.getName() + 
               " - required annotation @TokenUsername not found on any Field or Method.");
      }
      
      if (!tokenValueProperty.isSet()) 
      {
         throw new IllegalStateException("Invalid tokenClass " + tokenClass.getName() + 
               " - required annotation @TokenValue not found on any Field or Method.");
      }       
   }
   
   public void createToken(String username, String value)
   {
      if (tokenClass == null)
      {
         throw new IllegalStateException("Could not create token, tokenClass not set");
      }   
      
      try
      {
         Object token = tokenClass.newInstance();
         
         tokenUsernameProperty.setValue(token, username);
         tokenValueProperty.setValue(token, value);
         
         lookupEntityManager().persist(token);
      }
      catch (Exception ex)
      {
         if (ex instanceof IdentityManagementException)
         {
            throw (IdentityManagementException) ex;
         }
         else
         {
            throw new IdentityManagementException("Could not create account", ex);
         }
      }       
   }
   
   public boolean validateToken(String username, String value)
   {
      return lookupToken(username, value) != null;
   }
   
   public void invalidateToken(String username, String value)
   {
      Object token = lookupToken(username, value);
      if (token != null)
      {
         lookupEntityManager().remove(token);
      }
   }
   
   public void invalidateAll(String username)
   {
      Query query = lookupEntityManager().createQuery(
         "select t from " + tokenClass.getName() + " t where " + tokenUsernameProperty.getName() +
         " = :username")
         .setParameter("username", username);
      
      for (Object token : query.getResultList())
      {
         lookupEntityManager().remove(token);
      }      
   }
   
   public Object lookupToken(String username, String value)       
   {
      try
      {
         Object token = lookupEntityManager().createQuery(
            "select t from " + tokenClass.getName() + " t where " + tokenUsernameProperty.getName() +
            " = :username and " + tokenValueProperty.getName() + " = :value")
            .setParameter("username", username)
            .setParameter("value", value)
            .getSingleResult();
         
         return token;
      }
      catch (NoResultException ex)
      {
         return null;        
      }      
   }   
   
   public Class getTokenClass()
   {
      return tokenClass;
   }
   
   public void setTokenClass(Class tokenClass)
   {
      this.tokenClass = tokenClass;
   }
   
   private EntityManager lookupEntityManager()
   {
      return entityManager.getValue();
   }
   
   public ValueExpression getEntityManager()
   {
      return entityManager;
   }
   
   public void setEntityManager(ValueExpression expression)
   {
      this.entityManager = expression;
   }    
}
