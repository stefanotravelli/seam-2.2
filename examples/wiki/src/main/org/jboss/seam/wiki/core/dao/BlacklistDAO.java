package org.jboss.seam.wiki.core.dao;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

/**
 * DAO for blacklist-related operations
 * 
 * @author Shane Bryzak
 */
@Name("blacklistDAO")
@AutoCreate
public class BlacklistDAO
{
   @In
   protected EntityManager entityManager;

   public boolean isEmailBlacklisted(String email)
   {
      if (email == null) return false;
      
      return entityManager.createQuery("select bl from Blacklist bl where bl.email = :email")
         .setParameter("email", email)
         .getResultList().size() > 0;
   }
   
   public boolean isIpAddressBlacklisted(String ipAddress)
   {
      if (ipAddress == null) return false;
      
      return entityManager.createQuery("select bl from Blacklist bl where bl.ipAddress = :ipAddress")
         .setParameter("ipAddress", ipAddress)
         .getResultList().size() > 0;
   }
}
