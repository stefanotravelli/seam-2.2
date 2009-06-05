package org.jboss.seam.remoting.wrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * @author Shane Bryzak
 */
public class NullWrapper extends BaseWrapper implements Wrapper
{
  private static final byte[] NULL_WRAPPER_TAG = "<null/>".getBytes();

  public void marshal(OutputStream out) throws IOException
  {
    out.write(NULL_WRAPPER_TAG);
  }

  public Object convert(Type type)
      throws ConversionException
  {
    return null;
  }

  public ConversionScore conversionScore(Class cls)
  {
    return ConversionScore.compatible;
  }
}
