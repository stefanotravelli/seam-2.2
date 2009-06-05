package org.jboss.seam.ui.renderkit;

import static org.jboss.seam.ui.util.HTML.SCRIPT_ELEM;
import static org.jboss.seam.ui.util.HTML.SCRIPT_LANGUAGE_ATTR;
import static org.jboss.seam.ui.util.HTML.SCRIPT_LANGUAGE_JAVASCRIPT;
import static org.jboss.seam.ui.util.HTML.SCRIPT_TYPE_ATTR;
import static org.jboss.seam.ui.util.HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.ui.util.cdk.RendererBase;

/**
 * @author Pete Muir
 *
 */
public abstract class CommandButtonParameterRendererBase extends RendererBase
{
   
   protected abstract LogProvider getLog();
   
   protected abstract String getParameterName(UIComponent component);

   @Override
   protected void doEncodeEnd(ResponseWriter writer, FacesContext context, UIComponent component)
         throws IOException
   {
      UIComponent actionComponent = component.getParent();
      UIComponent form = getUtils().getForm(actionComponent);
      UIParameter parameter = (UIParameter) component;
      if (getUtils().isCommandButton(actionComponent))
      { 
         String formId = form.getClientId(context);
         writer.startElement(SCRIPT_ELEM, component);
         writer.writeAttribute(SCRIPT_LANGUAGE_ATTR, SCRIPT_LANGUAGE_JAVASCRIPT, SCRIPT_LANGUAGE_ATTR);
         writer.writeAttribute(SCRIPT_TYPE_ATTR, SCRIPT_TYPE_TEXT_JAVASCRIPT, SCRIPT_TYPE_ATTR);
         if (actionComponent.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
         {
            getLog().warn("Must set an id for the command buttons with s:conversationPropagation");
         }
         
         
         String functionBody = 
            "{" +
               "if (document.getElementById)" +
               "{" + 
                  "var form = document.getElementById('" + formId + "');" +
                  "var input = document.createElement('input');" +
                  "if (document.all)" +
                  "{ " + // what follows should work with NN6 but doesn't in M14"
                     "input.type = 'hidden';" +
                     "input.name = '" + getParameterName(component) + "';" + 
                     "input.value = '" + parameter.getValue() + "';" +
                  "}" +
                  "else if (document.getElementById) " +
                  "{" +  // so here is theNN6 workaround
                     "input.setAttribute('type', 'hidden');" + 
                     "input.setAttribute('name', '" + getParameterName(component) + "');" + 
                     "input.setAttribute('value', '" + parameter.getValue() + "');" +
                  "}" +
                  "form.appendChild(input);" +
                  "return true;" +
               "}" +
            "}";
         
         String functionName = "cp_" + actionComponent.getId();
         
         String functionCode = 
             "var " + functionName + " = " +
             "new Function(\"event\", \"" + functionBody + "\");";
         
         writer.write(functionCode);
         
         // We are either written before the HTML element (e.g. a:commandButton)
         // In this case we can simply prepend to the existing onClick
         
         String existingOnClick = (String) actionComponent.getAttributes().get("onclick");
         
         actionComponent.getAttributes().put("onclick", functionName + "();" + existingOnClick);
         
         // But we also might be written after (e.g. JSF RI h:commandButton
         // In this case we can use event capture
         
         String functionRegister = 
             "if (document.getElementById('" + actionComponent.getClientId(context) + "'))" +
             "{" +
                 "document.getElementById('" + actionComponent.getClientId(context) + "').onclick = new Function(\"event\", \"" + functionBody + "\");" +
             "}";
         writer.write(functionRegister);
         writer.endElement("script");
      }
   }

}