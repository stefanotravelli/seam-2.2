package @actionPackage@;

import javax.ejb.Local;

@Local
public interface @interfaceName@
{
    public String begin();
    public String increment();
    public String end();
    public int getValue();
    public void destroy();
  
    // add additional interface methods here

}
