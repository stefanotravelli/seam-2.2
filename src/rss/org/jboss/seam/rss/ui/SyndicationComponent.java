package org.jboss.seam.rss.ui;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;

public abstract class SyndicationComponent extends UIComponentBase
{
   protected static final String FEED_IMPL_KEY = "theFeed";
   protected static final String ATOM_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

   protected Object valueOf(String name, Object defaultValue)
   {
      Object value = defaultValue;
      if (getValueExpression(name) != null)
      {
         value = getValueExpression(name).getValue(FacesContext.getCurrentInstance().getELContext());
      }
      return value;
   }
   
}
