package org.jboss.seam.mock;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;

/**
 * Used by security unit tests
 * 
 * @author Shane Bryzak
 */
@Name("org.jboss.seam.mock.mockSecureEntity")
@Install(false)
@Entity
public class MockSecureEntity implements Serializable
{
   private static final long serialVersionUID = -6885685305122412324L;

   private Integer id;

   private String value;

   @Id
   public Integer getId()
   {
      return id;
   }

   public void setId(Integer id)
   {
      this.id = id;
   }

   public String getValue()
   {
      return value;
   }

   public void setValue(String value)
   {
      this.value = value;
   }
}
