package org.jboss.seam.init;

import org.jboss.seam.ScopeType;

class FactoryDescriptor
{
   private String name;
   private ScopeType scope;
   private String method;
   private String value;
   private boolean autoCreate;

   FactoryDescriptor(String name, ScopeType scope, String method, String value,
                     boolean autoCreate)
   {
      super();
      this.name = name;
      this.scope = scope;
      this.method = method;
      this.value = value;
      this.autoCreate = autoCreate;
   }

   public String getMethod()
   {
      return method;
   }

   public String getValue()
   {
      return value;
   }

   public String getName()
   {
      return name;
   }

   public ScopeType getScope()
   {
      return scope;
   }

   public boolean isValueBinding()
   {
      return method == null;
   }

   public boolean isAutoCreate()
   {
      return autoCreate;
   }

   @Override
   public String toString()
   {
      return "FactoryDescriptor(" + name + ')';
   }
}

