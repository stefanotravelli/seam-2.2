package @actionPackage@;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;
import org.jboss.seam.international.StatusMessages;
import org.hibernate.validator.Length;

@Name("@componentName@")
public class @interfaceName@
{
    @Logger private Log log;

    @In StatusMessages statusMessages;

    private String value;

    public void @methodName@()
    {
        // implement your business logic here
        log.info("@componentName@.@methodName@() action called with: #{@componentName@.value}");
        statusMessages.add("@methodName@ #{@componentName@.value}");
    }

    // add additional action methods
  
    @Length(max = 10)
    public String getValue()
    {
        return value;
    }
  
    public void setValue(String value)
    {
        this.value = value;
    }
  
}
