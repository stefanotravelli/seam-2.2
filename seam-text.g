header
{
package org.jboss.seam.text;
}

class SeamTextParser extends Parser;
options
{
    k=4;
    defaultErrorHandler=false;
}
{   
    public class Macro {
        public String name;
        public java.util.SortedMap<String,String> params = new java.util.TreeMap<String,String>();

        public Macro(String name) {
            this.name = name;
        }
    }

     public class HtmlRecognitionException extends RecognitionException {
         Token openingElement;
         RecognitionException wrappedException;

         public HtmlRecognitionException(Token openingElement, RecognitionException wrappedException) {
             this.openingElement = openingElement;
             this.wrappedException = wrappedException;
         }

         public Token getOpeningElement() {
             return openingElement;
         }

         public String getMessage() {
             return wrappedException.getMessage();
         }

         public Throwable getCause() {
             return wrappedException;
         }
     }

    /**
     * Sanitization of user input, used to clean links and plain HTML.
     */
    public interface Sanitizer {

        /**
         * Called by the SeamTextParser when a link tag is parsed, i.e. [=>some URI].
         *
         * @param element the token of the parse tree, here the ">" symbol which comes after the "="
         * @param uri the user-entered link text
         * @throws SemanticException thrown if the URI is not syntactically or semantically valid
         */
        public void validateLinkTagURI(Token element, String uri) throws SemanticException;

        /**
         * Called by the SeamTextParser when a plain HTML element is parsed.
         *
         * @param element the token of the parse tree, call <tt>getText()</tt> to access the HTML tag name
         * @throws SemanticException thrown when the HTML tag is not valid
         */
        public void validateHtmlElement(Token element) throws SemanticException;

        /**
         * Called by the SeamTextParser when a plain HTML attribute is parsed.
         *
         * @param element the token of the parse tree that represents the HTML tag
         * @param attribute the token of the parse tree that represents the HTML attribute
         * @throws SemanticException thrown if the attribute is not valid for the given HTML tag
         */
        public void validateHtmlAttribute(Token element, Token attribute) throws SemanticException;

        /**
         * Called by the SeamTextParser when a plain HTML attribute value is parsed.
         *
         * @param element the token of the parse tree that represents the HTML tag
         * @param attribute the token of the parse tree that represents the HTML attribute
         * @param attributeValue the plain string value of the HTML attribute
         * @throws SemanticException thrown if the attribute value is not valid for the given HTML attribute and element
         */
        public void validateHtmlAttributeValue(Token element, Token attribute, String attributeValue) throws SemanticException;

        public String getInvalidURIMessage(String uri);
        public String getInvalidElementMessage(String elementName);
        public String getInvalidAttributeMessage(String elementName, String attributeName);
        public String getInvalidAttributeValueMessage(String elementName, String attributeName, String value);
    }

    /**
     * Implementation of the rules in http://wiki.whatwg.org/wiki/Sanitization_rules
     *
     * <pre>
     * Changes and additions:
     *
     * 1. Expanded all -* wildcard values to their full CSS property name (e.g. border-*).
     *
     * 2. Added dash as allowed characater to REGEX_VALID_CSS_STRING1.
     *
     * 3. Improved REGEX_VALID_CSS_VALUE with range {n,m} checks for color values and negative units.
     *
     * 4. Added more options (mostly of vertical-align property, e.g. "middle", "text-top") as allowed CSS values.
     *
     * 5. Added "max-height", "max-width", "min-height", "min-width" to CSS properties.
     *
     * 6. Removed 'data' URI scheme.
     *
     * 7. Not implemented filtering of CSS url() - it's an invalid value always.
     *
     * 8. Removed all &lt;form&gt;, &lt;input&gt; and other form tags. Attackers might use them to compromise
     *    "outer" forms when entering such markup in a textarea.
     * </pre>
     *
     */
    public static class DefaultSanitizer implements SeamTextParser.Sanitizer {

        public final java.util.regex.Pattern REGEX_VALID_CSS_STRING1 = java.util.regex.Pattern.compile(
            "^([-:,;#%.\\sa-zA-Z0-9!]|\\w-\\w|'[\\s\\w]+'|\"[\\s\\w]+\"|\\([\\d,\\s]+\\))*$"
        );

        public final java.util.regex.Pattern REGEX_VALID_CSS_STRING2 = java.util.regex.Pattern.compile(
            "^(\\s*[-\\w]+\\s*:\\s*[^:;]*(;|$))*$"
        );

        public final java.util.regex.Pattern REGEX_VALID_CSS_VALUE = java.util.regex.Pattern.compile(
            "^(#[0-9a-f]{3,6}|rgb\\(\\d{1,3}%?,\\d{1,3}%?,?\\d{1,3}%?\\)?|-?\\d{0,2}\\.?\\d{0,2}(cm|em|ex|in|mm|pc|pt|px|%|,|\\))?)$"
        );

        public final java.util.regex.Pattern REGEX_INVALID_CSS_URL = java.util.regex.Pattern.compile(
            "url\\s*\\(\\s*[^\\s)]+?\\s*\\)\\s*"
        );

        protected java.util.Set<String> acceptableElements = new java.util.HashSet(java.util.Arrays.asList(
            "a", "abbr", "acronym", "address", "area", "b", "bdo", "big", "blockquote",
            "br", "caption", "center", "cite", "code", "col", "colgroup", "dd",
            "del", "dfn", "dir", "div", "dl", "dt", "em", "font",
            "h1", "h2", "h3", "h4", "h5", "h6", "hr", "i", "img", "ins", "kbd",
            "label", "legend", "li", "map", "menu", "ol", "p",
            "pre", "q", "s", "samp", "small", "span", "strike", "strong",
            "sub", "sup", "table", "tbody", "td", "tfoot", "th", "thead",
            "tr", "tt", "u", "ul", "var", "wbr"
        ));

        protected java.util.Set<String> mathmlElements = new java.util.HashSet(java.util.Arrays.asList(
            "maction", "math", "merror", "mfrac", "mi", "mmultiscripts", "mn", "mo",
            "mover", "mpadded", "mphantom", "mprescripts", "mroot", "mrow", "mspace",
            "msqrt", "mstyle", "msub", "msubsup", "msup", "mtable", "mtd", "mtext",
            "mtr", "munder", "munderover", "none"
        ));

        protected java.util.Set<String> svgElements = new java.util.HashSet(java.util.Arrays.asList(
            "a", "animate", "animateColor", "animateMotion", "animateTransform",
            "circle", "defs", "desc", "ellipse", "font-face", "font-face-name",
            "font-face-src", "g", "glyph", "hkern", "image", "line", "linearGradient",
            "marker", "metadata", "missing-glyph", "mpath", "path", "polygon",
            "polyline", "radialGradient", "rect", "set", "stop", "svg", "switch", "text",
            "title", "tspan", "use"
        ));

        protected java.util.Set<String> acceptableAttributes = new java.util.HashSet(java.util.Arrays.asList(
            "abbr", "accept", "accept-charset", "accesskey", "action", "align", "alt",
            "axis", "border", "cellpadding", "cellspacing", "char", "charoff", "charset",
            "checked", "cite", "class", "clear", "color", "cols", "colspan", "compact",
            "coords", "datetime", "dir", "disabled", "enctype", "for", "frame",
            "headers", "height", "href", "hreflang", "hspace", "id", "ismap", "label",
            "lang", "longdesc", "maxlength", "media", "method", "multiple", "name",
            "nohref", "noshade", "nowrap", "prompt", "readonly", "rel", "rev", "rows",
            "rowspan", "rules", "scope", "selected", "shape", "size", "span", "src",
            "start", "style", "summary", "tabindex", "target", "title", "type", "usemap",
            "valign", "value", "vspace", "width", "xml:lang"
        ));

        protected java.util.Set<String> mathmlAttributes = new java.util.HashSet(java.util.Arrays.asList(
            "actiontype", "align", "columnalign", "columnalign", "columnalign",
            "columnlines", "columnspacing", "columnspan", "depth", "display",
            "displaystyle", "equalcolumns", "equalrows", "fence", "fontstyle",
            "fontweight", "frame", "height", "linethickness", "lspace", "mathbackground",
            "mathcolor", "mathvariant", "mathvariant", "maxsize", "minsize", "other",
            "rowalign", "rowalign", "rowalign", "rowlines", "rowspacing", "rowspan",
            "rspace", "scriptlevel", "selection", "separator", "stretchy", "width",
            "width", "xlink:href", "xlink:show", "xlink:type", "xmlns", "xmlns:xlink"
        ));

        protected java.util.Set<String> svgAttributes = new java.util.HashSet(java.util.Arrays.asList(
            "accent-height", "accumulate", "additive", "alphabetic", "arabic-form",
            "ascent", "attributeName", "attributeType", "baseProfile", "bbox", "begin",
            "by", "calcMode", "cap-height", "class", "color", "color-rendering",
            "content", "cx", "cy", "d", "descent", "display", "dur", "dx", "dy", "end",
            "fill", "fill-rule", "font-family", "font-size", "font-stretch",
            "font-style", "font-variant", "font-weight", "from", "fx", "fy", "g1", "g2",
            "glyph-name", "gradientUnits", "hanging", "height", "horiz-adv-x",
            "horiz-origin-x", "id", "ideographic", "k", "keyPoints", "keySplines",
            "keyTimes", "lang", "marker-end", "marker-mid", "marker-start",
            "markerHeight", "markerUnits", "markerWidth", "mathematical", "max", "min",
            "name", "offset", "opacity", "orient", "origin", "overline-position",
            "overline-thickness", "panose-1", "path", "pathLength", "points",
            "preserveAspectRatio", "r", "refX", "refY", "repeatCount", "repeatDur",
            "requiredExtensions", "requiredFeatures", "restart", "rotate", "rx", "ry",
            "slope", "stemh", "stemv", "stop-color", "stop-opacity",
            "strikethrough-position", "strikethrough-thickness", "stroke",
            "stroke-dasharray", "stroke-dashoffset", "stroke-linecap", "stroke-linejoin",
            "stroke-miterlimit", "stroke-opacity", "stroke-width", "systemLanguage",
            "target", "text-anchor", "to", "transform", "type", "u1", "u2",
            "underline-position", "underline-thickness", "unicode", "unicode-range",
            "units-per-em", "values", "version", "viewBox", "visibility", "width",
            "widths", "x", "x-height", "x1", "x2", "xlink:actuate", "xlink:arcrole",
            "xlink:href", "xlink:role", "xlink:show", "xlink:title", "xlink:type",
            "xml:base", "xml:lang", "xml:space", "xmlns", "xmlns:xlink", "y", "y1", "y2",
            "zoomAndPan"
        ));

        protected java.util.Set<String> styleProperties = new java.util.HashSet(java.util.Arrays.asList(
            "azimuth",
            "background", "background-attachment", "background-color", "background-image",
            "background-position", "background-repeat",
            "border", "border-bottom", "border-bottom-color", "border-bottom-style",
            "border-bottom-width", "border-collapse", "border-color", "border-left",
            "border-left-color", "border-left-style", "border-left-width", "border-right",
            "border-right-color", "border-right-style", "border-right-width", "border-spacing",
            "border-style", "border-top", "border-top-color", "border-top-style",
            "border-top-width", "border-width",
            "clear", "color",
            "cursor", "direction", "display", "elevation", "float", "font",
            "font-family", "font-size", "font-style", "font-variant", "font-weight",
            "height", "letter-spacing", "line-height",
            "margin", "margin-bottom", "margin-left", "margin-right", "margin-top",
            "max-height", "max-width", "min-height", "min-width",
            "overflow",
            "padding", "padding-bottom", "padding-left", "padding-right", "padding-top",
            "pause", "pause-after", "pause-before", "pitch",
            "pitch-range", "richness", "speak", "speak-header", "speak-numeral",
            "speak-punctuation", "speech-rate", "stress", "text-align",
            "text-decoration", "text-indent", "unicode-bidi", "vertical-align",
            "voice-family", "volume", "white-space", "width"
        ));

        protected java.util.Set<String> stylePropertiesValues = new java.util.HashSet(java.util.Arrays.asList(
            "aqua", "auto", "baseline", "black", "block", "blue", "bold", "both", "bottom", "brown",
            "center", "collapse", "dashed", "dotted", "fuchsia", "gray", "green",
            "inherit", "italic", "left", "length", "lime", "maroon", "medium", "middle", "navy", "none", "normal",
            "nowrap", "olive", "percentage", "pointer", "purple", "red", "right", "silver", "solid", "sub", "super",
            "teal", "text-bottom", "text-top", "top", "transparent", "underline", "white", "yellow"
        ));

        protected java.util.Set<String> svgStyleProperties = new java.util.HashSet(java.util.Arrays.asList(
            "fill", "fill-opacity", "fill-rule", "stroke", "stroke-linecap",
            "stroke-linejoin", "stroke-opacity", "stroke-width"
        ));

        protected java.util.Set<String> attributesWhoseValueIsAURI = new java.util.HashSet(java.util.Arrays.asList(
            "action", "cite", "href", "longdesc", "src", "xlink:href", "xml:base"
        ));

        protected java.util.Set<String> uriSchemes = new java.util.HashSet(java.util.Arrays.asList(
            "afs", "aim", "callto", "ed2k", "feed", "ftp", "gopher", "http", "https",
            "irc", "mailto", "news", "nntp", "rsync", "rtsp", "sftp", "ssh", "tag",
            "tel", "telnet", "urn", "webcal", "wtai", "xmpp"
        ));

        public void validateLinkTagURI(Token element, String uri) throws SemanticException {
            if (!validateURI(uri)) {
                throw createSemanticException("Invalid URI", element);
            }
        }

        public void validateHtmlElement(Token element) throws SemanticException {
            String elementName = element.getText().toLowerCase();

            if (!acceptableElements.contains(elementName) &&
                !svgElements.contains(elementName) &&
                !mathmlElements.contains(elementName)) {
                throw createSemanticException(getInvalidElementMessage(elementName), element);
            }
        }

        public void validateHtmlAttribute(Token element, Token attribute) throws SemanticException {
            String elementName = element.getText().toLowerCase();
            String attributeName = attribute.getText().toLowerCase();
            if (!acceptableAttributes.contains(attributeName) &&
                !svgAttributes.contains(attributeName) &&
                !mathmlAttributes.contains(attributeName)) {
                throw createSemanticException(getInvalidAttributeMessage(elementName, attributeName), element);
            }
        }

        public void validateHtmlAttributeValue(Token element,
                                               Token attribute,
                                               String attributeValue) throws SemanticException {

            if (attributeValue == null || attributeValue.length() == 0) return;

            String elementName = element.getText().toLowerCase();
            String attributeName = attribute.getText().toLowerCase();

            // Check element with attribute that has URI value (href, src, etc.)
            if (attributesWhoseValueIsAURI.contains(attributeName) && !validateURI(attributeValue)) {
                throw createSemanticException(getInvalidURIMessage(attributeValue), element);
            }

            // Check attribute value of style (CSS filtering)
            if (attributeName.equals("style")) {
                if (!REGEX_VALID_CSS_STRING1.matcher(attributeValue).matches() ||
                    !REGEX_VALID_CSS_STRING2.matcher(attributeValue).matches()) {
                    throw createSemanticException(
                        getInvalidAttributeValueMessage(elementName, attributeName, attributeValue),
                        element
                    );
                }

                String[] cssProperties = attributeValue.split(";");
                for (String cssProperty : cssProperties) {
                    if (!cssProperty.contains(":")) {
                        throw createSemanticException(
                            getInvalidAttributeValueMessage(elementName, attributeName, attributeValue),
                            element
                        );
                    }
                    String[] property = cssProperty.split(":");
                    String propertyName = property[0].trim();
                    String propertyValue = property.length == 2 ? property[1].trim() : null;

                    // CSS property name
                    if (!styleProperties.contains(propertyName) &&
                        !svgStyleProperties.contains(propertyName)) {
                        throw createSemanticException(
                            getInvalidAttributeValueMessage(elementName, attributeName, attributeValue),
                            element
                        );
                    }

                    // CSS property value
                    if (propertyValue != null && !stylePropertiesValues.contains(propertyValue)) {
                        // Not in list, now check the regex
                        if (!REGEX_VALID_CSS_VALUE.matcher(propertyValue).matches()) {
                            throw createSemanticException(
                                getInvalidAttributeValueMessage(elementName, attributeName, attributeValue),
                                element
                            );
                        }
                    }
                }
            }

            // TODO: Implement SVG style checking?! Who cares...
        }

        /**
         * Validate a URI string.
         * <p>
         * The default implementation accepts any URI string that starts with a slash,
         * this is considered a relative URL. Any absolute URI is parsed by the JDK with
         * the <tt>java.net.URI</tt> constructor. Finally, the scheme of the parsed
         * absolute URI is checked with a list of valid schemes.
         * </p>
         *
         * @param uri the URI string
         * @return return true if the String represents a safe and valid URI
         */
        protected boolean validateURI(String uri) {

            // Relative URI starts with a slash
            if (uri.startsWith("/")) return true;

            java.net.URI parsedURI;
            try {
                parsedURI = new java.net.URI(uri);
            } catch (java.net.URISyntaxException ex) {
                return false;
            }

            if (!uriSchemes.contains(parsedURI.getScheme())) {
                return false;
            }
            return true;
        }

        public String getInvalidURIMessage(String uri) {
            return "invalid URI";
        }

        public String getInvalidElementMessage(String elementName) {
            return "invalid element '" + elementName + "'";
        }

        public String getInvalidAttributeMessage(String elementName, String attributeName) {
            return "invalid attribute '" + attributeName + "' for element '" + elementName + "'";
        }

        public String getInvalidAttributeValueMessage(String elementName, String attributeName, String value) {
            return "invalid value of attribute '" + attributeName + "' for element '" + elementName + "'";
        };

        public SemanticException createSemanticException(String message, Token element) {
            return new SemanticException(
                message,
                element.getFilename(), element.getLine(), element.getColumn()
            );
        }

    }

    private Sanitizer sanitizer = new DefaultSanitizer();
    public void setSanitizer(Sanitizer sanitizer) {
       this.sanitizer = sanitizer;
    }

    private Macro currentMacro;
    private java.util.Stack<Token> htmlElementStack = new java.util.Stack<Token>();

    private StringBuilder mainBuilder = new StringBuilder();
    private StringBuilder builder = mainBuilder;

    public String toString() {
        return builder.toString();
    }
    
    private void append(String... strings) {
        for (String string: strings) builder.append(string);
    }
    
    private static boolean hasMultiple(String string, char c) {
        return string.indexOf(c)!=string.lastIndexOf(c);
    }

    private void beginCapture() {
        builder = new StringBuilder();
    }
    
    private String endCapture() {
        String result = builder.toString();
        builder = mainBuilder;
        return result;
    }

    protected String linkTag(String description, String url) {
        return "<a href=\"" + url + "\" class=\"seamTextLink\">" + description + "</a>";
    }

    protected String macroInclude(String macroName) {
        return "";
    }

    protected String macroInclude(Macro m) {
        return macroInclude(m.name);
    }

    protected String paragraphOpenTag() {
        return "<p class=\"seamTextPara\">\n";
    }

    protected String preformattedText(String text) {
        return "<pre class=\"seamTextPreformatted\">\n" + text + "</pre>\n";
    }

    protected String blockquoteOpenTag() {
        return "<blockquote class=\"seamTextBlockquote\">\n";
    }

    protected String headline1(String line) {
        return "<h1 class=\"seamTextHeadline1\">" + line + "</h1>";
    }

    protected String headline2(String line) {
        return "<h2 class=\"seamTextHeadline2\">" + line + "</h2>";
    }

    protected String headline3(String line) {
        return "<h3 class=\"seamTextHeadline3\">" + line + "</h3>";
    }

    protected String headline4(String line) {
        return "<h4 class=\"seamTextHeadline4\">" + line + "</h4>";
    }

    protected String orderedListOpenTag() {
        return "<ol class=\"seamTextOrderedList\">\n";
    }

    protected String orderedListItemOpenTag() {
        return "<li class=\"seamTextOrderedListItem\">";
    }

    protected String unorderedListOpenTag() {
        return "<ul class=\"seamTextUnorderedList\">\n";
    }

    protected String unorderedListItemOpenTag() {
        return "<li class=\"seamTextUnorderedListItem\">";
    }

    protected String emphasisOpenTag() {
        return "<i class=\"seamTextEmphasis\">";
    }

    protected String emphasisCloseTag() {
        return "</i>";
    }
}

