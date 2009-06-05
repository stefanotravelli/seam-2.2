package org.jboss.seam.navigation;

import java.util.Map;

import javax.el.ELContext;
import javax.el.ELException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.hibernate.validator.InvalidValue;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Validators;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * Metadata for a &lt;param/&gt; in pages.xml
 * 
 * @author Gavin King
 *
 */
public final class Param
{
   private static final LogProvider log = Logging.getLogProvider(Param.class);
   
   public static final String INVALID_MESSAGE_ID = "org.jboss.seam.param.Invalid";
   public static final String REQUIRED_MESSAGE_ID = "org.jboss.seam.param.Required";
   
   private final String name;
   private ValueExpression valueExpression;
   
   private boolean required;
   private boolean validateModel;
   
   private ValueExpression converterValueExpression;
   private String converterId;
   
   private ValueExpression validatorValueExpression;
   private String validatorId;
   
   public Param(String name)
   {
      this(name, true);
   }
   
   public Param(String name, boolean validateModel)
   {
      this.name = name;
      this.validateModel = validateModel;
   }
   
   public Converter getConverter()
   {
      if (converterId!=null)
      {
         return FacesContext.getCurrentInstance().getApplication().createConverter(converterId);
      }
      else if (converterValueExpression!=null)
      {
         return (Converter) converterValueExpression.getValue();
      }
      else if (valueExpression==null)
      {
         return null;
      }
      else
      {
         Class<?> type = valueExpression.getType();
         if (type==null)
         {
            return null;
         }
         return FacesContext.getCurrentInstance().getApplication().createConverter(type);           
      }
   }

   public Validator getValidator()
   {
      if (validatorId!=null)
      {
         return FacesContext.getCurrentInstance().getApplication().createValidator(validatorId);
      }
      else if (validatorValueExpression!=null)
      {
         return (Validator) validatorValueExpression.getValue();
      }
      else
      {
         return null;
      }
   }

   public String getName()
   {
      return name;
   }

   public void setValueExpression(ValueExpression valueExpression)
   {
      this.valueExpression = valueExpression;
   }

   public ValueExpression getValueExpression()
   {
      return valueExpression;
   }

   public void setConverterValueExpression(ValueExpression converterValueExpression)
   {
      this.converterValueExpression = converterValueExpression;
   }

   public ValueExpression getConverterValueExpression()
   {
      return converterValueExpression;
   }

   public void setConverterId(String converterId)
   {
      this.converterId = converterId;
   }

   public String getConverterId()
   {
      return converterId;
   }

   @Override
   public String toString()
   {
      return "PageParameter(" + name + ")";
   }

   /**
    * Get the current value of a page or redirection parameter
    * from the model, and convert to a String
    */
   public String getStringValueFromModel(FacesContext facesContext)
   {
      Object value = getValueExpression().getValue();
      if (value==null)
      {
         return null;
      }
      else
      {
         Converter converter = null;
         try
         {
            converter = getConverter();
         }
         catch (RuntimeException re)
         {
            //YUCK! due to bad JSF/MyFaces error handling
            log.warn("could not create converter for: " + name, re);
            return null;
         }
         
         return converter==null ? 
               value.toString() : 
               converter.getAsString( facesContext, facesContext.getViewRoot(), value );
      }
   }

   /**
    * Get the current value of a page parameter from the request parameters
    */
   public String getStringValueFromRequest(FacesContext facesContext, Map<String, String[]> requestParameters)
            throws ValidatorException
   {
      String[] parameterValues = requestParameters.get( getName() );

      if (parameterValues==null || parameterValues.length==0)
      {
         // Page parameters are propagated in the page context through faces requests
         if ( isRequired() && !Contexts.getPageContext().isSet( getName() ) )
         {
            addRequiredMessage(facesContext);
         }
         return null;
      }

      if (parameterValues.length>1)
      {
         throw new IllegalArgumentException("page parameter may not be multi-valued: " + getName());
      }         

      String value = parameterValues[0];
      
      // if the length is zero and the parameter is required, treat the value as
      // missing (and report it as null); this differs from how JSF handles inputs
      if (value.length() == 0)
      {
         if (isRequired())
         {
         addRequiredMessage(facesContext);
         return null;
      }
      }
      
      return value;
   }
   
   /**
    * Convert the string value of a page parameter to the required type
    */
   public Object convertValueFromString(FacesContext facesContext, String value)
   {
      Converter converter = null;
      try
      {
         converter = getConverter();
      }
      catch (RuntimeException re)
      {
         //YUCK! due to bad JSF/MyFaces error handling
         log.warn("could not create converter for: " + name, re);
         return null;
      }
      
      return converter==null ? 
            value :
            converter.getAsObject( facesContext, facesContext.getViewRoot(), value );
   }

   /**
    * Validate the pre-converted value of the parameter using the JSF
    * validator specified in pages.xml, and using Hibernate Validator
    * annotations specified on the model.
    */
   public void validateConvertedValue(FacesContext facesContext, Object value)
   {
      Validator validator = getValidator();
      if (validator!=null)
      {
         validator.validate( facesContext, facesContext.getViewRoot(), value );
      }
      
      if (valueExpression!=null && validateModel)
      {
         //TODO: note that this code is duplicated from ModelValidator!!
         ELContext elContext = facesContext.getELContext();
         InvalidValue[] invalidValues;
         try
         {
            invalidValues = Validators.instance().validate( valueExpression.toUnifiedValueExpression(), elContext, value );
         }
         catch (ELException ele)
         {
            Throwable cause = ele.getCause();
            if (cause==null) cause = ele;
            throw new ValidatorException( createMessage(cause), cause );
         }
         
         if ( invalidValues.length>0 )
         {
            throw new ValidatorException( createMessage(invalidValues) );
         }
      }
   }

   private FacesMessage createMessage(InvalidValue[] invalidValues)
   {
      return FacesMessages.createFacesMessage(
            FacesMessage.SEVERITY_ERROR,
            INVALID_MESSAGE_ID,
            "'" + name + "' parameter is invalid: " + invalidValues[0].getMessage(),
            new Object[] { invalidValues[0], name }
      );
   }

   private FacesMessage createMessage(Throwable cause)
   {
      return FacesMessages.createFacesMessage(
            FacesMessage.SEVERITY_ERROR,
            INVALID_MESSAGE_ID,
            "'" + name + "' parameter is invalid: " + cause.getMessage(),
            new Object[] { cause.getMessage(), name }
      );
   }

   private void addRequiredMessage(FacesContext facesContext)
   {
      throw new ValidatorException(
            FacesMessages.createFacesMessage(
                FacesMessage.SEVERITY_ERROR,
                REQUIRED_MESSAGE_ID,
                "'" + name + "' parameter is required",
                name)
      );
   }

   public String getValidatorId()
   {
      return validatorId;
   }

   public void setValidatorId(String validatorId)
   {
      this.validatorId = validatorId;
   }

   public ValueExpression getValidatorValueExpression()
   {
      return validatorValueExpression;
   }

   public void setValidatorValueExpression(ValueExpression validatorValueExpression)
   {
      this.validatorValueExpression = validatorValueExpression;
   }

   public boolean isRequired()
   {
      return required;
   }

   public void setRequired(boolean required)
   {
      this.required = required;
   }

   public boolean isValidateModel()
   {
      return validateModel;
   }

   public void setValidateModel(boolean validateModel)
   {
      this.validateModel = validateModel;
   }
   
}
