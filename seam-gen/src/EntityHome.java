package @actionPackage@;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import @modelPackage@.@entityName@;

@Name("@homeName@")
public class @entityName@Home extends EntityHome<@entityName@>
{
    @RequestParameter Long @componentName@Id;

    @Override
    public Object getId()
    {
        if (@componentName@Id == null)
        {
            return super.getId();
        }
        else
        {
            return @componentName@Id;
        }
    }

    @Override @Begin
    public void create() {
        super.create();
    }

}
