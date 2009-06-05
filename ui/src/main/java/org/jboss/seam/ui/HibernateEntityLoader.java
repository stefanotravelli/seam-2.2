package org.jboss.seam.ui;

import static org.jboss.seam.ScopeType.STATELESS;
import static org.jboss.seam.annotations.Install.FRAMEWORK;

import org.hibernate.Session;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.framework.HibernateEntityIdentifier;
import org.jboss.seam.framework.Identifier;

/**
 * Stores entity identifiers under a key, which can be used on a page
 *
 * @author Pete Muir
 */

@Name("org.jboss.seam.ui.entityLoader")
@Install(precedence=FRAMEWORK, classDependencies="org.hibernate.Session", value=false)
@Scope(STATELESS)
public class HibernateEntityLoader extends AbstractEntityLoader<Session>
{

   @Override
   protected Identifier createIdentifier(Object entity)
   {
      return new HibernateEntityIdentifier(entity, getPersistenceContext());
   }

   @Override
   protected String getPersistenceContextName()
   {
      return "hibernateSession";
   }

   @Override
   public void validate()
   {
      if (getPersistenceContext() == null)
      {
         throw new IllegalStateException("Unable to access a Seam Managed Hibernate Session. You must either have a Seam Managed Hibernate Session called hibernateSession or configure one in components.xml");
      }
      
   }
   
   public Session getSession()
   {
      return getPersistenceContext();
   }
   
   public void setSession(Session session)
   {
      setPersistenceContext(session);
   }

}
