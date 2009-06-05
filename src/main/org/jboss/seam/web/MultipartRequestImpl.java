package org.jboss.seam.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Request wrapper for supporting multipart requests, used for file uploading.
 * 
 * @author Shane Bryzak
 */
public class MultipartRequestImpl extends HttpServletRequestWrapper implements MultipartRequest
{   
   private static final String PARAM_NAME = "name";
   private static final String PARAM_FILENAME = "filename";
   private static final String PARAM_CONTENT_TYPE = "Content-Type";
   
   private static final int BUFFER_SIZE = 2048;
   private static final int CHUNK_SIZE = 512;
   
   private boolean createTempFiles;
   
   private String encoding = null;
   
   private Map<String,Param> parameters = null;
   
   private enum ReadState { BOUNDARY, HEADERS, DATA }   
   
   private static final byte CR = 0x0d;
   private static final byte LF = 0x0a;   
   private static final byte[] CR_LF = {CR,LF};
         
   private abstract class Param
   {
      private String name;
      
      public Param(String name)
      {
         this.name = name;
      }
      
      public String getName()
      {
         return name;
      }
      
      public abstract void appendData(byte[] data, int start, int length) 
         throws IOException;
   }
   
   private class ValueParam extends Param
   {
      private Object value = null;
      private ByteArrayOutputStream buf = new ByteArrayOutputStream();      
      
      public ValueParam(String name)
      {
         super(name);
      }
      
      @Override
      public void appendData(byte[] data, int start, int length)
         throws IOException
      {
         buf.write(data, start, length);
      }
      
      public void complete()
         throws UnsupportedEncodingException
      {
         String val = encoding == null ? new String(buf.toByteArray()) :
                                         new String(buf.toByteArray(), encoding);
         if (value == null)
         {
            value = val;
         }
         else 
         {
            if (!(value instanceof List))
            {
               List<String> v = new ArrayList<String>();
               v.add((String) value);
               value = v;
            }
            
            ((List) value).add(val);
         }            
         buf.reset();
      }
      
      public Object getValue()
      {
         return value;
      }
   }
   
   private class FileParam extends Param
   {
      private String filename;
      private String contentType;
      private int fileSize;
           
      private ByteArrayOutputStream bOut = null;
      private FileOutputStream fOut = null;
      private File tempFile = null;
      
      public FileParam(String name)
      {
         super(name);
      }      
      
      public String getFilename()
      {
         return filename;
      }
      
      public void setFilename(String filename)
      {
         this.filename = filename;
      }
      
      public String getContentType()
      {
         return contentType;
      }
      
      public void setContentType(String contentType)
      {
         this.contentType = contentType;
      }
      
      public int getFileSize()
      {
         return fileSize;
      }      
      
      public void createTempFile()
      {
         try
         {
            tempFile = File.createTempFile(new UID().toString().replace(":", "-"), ".upload");
            tempFile.deleteOnExit();
            fOut = new FileOutputStream(tempFile);            
         }
         catch (IOException ex)
         {
            throw new FileUploadException("Could not create temporary file");
         }
      }
      
      @Override
      public void appendData(byte[] data, int start, int length)
         throws IOException
      {
         if (fOut != null)
         {
            fOut.write(data, start, length);
            fOut.flush();
         }
         else
         {
            if (bOut == null) bOut = new ByteArrayOutputStream();
            bOut.write(data, start, length);
         }
         
         fileSize += length;
      }
      
      public byte[] getData()
      {
         if (fOut != null)
         {
            try
            {
               fOut.close();
            }
            catch (IOException ex) {}
            fOut = null;
         }
         
         if (bOut != null)
         {
            return bOut.toByteArray();
         }
         else if (tempFile != null)
         {
            if (tempFile.exists())
            {
               try
               {
                  FileInputStream fIn = new FileInputStream(tempFile);
                  ByteArrayOutputStream bOut = new ByteArrayOutputStream();
                  byte[] buf = new byte[512];
                  int read = fIn.read(buf);
                  while (read != -1)
                  {
                     bOut.write(buf, 0, read);
                     read = fIn.read(buf);
                  }
                  bOut.flush();

                  fIn.close();
                  tempFile.delete();
                  return bOut.toByteArray();
               }
               catch (IOException ex) { /* too bad? */}
            }
         }
        
        return null;
      }
      
      public InputStream getInputStream()
      {
         if (fOut != null)
         {
            try
            {
               fOut.close();
            }
            catch (IOException ex) {}
            fOut = null;
         }
         
         if (bOut!=null)
         {
            return new ByteArrayInputStream(bOut.toByteArray());
         }
         else if (tempFile!=null)
         {
            try
            {
               return new FileInputStream(tempFile) {
                  @Override
                  public void close() throws IOException
                  {
                     super.close();
                     tempFile.delete();
                  }
               };
            }
            catch (FileNotFoundException ex) { }
         }
         
         return null;
      }
   }
   
