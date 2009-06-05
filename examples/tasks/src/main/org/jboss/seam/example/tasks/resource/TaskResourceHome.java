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

import java.io.InputStream;
import java.util.Date;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.example.tasks.CategoryHome;
import org.jboss.seam.example.tasks.ResourceNotFoundException;
import org.jboss.seam.example.tasks.entity.Task;
import org.jboss.seam.example.tasks.entity.User;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.framework.Home;
import org.jboss.seam.resteasy.ResourceHome;

/**
 * This class exposes CRUD interface for manipulating tasks. ResourceHome
 * component is used as the base and is significantly customized to correspond
 * with usecase.
 * 
 * @author Jozef Hartinger
 * 
 */
@Path("/auth/category/{category}/{status}")
@Name("taskResourceHome")
public class TaskResourceHome extends ResourceHome<Task, Long>
{

   @PathParam("status")
   private String taskStatus;
   @In
   private User user;
   @In
   private CategoryHome categoryHome;
   @In
   private EntityHome<Task> taskHome;
   @PathParam("category")
   private String categoryName;

   public TaskResourceHome()
   {
      setMediaTypes(new String[] { "application/xml", "application/json", "application/fastinfoset" });
   }

   @Override
   public Task getEntity(Long id)
   {
      Task task = super.getEntity(id);
      if (!task.getCategory().getName().equals(categoryName) || !task.getOwner().getUsername().equals(user.getUsername()))
      {
         throw new ResourceNotFoundException("Task not found");
      }
      if (!task.isResolved() == isResolved())
      {
         throw new ResourceNotFoundException("Task found, but in different state.");
      }
      return task;
   }

   @Override
   @POST
   public Response createResource(InputStream messageBody)
   {
      if (isResolved())
      {
         // do not allow creating already resolved tasks
         return Response.status(405).build();
      }
      return super.createResource(messageBody);
   }

   @Override
   public Long createEntity(Task entity)
   {
      entity.setCategory(categoryHome.findByUsernameAndCategory(user.getUsername(), categoryName));
      entity.setResolved(false);
      entity.setCreated(new Date());
      return super.createEntity(entity);
   }

   @Override
   public void updateEntity(Task entity, Long id)
   {
      Task task = super.getEntity(id);
      task.setCategory(categoryHome.findByUsernameAndCategory(user.getUsername(), categoryName));
      task.setResolved(isResolved());
      if (entity.getName() != null)
      {
         task.setName(entity.getName());
      }
      if (isResolved())
      {
         task.setUpdated(new Date());
      }
      taskHome.update();
   }

   @Override
   public Home<?, Task> getEntityHome()
   {
      return taskHome;
   }

   private boolean isResolved()
   {
      if (taskStatus.equals("resolved"))
      {
         return true;
      }
      else if (taskStatus.equals("unresolved"))
      {
         return false;
      }
      else
      {
         throw new ResourceNotFoundException();
      }
   }
}
