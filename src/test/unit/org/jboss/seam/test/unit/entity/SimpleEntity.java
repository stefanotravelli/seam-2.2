package org.jboss.seam.test.unit.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * A simple entity class that can be used in tests.
 */
@Entity
public class SimpleEntity implements Serializable
{
   private Long id;

   private String name;

   @Id @GeneratedValue
   public Long getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public void setId(Long id)
   {
      this.id = id;
   }

   public void setName(String name)
   {
      this.name = name;
   }
}
