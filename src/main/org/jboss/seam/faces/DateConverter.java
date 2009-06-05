package org.jboss.seam.faces;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.faces.Converter;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

/**
 * Provides a default JSF converter for properties of type java.util.Date.
 * 
 * <p>This converter is provided to save a developer from having to specify
 * a DateTimeConverter on an input field or page parameter. By default, it
 * assumes the type to be a date (as opposed to a time or date plus time) and
 * uses the short input style adjusted to the Locale of the user. For Locale.US,
 * the input pattern is mm/DD/yy. However, to comply with Y2K, the year is changed
 * from two digits to four (e.g., mm/DD/yyyy).</p>
 * <p>It's possible to override the input pattern globally using component configuration.
 * Here is an example of changing the style to both and setting the date and
 * time style to medium.</p>
 * <pre>
 * org.jboss.seam.faces.dateConverter.type=both
 * org.jboss.seam.faces.dateConverter.dateStyle=medium
 * org.jboss.seam.faces.dateConverter.timeStyle=medium
 * </pre>
 * <p>Alternatively, a fixed pattern can be specified.</p>
 * <pre>
 * org.jboss.seam.faces.dateConverter.pattern=yyyy-mm-DD
 * </pre>
 * 
 * @author Dan Allen
 */
@Converter(forClass = Date.class)
@Name("org.jboss.seam.faces.dateConverter")
@Install(precedence = BUILT_IN, classDependencies = "javax.faces.context.FacesContext")
@BypassInterceptors
public class DateConverter extends javax.faces.convert.DateTimeConverter {

	private Log log = Logging.getLog(DateConverter.class);

	private static final String TYPE_DATE = "date";
	private static final String STYLE_SHORT = "short";
	private static final String TWO_DIGIT_YEAR_PATTERN = "yy";
	private static final String FOUR_DIGIT_YEAR_PATTERN = "yyyy";
	
	// constructor is used to initialize converter to allow these values to be overridden using component properties
	public DateConverter() {
		super();
		setType(TYPE_DATE);
		setDateStyle(STYLE_SHORT);
		setTimeStyle(STYLE_SHORT); // default in case developer overrides type to be time or both
	}
	
	@Create
	public void create() {
		// TODO make this work if using "both" for type; requires more analysis of time style
		if (TYPE_DATE.equals(getType()) && STYLE_SHORT.equals(getDateStyle()) && getPattern() == null) {
			// attempt to make the pattern Y2K compliant, which it isn't by default
			DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, getLocale());
			if (dateFormat instanceof SimpleDateFormat) {
				setPattern(((SimpleDateFormat) dateFormat).toPattern().replace(TWO_DIGIT_YEAR_PATTERN, FOUR_DIGIT_YEAR_PATTERN));
			}
		}
		// required since the superclass may access the fields directly
		setTimeZone(getTimeZone());
		setLocale(getLocale());
	}

	@Override
	public TimeZone getTimeZone() {
		if (Contexts.isApplicationContextActive()) {
			return org.jboss.seam.international.TimeZone.instance();
		} else {
			// we don't want to use JSF's braindead default (maybe in JSF 2)
			return TimeZone.getDefault();
		}
	}

	@Override
	public Locale getLocale() {
		if (Contexts.isApplicationContextActive()) {
			return org.jboss.seam.international.Locale.instance();
		} else {
			return super.getLocale();
		}
	}

	@Override
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) throws ConverterException {
		if (log.isDebugEnabled()) {
			log.debug("Converting string '#0' to date for clientId '#1' using Seam's built-in JSF date converter", value, component.getClientId(context));
		}
		return super.getAsObject(context, component, value);
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object value) throws ConverterException {
		if (log.isDebugEnabled()) {
			log.debug("Converting date '#0' to string for clientId '#1' using Seam's built-in JSF date converter", value, component.getClientId(context));
		}
		return super.getAsString(context, component, value);
	}
}
