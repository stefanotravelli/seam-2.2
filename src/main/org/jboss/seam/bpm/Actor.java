package org.jboss.seam.bpm;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.AbstractMutable;

/**
 * Allows the application to specify the jBPM actorId
 * during the login cycle.
 * 
 * @author Gavin King
 */
@Name("org.jboss.seam.bpm.actor")
@Scope(ScopeType.SESSION)
@BypassInterceptors
@Install(dependencies="org.jboss.seam.bpm.jbpm", precedence=BUILT_IN)
public class Actor extends AbstractMutable implements Serializable
{
   private static final long serialVersionUID = -6515302276074415520L;
   private String id;
   private Set<String> groupActorIds = new HashSet<String>()
   {
      @Override
      public boolean add(String o)
      {
         boolean dirty = super.add(o);
         if (dirty) setDirty();
         return dirty;
      }
      
      @Override
      public void clear()
      {
         if (size() > 0) setDirty();
         super.clear();
      }
      
      @Override
      public boolean remove(Object o)
      {
         boolean dirty = super.remove(o);
         if (dirty) setDirty();
         return dirty;
      }
      
      @Override
      public Iterator<String> iterator()
      {
         final Iterator<String> it = super.iterator();
         return new Iterator<String>()
         {

            public boolean hasNext()
            {
               return it.hasNext();
            }

            public String next()
            {
               return it.next();
            }

            public void remove()
            {
               setDirty();
               it.remove();
            }
            
         };
      }
   };
   
   public String getId() 
   {
      return id;
   }
   public void setId(String id) 
   {
      setDirty(this.id, id);
      this.id = id;
   }
 
   public Set<String> getGroupActorIds()
   {
      return groupActorIds;
   }
   public static Actor instance()
   {
      if ( !Contexts.isSessionContextActive() )
      {
         throw new IllegalStateException("No active session context");
      }
      return (Actor) Component.getInstance(Actor.class);
   }
   
   @Override
   public String toString()
   {
      return "Actor(" + id + ")";
   }
}

               