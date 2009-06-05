package org.jboss.seam.wicket.ioc;

import java.lang.annotation.Annotation;

/**
 * A bijected attribute (field or get/set pair)
 * @author Pete Muir
 *
 *
 * TODO Move into Seam core 
 */
public interface BijectedAttribute<T extends Annotation> extends InjectedAttribute<T>
{

   public String getContextVariableName();
   public Object get(Object bean);
   
}