startRule: (newline)* ( (heading (newline)* )? text (heading (newline)* text)* )?
    ;

text: ( (paragraph|preformatted|blockquote|list|html) (newline)* )+
    ;
        
paragraph: { append( paragraphOpenTag() ); } (line newlineOrEof)+ { append("</p>\n"); } newlineOrEof
    ;
    
line: (plain|formatted) (plain|formatted|preformatted|quoted|html)*
    ;
    
blockquote: DOUBLEQUOTE { append( blockquoteOpenTag() ); }
            (plain|formatted|preformatted|newline|html|list)*
            DOUBLEQUOTE newlineOrEof { append("</blockquote>\n"); }
    ;
    
preformatted: BACKTICK
              { beginCapture(); }
              (word|punctuation|specialChars|moreSpecialChars|htmlSpecialChars|space|newline)*
              { String text=endCapture(); }
              { append( preformattedText(text) ); }
              BACKTICK
    ;
    
plain: word|punctuation|escape|space|link|macro
    ;
  
formatted: underline|emphasis|monospace|superscript|deleted
    ;

word: an:ALPHANUMERICWORD { append( an.getText() ); } | uc:UNICODEWORD { append( uc.getText() ); }
    ;

punctuation: p:PUNCTUATION { append( p.getText() ); }
           | sq:SINGLEQUOTE { append( sq.getText() ); }
           | s:SLASH { append( s.getText() ); }
    ;
    
