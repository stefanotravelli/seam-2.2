package org.jboss.seam.ui.renderkit;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.http.HttpSession;

import org.jboss.seam.ui.RenderStampStore;
import org.jboss.seam.ui.UnauthorizedCommandException;
import org.jboss.seam.ui.component.UIToken;
import org.jboss.seam.ui.util.HTML;
import org.jboss.seam.ui.util.cdk.RendererBase;
import org.jboss.seam.util.Base64;
import org.jboss.seam.util.RandomStringUtils;

/**
 * <p>
 * The <strong>TokenRendererBase</strong> renders the form's signature as a
 * hidden form field for the UIToken component. If the renderStampStore
 * component is enabled, the actually signature will be stored in the session
 * and the key to this token store in the hidden form field, providing the same
 * guarantee for client-side state saving as with server-side state saving.
 * </p>
 * 
 * <p>
 * The form signature is calculated as follows:
 * </p>
 * 
 * <pre>
 * sha1(signature = contextPath + viewId + &quot;,&quot; + formClientId + random alphanum, salt = clientUid)
 * </pre>
 * 
 * <p>
 * The developer can also choose to incorporate the session id into this hash to
 * generate a more secure token (at the cost of binding it to the session) by
 * setting the requireSession attribute to true. Then the calculation becomes:
 * </p>
 * 
 * <pre>
 * sha1(signature = contextPath + viewId + &quot;,&quot; + formClientId + &quot;,&quot; + random alphanum + sessionId, salt = clientUid)
 * </pre>
 * 
 * <p>
 * The decode method performs the following steps:
 * </p>
 * <ol>
 * <li>Check if this is a postback, otherwise skip the check</li>
 * <li>Check that this form was the one that was submitted, otherwise skip the
 * check</li>
 * <li>Get the unique client identifier (from cookie), otherwise throw an
 * exception that the browser must have unique identifier</li>
 * <li>Get the javax.faces.FormSignature request parameter, otherwise throw an
 * exception that the form signature is missing</li>
 * <li>If the renderStampStore component is enabled, retrieve the render stamp
 * from the store using the key stored in the render stamp attribute of the form.</li>
 * <li>Generate the hash as before and verify that it equals the value of the
 * javax.faces.FormSignature request parameter, otherwise throw an exception</li>
 * </ol>
 * 
 * <p>
 * If all of that passes, we are okay to process the form (advance to validate
 * phase as decode() is called in apply request values).
 * </p>
 * 
 * @author Dan Allen
 * @author Stuart Douglas
 * @see UnauthorizedCommandException
 */
public class TokenRendererBase extends RendererBase
{
   public static final String FORM_SIGNATURE_PARAM = "javax.faces.FormSignature";

   public static final String RENDER_STAMP_ATTR = "javax.faces.RenderStamp";

   private static final String COOKIE_CHECK_SCRIPT_KEY = "org.jboss.seam.ui.COOKIE_CHECK_SCRIPT";

   @Override
   protected Class getComponentClass()
   {
      return UIToken.class;
   }

   @Override
   protected void doDecode(FacesContext context, UIComponent component)
   {
      UIToken token = (UIToken) component;
      UIForm form = token.getParentForm();
      if (context.getRenderKit().getResponseStateManager().isPostback(context) && form.isSubmitted())
      {
         String clientToken = token.getClientUid();
         String viewId = context.getViewRoot().getViewId();
         if (clientToken == null)
         {
            throw new UnauthorizedCommandException(viewId, "No client identifier provided");
         }

         String requestedViewSig = context.getExternalContext().getRequestParameterMap().get(FORM_SIGNATURE_PARAM);
         if (requestedViewSig == null)
         {
            throw new UnauthorizedCommandException(viewId, "No form signature provided");
         }

         if (!requestedViewSig.equals(generateViewSignature(context, form, !token.isAllowMultiplePosts(), token.isRequireSession(), clientToken)))
         {
            throw new UnauthorizedCommandException(viewId, "Form signature invalid");
         }
         RenderStampStore store = RenderStampStore.instance();
         if (store != null)
         {
            // remove the key from the store if we are using it
            store.removeStamp(String.valueOf(form.getAttributes().get(RENDER_STAMP_ATTR)));
         }
         form.getAttributes().remove(RENDER_STAMP_ATTR);
      }
   }

