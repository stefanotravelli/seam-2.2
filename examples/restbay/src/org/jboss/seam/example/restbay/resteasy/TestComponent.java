package org.jboss.seam.example.restbay.resteasy;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.AutoCreate;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Christian Bauer
 */
@Name("testComponent")
@AutoCreate
public class TestComponent
{

   public List<String[]> getCommaSeparated() {
      List<String[]> csv = new ArrayList();
      csv.add(new String[]{"foo", "bar"});
      csv.add(new String[]{"asdf", "123"});
      return csv;
   }

   public String getTestString() {
      return "abc";
   }
}
