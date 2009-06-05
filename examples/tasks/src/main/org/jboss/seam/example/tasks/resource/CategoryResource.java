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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.example.tasks.CategoryHome;
import org.jboss.seam.example.tasks.entity.User;

/**
 * This resource demonstrates use of plain JAX-RS resource (no ResourceHome
 * component) with Seam. It allows retrieving (GET), creating (PUT) and deleting
 * (DELETE) of task categories. To create new category, simply send a PUT request
 * without entity body to /auth/category/name where "name" is the name of
 * category. Same path applies for retrieving and deleting of category.
 * 
 * @author Jozef Hartinger
 * 
 */

@Path("/auth/category/{category}")
@Name("categoryResource")
@Produces( { "application/xml", "application/json", "application/fastinfoset" })
@Consumes( { "application/xml", "application/json", "application/fastinfoset" })
public class CategoryResource
{
   @In
   private CategoryHome categoryHome;

   @javax.ws.rs.core.Context
   private UriInfo uriInfo;

   @In
   private User user;

   @PathParam("category")
   private String category;

   @GET
   public Response getCategory()
   {
      return Response.ok(categoryHome.findByUsernameAndCategory(user.getUsername(), category)).build();
   }

   @PUT
   public Response putCategory()
   {
      categoryHome.getInstance().setName(category);
      categoryHome.getInstance().setOwner(user);
      // may cause exception if user already has a category with that name
      // in that case the exception is handled by exception mapper
      categoryHome.persist();
      return Response.created(uriInfo.getAbsolutePath()).build();
   }

   @DELETE
   public void deleteCategory()
   {
      categoryHome.findByUsernameAndCategory(user.getUsername(), category);
      categoryHome.remove();
   }
}
