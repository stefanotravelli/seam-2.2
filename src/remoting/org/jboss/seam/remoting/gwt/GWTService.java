package org.jboss.seam.remoting.gwt;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.core.ConversationPropagation;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.jboss.seam.web.AbstractResource;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RPCServletUtils;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;
import com.google.gwt.user.server.rpc.UnexpectedException;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;

/**
 * Abstract base class for GWT 1.5 integration.
 * 
 * @author Shane Bryzak
 */
public abstract class GWTService extends AbstractResource implements SerializationPolicyProvider
{
   protected static final LogProvider log = Logging.getLogProvider(GWTService.class);

   private static final HashMap<String, Class<?>> TYPE_NAMES;

   static
   {
      TYPE_NAMES = new HashMap<String, Class<?>>();
      TYPE_NAMES.put("Z", boolean.class);
      TYPE_NAMES.put("B", byte.class);
      TYPE_NAMES.put("C", char.class);
      TYPE_NAMES.put("D", double.class);
      TYPE_NAMES.put("F", float.class);
      TYPE_NAMES.put("I", int.class);
      TYPE_NAMES.put("J", long.class);
      TYPE_NAMES.put("S", short.class);

   }
   
   /**
    * A cache of moduleBaseURL and serialization policy strong name to
    * {@link SerializationPolicy}.
    */
   private final Map<String, SerializationPolicy> serializationPolicyCache = new HashMap<String, SerializationPolicy>();

   @Override
   public String getResourcePath()
   {
      return "/gwt";
   }

   protected abstract ServerSerializationStreamReader getStreamReader();

   protected abstract ServerSerializationStreamWriter getStreamWriter();

   protected abstract String createResponse(
         ServerSerializationStreamWriter stream, Class responseType,
         Object responseObj, boolean isException);

   // private final Set knownImplementedInterfaces = new HashSet();
   private final ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();

   private final ThreadLocal<HttpServletResponse> perThreadResponse = new ThreadLocal<HttpServletResponse>();

   /**
    * This is called internally.
    * 
    * @see RemoteServiceServlet#doPost
    */
   @Override
   public final void getResource(final HttpServletRequest request,
         final HttpServletResponse response) throws ServletException,
         IOException
   {
      try
      {
         // Store the request & response objects in thread-local storage.
         perThreadRequest.set(request);
         perThreadResponse.set(response);

         new ContextualHttpServletRequest(request) {
            @Override
            public void process() throws Exception
            {

               try
               {
                  // Read the request fully.
                  //
                  String requestPayload = RemoteServiceServlet_readContent(request);

                  RemoteServiceServlet_onBeforeRequestDeserialized(requestPayload);

                  // Invoke the core dispatching logic, which returns the
                  // serialized result
                  String responsePayload = processCall(requestPayload);

                  RemoteServiceServlet_onAfterResponseSerialized(responsePayload);

                  // Write the response.
                  //
                  RemoteServiceServlet_writeResponse(request, response,
                        responsePayload);

               } catch (Throwable e)
               {
                  RemoteServiceServlet_doUnexpectedFailure(e);
               }

            }

            @Override
            protected void restoreConversationId()
            {
               ConversationPropagation.instance().setConversationId(
                     GWTService.this.perThreadRequest.get().getParameter(
                           "conversationId"));
            }

            @Override
            protected void handleConversationPropagation()
            {
            }
         }.run();
      } finally
      {
         perThreadRequest.remove();
         perThreadResponse.remove();
      }
   }

