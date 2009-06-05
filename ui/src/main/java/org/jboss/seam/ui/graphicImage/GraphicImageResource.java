package org.jboss.seam.ui.graphicImage;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.jboss.seam.ui.graphicImage.GraphicImageStore.ImageWrapper;
import org.jboss.seam.web.AbstractResource;

/**
 * Serves images from the image store
 */
@Scope(APPLICATION)
@Name("org.jboss.seam.ui.graphicImage.graphicImageResource")
@Install(precedence = BUILT_IN)
@BypassInterceptors
public class GraphicImageResource extends AbstractResource
{

 public static final String GRAPHIC_IMAGE_RESOURCE_PATH = "/seam/resource/graphicImage";
   
   private static final String RESOURCE_PATH = "/graphicImage";
   
   @Override
   public String getResourcePath()
   {
      return RESOURCE_PATH;
   }
   
   @Override
   public void getResource(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException
   {

      new ContextualHttpServletRequest(request)
      {
         @Override
         public void process() throws IOException 
         {
            doWork(request, response);
         }
      }.run();
      
   }
   
   private void doWork(HttpServletRequest request, HttpServletResponse response)
      throws IOException
   {
      String pathInfo = request.getPathInfo().substring(getResourcePath().length() + 1,
               request.getPathInfo().lastIndexOf("."));
      ImageWrapper image = GraphicImageStore.instance().remove(pathInfo);
      if (image != null && image.getImage() != null)
      {
         response.setContentType(image.getContentType().getMimeType());
         response.setStatus(HttpServletResponse.SC_OK);
         response.setContentLength(image.getImage().length);
         ServletOutputStream os = response.getOutputStream();
         os.write(image.getImage());
         os.flush();
      }
      else
      {
         response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
   }

}
