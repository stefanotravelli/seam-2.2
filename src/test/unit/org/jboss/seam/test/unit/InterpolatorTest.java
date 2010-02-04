package org.jboss.seam.test.unit;

import java.text.DateFormat;
import java.util.Date;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Interpolator;
import org.jboss.seam.core.Locale;
import org.testng.Assert;
import org.testng.annotations.Test;

public class InterpolatorTest extends MockContainerTest
{
    private static final String CHOICE_EXPR = "There {0,choice,0#are no files|1#is one file|1<are {0,number,integer} files}.";
    
    @Override
    protected Class[] getComponentsToInstall()
    {
       return new Class[] { Interpolator.class, Locale.class, Expressions.class };
    }
    
    @Test
    public void testInterpolation() 
    {
        Interpolator interpolator = Interpolator.instance();

        Assert.assertEquals(interpolator.interpolate("#0 #1 #2", 3, 5, 7), "3 5 7");
        Assert.assertEquals(interpolator.interpolate("{0} {1} {2}", 3, 5, 7), "3 5 7");

        // this tests that the result of an expression evaluation is not evaluated again
        Assert.assertEquals(interpolator.interpolate("{1}", "bad", "{0}"), "{0}");
        
        // this tests that embedded {} expressions are parsed correctly.
        Assert.assertEquals(interpolator.interpolate(CHOICE_EXPR, 0), "There are no files.");
        Assert.assertEquals(interpolator.interpolate(CHOICE_EXPR, 1), "There is one file.");
        Assert.assertEquals(interpolator.interpolate(CHOICE_EXPR, 2), "There are 2 files.");

        // test sequences of multiple #
        Assert.assertEquals(interpolator.interpolate("#0",2), "2");
        Assert.assertEquals(interpolator.interpolate("##0",2), "#2");
        Assert.assertEquals(interpolator.interpolate("###0",2), "##2");
        
        // test a value expression in the mix
        Contexts.getEventContext().set("contextVariable", "value");
        Assert.assertEquals(interpolator.interpolate("#{contextVariable}"), "value");
        Assert.assertEquals(interpolator.interpolate("#0 #{contextVariable} #1", "a", "z"), "a value z");
        Assert.assertEquals(interpolator.interpolate("#0 ##{contextVariable} #1", "a", "z"), "a #value z");
        
        Date date = new Date(0);
                
        Assert.assertEquals(interpolator.interpolate("{0,date,short}", date), DateFormat.getDateInstance(DateFormat.SHORT).format(date)); 
        
        // test that a messageformat error doesn't blow up
        Assert.assertEquals(interpolator.interpolate("{nosuchmessage}"), "{nosuchmessage}");
        
        try
        {
            interpolator.interpolate("hello #{", (Object) null);
            Assert.fail("interpolator not raised an exception");
        } catch (Throwable t)
        {
            
        }
    }

}
