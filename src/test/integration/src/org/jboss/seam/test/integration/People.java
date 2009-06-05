package org.jboss.seam.test.integration;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.datamodel.DataModelSelection;

@Name("people")
public class People
{
   @org.jboss.seam.annotations.datamodel.DataModel(scope=ScopeType.PAGE)
   private List<Person> peopleList;
   
   @DataModelSelection
   private Person selectedPerson;
   
   @Factory("peopleList")
   public void peopleFactory()
   {
      peopleList = new ArrayList<Person>();
      peopleList.add(new Person("Gavin"));
      peopleList.add(new Person("Pete"));
      peopleList.add(new Person("Shane"));
      peopleList.add(new Person("Norman"));
   }
   
   public Person getSelectedPerson()
   {
      return selectedPerson;
   }

}