escape: ESCAPE ( specialChars | moreSpecialChars | evenMoreSpecialChars | htmlSpecialChars | b:BACKTICK {append( b.getText() );} )
    ;
    
specialChars:
          st:STAR { append( st.getText() ); } 
        | b:BAR { append( b.getText() ); }
        | h:HAT { append( h.getText() ); }
        | p:PLUS { append( p.getText() ); }
        | eq:EQ { append( eq.getText() ); }
        | hh:HASH { append( hh.getText() ); }
        | e:ESCAPE { append( e.getText() ); }
        | t:TWIDDLE { append( t.getText() ); }
        | u:UNDERSCORE { append( u.getText() ); }
    ;
    
moreSpecialChars:
          o:OPEN { append( o.getText() ); }
        | c:CLOSE { append( c.getText() ); }
    ;
    
evenMoreSpecialChars: 
          q:QUOTE { append( q.getText() ); }
    ;

htmlSpecialChars: 
      GT { append("&gt;"); } 
    | LT { append("&lt;"); } 
    | DOUBLEQUOTE { append("&quot;"); } 
    | AMPERSAND { append("&amp;"); }
    ;

link: OPEN
      { beginCapture(); } 
      (word|punctuation|escape|space)*
      { String text=endCapture(); } 
      EQ gt:GT
      { beginCapture(); }
      attributeValue 
      {
         String link = endCapture();
         sanitizer.validateLinkTagURI(gt, link);
         append(linkTag(text, link));
      }
      CLOSE
    ;

