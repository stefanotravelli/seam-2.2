package org.jboss.seam.ui.renderkit;

import java.io.IOException;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.validator.Validator;

import org.jboss.seam.ui.component.UIEqualityValidator;
import org.jboss.seam.ui.util.cdk.RendererBase;
import org.jboss.seam.ui.validator.EqualityValidator;

/**
 * This class mainly does some validation "hook-in"
 * 
 * @author Daniel Roth
 * 
 */
public class EqualityValidatorRendererBase extends RendererBase
{

   @Override
   protected Class getComponentClass()
   {
      return UIEqualityValidator.class;
   }

   @Override
   protected void doEncodeChildren(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
   {
      UIEqualityValidator ev = (UIEqualityValidator) component;
      EditableValueHolder evh = null;
      if (ev != null && ev.getParent() instanceof EditableValueHolder)
      {
         evh = (EditableValueHolder) ev.getParent();
      }

      if (evh == null)
         throw new IllegalArgumentException("validateEquality tag must be nested in an EditableValueHolder (\"input tag\")");

      if (!hasEqualityValidator(evh))
      {
         evh.addValidator(new EqualityValidator(ev.getFor(), ev.getMessage(), ev.getMessageId(), ev.getOperator()));
         evh.setRequired(ev.isRequired());
      }

      renderChildren(context, component);
   }

   private boolean hasEqualityValidator(EditableValueHolder evh)
   {
      for (Validator validator : evh.getValidators())
      {
         if (validator instanceof EqualityValidator)
         {
            return true;
         }
      }
      return false;
   }

   @Override
   public boolean getRendersChildren()
   {
      return true;
   }

}
