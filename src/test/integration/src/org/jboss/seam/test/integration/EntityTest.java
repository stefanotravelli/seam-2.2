package org.jboss.seam.test.integration;

import org.jboss.seam.mock.SeamTest;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.hibernate.StaleStateException;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Manager;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EntityTest 
    extends SeamTest 
{

    @Test
    public void entityUpdatedInNestedConversation() throws Exception {
        String parentConversation = new FacesRequest("/page.xhtml") {
            @Override
            protected void invokeApplication() throws Exception {
                Thing thing = new Thing();
                thing.setName("thing");
                EntityManager entityManager = (EntityManager) getValue("#{entityManager}");
                entityManager.persist(thing);
                Contexts.getConversationContext().set("thing", thing);
                Manager.instance().beginConversation();
            }
        }.run();


        new FacesRequest("/page.xhtml", parentConversation) {
        }.run();

        // nested conversation
        String nestedId = new FacesRequest("/page.xhtml", parentConversation) {
            @Override
            protected void invokeApplication() throws Exception {
                Manager.instance().beginNestedConversation();
            }
        }.run();

        // update entity in nested conversation
        new FacesRequest("/page.xhtml", nestedId) {
            @Override
            protected void invokeApplication() throws Exception {
                Thing thing = (Thing) Contexts.getConversationContext().get("thing");
                thing.setName("foo");
                EntityManager entityManager = (EntityManager) getValue("#{entityManager}");
                entityManager.flush();
            }
        }.run();

        // end nested conversation
        assert new FacesRequest("/page.xhtml", nestedId) {
            @Override
            protected void invokeApplication() throws Exception {
                Manager.instance().endConversation(false);
            }
        }.run().equals(parentConversation);

  
        // This tests that the activation in the parent conversation
        // doesn't fail
        new FacesRequest("/page.xhtml",parentConversation) {
            @Override
            protected void renderResponse() throws Exception {         
                Thing thing = (Thing) Contexts.getConversationContext().get("thing");
                assert thing.getName().equals("foo");
            }
        }.run();
    }

    @Test
    public void testStale() throws Exception {

        final Map<String, Long> holder = new HashMap<String, Long>();

        final String conversation1 = new FacesRequest("/page.xhtml") {
            @Override
            protected void invokeApplication() throws Exception {
                Thing thing = new Thing();
                thing.setName("thing");
                EntityManager entityManager = (EntityManager) getValue("#{entityManager}");
                entityManager.persist(thing);
                holder.put("id", thing.getId());
                Contexts.getConversationContext().set("thing", thing);
                Manager.instance().beginConversation();
            }
        }.run();

        new FacesRequest("/page.xhtml", conversation1) {
        }.run();

        // update in second conversation
        new FacesRequest("/page.xhtml") {
            @Override
            @SuppressWarnings("cast")
            protected void invokeApplication() throws Exception {
                EntityManager entityManager = (EntityManager) getValue("#{entityManager}");
                Thing thing = (Thing) entityManager.find(Thing.class, holder.get("id"));
                thing.setName("foo");
                entityManager.flush();
            }
        }.run();

        try {
            new FacesRequest("/page.xhtml", conversation1) {
                EntityExceptionObserver observer;
                
                @Override
                protected void invokeApplication() throws Exception {
                    Thing thing = (Thing) Contexts.getConversationContext().get("thing");
                    thing.setName("bar");
                   
                    observer = (EntityExceptionObserver) getValue("#{entityExceptionObserver}");
                    assert observer != null;
                }
                
                @Override
                protected void renderResponse() throws Exception {
                    Assert.fail("page rendered without redirect, expected StaleStateException!");
                }
                
                @Override
                protected void afterRequest() {
                   assert observer.getOptimisticLockExceptionSeen();
                }        
            }.run();

        } catch (StaleStateException e) {
        }
    }
    
    @Name("entityExceptionObserver")
    public static class EntityExceptionObserver {
        
        private boolean exceptionSeen;

        @Observer(value="org.jboss.seam.exceptionHandled.javax.persistence.OptimisticLockException")
        public void handleException(Exception e) {
            exceptionSeen=true;
        }
        
        public boolean getOptimisticLockExceptionSeen() {
            return exceptionSeen;
        }
        
        @Override
        public String toString() {
            return "EntityExceptionObserver[" + exceptionSeen + "]";
        }
    }
}
