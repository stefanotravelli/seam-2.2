/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.seam.example.tasks;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.example.tasks.entity.User;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;

/**
 * Application authenticator. User is added to admin role if the admin property
 * is set to true.
 * 
 * @author Jozef Hartinger
 * 
 */
@Name("authenticator")
@Scope(ScopeType.EVENT)
public class Authenticator
{

   @In
   private Identity identity;
   @In
   private Credentials credentials;
   @In
   private EntityManager entityManager;
   @Out(scope = ScopeType.SESSION)
   private User user;
   @Logger
   private Log log;

   public boolean authenticate()
   {
      user = entityManager.find(User.class, credentials.getUsername());
      if ((user != null) && (user.getPassword().equals(credentials.getPassword())))
      {
         if (user.isAdmin())
         {
            log.info("Admin rights granted for {0}", user.getUsername());
            identity.addRole("admin");
         }
         return true;
      }
      else
      {
         return false;
      }
   }
}
