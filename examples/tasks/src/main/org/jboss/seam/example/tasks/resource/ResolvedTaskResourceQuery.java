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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.atom.Person;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.example.tasks.ResourceNotFoundException;
import org.jboss.seam.example.tasks.entity.Task;
import org.jboss.seam.resteasy.ResourceQuery;

/**
 * This resource exposes a list of resolved tasks for a particular user using
 * ResourceQuery component. This list is available publicly without
 * authentication needed. The list can be marshalled to XML, JSON, Fastinfoset.
 * ResourceQuery component is extended in order to provide Atom feed.
 * 
 * @author Jozef Hartinger
 * 
 */
@Name("resolvedTaskResourceQuery")
@Path("/user/{username}/tasks/resolved")
public class ResolvedTaskResourceQuery extends ResourceQuery<Task>
{

   @PathParam("username")
   private String username;

   public ResolvedTaskResourceQuery()
   {
      setMediaTypes(new String[] { "application/xml", "application/json", "application/fastinfoset" });
   }

   @Override
   @Create
   public void create()
   {
      super.create();
      List<String> restrictions = new ArrayList<String>();
      restrictions.add("category.owner.username = #{resolvedTaskResourceQuery.username} AND resolved = true");
      getEntityQuery().setRestrictionExpressionStrings(restrictions);
      getEntityQuery().setOrderColumn("updated");
      getEntityQuery().setOrderDirection("desc");
   }

   @Produces("application/atom+xml")
   @GET
   public Feed getFeed() throws URISyntaxException
   {
      List<Task> tasks = getEntityList(0, 0);
      if (tasks.size() == 0)
      {
         // TODO make difference between empty list and nonsense user
         throw new ResourceNotFoundException();
      }

      Feed feed = new Feed();
      feed.setTitle("Resolved feeds for " + username);
      feed.getAuthors().add(new Person(username));
      feed.setUpdated(new Date(0));

      for (Task task : tasks)
      {
         Entry entry = new Entry();
         entry.setTitle(task.getName());
         entry.setSummary(task.getName());
         entry.setPublished(task.getCreated());
         entry.setUpdated(task.getUpdated());
         feed.getEntries().add(entry);
      }
      return feed;
   }

   public String getUsername()
   {
      return username;
   }
}
