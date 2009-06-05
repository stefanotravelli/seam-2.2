package org.jboss.seam.ui;

import static org.jboss.seam.ScopeType.PAGE;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.AbstractMutable;
import org.jboss.seam.framework.Identifier;

/**
 * Page scoped store for entity identifiers used by the EntityConverter.
 * 
 * By default a list is used and the key returned is the list index. The key
 * used can be customised by overriding this component and implementing the
 * get and put methods
 * 
 * @author Pete Muir
 *
 */
@Name("org.jboss.seam.ui.entityIdentifierStore")
@Install(precedence=BUILT_IN)
@Scope(PAGE)
public class EntityIdentifierStore extends AbstractMutable
{
   
   private List<Identifier> store;
   
   @Create
   public void create()
   {
      store = new ArrayList<Identifier>();
   }
   
   public Identifier get(String key)
   {
      try
      {
         return store.get(new Integer(key));
      }
      catch (IndexOutOfBoundsException e)
      {
         return null;
      }   
   }
      
   public String put(Identifier identifier, Object entity)
   {      
      if (!store.contains(identifier))
      {
         store.add(identifier);
         setDirty();
      }
      return ((Integer) store.indexOf(identifier)).toString();
   }

   public static EntityIdentifierStore instance()
   {
      if (!Contexts.isPageContextActive())
      {
         throw new IllegalArgumentException("Page scope not active");
      }
      return (EntityIdentifierStore) Component.getInstance(EntityIdentifierStore.class, PAGE);
   }
}