   /**
    * This is public so that it can be unit tested easily without HTTP.
    */
   public String processCall(String payload) throws SerializationException
   {
      // Create a stream to deserialize the request.
      //
      // ServerSerializationStreamReader streamReader = getStreamReader();
      // streamReader.prepareToRead(payload);
      //
      // // Read the service interface
      // //
      // String serviceIntfName = streamReader.readString();
      //
      // // Read the method name.
      // //
      // String methodName = streamReader.readString();
      //
      // // Read the number and names of the parameter classes from the stream.
      // // We have to do this so that we can find the correct overload of the
      // // method.
      // //
      // int paramCount = streamReader.readInt();
      // Class[] paramTypes = new Class[paramCount];
      // for (int i = 0; i < paramTypes.length; i++)
      // {
      // String paramClassName = streamReader.readString();
      // try
      // {
      // paramTypes[i] = getClassOrPrimitiveFromName(paramClassName);
      // } catch (ClassNotFoundException e)
      // {
      // throw new SerializationException("Unknown parameter " + i
      // + " type '" + paramClassName + "'", e);
      // }
      // }
      //
      // // Deserialize the parameters.
      // //
      // Object[] args = new Object[paramCount];
      // for (int i = 0; i < args.length; i++)
      // {
      // args[i] = streamReader.deserializeValue(paramTypes[i]);
      // }

      try
      {
         SeamRPCRequest rpcRequest = RPC_decodeRequest(payload,
               this.getClass(), this);

         return RPC_invokeAndEncodeResponse(this, rpcRequest.getMethod(),
               rpcRequest.getParameterTypes(), rpcRequest.getParameters(),
               rpcRequest.getSerializationPolicy());
      } catch (IncompatibleRemoteServiceException ex)
      {
         getServletContext()
               .log(
                     "An IncompatibleRemoteServiceException was thrown while processing this call.",
                     ex);
         return RPC.encodeResponseForFailure(null, ex);
      }

      // Make the call via reflection.
      //
      // String responsePayload = GENERIC_FAILURE_MSG;
      // ServerSerializationStreamWriter streamWriter = getStreamWriter();
      // Throwable caught = null;
      // try
      // {
      // GWTToSeamAdapter.ReturnedObject returnedObject =
      // adapter.callWebRemoteMethod(
      // serviceIntfName, methodName, paramTypes, args);
      // Class returnType = returnedObject.returnType;
      // Object returnVal = returnedObject.returnedObject;
      // // Class returnType = serviceIntfMethod.getReturnType();
      // // Object returnVal = serviceIntfMethod.invoke(this, args);
      // responsePayload = createResponse(streamWriter, returnType, returnVal,
      // false);
      // } catch (IllegalArgumentException e)
      // {
      // caught = e;
      // } catch (IllegalAccessException e)
      // {
      // caught = e;
      // } catch (InvocationTargetException e)
      // {
      // // Try to serialize the caught exception if the client is expecting it,
      // // otherwise log the exception server-side.
      // caught = e;
      // Throwable cause = e.getCause();
      // if (cause != null)
      // {
      // // Update the caught exception to the underlying cause
      // caught = cause;
      // // Serialize the exception back to the client if it's a declared
      // // exception
      // if (cause instanceof SerializableException)
      // {
      // Class thrownClass = cause.getClass();
      // responsePayload = createResponse(streamWriter, thrownClass,
      // cause, true);
      // // Don't log the exception on the server
      // caught = null;
      // }
      // }
      // }
      //
      // if (caught != null)
      // {
      // responsePayload = GENERIC_FAILURE_MSG;
      // ServletContext servletContext = getServletContext();
      // // servletContext may be null (for example, when unit testing)
      // if (servletContext != null)
      // {
      // // Log the exception server side
      // servletContext.log("Exception while dispatching incoming RPC call",
      // caught);
      // }
      // }
   }

   /**
    * Gets the <code>HttpServletRequest</code> object for the current call. It
    * is stored thread-locally so that simultaneous invocations can have
    * different request objects.
    */
   protected final HttpServletRequest getThreadLocalRequest()
   {
      return perThreadRequest.get();
   }

   /**
    * Gets the <code>HttpServletResponse</code> object for the current call. It
    * is stored thread-locally so that simultaneous invocations can have
    * different response objects.
    */
   protected final HttpServletResponse getThreadLocalResponse()
   {
      return perThreadResponse.get();
   }

