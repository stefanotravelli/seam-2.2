package org.jboss.seam.wicket;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

public abstract class SeamPropertyModel implements IModel
{
   
   private String expression;
   private PropertyModel model;
   
   public SeamPropertyModel(String expression)
   {
      this.expression = expression;
   }

   public abstract Object getTarget();
   
   public Object getObject()
   {
      return getModel().getObject();
   }
   
   public void setObject(Object object)
   {
      getModel().setObject(object);
   }
   
   private PropertyModel getModel()
   {
      if (model == null)
      {
         model = new PropertyModel(getTarget(), expression);
      }
      return model;
   }
   
   public void detach()
   {
      model = null;
   }
   
   public String getPropertyExpression()
   {
      return getModel().getPropertyExpression();
   }

}
