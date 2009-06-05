package org.jboss.seam.web;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface that declares multipart-specific API methods, to enable easier mocking of multipart
 * requests.
 *  
 * @author Shane Bryzak
 */
public interface MultipartRequest extends HttpServletRequest
{
   byte[] getFileBytes(String name);
   InputStream getFileInputStream(String name);
   String getFileContentType(String name);
   String getFileName(String name);
   int getFileSize(String name);
}