   /**
    * Returns an {@link RPCRequest} that is built by decoding the contents of an
    * encoded RPC request and optionally validating that type can handle the
    * request. If the type parameter is not <code>null</code>, the
    * implementation checks that the type is assignable to the
    * {@link com.google.gwt.user.client.rpc.RemoteService} interface requested
    * in the encoded request string.
    * 
    * <p>
    * If the serializationPolicyProvider parameter is not <code>null</code>, it
    * is asked for a {@link SerializationPolicy} to use to restrict the set of
    * types that can be decoded from the request. If this parameter is
    * <code>null</code>, then only subtypes of
    * {@link com.google.gwt.user.client.rpc.IsSerializable IsSerializable} or
    * types which have custom field serializers can be decoded.
    * </p>
    * 
    * <p>
    * Invoking this method with <code>null</code> for the type parameter,
    * <code>decodeRequest(encodedRequest, null)</code>, is equivalent to calling
    * <code>decodeRequest(encodedRequest)</code>.
    * </p>
    * 
    * @param encodedRequest
    *           a string that encodes the
    *           {@link com.google.gwt.user.client.rpc.RemoteService} interface,
    *           the service method, and the arguments to pass to the service
    *           method
    * @param type
    *           if not <code>null</code>, the implementation checks that the
    *           type is assignable to the
    *           {@link com.google.gwt.user.client.rpc.RemoteService} interface
    *           encoded in the encoded request string.
    * @param serializationPolicyProvider
    *           if not <code>null</code>, the implementation asks this provider
    *           for a {@link SerializationPolicy} which will be used to restrict
    *           the set of types that can be decoded from this request
    * @return an {@link RPCRequest} instance
    * 
    * @throws NullPointerException
    *            if the encodedRequest is <code>null</code>
    * @throws IllegalArgumentException
    *            if the encodedRequest is an empty string
    * @throws IncompatibleRemoteServiceException
    *            if any of the following conditions apply:
    *            <ul>
    *            <li>if the types in the encoded request cannot be deserialized</li>
    *            <li>if the {@link ClassLoader} acquired from
    *            <code>Thread.currentThread().getContextClassLoader()</code>
    *            cannot load the service interface or any of the types specified
    *            in the encodedRequest</li>
    *            <li>the requested interface is not assignable to
    *            {@link com.google.gwt.user.client.rpc.RemoteService}</li>
    *            <li>the service method requested in the encodedRequest is not a
    *            member of the requested service interface</li>
    *            <li>the type parameter is not <code>null</code> and is not
    *            assignable to the requested
    *            {@link com.google.gwt.user.client.rpc.RemoteService} interface
    *            </ul>
    */
   public static SeamRPCRequest RPC_decodeRequest(String encodedRequest,
         Class<?> type, SerializationPolicyProvider serializationPolicyProvider)
   {
      if (encodedRequest == null)
      {
         throw new NullPointerException("encodedRequest cannot be null");
      }

      if (encodedRequest.length() == 0)
      {
         throw new IllegalArgumentException("encodedRequest cannot be empty");
      }

      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      try
      {
         ServerSerializationStreamReader streamReader = new ServerSerializationStreamReader(
               classLoader, serializationPolicyProvider);
         streamReader.prepareToRead(encodedRequest);

         // Read the name of the RemoteService interface
         String serviceIntfName = streamReader.readString();

         /*
          * todo?? if (type != null) { if (!implementsInterface(type,
          * serviceIntfName)) { // The service does not implement the requested
          * interface throw new IncompatibleRemoteServiceException(
          * "Blocked attempt to access interface '" + serviceIntfName +
          * "', which is not implemented by '" + printTypeName(type) +
          * "'; this is either misconfiguration or a hack attempt"); } }
          */

         SerializationPolicy serializationPolicy = streamReader
               .getSerializationPolicy();
         Class<?> serviceIntf;
         try
         {
            serviceIntf = RPC_getClassFromSerializedName(serviceIntfName,
                  classLoader);
            if (!RemoteService.class.isAssignableFrom(serviceIntf))
            {
               // The requested interface is not a RemoteService interface
               throw new IncompatibleRemoteServiceException(
                     "Blocked attempt to access interface '"
                           + printTypeName(serviceIntf)
                           + "', which doesn't extend RemoteService; this is either misconfiguration or a hack attempt");
            }
         } catch (ClassNotFoundException e)
         {
            throw new IncompatibleRemoteServiceException(
                  "Could not locate requested interface '" + serviceIntfName
                        + "' in default classloader", e);
         }

         String serviceMethodName = streamReader.readString();

         int paramCount = streamReader.readInt();
         Class<?>[] parameterTypes = new Class[paramCount];

         for (int i = 0; i < parameterTypes.length; i++)
         {
            String paramClassName = streamReader.readString();
            try
            {
               parameterTypes[i] = RPC_getClassFromSerializedName(
                     paramClassName, classLoader);
            } catch (ClassNotFoundException e)
            {
               throw new IncompatibleRemoteServiceException("Parameter " + i
                     + " of is of an unknown type '" + paramClassName + "'", e);
            }
         }

         try
         {
            Method method = serviceIntf.getMethod(serviceMethodName,
                  parameterTypes);

            Object[] parameterValues = new Object[parameterTypes.length];
            for (int i = 0; i < parameterValues.length; i++)
            {
               parameterValues[i] = streamReader
                     .deserializeValue(parameterTypes[i]);
            }

            return new SeamRPCRequest(method, parameterValues, parameterTypes,
                  serializationPolicy);

         } catch (NoSuchMethodException e)
         {
            throw new IncompatibleRemoteServiceException(
                  formatMethodNotFoundErrorMessage(serviceIntf,
                        serviceMethodName, parameterTypes));
         }
      } catch (SerializationException ex)
      {
         throw new IncompatibleRemoteServiceException(ex.getMessage(), ex);
      }
   }

