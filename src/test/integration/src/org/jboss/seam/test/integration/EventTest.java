package org.jboss.seam.test.integration;

import org.jboss.seam.Component;
import org.jboss.seam.core.Events;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;


/**
 * 
 * @author Pete Muir
 *
 */
public class EventTest extends SeamTest {

    @Test
    public void testEventChain() throws Exception {

        new FacesRequest("/index.xhtml") {

            @Override
            protected void invokeApplication() throws Exception {
                BeanA beanA = (BeanA) Component.getInstance("beanA");
                BeanB beanB = (BeanB) Component.getInstance("beanB");

                assert "Foo".equals(beanA.getMyValue());
                assert beanB.getMyValue() == null;

                Events.instance().raiseEvent("BeanA.refreshMyValue");

                beanA = (BeanA) Component.getInstance("beanA");
                
                assert "Bar".equals(beanA.getMyValue());        
            }
            
            @Override
            protected void renderResponse() throws Exception
            {
               BeanB beanB = (BeanB) Component.getInstance("beanB");
               assert "Bar".equals(beanB.getMyValue());
            }
        }.run();
    }

}


