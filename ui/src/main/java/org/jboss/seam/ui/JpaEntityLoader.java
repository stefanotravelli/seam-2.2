package org.jboss.seam.ui;

import static org.jboss.seam.ScopeType.STATELESS;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.framework.EntityIdentifier;
import org.jboss.seam.framework.Identifier;

/**
 * Stores entity identifiers under a key, which can be used on a page
 *
 * @author Pete Muir
 */

@Name("org.jboss.seam.ui.entityLoader")
@Install(precedence=BUILT_IN, value=true, classDependencies="javax.persistence.EntityManager")
@Scope(STATELESS)
public class JpaEntityLoader extends AbstractEntityLoader<EntityManager>
{

   @Override
   protected Identifier createIdentifier(Object entity)
   {
      return new EntityIdentifier(entity, getPersistenceContext());
   }

   @Override
   protected String getPersistenceContextName()
   {
      return "entityManager";
   }
   
   @Override
   public void validate()
   {
      if (getPersistenceContext() == null)
      {
         throw new IllegalStateException("Unable to access a persistence context. You must either have a SMPC called entityManager or configure one in components.xml");
      }
      
   }
   
   public EntityManager getEntityManager()
   {
      return getPersistenceContext();
   }

   public void setEntityManager(EntityManager entityManager)
   {
      setPersistenceContext(entityManager);
   }
   
}