   /**
    * Returns the {@link Class} instance for the named class or primitive type.
    * 
    * @param serializedName
    *           the serialized name of a class or primitive type
    * @param classLoader
    *           the classLoader used to load {@link Class}es
    * @return Class instance for the given type name
    * @throws ClassNotFoundException
    *            if the named type was not found
    */
   private static Class<?> RPC_getClassFromSerializedName(
         String serializedName, ClassLoader classLoader)
         throws ClassNotFoundException
   {
      Class<?> value = TYPE_NAMES.get(serializedName);
      if (value != null)
      {
         return value;
      }

      return Class.forName(serializedName, false, classLoader);
   }

   /**
    * Returns a string that encodes the result of calling a service method,
    * which could be the value returned by the method or an exception thrown by
    * it.
    * 
    * <p>
    * If the serializationPolicy parameter is not <code>null</code>, it is used
    * to determine what types can be encoded as part of this response. If this
    * parameter is <code>null</code>, then only subtypes of
    * {@link com.google.gwt.user.client.rpc.IsSerializable IsSerializable} or
    * types which have custom field serializers may be encoded.
    * </p>
    * 
    * <p>
    * This method does no security checking; security checking must be done on
    * the method prior to this invocation.
    * </p>
    * 
    * @param target
    *           instance on which to invoke the serviceMethod
    * @param serviceMethod
    *           the method to invoke
    * @param args
    *           arguments used for the method invocation
    * @param serializationPolicy
    *           determines the serialization policy to be used
    * @return a string which encodes either the method's return or a checked
    *         exception thrown by the method
    * 
    * @throws NullPointerException
    *            if the serviceMethod or the serializationPolicy are
    *            <code>null</code>
    * @throws SecurityException
    *            if the method cannot be accessed or if the number or type of
    *            actual and formal arguments differ
    * @throws SerializationException
    *            if an object could not be serialized by the stream
    * @throws UnexpectedException
    *            if the serviceMethod throws a checked exception that is not
    *            declared in its signature
    */
   public static String RPC_invokeAndEncodeResponse(Object target,
         Method serviceMethod, Class[] paramTypes, Object[] args,
         SerializationPolicy serializationPolicy) throws SerializationException
   {
      if (serviceMethod == null)
      {
         throw new NullPointerException("serviceMethod");
      }

      if (serializationPolicy == null)
      {
         throw new NullPointerException("serializationPolicy");
      }

      String responsePayload;
      try
      {
         GWTToSeamAdapter adapter = GWTToSeamAdapter.instance();

         String serviceIntfName = serviceMethod.getDeclaringClass().getName();

         GWTToSeamAdapter.ReturnedObject returnedObject = adapter
               .callWebRemoteMethod(serviceIntfName, serviceMethod.getName(),
                     paramTypes, args);

         // Object result = serviceMethod.invoke(target, args);

         responsePayload = RPC.encodeResponseForSuccess(serviceMethod,
               returnedObject.returnedObject, serializationPolicy);
      } catch (IllegalAccessException e)
      {
         SecurityException securityException = new SecurityException(
               formatIllegalAccessErrorMessage(target, serviceMethod));
         securityException.initCause(e);
         throw securityException;
      } catch (IllegalArgumentException e)
      {
         SecurityException securityException = new SecurityException(
               formatIllegalArgumentErrorMessage(target, serviceMethod, args));
         securityException.initCause(e);
         throw securityException;
      } catch (InvocationTargetException e)
      {
         // Try to encode the caught exception
         //
         Throwable cause = e.getCause();

         responsePayload = RPC.encodeResponseForFailure(serviceMethod, cause,
               serializationPolicy);
      }

      return responsePayload;
   }

