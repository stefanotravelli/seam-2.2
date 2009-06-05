//$Id$
package org.jboss.seam.test.unit;

import org.jboss.seam.Component;
import org.jboss.seam.ComponentType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.Seam;
import org.jboss.seam.core.Init;
import org.jboss.seam.core.Manager;
import org.jboss.seam.persistence.ManagedHibernateSession;
import org.jboss.seam.persistence.ManagedPersistenceContext;
import org.testng.annotations.Test;

public class ComponentTest
{
   @Test
   public void testStaticMethods()
   {
      assert Seam.getComponentName(Bar.class).equals("bar");
      assert Seam.getComponentType(Bar.class)==ComponentType.JAVA_BEAN;
      assert Seam.getComponentScope(Bar.class)==ScopeType.CONVERSATION;
      assert Seam.getComponentName(Foo.class).equals("foo");
      assert Seam.getComponentType(Foo.class)==ComponentType.JAVA_BEAN;
      assert Seam.getComponentScope(Foo.class)==ScopeType.SESSION;
      //assert Seam.getBeanClass(Foo.class)==Foo.class;
      assert Seam.getEjbName(EjbBean.class).equals("EjbBean");
      assert Seam.getEjbName(Foo.class)==null;
      assert Seam.isInterceptionEnabled(Foo.class)==true;
   }
   
   @Test
   public void testComponent()
   {
      Component c = new Component(Bar.class);
      assert c.getName().equals("bar");
      assert c.getBeanClass()==Bar.class;
      assert c.getType()==ComponentType.JAVA_BEAN;
      assert c.getScope()==ScopeType.CONVERSATION;
      assert c.hasDestroyMethod();
      assert c.hasCreateMethod();
      assert c.getCreateMethod().getName().equals("create");
      assert c.getDestroyMethod().getName().equals("destroy");
      assert c.getInAttributes().size()==2;
      assert c.getUnwrapMethod()==null;
      assert c.getOutAttributes().size()==2;
      assert c.getRemoveMethods().size()==0;
      assert c.isInstance( new Bar() );

      c = new Component(Foo.class);
      assert c.getName().equals("foo");
      assert c.getBeanClass()==Foo.class;
      assert c.getType()==ComponentType.JAVA_BEAN;
      assert c.getScope()==ScopeType.SESSION;
      assert !c.hasDestroyMethod();
      assert !c.hasCreateMethod();
      assert c.getCreateMethod()==null;
      assert c.getDestroyMethod()==null;
      assert c.getInAttributes().size()==0;
      assert c.getUnwrapMethod()==null;
      assert c.getOutAttributes().size()==0;
      assert c.getRemoveMethods().size()==1;
      assert c.isInstance( new Foo() );
      
      c = new Component(EjbBean.class);
      assert c.getName().equals("ejb");
      assert c.getBeanClass()==EjbBean.class;
      assert c.getType()==ComponentType.STATEFUL_SESSION_BEAN;
      assert c.getScope()==ScopeType.EVENT;
      assert c.hasDestroyMethod();
      assert !c.hasDefaultRemoveMethod();
      assert !c.hasCreateMethod();
      assert c.getCreateMethod()==null;
      assert c.getDestroyMethod()!=null;
      assert c.getDefaultRemoveMethod()==null;
      assert c.getInAttributes().size()==0;
      assert c.getUnwrapMethod()==null;
      assert c.getOutAttributes().size()==0;
      assert c.getRemoveMethods().size()==1;
      assert c.isInstance( new Ejb() {
         public void destroy() {}
         public void foo() {} 
      } );
   }
   
   public void testBuiltInComponents()
   {
      Component c = new Component(Manager.class);
      assert c.getName().equals("org.jboss.seam.conversationManager");
      assert c.getBeanClass()==Manager.class;
      assert c.getType()==ComponentType.JAVA_BEAN;
      assert c.getScope()==ScopeType.EVENT;
      assert c.hasDestroyMethod();
      assert !c.hasCreateMethod();
      assert c.getCreateMethod()==null;
      assert c.getDestroyMethod().getName().equals("destroy");
      assert c.getInAttributes().size()==0;
      assert c.getUnwrapMethod()==null;
      assert c.getOutAttributes().size()==0;
      assert c.getRemoveMethods().size()==0;

      c = new Component(Init.class);
      assert c.getName().equals("org.jboss.seam.settings");
      assert c.getBeanClass()==Init.class;
      assert c.getType()==ComponentType.JAVA_BEAN;
      assert c.getScope()==ScopeType.APPLICATION;
      assert !c.hasDestroyMethod();
      assert !c.hasCreateMethod();
      assert c.getCreateMethod()==null;
      assert c.getDestroyMethod()==null;
      assert c.getInAttributes().size()==0;
      assert c.getUnwrapMethod()==null;
      assert c.getOutAttributes().size()==0;
      assert c.getRemoveMethods().size()==0;
      c = new Component(ManagedPersistenceContext.class, "pc");
      assert c.getName().equals("pc");
      assert c.getBeanClass()==ManagedPersistenceContext.class;
      assert c.getType()==ComponentType.JAVA_BEAN;
      assert c.getScope()==ScopeType.CONVERSATION;
      assert c.hasDestroyMethod();
      assert c.hasCreateMethod();
      assert c.getCreateMethod().getName().equals("create");
      assert c.getDestroyMethod().getName().equals("destroy");
      assert c.getInAttributes().size()==0;
      assert c.getUnwrapMethod().getName().equals("getEntityManager");
      assert c.getOutAttributes().size()==0;
      assert c.getRemoveMethods().size()==0;

      c = new Component(ManagedHibernateSession.class, "pc");
      assert c.getName().equals("pc");
      assert c.getBeanClass()==ManagedHibernateSession.class;
      assert c.getType()==ComponentType.JAVA_BEAN;
      assert c.getScope()==ScopeType.CONVERSATION;
      assert c.hasDestroyMethod();
      assert c.hasCreateMethod();
      assert c.getCreateMethod().getName().equals("create");
      assert c.getDestroyMethod().getName().equals("destroy");
      assert c.getInAttributes().size()==0;
      assert c.getUnwrapMethod().getName().equals("getSession");
      assert c.getOutAttributes().size()==0;
      assert c.getRemoveMethods().size()==0;
   }
}