/*

[<=macro[param1=value "1"][param2=value '2']]

*/
macro: OPEN
      LT EQ
      mn:ALPHANUMERICWORD { currentMacro = new Macro(mn.getText()); }
      (macroParam)*
      CLOSE
      { append( macroInclude(currentMacro) ); currentMacro = null; }
    ;

macroParam:
      OPEN
      pn:ALPHANUMERICWORD
      EQ
      { beginCapture(); }
      macroParamValue
      { String pv = endCapture(); currentMacro.params.put(pn.getText(),pv); }
      CLOSE
    ;

macroParamValue:
      ( amp:AMPERSAND       { append(amp.getText()); } |
        dq:DOUBLEQUOTE      { append(dq.getText()); } |
        sq:SINGLEQUOTE      { append(sq.getText()); } |
        an:ALPHANUMERICWORD { append(an.getText()); } |
        p:PUNCTUATION       { append(p.getText()); } |
        s:SLASH             { append(s.getText()); } |
        lt:LT               { append(lt.getText()); } |
        gt:GT               { append(gt.getText()); } |
        space | specialChars )*
    ;

emphasis: STAR { append( emphasisOpenTag() ); }
      (plain|underline|monospace|superscript|deleted|newline)+
      STAR { append( emphasisCloseTag() ); }
    ;
    
