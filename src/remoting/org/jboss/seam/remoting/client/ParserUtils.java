package org.jboss.seam.remoting.client;

import org.dom4j.Element;
import org.jboss.seam.remoting.CallContext;
import org.jboss.seam.remoting.wrapper.Wrapper;
import java.util.Iterator;

/**
 *
 *
 * @author Shane Bryzak
 */
public class ParserUtils
{


  public static Object unmarshalResult(Element resultElement)
  {
    Element valueElement = resultElement.element("value");
    Element refsElement = resultElement.element("refs");

    CallContext ctx = new CallContext();

    Iterator iter = refsElement.elementIterator("ref");
    while (iter.hasNext())
    {
      ctx.createWrapperFromElement((Element) iter.next());
    }

    Wrapper resultWrapper = ctx.createWrapperFromElement((Element) valueElement.elementIterator().next());

    // Now unmarshal the ref values
    for (Wrapper w : ctx.getInRefs().values())
      w.unmarshal();

    return resultWrapper.getValue();
  }


}
