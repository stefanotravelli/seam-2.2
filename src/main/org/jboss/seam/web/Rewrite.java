package org.jboss.seam.web;

public interface Rewrite {
    public boolean isMatch();
    public String rewrite();
}