   /**
    * Override this method to examine the serialized response that will be
    * returned to the client. The default implementation does nothing and need
    * not be called by subclasses.
    */
   protected void RemoteServiceServlet_onAfterResponseSerialized(
         String serializedResponse)
   {
   }

   /**
    * Override this method to examine the serialized version of the request
    * payload before it is deserialized into objects. The default implementation
    * does nothing and need not be called by subclasses.
    */
   protected void RemoteServiceServlet_onBeforeRequestDeserialized(
         String serializedRequest)
   {
   }

   /**
    * Override this method in order to control the parsing of the incoming
    * request. For example, you may want to bypass the check of the Content-Type
    * and character encoding headers in the request, as some proxies re-write
    * the request headers. Note that bypassing these checks may expose the
    * servlet to some cross-site vulnerabilities.
    * 
    * @param request
    *           the incoming request
    * @return the content of the incoming request encoded as a string.
    */
   protected String RemoteServiceServlet_readContent(HttpServletRequest request)
         throws ServletException, IOException
   {
      return RPCServletUtils.readContentAsUtf8(request, true);
   }

   public final SerializationPolicy getSerializationPolicy(
         String moduleBaseURL, String strongName)
   {

      SerializationPolicy serializationPolicy = getCachedSerializationPolicy(
            moduleBaseURL, strongName);
      if (serializationPolicy != null)
      {
         return serializationPolicy;
      }

      serializationPolicy = doGetSerializationPolicy(getThreadLocalRequest(),
            moduleBaseURL, strongName);

      if (serializationPolicy == null)
      {
         // Failed to get the requested serialization policy; use the default
         getServletContext()
               .log(
                     "WARNING: Failed to get the SerializationPolicy '"
                           + strongName
                           + "' for module '"
                           + moduleBaseURL
                           + "'; a legacy, 1.3.3 compatible, serialization policy will be used.  You may experience SerializationExceptions as a result.");
         serializationPolicy = RPC.getDefaultSerializationPolicy();
      }

      // This could cache null or an actual instance. Either way we will not
      // attempt to lookup the policy again.
      putCachedSerializationPolicy(moduleBaseURL, strongName,
            serializationPolicy);

      return serializationPolicy;
   }

   private SerializationPolicy getCachedSerializationPolicy(
         String moduleBaseURL, String strongName)
   {
      synchronized (serializationPolicyCache)
      {
         return serializationPolicyCache.get(moduleBaseURL + strongName);
      }
   }

   private void putCachedSerializationPolicy(String moduleBaseURL,
         String strongName, SerializationPolicy serializationPolicy)
   {
      synchronized (serializationPolicyCache)
      {
         serializationPolicyCache.put(moduleBaseURL + strongName,
               serializationPolicy);
      }
   }