underline: UNDERSCORE { append("<u>"); }
           (plain|emphasis|monospace|superscript|deleted|newline)+
           UNDERSCORE { append("</u>"); }
    ;
    

monospace: BAR { append("<tt>"); }
           (word | punctuation | space 
          | st:STAR { append( st.getText() ); }
          | h:HAT { append( h.getText() ); }
          | p:PLUS { append( p.getText() ); }
          | eq:EQ { append( eq.getText() ); }
          | hh:HASH { append( hh.getText() ); }
          | e:ESCAPE { append( e.getText() ); }
          | t:TWIDDLE { append( t.getText() ); }
          | u:UNDERSCORE { append( u.getText() ); }
          | moreSpecialChars
          | htmlSpecialChars
          | newline)+
           BAR { append("</tt>"); }
    ;
    
superscript: HAT { append("<sup>"); }
             (plain|emphasis|underline|monospace|deleted|newline)+
             HAT { append("</sup>"); }
    ;
    
deleted: TWIDDLE { append("<del>"); }
         (plain|emphasis|underline|monospace|superscript|newline)+
         TWIDDLE { append("</del>"); }
    ;
    
quoted: DOUBLEQUOTE { append("<q>"); }
        (plain|emphasis|underline|monospace|superscript|deleted|newline)+
        DOUBLEQUOTE { append("</q>"); }
    ;

