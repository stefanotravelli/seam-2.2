package org.jboss.seam.navigation;

import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.core.Expressions.ValueExpression;

public class Header {
    public String name;
    public ValueExpression<Object> expression;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public ValueExpression<Object> getValue() {
        return expression;
    }
    public void setValue(ValueExpression<Object> valueExpression) {
        this.expression = valueExpression;
    }
    
    public void sendHeader(HttpServletResponse response) {
        sendHeader(response, name, evaluateValue());
    }
    
    private String evaluateValue() {
        String result = "";
        
        if (expression != null) {
            Object value = expression.getValue();
       
            if (value != null) {
                result = value.toString(); 
            }
        }
        
        return result;
    }
    
    
    public static void sendHeader(HttpServletResponse response, 
                                  String name,
                                  String value)
    {
        if (response !=null && name !=null && value != null) {
            response.addHeader(name, value);
        }
    }
}
