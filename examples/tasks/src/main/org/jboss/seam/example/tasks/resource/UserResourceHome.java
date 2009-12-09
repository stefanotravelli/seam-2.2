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
package org.jboss.seam.example.tasks.resource;

import javax.ws.rs.Path;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.example.tasks.entity.User;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.resteasy.ResourceHome;

/**
 * This resource demonstrates use of ResourceHome component together with Seam
 * security. This resource exposes user management as a web service, however only
 * users with admin permission are allowed to access it.
 * 
 * @author Jozef Hartinger
 * 
 */
@Path("/auth/user")
@Name("userResourceHome")
@Restrict("#{s:hasRole('admin')}")
public class UserResourceHome extends ResourceHome<User, String>
{

   public UserResourceHome()
   {
      setMediaTypes(new String[] { "application/xml", "application/json", "application/fastinfoset" });
   }

   @In
   private EntityHome<User> userHome;

   @Override
   public EntityHome getEntityHome()
   {
      return userHome;
   }
}
