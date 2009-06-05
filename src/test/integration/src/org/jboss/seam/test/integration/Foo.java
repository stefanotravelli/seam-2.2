/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.test.integration;

import java.io.Serializable;

import javax.ejb.Remove;

import org.hibernate.validator.NotNull;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author <a href="mailto:theute@jboss.org">Thomas Heute </a>
 * @version $Revision: 6435 $
 */
@Name("foo")
@Scope(ScopeType.SESSION)
@SuppressWarnings("deprecation")
public class Foo implements Serializable
{
   private static final long serialVersionUID = -5448030633067107049L;
   
   private String value;
   
   public String foo() { return "foo"; }

   @Remove
   public void destroy() {}

   @NotNull
   public String getValue()
   {
      return value;
   }

   public void setValue(String value)
   {
      this.value = value;
   }
   
   public String bar()
   {
      return "bar";
   }
   
   @Begin
   public String begin()
   {
      return "begun";
   }
   @End
   public String end()
   {
      return "ended";
   }
   
   @Begin
   public String beginNull()
   {
      return null;
   }
   @End
   public String endNull()
   {
      return null;
   }
   
   @Begin
   public void beginVoid() { }
   @End
   public void endVoid() { }
   
   @Begin(ifOutcome="success")
   public String beginIf()
   {
      return "success";
   }
   @End(ifOutcome="success")
   public String endIf()
   {
      return "success";
   }
   
}