   /**
    * Gets the {@link SerializationPolicy} for given module base URL and strong
    * name if there is one.
    * 
    * Override this method to provide a {@link SerializationPolicy} using an
    * alternative approach.
    * 
    * @param request
    *           the HTTP request being serviced
    * @param moduleBaseURL
    *           as specified in the incoming payload
    * @param strongName
    *           a strong name that uniquely identifies a serialization policy
    *           file
    * @return a {@link SerializationPolicy} for the given module base URL and
    *         strong name, or <code>null</code> if there is none
    */
   protected SerializationPolicy doGetSerializationPolicy(
         HttpServletRequest request, String moduleBaseURL, String strongName)
   {
      // The request can tell you the path of the web app relative to the
      // container root.
      String contextPath = request.getContextPath();

      String modulePath = null;
      if (moduleBaseURL != null)
      {
         try
         {
            modulePath = new URL(moduleBaseURL).getPath();
         } catch (MalformedURLException ex)
         {
            // log the information, we will default
            getServletContext().log(
                  "Malformed moduleBaseURL: " + moduleBaseURL, ex);
         }
      }

      SerializationPolicy serializationPolicy = null;

      /*
       * Check that the module path must be in the same web app as the servlet
       * itself. If you need to implement a scheme different than this, override
       * this method.
       */
      if (modulePath == null || !modulePath.startsWith(contextPath))
      {
         String message = "ERROR: The module path requested, "
               + modulePath
               + ", is not in the same web application as this servlet, "
               + contextPath
               + ".  Your module may not be properly configured or your client and server code maybe out of date.";
         getServletContext().log(message);
      } else
      {
         // Strip off the context path from the module base URL. It should be a
         // strict prefix.
         String contextRelativePath = modulePath
               .substring(contextPath.length());

         String serializationPolicyFilePath = SerializationPolicyLoader
               .getSerializationPolicyFileName(contextRelativePath + strongName);

         // Open the RPC resource file read its contents.
         InputStream is = getServletContext().getResourceAsStream(
               serializationPolicyFilePath);
         try
         {
            if (is != null)
            {
               try
               {
                  serializationPolicy = SerializationPolicyLoader
                        .loadFromStream(is, null);
               } catch (ParseException e)
               {
                  getServletContext().log(
                        "ERROR: Failed to parse the policy file '"
                              + serializationPolicyFilePath + "'", e);
               } catch (IOException e)
               {
                  getServletContext().log(
                        "ERROR: Could not read the policy file '"
                              + serializationPolicyFilePath + "'", e);
               }
            } else
            {
               String message = "ERROR: The serialization policy file '"
                     + serializationPolicyFilePath
                     + "' was not found; did you forget to include it in this deployment?";
               getServletContext().log(message);
            }
         } finally
         {
            if (is != null)
            {
               try
               {
                  is.close();
               } catch (IOException e)
               {
                  // Ignore this error
               }
            }
         }
      }

      return serializationPolicy;
   }

   private void RemoteServiceServlet_writeResponse(HttpServletRequest request,
         HttpServletResponse response, String responsePayload)
         throws IOException
   {
      boolean gzipEncode = RPCServletUtils.acceptsGzipEncoding(request)
            && shouldCompressResponse(request, response, responsePayload);

      RPCServletUtils.writeResponse(getServletContext(), response,
            responsePayload, gzipEncode);
   }

   /**
    * Override this method to control what should happen when an exception
    * escapes the {@link #processCall(String)} method. The default
    * implementation will log the failure and send a generic failure response to
    * the client.
    * <p/>
    * 
    * An "expected failure" is an exception thrown by a service method that is
    * declared in the signature of the service method. These exceptions are
    * serialized back to the client, and are not passed to this method. This
    * method is called only for exceptions or errors that are not part of the
    * service method's signature, or that result from SecurityExceptions,
    * SerializationExceptions, or other failures within the RPC framework.
    * <p/>
    * 
    * Note that if the desired behavior is to both send the GENERIC_FAILURE_MSG
    * response AND to rethrow the exception, then this method should first send
    * the GENERIC_FAILURE_MSG response itself (using getThreadLocalResponse),
    * and then rethrow the exception. Rethrowing the exception will cause it to
    * escape into the servlet container.
    * 
    * @param e
    *           the exception which was thrown
    */
   protected void RemoteServiceServlet_doUnexpectedFailure(Throwable e)
   {
      ServletContext servletContext = getServletContext();
      RPCServletUtils.writeResponseForUnexpectedFailure(servletContext,
            getThreadLocalResponse(), e);
   }

