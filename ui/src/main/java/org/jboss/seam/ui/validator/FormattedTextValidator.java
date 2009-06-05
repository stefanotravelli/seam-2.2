package org.jboss.seam.ui.validator;

import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.jboss.seam.text.SeamTextLexer;
import org.jboss.seam.text.SeamTextParser;

import antlr.*;

/**
 * Formatted Text validator
 * 
 * Use as a JSF validator on an input control that allows entering Seam Text
 * markup.
 * <p>
 * The Seam Text parser has a disabled default error handler, catch exceptions
 * as appropriate if you display Seam Text (see <a
 * href="http://www.doc.ic.ac.uk/lab/secondyear/Antlr/err.html">http://www.doc.ic.ac.uk/lab/secondyear/Antlr/err.html</a>)
 * and call the static convenience method
 * <tt>FormattedTextValidator.getErrorMessage(originalText, recognitionException)</tt>
 * if you want to display or log a nice error message.
 * </p>
 * <p>
 * Uses an instance of <tt>SeamTextParser</tt> by default, override if you require
 * validation with your customized instance of <tt>SeamTextParser</tt>.
 * </p>
 *
 * @author matthew.drees
 * @author Christian Bauer
 */
public class FormattedTextValidator implements javax.faces.validator.Validator, Serializable {

    private static final long serialVersionUID               = 1L;
    private static final int  NUMBER_OF_CONTEXT_CHARS_AFTER  = 10;
    private static final int  NUMBER_OF_CONTEXT_CHARS_BEFORE = 10;
    private static final String END_OF_TEXT = "END OF TEXT";
    String firstError;
    String firstErrorDetail;

