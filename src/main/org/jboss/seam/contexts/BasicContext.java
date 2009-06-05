//$Id$
package org.jboss.seam.contexts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.Seam;
import org.jboss.seam.core.Events;

/**
 * A basic implementation of Context that keeps the state 
 * in a Map.
 * 
 * @author Gavin King
 */
public class BasicContext implements Context
{
   
   private final Map<String, Object> map;
   private final ScopeType scope;
   
   public BasicContext(ScopeType scope)
   {
      this.scope = scope;
      this.map = new HashMap<String, Object>();
   }

   protected BasicContext(ScopeType scope, Map<String, Object> map)
   {
      this.scope = scope;
      this.map = map;
   }

   public ScopeType getType()
   {
      return scope;
   }

   public Object get(Class clazz)
   {
      return get( Component.getComponentName(clazz) );
   }

   public Object get(String name)
   {
      return map.get(name);
   }

   public String[] getNames()
   {
      // yes, I know about the toArray() method,
      // but there is a bug in the RI!
      // XXX - what bug?
      ArrayList<String> keys = new ArrayList<String>();
      for (String key : map.keySet())
      {
         keys.add(key);
      }
      return keys.toArray(new String[keys.size()]);
   }

   public boolean isSet(String name)
   {
      return map.containsKey(name);
   }

   public void remove(String name)
   {
      if ( Events.exists() ) Events.instance().raiseEvent("org.jboss.seam.preRemoveVariable." + name);
      map.remove(name);
      if ( Events.exists() ) Events.instance().raiseEvent("org.jboss.seam.postRemoveVariable." + name);
   }

   public void set(String name, Object value)
   {
      // We can't raise a preSetVariable event for Events itself because it doesn't exist yet...
      if ( !Seam.getComponentName(Events.class).equals(name) && Events.exists() ) 
      {
         Events.instance().raiseEvent("org.jboss.seam.preSetVariable." + name);
      }
      map.put(name, value);
      if ( Events.exists() ) Events.instance().raiseEvent("org.jboss.seam.postSetVariable." + name);
   }

   public void flush() {}

   @Override
   public String toString()
   {
      return "BasicContext(" + scope + ")";
   }

}