   private HttpServletRequest request;

   public MultipartRequestImpl(HttpServletRequest request, boolean createTempFiles,
            int maxRequestSize)
   {
      super(request);
      this.request = request;
      this.createTempFiles = createTempFiles;
      
      String contentLength = request.getHeader("Content-Length");
      if (contentLength != null && maxRequestSize > 0 && 
               Integer.parseInt(contentLength) > maxRequestSize)
      {
         throw new FileUploadException("Multipart request is larger than allowed size");
      }
   }

   private void parseRequest()
   {               
      byte[] boundaryMarker = getBoundaryMarker(request.getContentType());
      if (boundaryMarker == null)
      {
         throw new FileUploadException("The request was rejected because "
                  + "no multipart boundary was found");
      }
      
      encoding = request.getCharacterEncoding();    
      
      parameters = new HashMap<String,Param>();      
      
      try
      {
         byte[] buffer = new byte[BUFFER_SIZE];         
         Map<String,String> headers = new HashMap<String,String>();
         
         ReadState readState = ReadState.BOUNDARY;
         
         InputStream input = request.getInputStream();
         int read = input.read(buffer);
         int pos = 0;
         
         Param p = null;
         
         // This is a fail-safe to prevent infinite loops from occurring in some environments
         int loopCounter = 20;
         
         while (read > 0 && loopCounter > 0)
         {
            for (int i = 0; i < read; i++)
            {
               switch (readState)
               {
                  case BOUNDARY:
                  {
                     if (checkSequence(buffer, i, boundaryMarker) && checkSequence(buffer, i + 2, CR_LF))
                     {
                        readState = ReadState.HEADERS;
                        i += 2;
                        pos = i + 1;
                     }
                     break;
                  }
                  case HEADERS:
                  {
                     if (checkSequence(buffer, i, CR_LF))
                     {
                        String param = (encoding == null) ? 
                                 new String(buffer, pos, i - pos - 1) :
                                 new String(buffer, pos, i - pos - 1, encoding);                        
                        parseParams(param, ";", headers);
                           
                        if (checkSequence(buffer, i + CR_LF.length, CR_LF))
                        {
                           readState = ReadState.DATA;
                           i += CR_LF.length;
                           pos = i + 1;
                           
                           String paramName = headers.get(PARAM_NAME);
                           if (paramName != null)
                           {
                              if (headers.containsKey(PARAM_FILENAME))
                              {
                                 FileParam fp = new FileParam(paramName);
                                 if (createTempFiles) fp.createTempFile();                                 
                                 fp.setContentType(headers.get(PARAM_CONTENT_TYPE));
                                 fp.setFilename(headers.get(PARAM_FILENAME));
                                 p = fp;                                 
                              }
                              else
                              {
                                 if (parameters.containsKey(paramName))
                                 {
                                    p = parameters.get(paramName);
                                 }
                                 else
                                 {
                                    p = new ValueParam(paramName);
                                 }
                              }
                              
                              if (!parameters.containsKey(paramName))
                              {
                                 parameters.put(paramName, p);                              
                              }
                           }
                           
                           headers.clear();
                        }
                        else
                        {
                           pos = i + 1;
                        }
                     }
                     break;                     
                  }
                  case DATA:
                  {
                     // If we've encountered another boundary...
                     if (checkSequence(buffer, i - boundaryMarker.length - CR_LF.length, CR_LF) &&
                         checkSequence(buffer, i, boundaryMarker))
                     {
                        // Write any data before the boundary (that hasn't already been written) to the param
                        if (pos < i - boundaryMarker.length - CR_LF.length - 1)
                        {
                          p.appendData(buffer, pos, i - pos - boundaryMarker.length - CR_LF.length - 1);
                        }
                        
                        if (p instanceof ValueParam) ((ValueParam) p).complete();
                        
                        if (checkSequence(buffer, i + CR_LF.length, CR_LF))
                        {
                           i += CR_LF.length;
                           pos = i + 1;
                        }
                        else
                        {
                           pos = i;
                        }
                        
                        readState = ReadState.HEADERS;
                     }
                     // Otherwise write whatever data we have to the param
                     else if (i > (pos + boundaryMarker.length + CHUNK_SIZE + CR_LF.length))
                     {
                        p.appendData(buffer, pos, CHUNK_SIZE);
                        pos += CHUNK_SIZE;
                     }
                     break;                     
                  }               
               }
            }               
            
            if (pos < read)
            {
               // move the bytes that weren't read to the start of the buffer
               int bytesNotRead = read - pos;
               System.arraycopy(buffer, pos, buffer, 0, bytesNotRead);               
               read = input.read(buffer, bytesNotRead, buffer.length - bytesNotRead);
               
               // Decrement loopCounter if no data was readable
               if (read == 0)
               {
                  loopCounter--;
               }
               
               read += bytesNotRead;
            }
            else
            {
               read = input.read(buffer);
            }
            
            pos = 0;                                    
         }
      }
      catch (IOException ex)
      {
         throw new FileUploadException("IO Error parsing multipart request", ex);
      }
   }
   