    /**
     * Validate the given value as well-formed Seam Text. If there are parse
     * errors, throw a ValidatorException including the first parse error.
     */
    public void validate(FacesContext context, UIComponent component,
            Object value) throws ValidatorException {
        firstError = null;
        firstErrorDetail = null;
        if (value == null) {
            return;
        }

        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Value is not a string: "
                    + value);
        }
        String text = (String) value;
        SeamTextParser parser = getSeamTextParser(text);
        try {
            parser.startRule();
        }
        // Error handling for ANTLR lexer/parser errors, see
        // http://www.doc.ic.ac.uk/lab/secondyear/Antlr/err.html
        catch (TokenStreamException tse) {
            // Problem with the token input stream
            throw new RuntimeException(tse);
        } catch (RecognitionException re) {
            // A parser error
            if (firstError == null) {
                firstError = getParserErrorMessage(text, re);
                firstErrorDetail = re.getMessage().replace("\uFFFF",END_OF_TEXT);
            }
        }

        if (firstError != null) {
            throw new ValidatorException(new FacesMessage(firstError, firstErrorDetail));
        }
    }

    /**
     * Override to instantiate a custom <tt>SeamTextLexer</tt> and <tt>SeamTextParser</tt>.
     *
     * @param text the raw markup text
     * @return an instance of <tt>SeamTextParser</tt>
     */
    public SeamTextParser getSeamTextParser(String text) {
       Reader r = new StringReader(text);
       SeamTextLexer lexer = new SeamTextLexer(r);
       return new SeamTextParser(lexer);
    }

    public String getParserErrorMessage(String originalText, RecognitionException re) {
        String parserErrorMsg;
        if (NoViableAltException.class.isAssignableFrom(re.getClass())) {
            parserErrorMsg = getNoViableAltErrorMessage(
                re.getMessage(),
                getErrorLocation(originalText, re, getNumberOfCharsBeforeErrorLocation(), getNumberOfCharsAfterErrorLocation())
            );
        } else if (MismatchedTokenException.class.isAssignableFrom(re.getClass())) {
            parserErrorMsg = getMismatchedTokenErrorMessage(
                re.getMessage(),
                getErrorLocation(originalText, re, getNumberOfCharsBeforeErrorLocation(), getNumberOfCharsAfterErrorLocation())
            );
        } else if (SemanticException.class.isAssignableFrom(re.getClass())) {
            parserErrorMsg = getSemanticErrorMessage(re.getMessage());
        } else {
            parserErrorMsg = re.getMessage();
        }
        return parserErrorMsg;
    }

    public int getNumberOfCharsBeforeErrorLocation() {
        return NUMBER_OF_CONTEXT_CHARS_BEFORE;
    }

    public int getNumberOfCharsAfterErrorLocation() {
        return NUMBER_OF_CONTEXT_CHARS_AFTER;
    }

    /**
     * Override (e.g. for i18n) ANTLR parser error messages.
     *
     * @param originalMessage the ANTLR parser error message of the RecognitionException
     * @param location a snippet that indicates the location in the original markup, might be null
     * @return a message that is thrown by this validator
     */
    public String getNoViableAltErrorMessage(String originalMessage, String location) {
        return location != null
                ? "Text parsing error at '..." + location.trim() + "...'"
                : "Text parsing error, " + originalMessage.replace("\uFFFF",END_OF_TEXT);
    }

    /**
     * Override (e.g. for i18n) ANTLR parser error messages.
     *
     * @param originalMessage the ANTLR parser error message of the RecognitionException
     * @param location a snippet that indicates the location in the original markup, might be null
     * @return a message that is thrown by this validator
     */
    public String getMismatchedTokenErrorMessage(String originalMessage, String location) {
        return location != null
                ? "Text parsing error at '..." + location.trim() + "...'"
                : "Text parsing error, " + originalMessage.replace("\uFFFF",END_OF_TEXT);
    }

    /**
     * Override (e.g. for i18n) ANTLR parser error messages.
     *
     * @param originalMessage the ANTLR parser error message of the RecognitionException
     * @return a message that is thrown by this validator
     */
    public String getSemanticErrorMessage(String originalMessage) {
        return "Text parsing error, " + originalMessage.replace("\uFFFF",END_OF_TEXT);
    }

    /**
     * Extracts the error from the <tt>RecognitionException</tt> and generates
     * a location of the error by extracting the original text at the exceptions
     * line and column.
     * 
     * @param originalText
     *            the original Seam Text markup as fed into the parser
     * @param re
     *            an ANTLR <tt>RecognitionException</tt> thrown by the parser
     * @param charsBefore
     *            characters before error location included in message
     * @param charsAfter
     *            characters after error location included in message
     * @return an error message with some helpful context about where the error
     *         occured
     */
    public static String getErrorLocation(String originalText, RecognitionException re, int charsBefore, int charsAfter) {

        int beginIndex = Math.max(re.getColumn() - 1 - charsBefore, 0);
        int endIndex = Math.min(re.getColumn() + charsAfter, originalText.length());

        String location = null;

        // Avoid IOOBE even if what we show is wrong, we need to figure out why the indexes are off sometimes
        if (beginIndex > 0 && beginIndex < endIndex && endIndex > 0 && endIndex < originalText.length())
            location = originalText.substring(beginIndex, endIndex);

        if (location == null) return location;

        // Filter some dangerous characters we do not want in error messages
        return location.replace("\n", " ").replace("\r", " ").replace("#{", "# {");
    }

    /**
     * Extracts the error from the <tt>RecognitionException</tt> and generates
     * a message including the location of the error.
     *
     * @param originalText
     *            the original Seam Text markup as fed into the parser
     * @param re
     *            an ANTLR <tt>RecognitionException</tt> thrown by the parser
     * @return an error message with some helpful context about where the error
     *         occured
     */
    public static String getErrorMessage(String originalText, RecognitionException re) {
        return re.getMessage().replace("\uFFFF",END_OF_TEXT)
                + " at '"
                + getErrorLocation(
                    originalText, re,
                    NUMBER_OF_CONTEXT_CHARS_BEFORE, NUMBER_OF_CONTEXT_CHARS_AFTER
                  )
                + "'";

    }
}