   /**
    * Determines whether the response to a given servlet request should or
    * should not be GZIP compressed. This method is only called in cases where
    * the requester accepts GZIP encoding.
    * <p>
    * This implementation currently returns <code>true</code> if the response
    * string's estimated byte length is longer than 256 bytes. Subclasses can
    * override this logic.
    * </p>
    * 
    * @param request
    *           the request being served
    * @param response
    *           the response that will be written into
    * @param responsePayload
    *           the payload that is about to be sent to the client
    * @return <code>true</code> if responsePayload should be GZIP compressed,
    *         otherwise <code>false</code>.
    */
   protected boolean shouldCompressResponse(HttpServletRequest request,
         HttpServletResponse response, String responsePayload)
   {
      return RPCServletUtils
            .exceedsUncompressedContentLengthLimit(responsePayload);
   }

   private static String formatMethodNotFoundErrorMessage(Class<?> serviceIntf,
         String serviceMethodName, Class<?>[] parameterTypes)
   {
      StringBuffer sb = new StringBuffer();

      sb.append("Could not locate requested method '");
      sb.append(serviceMethodName);
      sb.append("(");
      for (int i = 0; i < parameterTypes.length; ++i)
      {
         if (i > 0)
         {
            sb.append(", ");
         }
         sb.append(printTypeName(parameterTypes[i]));
      }
      sb.append(")'");

      sb.append(" in interface '");
      sb.append(printTypeName(serviceIntf));
      sb.append("'");

      return sb.toString();
   }

   private static String formatIllegalAccessErrorMessage(Object target,
         Method serviceMethod)
   {
      StringBuffer sb = new StringBuffer();
      sb.append("Blocked attempt to access inaccessible method '");
      sb.append(getSourceRepresentation(serviceMethod));
      sb.append("'");

      if (target != null)
      {
         sb.append(" on target '");
         sb.append(printTypeName(target.getClass()));
         sb.append("'");
      }

      sb.append("; this is either misconfiguration or a hack attempt");

      return sb.toString();
   }

   private static String formatIllegalArgumentErrorMessage(Object target,
         Method serviceMethod, Object[] args)
   {
      StringBuffer sb = new StringBuffer();
      sb.append("Blocked attempt to invoke method '");
      sb.append(getSourceRepresentation(serviceMethod));
      sb.append("'");

      if (target != null)
      {
         sb.append(" on target '");
         sb.append(printTypeName(target.getClass()));
         sb.append("'");
      }

      sb.append(" with invalid arguments");

      if (args != null && args.length > 0)
      {
         sb.append(Arrays.asList(args));
      }

      return sb.toString();
   }

   /**
    * Returns the source representation for a method signature.
    * 
    * @param method
    *           method to get the source signature for
    * @return source representation for a method signature
    */
   private static String getSourceRepresentation(Method method)
   {
      return method.toString().replace('$', '.');
   }

   /**
    * Straight copy from
    * {@link com.google.gwt.dev.util.TypeInfo#getSourceRepresentation(Class)} to
    * avoid runtime dependency on gwt-dev.
    */
   private static String printTypeName(Class<?> type)
   {
      // Primitives
      //
      if (type.equals(Integer.TYPE))
      {
         return "int";
      } else if (type.equals(Long.TYPE))
      {
         return "long";
      } else if (type.equals(Short.TYPE))
      {
         return "short";
      } else if (type.equals(Byte.TYPE))
      {
         return "byte";
      } else if (type.equals(Character.TYPE))
      {
         return "char";
      } else if (type.equals(Boolean.TYPE))
      {
         return "boolean";
      } else if (type.equals(Float.TYPE))
      {
         return "float";
      } else if (type.equals(Double.TYPE))
      {
         return "double";
      }

      // Arrays
      //
      if (type.isArray())
      {
         Class<?> componentType = type.getComponentType();
         return printTypeName(componentType) + "[]";
      }

      // Everything else
      //
      return type.getName().replace('$', '.');
   }

}