heading: ( h1 | h2 | h3 | h4 ) newlineOrEof
    ;
  
h1: PLUS
      { beginCapture(); }
      line
      { String headline=endCapture(); }
      { append(headline1(headline.trim())); }
    ;
 
h2: PLUS PLUS
      { beginCapture(); }
      line
      { String headline=endCapture(); }
      { append(headline2(headline.trim())); }
    ;

h3: PLUS PLUS PLUS
      { beginCapture(); }
      line
      { String headline=endCapture(); }
      { append(headline3(headline.trim())); }
    ;
 
h4: PLUS PLUS PLUS PLUS
      { beginCapture(); }
      line
      { String headline=endCapture(); }
      { append(headline4(headline.trim())); }
    ;

list: ( olist | ulist ) newlineOrEof
    ;
    
olist: { append( orderedListOpenTag() ); } (olistLine newlineOrEof)+ { append("</ol>\n"); }
    ;
    
olistLine: HASH { append( orderedListItemOpenTag() ); } line { append("</li>"); }
    ;
    
ulist: { append( unorderedListOpenTag() ); } (ulistLine newlineOrEof)+ { append("</ul>\n"); }
    ;
    
ulistLine: EQ { append( unorderedListItemOpenTag() ); } line { append("</li>"); }
    ;

