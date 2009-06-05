package org.jboss.seam.pdf.ui;

import java.awt.Component;
import java.awt.Graphics2D;

public class UISwingComponent extends UIGraphics2D
{
   private Component component;

   public void setComponent(Component component)
   {
      this.component = component;
   }

   public Component getComponent()
   {
      return (Component) valueBinding("component", component);
   }

   @Override
   public void render(Graphics2D g2)
   {
      // don't use the component variable directly!
      // we need to check for the valueBinding
      Component component = getComponent();

      if (component == null)
      {
         throw new RuntimeException("Component was null");
      }

      // setSize() is very important. The default size
      // for this component is zero, which means it will not display
      // unless the size is set
      component.setSize(getWidth(), getHeight());

      component.paint(g2);
   }
}
