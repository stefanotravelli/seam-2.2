package org.jboss.seam.test.unit.web;

import org.testng.annotations.Test;

import org.jboss.seam.web.Pattern;
import org.jboss.seam.web.Rewrite;
import org.jboss.seam.web.ServletMapping;

import static org.testng.Assert.*;

public class RewriteTest 
{
    @Test
    public void testBasicInPattern()
        throws Exception
    {
        Pattern pattern = new Pattern("/foo.seam", "/foo");
        pattern.setViewMapping(new ServletMapping("*.seam"));
                 
        testNoMatchIn(pattern, "/bar");
        testNoMatchIn(pattern, "/fool");
        testNoMatchIn(pattern, "/foo.seam");
        testNoMatchIn(pattern, "/foo/");
        testNoMatchIn(pattern, "/foo/bar");

        testMatchIn(pattern, "/foo",     "/foo.seam");
        testMatchIn(pattern, "/foo?x=y", "/foo.seam?x=y");   
    }
    
    @Test
    public void testSingleArgInPattern()
        throws Exception
    {
        Pattern pattern = new Pattern("/foo.seam", "/foo/{id}");
        pattern.setViewMapping(new ServletMapping("*.seam"));
                        
        testNoMatchIn(pattern, "/foo");
        testNoMatchIn(pattern, "/foo/bar/baz");
        testNoMatchIn(pattern, "/foo/bar/baz?x=y");
        testNoMatchIn(pattern, "/foo/bar/?x=y");

        testMatchIn(pattern, "/foo/",         "/foo.seam?id=");
        testMatchIn(pattern, "/foo/bar",      "/foo.seam?id=bar");
        testMatchIn(pattern, "/foo/bar?x=y",  "/foo.seam?x=y&id=bar");
    }

    @Test
    public void testMultiArgInPattern()
        throws Exception
    {
        Pattern pattern = new Pattern("/foo.seam", "/foo/{id}/{action}");
        pattern.setViewMapping(new ServletMapping("*.seam"));
                              
        testNoMatchIn(pattern, "/foo");
        testNoMatchIn(pattern, "/foo/bar");
        testNoMatchIn(pattern, "/foo/bar/baz/qux");

        testMatchIn(pattern, "/foo/bar/baz",     "/foo.seam?id=bar&action=baz");
        testMatchIn(pattern, "/foo/bar/baz?x=y", "/foo.seam?x=y&id=bar&action=baz");
    }
    
    
    @Test
    public void testBasicOutPattern()
        throws Exception
    {
        Pattern pattern = new Pattern("/foo.seam", "/foo");
        pattern.setViewMapping(new ServletMapping("*.seam"));
                 
        testNoMatchOut(pattern, "/bar.seam");
        testNoMatchOut(pattern, "/fool.seam");
        testNoMatchOut(pattern, "/foo");
        
        testMatchOut(pattern, "/foo.seam",      "/foo");
        testMatchOut(pattern, "/foo.seam?x=y",  "/foo?x=y");
    }
    
    
    @Test
    public void testSingleArgOutPattern()
        throws Exception
    {
        Pattern pattern = new Pattern("/foo.seam", "/foo/{id}");
        pattern.setViewMapping(new ServletMapping("*.seam"));
                        
        testNoMatchOut(pattern, "/foo.seam");
        testNoMatchOut(pattern, "/foo.seam?x=y");
        testNoMatchOut(pattern, "/foo.seam/bar");       
        //should this match?  
        //testNoMatchOut(pattern, "/foo.seam/bar?id=test");       
        
        testMatchOut(pattern, "/foo.seam?id=bar",      "/foo/bar");
        testMatchOut(pattern, "/foo.seam?x=y&id=bar",  "/foo/bar?x=y");
        testMatchOut(pattern, "/foo.seam?id=bar&x=y",  "/foo/bar?x=y");
        testMatchOut(pattern, "/foo.seam?a=b&x=y&id=bar&c=d&c=e",  "/foo/bar?a=b&x=y&c=d&c=e");
    }
    
    @Test
    public void testMultiArgOutPattern()
        throws Exception
    {
        Pattern pattern = new Pattern("/foo.seam", "/foo/{id}/{action}");
        pattern.setViewMapping(new ServletMapping("*.seam"));
                              
        testNoMatchOut(pattern, "/foo.seam");
        testNoMatchOut(pattern, "/foo.seam?id=bar");
        testNoMatchOut(pattern, "/foo.seam?action=baz");

        testMatchOut(pattern, "/foo.seam?action=baz&id=bar", "/foo/bar/baz");
        testMatchOut(pattern, "/foo.seam?y=z&action=baz&n=one&n=two&id=bar&x=y", "/foo/bar/baz?y=z&n=one&n=two&x=y");
    }
    
    
    public void testNoMatchIn(Pattern pattern, String incoming) {
        assertNull(pattern.matchIncoming(incoming), incoming);
    }
    
    public void testNoMatchOut(Pattern pattern, String incoming) {
        assertNull(pattern.matchOutgoing(incoming), incoming);
    }
    
    public void testMatchIn(Pattern pattern, String incoming, String expected) {
        Rewrite rewrite = pattern.matchIncoming(incoming);
        assertTrue(rewrite.isMatch(), incoming);
        assertEquals(rewrite.rewrite(), expected);
    }
    
    public void testMatchOut(Pattern pattern, String incoming, String expected) {
        Rewrite rewrite = pattern.matchOutgoing(incoming);
        assertTrue(rewrite.isMatch(), incoming);
        assertEquals(rewrite.rewrite(), expected);
    }

}

