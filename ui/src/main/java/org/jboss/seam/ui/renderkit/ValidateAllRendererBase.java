package org.jboss.seam.ui.renderkit;

import java.io.IOException;
import java.util.List;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.seam.ui.component.UIValidateAll;
import org.jboss.seam.ui.util.cdk.RendererBase;
import org.jboss.seam.ui.validator.ModelValidator;

public class ValidateAllRendererBase extends RendererBase
{

   @Override
   protected Class getComponentClass()
   {
      return UIValidateAll.class;
   }
   
   @Override
   protected void doEncodeChildren(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
   {      
      renderChildren(context, component);
      
      UIValidateAll validateAll = (UIValidateAll) component;
      if (!validateAll.isValidatorsAdded())
      {
         addValidators(validateAll.getChildren());
         validateAll.setValidatorsAdded(true);
      }
      
   }
   
   private void addValidators(List children)
   {
      for (Object child: children)
      {
         if (child instanceof EditableValueHolder)
         {
            EditableValueHolder evh =  (EditableValueHolder) child;
            if ( evh.getValidators().length==0 )
            {
               evh.addValidator( new ModelValidator() );
            }
         }
         addValidators( ( (UIComponent) child ).getChildren() );
      }
   }
   
   @Override
   public boolean getRendersChildren()
   {
      return true;
   }


}
