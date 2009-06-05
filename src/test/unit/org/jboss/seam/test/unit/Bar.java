/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.test.unit;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Conversational;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;

/**
 * @author <a href="mailto:theute@jboss.org">Thomas Heute </a>
 */
@Name("bar")
@Scope(ScopeType.CONVERSATION)
@Conversational
public class Bar implements Serializable
{
   private static final long serialVersionUID = -5325217160542604204L;

   @In(required=true)
   Foo otherFoo;
   
   @In(create=true)
   Foo foo;
   
   @Out(required=false)
   String string;
   
   @Out(required=false, scope=ScopeType.EVENT)
   String otherString;
   
   @Begin
   public String begin()
   {
      return "begun";
   }
   
   public String foo()
   {
      string = "out";
      otherString = "outAgain";
      return "foo";
   }
   
   @End
   public String end()
   {
      return "ended";
   }
   
   @Destroy
   public void destroy(){}
   @Create
   public void create(){}
   
}


