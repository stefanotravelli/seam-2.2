package org.jboss.seam.ui.renderkit;

import static org.jboss.seam.ui.util.HTML.SCRIPT_ELEM;
import static org.jboss.seam.ui.util.HTML.SCRIPT_LANGUAGE_ATTR;
import static org.jboss.seam.ui.util.HTML.SCRIPT_LANGUAGE_JAVASCRIPT;
import static org.jboss.seam.ui.util.HTML.SCRIPT_TYPE_ATTR;
import static org.jboss.seam.ui.util.HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.ui.util.cdk.RendererBase;

/**
 * @author Pete Muir
 * @author sim
 *
 */
public class DefaultActionRendererBase extends RendererBase
{
   
   public static final String MARK = "org.jboss.seam.ui.DefaultAction";
   
   private static LogProvider log = Logging.getLogProvider(DefaultActionRendererBase.class);
 
   @Override
   protected void doEncodeEnd(ResponseWriter writer, FacesContext context,
         UIComponent component) throws IOException
   {
      UIComponent actionComponent = component.getParent();
      String actionComponentId = actionComponent.getClientId(context); 
      UIComponent form = getUtils().getForm(actionComponent);
      if (form == null)
      {
         log.warn("Must embed default action inside a form");
      }
      else 
      { 
         String formId = form.getClientId(context);
         writer.startElement(SCRIPT_ELEM, component);
         writer.writeAttribute(SCRIPT_LANGUAGE_ATTR, SCRIPT_LANGUAGE_JAVASCRIPT, SCRIPT_LANGUAGE_ATTR);
         writer.writeAttribute(SCRIPT_TYPE_ATTR, SCRIPT_TYPE_TEXT_JAVASCRIPT, SCRIPT_TYPE_ATTR);
         if (actionComponent.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
         {
            log.warn("Must set an id for the default action source");
         }
         if (form.getAttributes().containsKey(MARK))
         {
            if (!form.getAttributes().get(MARK).equals(component.getClientId(context)))
            {
               log.warn("Can only specify one default action per form");
            }
         }
         else
         {
            form.getAttributes().put(MARK, component.getClientId(context));
         }
         String functionBody = 
            "{var keycode;" + 
            "if (window.event) keycode = window.event.keyCode;" +
            "else if (event) keycode = event.which;" +
            "else return true;" + 
            "if (keycode == 13) " +
            "{ document.getElementById('" + actionComponentId + "').click();return false; } " +
            "else return true; }";
         String functionCode = 
            "document.forms['" + formId + "'].onkeypress = " +
            "new Function(\"event\", \"" + functionBody + "\");"; 
         writer.write(functionCode);
         writer.endElement("script");
      }
   }

   @Override
   protected Class getComponentClass()
   {
      return UIOutput.class;
   }
}