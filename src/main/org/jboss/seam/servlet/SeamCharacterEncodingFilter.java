package org.jboss.seam.servlet;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.jboss.seam.web.CharacterEncodingFilter;

/**
 * A servlet filter that lets you set the character encoding of 
 * submitted data. There are two init parameters: "encoding" and
 * "overrideClient".
 * 
 * @deprecated use CharacterEncodingFilter
 * @author Gavin King
 * 
 */
public class SeamCharacterEncodingFilter extends CharacterEncodingFilter
{
   
   @Override
   public void init(FilterConfig config) throws ServletException 
   {
      super.init(config);
      setEncoding( config.getInitParameter("encoding") );
      setOverrideClient( Boolean.parseBoolean( config.getInitParameter("overrideClient") ) );
   }
   

}