package org.jboss.seam.test.unit.component;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;

@Name("configurableComponent")
@Install(false)
public class ConfigurableComponent {
    private PrimaryColor primaryColor;

    public PrimaryColor getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor( PrimaryColor primaryColor ) {
        this.primaryColor = primaryColor;
    }
}
