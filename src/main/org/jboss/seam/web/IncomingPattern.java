package org.jboss.seam.web;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;


public class IncomingPattern {
    String view;
    String pattern;
    ServletMapping viewMapping;

    java.util.regex.Pattern regexp;
    List<String> regexpArgs = new ArrayList<String>();
    
    public IncomingPattern(ServletMapping viewMapping, String view, String pattern) {
        this.view = view;
        this.pattern = pattern;
        this.viewMapping = viewMapping;
        
        parsePattern(pattern);
    }
    
    public Rewrite rewrite(String path) {
           return new IncomingRewrite(path);
    }

    public void parsePattern(String value) {
        StringBuffer expr = new StringBuffer();
          
        expr.append("^");
        while (value.length()>0) {
            int pos = value.indexOf('{');
            if (pos == -1) {
                expr.append(regexpLiteral(value));
                value = "";
            } else {
                int pos2 = value.indexOf('}');
                if (pos2 == -1) {
                    throw new IllegalArgumentException("invalid pattern");
                }
                expr.append(regexpLiteral(value.substring(0,pos)));
                String arg = value.substring(pos+1,pos2);
                expr.append(regexpArg(arg));
                regexpArgs.add(arg);
                value = value.substring(pos2+1);
            }
        }
        expr.append("$");
        
        regexp = java.util.regex.Pattern.compile(expr.toString());
    }

    private String regexpArg(String substring) {
        return "([^/]*)";
    }

    private String regexpLiteral(String value) {
        StringBuffer res = new StringBuffer();
        for (int i=0; i<value.length(); i++) {
            char c = value.charAt(i);
            
            if (Character.isLetterOrDigit(c)) {
                res.append(c);
            } else {
                res.append('\\').append(c);
            }
        }
        return res.toString();
    }
    
    public class IncomingRewrite
        implements Rewrite
    {
        String incoming;
        String queryArgs;

        Boolean isMatch = null;
        private List<String> matchedArgs = new ArrayList<String>();

        public IncomingRewrite(String incoming) {
            int queryPos = incoming.indexOf('?');

            if (queryPos == -1) {
                this.incoming = incoming;
                this.queryArgs = "";
            } else {
                this.incoming = incoming.substring(0, queryPos);
                this.queryArgs = incoming.substring(queryPos+1);
            }

            // don't match trailing slash - it might indicate an empty arg
            // this.incoming = stripTrailingSlash(this.incoming);
        }

//        private String stripTrailingSlash(String text) {
//            if (text.endsWith("/")) {
//                return stripTrailingSlash(text.substring(0,text.length()-1));
//            }
//            return text;
//        }

        public boolean isMatch() {
            if (isMatch == null) {
                isMatch = match();
            }
            return isMatch;
        }

        protected boolean match() {
            if (incoming==null) {
                return false;
            }

            Matcher matcher = regexp.matcher(incoming);
            if (matcher.find()) {
                for (int i=0; i<regexpArgs.size(); i++) {
                    matchedArgs.add(matcher.group(i+1));
                }
                return true;
            }
            return false;
        }

        public String rewrite() {
            StringBuffer result = new StringBuffer();
            
            result.append(mappedURL(view));

            boolean first = true;

            if (queryArgs.length()>0) {
                result.append('?').append(queryArgs);
                first = false;
            }

            for (int i=0; i<regexpArgs.size(); i++) {
                String key   = regexpArgs.get(i);
                String value = matchedArgs.get(i);
                
                if (first) {
                    result.append('?');
                    first = false;
                } else {
                    result.append('&');
                }
                result.append(key).append('=').append(value);
            }

            return result.toString();
        }
    }
    
    private String mappedURL(String viewId) {
        return viewMapping.mapViewIdToURL(viewId);
    }
}
