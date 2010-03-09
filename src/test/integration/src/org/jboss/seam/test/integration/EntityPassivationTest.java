package org.jboss.seam.test.integration;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.mock.SeamTest;
import org.jboss.seam.persistence.ManagedEntityInterceptor;
import org.testng.annotations.Test;

/**
 * Verifies the work of the ManagedEntityInterceptor. Specifically that
 * collections containing entity instances are properly put into the session and
 * pulled back into the conversation-scoped component between requests (during a
 * potential period of passivation of the SFSB). The test also verifies that
 * when a component in a nested conversation calls a component in a parent
 * conversation, that the passivated state is kept with the parent conversation.
 * 
 * @author Norman Richards
 * @author Dan Allen
 */
public class EntityPassivationTest extends SeamTest
{
   @Test
   public void testEntityList() throws Exception
   {
      String pid = new FacesRequest("/test.xhtml")
      {
         @Override
         protected void invokeApplication() throws Exception
         {
            // MEI is not installed by default, so we need to enable it
            Component.forName("entitytest.someComponent").addInterceptor(new ManagedEntityInterceptor());
            Component.forName("entitytest.nestedComponent").addInterceptor(new ManagedEntityInterceptor());
            
            Conversation.instance().begin(true, false);

            invokeAction("#{entitytest.someComponent.createSomeThings}");
            invokeAction("#{entitytest.someComponent.loadThings}");
         }

         @Override
         protected void renderResponse() throws Exception
         {
            Object thing = getValue("#{entitytest.someComponent.thing}");
            assert thing != null;

            List thingList = (List) getValue("#{entitytest.someComponent.thingsAsList}");
            assert thingList != null && !thingList.isEmpty();
            assert thingList.get(0) != null;

            Set thingSet = (Set) getValue("#{entitytest.someComponent.thingsAsSet}");
            assert thingSet != null && thingSet.size() > 0;
            assert thingSet.iterator().next() != null;

            Map thingMap = (Map) getValue("#{entitytest.someComponent.thingsAsMap}");
            assert thingMap != null && thingMap.size() > 0;
         }
      }.run();

      new FacesRequest("/test.xhtml", pid)
      {
         // the entities should be passivated
      }.run();

      new FacesRequest("/test.xhtml", pid)
      {
         // passivated a second time
      }.run();

      new FacesRequest("/test.xhtml", pid)
      {
         @Override
         protected void renderResponse() throws Exception
         {
            Object thing = getValue("#{entitytest.someComponent.thing}");
            assert thing != null;

            List thingList = (List) getValue("#{entitytest.someComponent.thingsAsList}");
            assert thingList != null && !thingList.isEmpty();
            assert thingList.get(0) != null;

            Set thingSet = (Set) getValue("#{entitytest.someComponent.thingsAsSet}");
            assert thingSet != null && thingSet.size() > 0;
            assert thingSet.iterator().next() != null;

            Map thingMap = (Map) getValue("#{entitytest.someComponent.thingsAsMap}");
            assert thingMap != null && thingMap.size() > 0;
         }

      }.run();

      // Start a nested conversation to verify that calls to a component in a parent conversation
      // will passivate that component's state in the parent conversation context and not in
      // the nested conversation. Thus, when the parent conversation is restored, its state
      // will be properly restored.
      String nid = new FacesRequest("/test.xhtml", pid)
      {
         @Override
         protected void invokeApplication() throws Exception
         {
            invokeMethod("#{entitytest.nestedComponent.nest}");
         }

         @Override
         protected void renderResponse() throws Exception
         {
            assert Conversation.instance().isNested();
         }

      }.run();
      
      new FacesRequest("/test.xhtml", nid)
      {
         @Override
         protected void invokeApplication() throws Exception
         {
            // invoke component in parent conversation from nested conversation
            invokeMethod("#{entitytest.someComponent.removeFirstThingFromList}");
         }
         
         @Override
         protected void renderResponse() throws Exception
         {
            // the nested conversation should not hold the serialized property of the component in the parent conversation
            assert !Arrays.asList(Contexts.getConversationContext().getNames()).contains("entitytest.someComponent.thingList");
            List thingList = (List) getValue("#{entitytest.someComponent.thingsAsList}");
            assert thingList.size() == 1;
         }   
      }.run();
      
      new FacesRequest("/test.xhtml", nid)
      {
         @Override
         protected void invokeApplication() throws Exception
         {
            invokeMethod("#{entitytest.nestedComponent.end}");
         }  
      }.run();
      
      new FacesRequest("/test.xhtml", pid)
      {
         @Override
         protected void renderResponse() throws Exception
         {
            // The state of the component in the parent conversation should be preserved.
            List thingList = (List) getValue("#{entitytest.someComponent.thingsAsList}");
            assert thingList.size() == 1;
         }  
      }.run();
   }

   @Name("entitytest.someComponent")
   @Scope(ScopeType.CONVERSATION)
   @AutoCreate
   public static class SomeComponent implements Serializable
   {
      @In
      EntityManager entityManager;

      Set<UnversionedThing> thingSet;
      List<UnversionedThing> thingList;
      Map<Long, UnversionedThing> thingMap;
      UnversionedThing thing;

      public void loadThings()
      {
         thingList = entityManager.createQuery("select t from UnversionedThing t").getResultList();
         thingSet = new HashSet<UnversionedThing>(thingList);

         thingMap = new HashMap<Long, UnversionedThing>();
         for (UnversionedThing thing : thingList)
         {
            thingMap.put(thing.getId(), thing);
         }

         thing = thingList.get(0);
      }

      public List<UnversionedThing> getThingsAsList()
      {
         return thingList;
      }

      public Set<UnversionedThing> getThingsAsSet()
      {
         return thingSet;
      }

      public Map<Long, UnversionedThing> getThingsAsMap()
      {
         return thingMap;
      }

      public UnversionedThing getThing()
      {
         return thing;
      }
      
      public void removeFirstThingFromList()
      {
         loadThings();
         thingList.remove(0);
      }

      public void createSomeThings()
      {
         UnversionedThing thing1 = new UnversionedThing();
         thing1.setName("thing one");
         entityManager.persist(thing1);
         
         UnversionedThing thing2 = new UnversionedThing();
         thing2.setName("thing two");
         entityManager.persist(thing2);
      }

   }

   @Name("entitytest.nestedComponent")
   @Scope(ScopeType.CONVERSATION)
   @AutoCreate
   public static class NestedComponent implements Serializable
   {
      @In
      EntityManager entityManager;

      @Begin(nested = true)
      public void nest()
      {
      }

      public void end()
      {
         Conversation.instance().end();
      }
   }
}
