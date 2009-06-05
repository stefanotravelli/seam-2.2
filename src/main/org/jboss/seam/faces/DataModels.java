package org.jboss.seam.faces;

import static org.jboss.seam.ScopeType.STATELESS;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.model.DataModel;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.framework.Query;
import org.jboss.seam.jsf.ArrayDataModel;
import org.jboss.seam.jsf.ListDataModel;
import org.jboss.seam.jsf.MapDataModel;
import org.jboss.seam.jsf.SetDataModel;

/**
 * Wraps a collection as a JSF {@link DataModel}. May be overridden
 * and extended if you don't like the built in collections
 * which are supported: list, map, set, array.
 *
 * @author pmuir
 */
@Name("org.jboss.seam.faces.dataModels")
@Install(precedence=BUILT_IN, classDependencies="javax.faces.context.FacesContext")
@Scope(STATELESS)
@BypassInterceptors
public class DataModels
{
   
   /**
    * Wrap the value in a DataModel
    * 
    * This implementation supports {@link List}, {@link Map}, {@link Set} and
    * arrays
    */
   public DataModel getDataModel(Object value)
   {
      if (value instanceof List)
      {
         return new ListDataModel( (List) value );
      }
      else if (value instanceof Object[])
      {
         return new ArrayDataModel( (Object[]) value ); 
      }
      else if (value instanceof Map)
      {
         return new MapDataModel( (Map) value );
      }
      else if (value instanceof Set)
      {
         return new SetDataModel( (Set) value );
      }
      else
      {
         throw new IllegalArgumentException("unknown collection type: " + value.getClass());
      }
   }
   
   /**
    * Wrap the the Seam Framework {@link Query} in a JSF DataModel
    */
   public DataModel getDataModel(Query query)
   {
      return getDataModel( query.getResultList() );
   }
   
   public static DataModels instance()
   {
      return (DataModels) Component.getInstance(DataModels.class, ScopeType.STATELESS);
   }
   
}
