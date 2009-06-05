package org.jboss.seam.test.unit;

import java.io.Serializable;

import org.jboss.seam.annotations.Name;


@Name("person")
public class Person implements Serializable
{
   
   public Person(String name)
   {
      this.name = name;
   }
   
   public Person() {}
   
   private String name;
   
   public String getName()
   {
      return name;
   }
   
    public void setName(String name)
   {
      this.name = name;
   }
    
    @Override
   public boolean equals(Object other)
   {
      if (other instanceof Person)
      {
         Person that = (Person) other;
         return (this.name == null && that.name == null) || (this.name != null && this.name.equals(that.name));     
      }
      else
      {
         return false;
      }
   }

}
