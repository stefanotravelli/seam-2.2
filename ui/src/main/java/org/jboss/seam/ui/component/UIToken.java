package org.jboss.seam.ui.component;

import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIOutput;

import org.jboss.seam.Component;
import org.jboss.seam.ui.ClientUidSelector;
import org.jboss.seam.ui.UnauthorizedCommandException;

/**
 * <p>
 * <strong>UIToken</strong> is a UIComponent that produces a random token that
 * is inserted into a hidden form field to help to secure JSF form posts against
 * cross-site request forgery (XSRF) attacks. This is an adaptation of the
 * recommendation called Keyed‐Hashing for Message Authentication that is
 * referenced in the Cross Site Reference Forgery by Jesse Burns
 * (http://www.isecpartners.com/files/XSRF_Paper_0.pdf)
 * </p>
 * 
 * <p>
 * When placed inside a form, this component will first assign a unique
 * identifier to the browser using a cookie that lives until the end of the
 * browser session. This is roughly the browser's private key. Then a unique
 * token is generated using various pieces of information that comprise the
 * form's signature. The token may or may not be bound to the session id, as
 * indicated by the value of the requireSession attribute. The token value is
 * stored in the hidden form field named javax.faces.FormSignature.
 * </p>
 * 
 * <p>
 * There is an assumption when using this component that the browser supports
 * cookies. Cookies are the only universally available persistent mechanism that
 * can give the browser an identifiable signature. It's important to know that
 * the browser submitting the form is the same browser that is requesting the
 * form.
 * </p>
 * 
 * <p>
 * During the decode process, the token is generated using the same algorithm
 * that was used during rendering and compared with the value of the request
 * parameter javax.faces.FormSignature. If the same token value can be produced,
 * then the form submission is permitted. Otherwise, an
 * {@link UnauthorizedCommandException} is thrown indicating the reason for the
 * failure.
 * </p>
 * 
 * <p>
 * The UIToken can be combined with client-side state saving or the
 * "build before restore" strategy to unbind a POST from the session that
 * created the view without sacrificing security. However, it's still the most
 * secure to require the view state to be present in the session (JSF 1.2
 * server-side state saving).
 * </p>
 * 
 * <p>
 * Please note that this solution isn't a complete panacea. If your site is
 * vulnerable to XSS or the connection to wire-tapping, then the unique browser
 * identifier can be revealed and a request forged.
 * </p>
 * 
 * @author Dan Allen
 */
public abstract class UIToken extends UIOutput
{
   @SuppressWarnings("unused")
   private static final String COMPONENT_TYPE = "org.jboss.seam.ui.Token";

   @SuppressWarnings("unused")
   private static final String COMPONENT_FAMILY = "org.jboss.seam.ui.Token";
   
   /**
    * Indicates whether the session id should be included in the form signature,
    * hence binding the token to the session. This value can be set to false
    * if the "build before restore" mode of Facelets is activated (the
    * default in JSF 2.0). The default value is false.
    */
   public abstract boolean isRequireSession();
   
   public abstract void setRequireSession(boolean required);
   
   /**
    * Indicates whether a JavaScript check should be inserted into the page to
    * verify that cookies are enabled in the browser. If cookies are not
    * enabled, present a notice to the user that form posts will not work.
    * The default value is false.
    */
   public abstract boolean isEnableCookieNotice();
   
   public abstract void setEnableCookieNotice(boolean state);

   /**
    * Indicates whether to allow the same form to be submitted multiple times
    * with the same signature (as long as the view does not change). This is a
    * common need if the form is perform Ajax calls but not rerendering itself
    * or, at the very least, the UIToken component. The preferred approach is to
    * have the UIToken component rerendered on any Ajax call where the UIToken
    * component would be processed. The default value is false.
    */
   public abstract boolean isAllowMultiplePosts();
   
   public abstract void setAllowMultiplePosts(boolean allow);
   
   /**
    * Return the selector that controls the unique browser identifier cookie.
    */
   public ClientUidSelector getClientUidSelector() {
      return (ClientUidSelector) Component.getInstance(ClientUidSelector.class);
   }
   
   public String getClientUid() {
      return getClientUidSelector().getClientUid();
   }
   
   public UIForm getParentForm() {
      UIComponent parent = getParent();
      while (parent != null) {
         if (parent instanceof UIForm) {
            return (UIForm) parent;
         }
         parent = parent.getParent();
      }
      
      return null;
   }
}
