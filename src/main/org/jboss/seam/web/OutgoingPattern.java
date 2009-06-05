package org.jboss.seam.web;

import java.util.ArrayList;
import java.util.List;

public class OutgoingPattern {
    String view;
    String pattern;
    ServletMapping viewMapping;

    List<String> parts = new ArrayList<String>();

    public OutgoingPattern(ServletMapping viewMapping, String view, String pattern) {
        this.view = view;
        this.pattern = pattern;
        this.viewMapping = viewMapping;

        parsePattern(pattern);
    }

    public Rewrite rewrite(String path) {
        return new OutgoingRewrite(path);
    }

    private void parsePattern(String value) {       
        while (value.length()>0) {
            int pos = value.indexOf('{');
            if (pos == -1) {
                parts.add(value);
                value = "";
            } else {
                int pos2 = value.indexOf('}');
                if (pos2 == -1) {
                    throw new IllegalArgumentException("invalid pattern");
                }
                parts.add(value.substring(0,pos));
                parts.add(value.substring(pos,pos2+1));
                value = value.substring(pos2+1);
            }
        }
    }

    public class OutgoingRewrite 
        implements Rewrite
    {
        Boolean isMatch;

        private String base;
        private List<String> queryArgs     = new ArrayList<String>();
        private List<String> matchedArgs   = new ArrayList<String>();

        public OutgoingRewrite(String outgoing) {           
            int queryPos = outgoing.indexOf('?');

            if (queryPos == -1) {
                this.base = outgoing;
            } else {
                this.base = outgoing.substring(0, queryPos);
                parseArgs(outgoing.substring(queryPos+1));
            }
        }

        private void parseArgs(String text) {
            for (String part: text.split("\\&")) {
                queryArgs.add(part);
            }
        }

        public boolean isMatch() {
            if (isMatch == null) {
                isMatch = match();
            }
            return isMatch;
        }

        private boolean match() {
            if (!viewMapping.isMapped(base,view)) {
                return false;
            }

            for (String part: parts) {
                if (part.startsWith("{") && part.endsWith("}")) {
                    String name = part.substring(1,part.length()-1);
                    String value = matchArg(name);

                    if (value == null) {
                        return false;
                    }

                    matchedArgs.add(value);
                }
            }

            return true;
        }

        


        private String matchArg(String argName) {
            for (int i=0; i<queryArgs.size(); i++) {
                String query = queryArgs.get(i);
                int pos = query.indexOf("=");

                if (query.subSequence(0, pos).equals(argName)) {
                    queryArgs.remove(i);
                    return query.substring(pos+1);
                }
            }
            return null;
        }

        public String rewrite() {
            StringBuffer res = new StringBuffer();

            int matchedPosition = 0;
            for (String part: parts) {
                if (part.startsWith("{")) { 
                    res.append(matchedArgs.get(matchedPosition++));
                } else {
                    res.append(part);
                }
            }

            char sep = '?';
            for (String arg: queryArgs) {
                res.append(sep).append(arg);
                sep = '&';
            }

            return res.toString();
        }
    }
}
