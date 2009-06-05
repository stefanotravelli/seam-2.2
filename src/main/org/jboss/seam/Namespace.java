package org.jboss.seam;

import java.util.HashMap;
import java.util.Map;

/**
 * A namespace for Seam component names. 
 * 
 * @author Gavin King
 *
 */
public class Namespace
{
   
   private String name;
   private Map<String, Namespace> children = new HashMap<String, Namespace>();
   
   public Namespace(String name) 
   {
      this.name = name;
   }
   
   /**
    * Get a component or child namespace
    */
   public Object get(String key)
   {
      Object component = getComponentInstance(key);
      return component==null ? children.get(key) : component;
   }
   
   public Namespace getChild(String key)
   {
      return children.get(key);
   }
   
   public Namespace getOrCreateChild(String key)
   {
      Namespace result = getChild(key);
      if (result==null)
      {
         result = new Namespace( qualifyName(key) + '.' );
         addChild(key, result);
      }
      return result;
   }
   
   public Object getComponentInstance(String key)
   {
      return getComponentInstance(key, true);
   }

   public Object getComponentInstance(String key, boolean create)
   {
      return Component.getInstance( qualifyName(key), create );
   }

   private String qualifyName(String key)
   {
      return name==null ? key : name + key;
   }
   
   public boolean hasChild(String key)
   {
      return children.containsKey(key);
   }
   
   public void addChild(String name, Namespace value)
   {
      children.put(name, value);
   }
   
   @Override
   public int hashCode()
   {
      return name==null ? 0 : name.hashCode();
   }
   
   @Override
   public boolean equals(Object other)
   {
      if ( !(other instanceof Namespace) )
      {
         return false;
      }
      else
      {
         Namespace ns = (Namespace) other;
         return this.name==ns.name || 
               ( this.name!=null && this.name.equals(ns.name) );
      }
   }
   
   @Override
   public String toString()
   {
      return "Namespace(" + ( name==null ? "Root" : name ) + ')';
   }

}
