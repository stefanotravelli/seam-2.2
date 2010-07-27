package org.jboss.seam.wiki.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Stores a list of blacklisted e-mail addresses and IP addresses. Any users
 * matching an entry in this table will not be permitted to register or to create
 * any type of content.
 *  
 * @author Shane Bryzak
 */
@Entity
@Table(name = "BLACKLIST")
public class Blacklist
{
   private Long id;
   private String email;
   private String ipAddress;
 
   @Id
   @GeneratedValue(generator = "wikiSequenceGenerator")
   @Column(name = "ID")
   public Long getId()
   {
      return id;
   }
   
   public void setId(Long id)
   {
      this.id = id;
   }
   
   @Column(name = "EMAIL")
   public String getEmail()
   {
      return email;
   }
   
   public void setEmail(String email)
   {
      this.email = email;
   }
   
   @Column(name = "IP_ADDRESS")
   public String getIpAddress()
   {
      return ipAddress;
   }
   
   public void setIpAddress(String ipAddress)
   {
      this.ipAddress = ipAddress;
   }
}
