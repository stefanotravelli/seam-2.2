package @actionPackage@;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.io.Serializable;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Scope(CONVERSATION)
@Name("@componentName@")
public class @interfaceName@ implements Serializable
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

}
