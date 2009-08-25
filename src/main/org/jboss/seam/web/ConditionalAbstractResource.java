package org.jboss.seam.web;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Resources;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.net.URLConnection;
import java.net.URL;
import java.lang.management.ManagementFactory;

/**
 * Subclass this resource if you want to be able to send the right response automatically to
 * any conditional <tt>GET</tt> or <tt>HEAD</tt> request. The typically usecase is as follows:
 * <p/>
 * <pre>
 * public class MyResource extends ConditionalAbstractResource {
 *
 *     public void getResource(final HttpServletRequest request, final HttpServletResponse response) {
 *         String resourceVersion = ... // Calculate current state as string
 *         or
 *         byte[] resourceVersion = ... // Calculate current state as bytes
 *
 *         String resourcePath = ... // Get the relative (to servlet) path of the requested resource
 *
 *         if ( !sendConditional(request,
 *                              response,
 *                              createdEntityTag(resourceVersion, false),
 *                              getLastModifiedTimestamp(resourcePath) ) {
 * 
 *             // Send the regular resource representation with 200 OK etc.
 *         }
 *     }
 * }
 * </pre>
 * <p/>
 * Note that the <tt>getLastModifiedTimestamp()</tt> method is only supplied for convenience; it may not
 * return what you expect as the "last modification timestamp" of the given resource. In many cases you'd
 * rather calculate that timestamp yourself.
 * <p/>
 *
 * @author Christian Bauer
 */
public abstract class ConditionalAbstractResource extends AbstractResource
{

   public static final String HEADER_LAST_MODIFIED = "Last-Modified";
   public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";

   public static final String HEADER_ETAG = "ETag";
   public static final String HEADER_IF_NONE_MATCH = "If-None-Match";

   private static final LogProvider log = Logging.getLogProvider(ConditionalAbstractResource.class);

   /**
    * Validates the request headers <tt>If-Modified-Since</tt> and <tt>If-None-Match</tt> to determine
    * if a <tt>304 NOT MODIFIED</tt> response can be send. If that is the case, this method will automatically
    * send the response and return <tt>true</tt>. If condition validation fails, it will not change the
    * response and return <tt>false</tt>.
    * <p/>
    * Note that both <tt>entityTag</tt> and <tt>lastModified</tt> arguments can be <tt>null</tt>. The validation
    * procedure and the outcome depends on what the client requested. If the client requires that both entity tags and
    * modification timestamps be validated, both arguments must be supplied to the method and they must match, for
    * a 304 response to be send.
    * <p/>
    * In addition to responding with <tt>304 NOT MODIFIED</tt> when conditions match, this method will also, if
    * arguments are not <tt>null</tt>, send the right entity tag and last modification timestamps with the response,
    * so that future requests from the client can be made conditional.
    * <p/>
    *
    * @param request         The usual HttpServletRequest for header retrieval.
    * @param response        The usual HttpServletResponse for header manipulation.
    * @param entityTag       An entity tag (weak or strong, in doublequotes), typically produced by hashing the content
    *                        of the resource representation. If <tt>null</tt>, no entity tag will be send and if
    *                        validation is requested by the client, no match for a NOT MODIFIED response will be possible.
    * @param lastModified    The timestamp in number of milliseconds since unix epoch when the resource was
    *                        last modified. If <tt>null</tt>, no last modification timestamp will be send  and if
    *                        validation is requested by the client, no match for a NOT MODIFIED response will be possible.
    * @return <tt>true</tt> if a <tt>304 NOT MODIFIED</tt> response status has been set, <tt>false</tt> if requested
    *         conditions were invalid given the current state of the resource.
    * @throws IOException If setting the response status failed.
    */
   public boolean sendConditional(HttpServletRequest request,
                                  HttpServletResponse response,
                                  String entityTag, Long lastModified) throws IOException
   {

      String noneMatchHeader = request.getHeader(HEADER_IF_NONE_MATCH);
      Long modifiedSinceHeader = request.getDateHeader(HEADER_IF_MODIFIED_SINCE); // Careful, returns -1 instead of null!

      boolean noneMatchValid = false;
      if (entityTag != null)
      {

         if (! (entityTag.startsWith("\"") || entityTag.startsWith("W/\"")) && !entityTag.endsWith("\""))
         {
            throw new IllegalArgumentException("Entity tag is not properly formatted (or quoted): " + entityTag);
         }

         // Always send an entity tag with the response
         response.setHeader(HEADER_ETAG, entityTag);

         if (noneMatchHeader != null)
         {
            noneMatchValid = isNoneMatchConditionValid(noneMatchHeader, entityTag);
         }
      }

      boolean modifiedSinceValid = false;
      if (lastModified != null)
      {

         // Always send the last modified timestamp with the response
         response.setDateHeader(HEADER_LAST_MODIFIED, lastModified);

         if (modifiedSinceHeader != -1)
         {
            modifiedSinceValid = isModifiedSinceConditionValid(modifiedSinceHeader, lastModified);
         }

      }

      if (noneMatchHeader != null && modifiedSinceHeader != -1)
      {
         log.debug(HEADER_IF_NONE_MATCH + " and " + HEADER_IF_MODIFIED_SINCE + " must match");

         // If both are received, we must not return 304 unless doing so is consistent with both header fields in the request!
         if (noneMatchValid && modifiedSinceValid)
         {
            log.debug(HEADER_IF_NONE_MATCH + " and " + HEADER_IF_MODIFIED_SINCE + " conditions match, sending 304");
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return true;
         }
         else
         {
            log.debug(HEADER_IF_NONE_MATCH + " and " + HEADER_IF_MODIFIED_SINCE + " conditions do not match, not sending 304");
            return false;
         }
      }

      if (noneMatchHeader != null && noneMatchValid)
      {
         log.debug(HEADER_IF_NONE_MATCH + " condition matches, sending 304");
         response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
         return true;
      }

      if (modifiedSinceHeader != -1 && modifiedSinceValid)
      {
         log.debug(HEADER_IF_MODIFIED_SINCE + " condition matches, sending 304");
         response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
         return true;
      }

      log.debug("None of the cache conditions match, not sending 304");
      return false;
   }

