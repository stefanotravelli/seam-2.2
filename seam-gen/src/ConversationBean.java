package @actionPackage@;

import java.io.Serializable;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;

@Stateful
@Name("@componentName@")
public class @beanName@ implements @interfaceName@, Serializable
{
    @Logger private Log log;

    private int value;

    @Begin
    public String begin()
    {
        // implement your begin conversation business logic
        log.info("beginning conversation");
        return "success";
    }
  
    public String increment()
    {
        log.info("incrementing");
        value++;
        return "success";
    }
  
    // add additional action methods that participate in this conversation
  
    @End
    public String end()
    {
        // implement your end conversation business logic
        log.info("ending conversation");
        return "home";
    }
  
    public int getValue()
    {
        return value;
    }
  
    @Remove
    public void destroy() {}

}