space: s:SPACE { append( s.getText() ); }
    ;
    
newline: n:NEWLINE { append( n.getText() ); }
    ;

newlineOrEof: newline | EOF
    ;

html: openTag ( space | space attribute )* ( ( beforeBody body closeTagWithBody ) | closeTagWithNoBody )
    ;

body: (plain|formatted|preformatted|quoted|html|list|newline)*
    ;

openTag:
      LT name:ALPHANUMERICWORD
      {
         htmlElementStack.push(name);
         sanitizer.validateHtmlElement(name);
         append("<");
         append(name.getText());
      }
    ;
    exception // for rule
        catch [RecognitionException ex] {
            // We'd like to have an error reported that names the opening HTML, this
            // helps users to find the actual start of their problem in the wiki text.
            if (htmlElementStack.isEmpty()) throw ex;
            Token tok = htmlElementStack.peek();
            if (tok != null) {
                throw new HtmlRecognitionException(tok, ex);
            } else {
                throw ex;
            }
        }

beforeBody: GT { append(">"); }
    ;
    exception // for rule
        catch [RecognitionException ex] {
            // We'd like to have an error reported that names the opening HTML, this
            // helps users to find the actual start of their problem in the wiki text.
            if (htmlElementStack.isEmpty()) throw ex;
            Token tok = htmlElementStack.peek();
            if (tok != null) {
                throw new HtmlRecognitionException(tok, ex);
            } else {
                throw ex;
            }
        }

closeTagWithBody:
      LT SLASH name:ALPHANUMERICWORD GT
      {
         append("</");
         append(name.getText());
         append(">");
         htmlElementStack.pop();
      }
    ;
    
closeTagWithNoBody:
      SLASH GT
      {
         append("/>");
         htmlElementStack.pop();
      }
    ;

attribute: att:ALPHANUMERICWORD (space)* EQ (space)*
           DOUBLEQUOTE
           {
               sanitizer.validateHtmlAttribute(htmlElementStack.peek(), att);
               append(att.getText());
               append("=\"");
           }
           {
               beginCapture();
           }
           attributeValue
           {
               String attValue = endCapture();
               sanitizer.validateHtmlAttributeValue(htmlElementStack.peek(), att, attValue);
               append(attValue);
           }
           DOUBLEQUOTE { append("\""); }
    ;
    exception // for rule
        catch [RecognitionException ex] {
            // We'd like to have an error reported that names the opening HTML, this
            // helps users to find the actual start of their problem in the wiki text.
            if (htmlElementStack.isEmpty()) throw ex;
            Token tok = htmlElementStack.peek();
            if (tok != null) {
                throw new HtmlRecognitionException(tok, ex);
            } else {
                throw ex;
            }
        }

