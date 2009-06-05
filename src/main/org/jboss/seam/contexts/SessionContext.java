/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.contexts;

import java.util.ArrayList;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.Seam;

/**
 * Session context - state associated with a user session.
 * Session state may be passivated or replicated.
 * 
 * @author Gavin King
 */
public class SessionContext extends BasicContext 
{
   
   public SessionContext(Map<String, Object> map)
   {
      super(ScopeType.SESSION, map);
   }

   @Override
	public String[] getNames() 
   {
		ArrayList<String> results = new ArrayList<String>();
      String prefix = ScopeType.CONVERSATION.getPrefix();
      for ( String name: super.getNames() ) 
      {
         if ( !name.contains(prefix) )
         {
            results.add(name);
         }
      }
		return results.toArray(new String[]{});
	}
   
   @Override
   public void flush() 
   {
      for ( String name: getNames() )
      {
         Object attribute = get(name);
         boolean dirty = attribute!=null && 
               ( Contexts.isAttributeDirty(attribute) || Seam.isEntityClass( attribute.getClass() ) );
         if ( dirty )
         {
            set(name, attribute);
         }
      }      
   }
  
   @Override
   public String toString()
   {
      return "SessionContext";
   }

}
