package org.jboss.seam.test.unit.bpm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jboss.seam.bpm.Actor;
import org.testng.annotations.Test;

/**
 * @author Pete Muir
 *
 */
public class ActorTest
{
   
   @Test
   public void testActorDirtyChecking()
   {
      // Test dirty checking on id
      Actor actor = new Actor();
      assert !actor.clearDirty();
      actor.setId("gavin");
      assert actor.clearDirty();
      actor.setId("pete");
      assert actor.clearDirty();
      
      // Test dirty checking on group actor ids
      actor.getGroupActorIds().add("manager");
      assert actor.clearDirty();
      actor.getGroupActorIds().remove("manager");
      assert actor.clearDirty();
      actor.getGroupActorIds().add("director");
      actor.clearDirty();
      assert !actor.clearDirty();
      actor.getGroupActorIds().clear();
      assert actor.clearDirty();
      
      
      Set<String> someAdditions = new HashSet<String>();
      someAdditions.add("engineering manager");
      someAdditions.add("sales manager");
      Set<String> additions = new HashSet<String>();
      additions.add("marketing manager");
      additions.addAll(someAdditions);
      actor.getGroupActorIds().addAll(additions);
      assert actor.getGroupActorIds().size() == 3;
      assert actor.clearDirty();
      actor.getGroupActorIds().removeAll(additions);
      assert actor.clearDirty();
      assert actor.getGroupActorIds().size() == 0;
      
      actor.getGroupActorIds().addAll(additions);
      assert actor.getGroupActorIds().size() == 3;
      actor.clearDirty();
      actor.getGroupActorIds().retainAll(someAdditions);
      assert actor.clearDirty();
      assert actor.getGroupActorIds().size() == 2;
      
      // Test dirt checking on group actor ids iterator
      actor.getGroupActorIds().add("slave");
      actor.clearDirty();
      Iterator it = actor.getGroupActorIds().iterator();
      assert it.hasNext();
      it.next();
      it.remove();
      assert actor.clearDirty();
      
      
   }

}
