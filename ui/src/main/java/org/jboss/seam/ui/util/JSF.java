/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.seam.ui.util;

import java.io.IOException;
import java.util.List;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * Constant declarations for JSF tags
 * 
 * @author Anton Koinov 
 * @author Manfred Geiler
 * 
 */
public class JSF
{

   // Common Attributes
   public static final String ID_ATTR = "id";

   public static final String VALUE_ATTR = "value";

   public static final String BINDING_ATTR = "binding";

   public static final String STYLE_CLASS_ATTR = "styleClass";

   public static final String ESCAPE_ATTR = "escape";

   // Common Output Attributes
   public static final String FOR_ATTR = "for";

   public static final String CONVERTER_ATTR = "converter";

   // Ouput_Time Attributes
   public static final String TIME_STYLE_ATTR = "timeStyle";

   public static final String TIMEZONE_ATTR = "timezone";

   // Common Input Attributes
   public static final String REQUIRED_ATTR = "required";

   public static final String VALIDATOR_ATTR = "validator";

   // Input_Secret Attributes
   public static final String REDISPLAY_ATTR = "redisplay";

   // Input_Checkbox Attributes
   public static final String LAYOUT_ATTR = "layout";

   // Select_Menu Attributes
   public static final String SIZE_ATTR = "size";

   // SelectMany Checkbox List/ Select One Radio Attributes
   public static final String BORDER_ATTR = "border";

   public static final String DISABLED_CLASS_ATTR = "disabledClass";

   public static final String ENABLED_CLASS_ATTR = "enabledClass";

   // Common Command Attributes
   /** @deprecated */
   public static final String COMMAND_CLASS_ATTR = "commandClass";

   public static final String LABEL_ATTR = "label";

   public static final String IMAGE_ATTR = "image";

   public static final String ACTION_ATTR = "action";

   public static final String IMMEDIATE_ATTR = "immediate";

   // Command_Button Attributes
   public static final String TYPE_ATTR = "type";

   // Common Panel Attributes
   /** @deprecated */
   public static final String PANEL_CLASS_ATTR = "panelClass";

   public static final String FOOTER_CLASS_ATTR = "footerClass";

   public static final String HEADER_CLASS_ATTR = "headerClass";

   public static final String COLUMN_CLASSES_ATTR = "columnClasses";

   public static final String ROW_CLASSES_ATTR = "rowClasses";

   // Panel_Grid Attributes
   public static final String COLUMNS_ATTR = "columns";

   public static final String COLSPAN_ATTR = "colspan"; // extension

   // UIMessage and UIMessages attributes
   public static final String SHOW_SUMMARY_ATTR = "showSummary";

   public static final String SHOW_DETAIL_ATTR = "showDetail";

   public static final String GLOBAL_ONLY_ATTR = "globalOnly";

   // HtmlOutputMessage attributes
   public static final String ERROR_CLASS_ATTR = "errorClass";

   public static final String ERROR_STYLE_ATTR = "errorStyle";

   public static final String FATAL_CLASS_ATTR = "fatalClass";

   public static final String FATAL_STYLE_ATTR = "fatalStyle";

   public static final String INFO_CLASS_ATTR = "infoClass";

   public static final String INFO_STYLE_ATTR = "infoStyle";

   public static final String WARN_CLASS_ATTR = "warnClass";

   public static final String WARN_STYLE_ATTR = "warnStyle";

   public static final String TITLE_ATTR = "title";

   public static final String TOOLTIP_ATTR = "tooltip";

   // GraphicImage attributes
   public static final String URL_ATTR = "url";

   // UISelectItem attributes
   public static final String ITEM_DISABLED_ATTR = "itemDisabled";

   public static final String ITEM_DESCRIPTION_ATTR = "itemDescription";

   public static final String ITEM_LABEL_ATTR = "itemLabel";

   public static final String ITEM_VALUE_ATTR = "itemValue";

   // UIData attributes
   public static final String ROWS_ATTR = "rows";

   public static final String VAR_ATTR = "var";

   public static final String FIRST_ATTR = "first";

   public static void renderChildren(FacesContext facesContext,
         UIComponent component) throws IOException
   {
      List children = component.getChildren();
      for (int j=0, size = component.getChildCount(); j<size; j++)
      {
         UIComponent child = (UIComponent) children.get(j);
         renderChild(facesContext, child);
      }
   }

   public static void renderChild(FacesContext facesContext, UIComponent child)
         throws IOException
   {
      if ( child.isRendered() )
      {
         child.encodeBegin(facesContext);
         if ( child.getRendersChildren() )
         {
            child.encodeChildren(facesContext);
         } 
         else
         {
            renderChildren(facesContext, child);
         }
         child.encodeEnd(facesContext);
      }
   }

   public static String getStringValue(FacesContext context, ValueExpression vb)
   {
       Object value = vb.getValue(context.getELContext());
       if (value == null)
       {
           return null;
       }
       return value.toString();
   }
   
   public static Integer getIntegerValue(FacesContext context, ValueExpression vb)
   {
       String value = getStringValue(context, vb);
       if (value == null)
       {
           return null;
       }
       return new Integer(value);
   }
   
   public static Double getDoubleValue(FacesContext context, ValueExpression vb)
   {
       String value = getStringValue(context, vb);
       if (value == null)
       {
           return null;
       }
       return new Double(value);
   }
   
   public static Boolean getBooleanValue(FacesContext context, ValueExpression vb)
   {
       Object value = vb.getValue(context.getELContext());
       if (value == null)
       {
          return null;
       }
       if (value instanceof Boolean) 
       {
          return (Boolean) value;
       }
       else
       {
          return Boolean.valueOf(value.toString());
       }
   }

}
