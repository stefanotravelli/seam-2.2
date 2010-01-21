package org.jboss.seam.test.unit;

import org.jboss.seam.framework.EntityQuery;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class QueryTest
{
   /**
    * These tests verify that the count query is properly extracted from the rendered
    * query. There are two points of focus. The first is that "join fetch" is replaced
    * with "join" in the where clause. The second is that the subject of the query is
    * used in the count() function unless the query does not have an explicit subject,
    * in which case a * is used instead.
    */
   @Test
   public void testCountQuery() {
      UnitQuery query = new UnitQuery();
      query.setEjbql("from Person p");
      query.parseEjbql();
      assertEquals(query.getCountEjbql(), "select count(*) from Person p");
      
      query.setEjbql("from Person p where p.location is not null");
      query.setOrderColumn("username");
      query.parseEjbql();
      assertEquals(query.getCountEjbql(), "select count(*) from Person p where p.location is not null");
      
      query.setEjbql("select p from Person p");
      query.setOrderColumn("username");
      query.parseEjbql();
      // TODO this should eventually become count(p)
      assertEquals(query.getCountEjbql(), "select count(*) from Person p");
      
      query.setEjbql("select v from Vehicle v join fetch v.person");
      query.setOrderColumn("make");
      query.parseEjbql();
      // TODO this should eventually become count(v)
      assertEquals(query.getCountEjbql(), "select count(*) from Vehicle v join v.person");
      
      query.setEjbql("select v.person from Vehicle v left join fetch v.person");
      query.parseEjbql();
      // TODO this should eventually become count(v.person)
      assertEquals(query.getCountEjbql(), "select count(*) from Vehicle v left join v.person");
      
      query.setEjbql("select v.person, v.color from Vehicle v");
      query.parseEjbql();
      assertEquals(query.getCountEjbql(), "select count(*) from Vehicle v");
      
      query = new CompliantUnitQuery();
      query.setEjbql("select p from Person p");
      query.parseEjbql();
      assertEquals(query.getCountEjbql(), "select count(p) from Person p");
   }

   class UnitQuery extends EntityQuery {

      @Override
      protected void parseEjbql()
      {
         super.parseEjbql();
      }

      @Override
      protected String getRenderedEjbql()
      {
         return super.getRenderedEjbql();
      }

      @Override
      protected String getCountEjbql()
      {
         return super.getCountEjbql();
      }
      
   }

   class CompliantUnitQuery extends UnitQuery {

      public CompliantUnitQuery() {
         setUseWildcardAsCountQuerySubject(false);
      }
      
   }
}
