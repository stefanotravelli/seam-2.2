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
package org.jboss.seam.example.tasks.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.validator.NotNull;

/**
 * 
 * @author Jozef Hartinger
 *
 */
@Entity
@XmlRootElement
@NamedQuery(name="taskByNameAndCategory", query="select task from Task task where task.name like :task and task.category.id = :category")
public class Task
{
   private Long id;
   private String name;
   private boolean resolved;
   private Date created;
   private Date updated;
   private Category category;

   @Id
   @GeneratedValue
   public Long getId()
   {
      return id;
   }

   public void setId(Long id)
   {
      this.id = id;
   }

   @NotNull
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   @NotNull
   public boolean isResolved()
   {
      return resolved;
   }

   public void setResolved(boolean resolved)
   {
      this.resolved = resolved;
   }

   @Temporal(TemporalType.TIMESTAMP)
   @XmlElement(name="created")
   @NotNull
   public Date getCreated()
   {
      return created;
   }

   public void setCreated(Date created)
   {
      this.created = created;
   }

   @Temporal(TemporalType.TIMESTAMP)
   public Date getUpdated()
   {
      return updated;
   }

   public void setUpdated(Date updated)
   {
      this.updated = updated;
   }

   @ManyToOne
   @XmlTransient
   @NotNull
   public Category getCategory()
   {
      return category;
   }
   

   public void setCategory(Category category)
   {
      this.category = category;
   }
   
   @Transient
   @XmlElement(name="category")
   public String getCategoryName() {
      return category.getName();
   }
   
   @Transient
   public User getOwner() {
      return category.getOwner();
   }
   
}