attributeValue: ( AMPERSAND { append("&amp;"); } |
                an:ALPHANUMERICWORD { append( an.getText() ); } |
                p:PUNCTUATION { append( p.getText() ); } |
                s:SLASH { append( s.getText() ); } |
                space | specialChars )*
    ;
    exception // for rule
        catch [RecognitionException ex] {
            // We'd like to have an error reported that names the opening HTML, this
            // helps users to find the actual start of their problem in the wiki text.
            if (htmlElementStack.isEmpty()) throw ex;
            Token tok = htmlElementStack.peek();
            if (tok != null) {
                throw new HtmlRecognitionException(tok, ex);
            } else {
                throw ex;
            }
        }

class SeamTextLexer extends Lexer;
options
{
   k=2;

   // Allow any char but \uFFFF (16 bit -1)
   charVocabulary='\u0000'..'\uFFFE';
}

// Unicode sets allowed:
// '\u00a0'..'\u00ff'  Latin 1 supplement (no control characters) http://www.unicode.org/charts/PDF/U0080.pdf
// '\u0100'..'\u017f'  Latin Extended A http://www.unicode.org/charts/PDF/U0100.pdf
// '\u0180'..'\u024f'  Latin Extended B http://www.unicode.org/charts/PDF/U0180.pdf
// '\u0250'..'\ufaff'  Various other languages, punctuation etc. (excluding "presentation forms")
// '\uff00'..'\uffef'  Halfwidth and Fullwidth forms (including CJK punctuation)

ALPHANUMERICWORD
    options {
        paraphrase = "letters or digits";
    }
    :   ('a'..'z'|'A'..'Z'|'0'..'9')+
    ;

UNICODEWORD
    options {
        paraphrase = "letters or digits";
    }
    : (
         '\u00a0'..'\u00ff' |
         '\u0100'..'\u017f' |
         '\u0180'..'\u024f' |
         '\u0250'..'\ufaff' |
         '\uff00'..'\uffef'
      )+
    ;

PUNCTUATION
    options {
        paraphrase = "a punctuation character";
    }
    : '-' | ';' | ':' | '(' | ')' | '{' | '}' | '?' | '!' | '@' | '%' | '.' | ',' | '$'
    ;
    
EQ
    options {
        paraphrase = "an equals '='";
    }
    : '='
    ;
    
PLUS
    options {
        paraphrase = "a plus '+'";
    }
    : '+'
    ;
    
UNDERSCORE
    options {
        paraphrase = "an underscore '_'";
    }
    : '_'
    ;

STAR
    options {
        paraphrase = "a star '*'";
    }
    : '*'
    ;

SLASH
    options {
        paraphrase = "a slash '/'";
    }

    : '/'
    ;

ESCAPE
    options {
        paraphrase = "the escaping blackslash '\'";
    }
    : '\\'
    ;
    
BAR
    options {
        paraphrase = "a bar or pipe '|'";
    }
    : '|'
    ;
    
BACKTICK
    options {
        paraphrase = "a backtick '`'";
    }
    : '`'
    ;
    
    
TWIDDLE
    options {
        paraphrase = "a tilde '~'";
    }
    : '~'
    ;

DOUBLEQUOTE
    options {
        paraphrase = "a doublequote \"";
    }
    : '"'
    ;

SINGLEQUOTE
    options {
        paraphrase = "a single quote '";
    }
    : '\''
    ;

OPEN
    options {
        paraphrase = "an opening square bracket '['";
    }
    : '['
    ;
    
CLOSE
    options {
        paraphrase = "a closing square bracket ']'";
    }
    : ']'
    ;

HASH
    options {
        paraphrase = "a hash '#'";
    }
    : '#'
    ;
    
HAT
    options {
        paraphrase = "a caret '^'";
    }
    : '^'
    ;
    
GT
    options {
        paraphrase = "a closing angle bracket '>'";
    }
    : '>'
    ;
    
LT
    options {
        paraphrase = "an opening angle bracket '<'";
    }
    : '<'
    ;

AMPERSAND
    options {
        paraphrase = "an ampersand '&'";
    }
    : '&'
    ;

SPACE
    options {
        paraphrase = "a space or tab";
    }
    : (' '|'\t')+
    ;
    
NEWLINE
    options {
        paraphrase = "a newline";
    }
    : "\r\n" | '\r' | '\n'
    ;

EOF
    options {
        paraphrase = "the end of the text";
    }
    : '\uFFFF'
    ;
