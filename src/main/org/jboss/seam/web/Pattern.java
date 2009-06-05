package org.jboss.seam.web;

public class Pattern 
{
    String view;
    String pattern;
    ServletMapping viewMapping;
    
    IncomingPattern inPattern;
    OutgoingPattern outPattern;

    public Pattern(String view, String pattern) {
        this.view = view;
        this.pattern = pattern;
    }    
    
    // not necessarily available when pattern is created 
    public void setViewMapping(ServletMapping viewMapping) {
       this.viewMapping = viewMapping;
    }
    
    protected IncomingPattern inPattern() {
       if (inPattern == null) {
          inPattern = new IncomingPattern(viewMapping, view, pattern);
       }
       return inPattern;
    }
    
    protected OutgoingPattern outPattern() {
       if (outPattern == null) {
          outPattern = new OutgoingPattern(viewMapping, view, pattern);
       }
       return outPattern;
    }

    public Rewrite matchIncoming(String path) {
        return returnIfMatch(inPattern().rewrite(path));
    }

    public Rewrite matchOutgoing(String path) {
        return returnIfMatch(outPattern().rewrite(path));
    }
    
    @Override
    public String toString() {
        return "Pattern(" + view + ":" + pattern + ")";
    }

    private Rewrite returnIfMatch(Rewrite rewrite) {
        return rewrite.isMatch() ? rewrite : null;
    }
}