   @Override
   protected void doEncodeBegin(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException
   {
      UIToken token = (UIToken) component;
      UIForm form = token.getParentForm();
      if (form == null)
      {
         throw new IllegalStateException("UIToken must be inside a UIForm.");
      }

      String renderStamp = RandomStringUtils.randomAlphanumeric(50);
      RenderStampStore store = RenderStampStore.instance();
      if (store != null)
      {
         // if the store is not null we store the key
         // instead of the actual stamp; this puts the
         // server in control of this value rather than
         // the component tree, which is owned by the client
         // when using client-side state saving
         renderStamp = store.storeStamp(renderStamp);
      }

      writeCookieCheckScript(context, writer, token);

      token.getClientUidSelector().seed();
      form.getAttributes().put(RENDER_STAMP_ATTR, renderStamp);
      writer.startElement(HTML.INPUT_ELEM, component);
      writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_HIDDEN, HTML.TYPE_ATTR);
      writer.writeAttribute(HTML.NAME_ATTR, FORM_SIGNATURE_PARAM, HTML.NAME_ATTR);
      writer.writeAttribute(HTML.VALUE_ATTR, generateViewSignature(context, form, !token.isAllowMultiplePosts(), token.isRequireSession(), token.getClientUidSelector().getClientUid()), HTML.VALUE_ATTR);
      writer.endElement(HTML.INPUT_ELEM);
   }

   /**
    * If the client has not already delivered us a cookie and the cookie notice is enabled, write out JavaScript that will show the user
    * an alert if cookies are not enabled.
    */
   private void writeCookieCheckScript(FacesContext context, ResponseWriter writer, UIToken token) throws IOException
   {
      if (!token.getClientUidSelector().isSet() && token.isEnableCookieNotice() && !context.getExternalContext().getRequestMap().containsKey(COOKIE_CHECK_SCRIPT_KEY)) {
         writer.startElement(HTML.SCRIPT_ELEM, token);
         writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", HTML.TYPE_ATTR);
         writer.write("if (!document.cookie) {" +
            " alert('This website uses a security measure that requires cookies to be enabled in your browser. Since you have cookies disabled, you will not be permitted to submit a form.');" +
            " }");
         writer.endElement(HTML.SCRIPT_ELEM);
         context.getExternalContext().getRequestMap().put(COOKIE_CHECK_SCRIPT_KEY, true);
      }
   }

   private String generateViewSignature(FacesContext context, UIForm form, boolean useRenderStamp, boolean useSessionId, String saltPhrase)
   {
      String rawViewSignature = context.getExternalContext().getRequestContextPath() + "," + context.getViewRoot().getViewId() + "," + form.getClientId(context);
      if (useRenderStamp)
      {
         String renderStamp = form.getAttributes().get(RENDER_STAMP_ATTR).toString();
         RenderStampStore store = RenderStampStore.instance();
         if (store != null)
         {
            // if we are using the RenderStampStore the key to access the render
            // stamp
            // is stored in the view root instead of the actual render stamp
            renderStamp = store.getStamp(renderStamp);
         }
         rawViewSignature += "," + renderStamp;
      }
      if (useSessionId)
      {
         rawViewSignature += "," + ((HttpSession) context.getExternalContext().getSession(true)).getId();
      }
      try
      {
         MessageDigest digest = MessageDigest.getInstance("SHA-1");
         digest.update(saltPhrase.getBytes());
         byte[] salt = digest.digest();
         digest.reset();
         digest.update(rawViewSignature.getBytes());
         digest.update(salt);
         byte[] raw = digest.digest();
         return Base64.encodeBytes(raw);
      }
      catch (NoSuchAlgorithmException ex)
      {
         ex.printStackTrace();
         return null;
      }
   }

}