   private byte[] getBoundaryMarker(String contentType)
   {
      Map<String, Object> params = parseParams(contentType, ";");
      String boundaryStr = (String) params.get("boundary");

      if (boundaryStr == null) return null;

      try
      {
         return boundaryStr.getBytes("ISO-8859-1");
      }
      catch (UnsupportedEncodingException e)
      {
         return boundaryStr.getBytes();
      }
   }   
   
   /**
    * Checks if a specified sequence of bytes ends at a specific position
    * within a byte array.
    * 
    * @param data
    * @param pos
    * @param seq
    * @return boolean indicating if the sequence was found at the specified position
    */
   private boolean checkSequence(byte[] data, int pos, byte[] seq)
   {
      if (pos - seq.length < -1 || pos >= data.length)
         return false;
      
      for (int i = 0; i < seq.length; i++)
      {
         if (data[(pos - seq.length) + i + 1] != seq[i])
            return false;
      }
      
      return true;
   }

   private static final Pattern PARAM_VALUE_PATTERN = Pattern
            .compile("^\\s*([^\\s=]+)\\s*[=:]\\s*(.+)\\s*$");

   private Map parseParams(String paramStr, String separator)
   {
      Map<String,String> paramMap = new HashMap<String, String>();
      parseParams(paramStr, separator, paramMap);
      return paramMap;
   }
   
   private void parseParams(String paramStr, String separator, Map paramMap)
   {
      String[] parts = paramStr.split("[" + separator + "]");

      for (String part : parts)
      {
         Matcher m = PARAM_VALUE_PATTERN.matcher(part);
         if (m.matches())
         {
            String key = m.group(1);
            String value = m.group(2);
            
            // Strip double quotes
            if (value.startsWith("\"") && value.endsWith("\""))
               value = value.substring(1, value.length() - 1);
            
            paramMap.put(key, value);
         }
      }    
   }

   private Param getParam(String name)
   {
      if (parameters == null) 
         parseRequest();
      return parameters.get(name);
   }

   @Override
   public Enumeration getParameterNames()
   {
      if (parameters == null) 
         parseRequest();

      return Collections.enumeration(parameters.keySet());
   }
   
   public byte[] getFileBytes(String name)
   {
      Param p = getParam(name);
      return (p != null && p instanceof FileParam) ? 
               ((FileParam) p).getData() : null;
   }
   
   public InputStream getFileInputStream(String name)
   {
      Param p = getParam(name);
      return (p != null && p instanceof FileParam) ? 
               ((FileParam) p).getInputStream() : null;      
   }
   
   public String getFileContentType(String name)
   {
      Param p = getParam(name);
      return (p != null && p instanceof FileParam) ? 
               ((FileParam) p).getContentType() : null;
   }
   
   public String getFileName(String name)
   {
      Param p = getParam(name);    
      return (p != null && p instanceof FileParam) ? 
               ((FileParam) p).getFilename() : null;
   }   
   
   public int getFileSize(String name)
   {
      Param p = getParam(name);    
      return (p != null && p instanceof FileParam) ? 
               ((FileParam) p).getFileSize() : -1;      
   }
   
   @Override
   public String getParameter(String name)
   {
      Param p = getParam(name);
      if (p != null && p instanceof ValueParam)
      {
         ValueParam vp = (ValueParam) p;
         if (vp.getValue() instanceof String) return (String) vp.getValue();
      }
      else if (p != null && p instanceof FileParam)
      {
         return "---BINARY DATA---";
      }      
      else
      {
         return super.getParameter(name);
      }
      
      return null;
   }

   @Override
   public String[] getParameterValues(String name)
   {
      Param p = getParam(name);
      if (p != null && p instanceof ValueParam)
      {
         ValueParam vp = (ValueParam) p;
         if (vp.getValue() instanceof List)
         {
            List vals = (List) vp.getValue();
            String[] values = new String[vals.size()];
            vals.toArray(values);
            return values;
         }
         else
         {
            return new String[] {(String) vp.getValue()};
         }
      }
      else
      {
         return super.getParameterValues(name);
      }
   }

   @Override
   public Map getParameterMap()
   {
      if (parameters == null)
         parseRequest();

      Map<String,Object> params = new HashMap<String,Object>(super.getParameterMap());
      
      for (String name : parameters.keySet())
      {
         Param p = parameters.get(name);
         if (p instanceof ValueParam)
         {
            ValueParam vp = (ValueParam) p;
            if (vp.getValue() instanceof String)
            {
               params.put(name, vp.getValue());               
            }
            else if (vp.getValue() instanceof List)
            {
               params.put(name, getParameterValues(name));
            }               
         }
      }
      
      return params;
   }
}