   protected boolean isNoneMatchConditionValid(String noneMatchHeader, String entityTag)
   {
      if (noneMatchHeader.trim().equals("*"))
      {
         log.debug("Found * conditional request, hence current entity tag matches");
         return true;
      }
      String[] entityTagsArray = noneMatchHeader.trim().split(",");
      for (String requestTag : entityTagsArray)
      {
         if (requestTag.trim().equals(entityTag))
         {
            log.debug("Found matching entity tag in request");
            return true;
         }
      }
      log.debug("Resource has different entity tag than requested");
      return false;
   }

   protected boolean isModifiedSinceConditionValid(Long modifiedSinceHeader, Long lastModified)
   {
      if (lastModified <= modifiedSinceHeader)
      {
         log.debug("Resource has not been modified since requested timestamp");
         return true;
      }
      log.debug("Resource has been modified since requested timestamp");
      return false;
   }

   /**
    * Tries to get last modification timestamp of the resource by obtaining
    * a <tt>URLConnection</tt> to the file in the filesystem or JAR.
    *
    * @param resourcePath The relative (to the servlet) resource path.
    * @return Either the last modified filestamp or if an error occurs, the JVM system startup timestamp.
    */
   protected Long getLastModifiedTimestamp(String resourcePath)
   {
      try
      {
         // Try to load it from filesystem or JAR through URLConnection
         URL resourceURL = Resources.getResource(resourcePath, getServletContext());
         if (resourceURL == null)
         {
            // Fall back to startup time of the JVM
            return ManagementFactory.getRuntimeMXBean().getStartTime();
         }
         URLConnection resourceConn = resourceURL.openConnection();
         return resourceConn.getLastModified();
      }
      catch (Exception ex)
      {
         // Fall back to startup time of the JVM
         return ManagementFactory.getRuntimeMXBean().getStartTime();
      }
   }

   /**
    * Generates a (globally) unique identifier of the current state of the resource. The string will be
    * hashed with MD5 and the hash result is then formatted before it is returned. If <tt>null</tt>,
    * a <tt>null</tt> will be returned.
    *
    * @param hashSource The string source for hashing or the already hashed (strong or weak) entity tag.
    * @param weak       Set to <tt>true</tt> if you want a weak entity tag.
    * @return The hashed and formatted entity tag result.
    */
   protected String createEntityTag(String hashSource, boolean weak)
   {
      if (hashSource == null) return null;
      return (weak ? "W/\"" : "\"") + hash(hashSource, "UTF-8", "MD5") + "\"";
   }

   /**
    * Generates a (globally) unique identifier of the current state of the resource. The bytes will be
    * hashed with MD5 and the hash result is then formatted before it is returned. If <tt>null</tt>,
    * a <tt>null</tt> will be returned.
    *
    * @param hashSource The string source for hashing.
    * @param weak       Set to <tt>true</tt> if you want a weak entity tag.
    * @return The hashed and formatted entity tag result.
    */
   protected String createEntityTag(byte[] hashSource, boolean weak)
   {
      if (hashSource == null) return null;
      return (weak ? "W/\"" : "\"") + hash(hashSource, "MD5") + "\"";
   }

   protected String hash(String text, String charset, String algorithm)
   {
      try
      {
         return hash(text.getBytes(charset), algorithm);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   protected String hash(byte[] bytes, String algorithm)
   {
      try
      {
         MessageDigest md = MessageDigest.getInstance(algorithm);
         md.update(bytes);
         BigInteger number = new BigInteger(1, md.digest());
         StringBuffer sb = new StringBuffer("0");
         sb.append(number.toString(16));
         return sb.toString();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

}
