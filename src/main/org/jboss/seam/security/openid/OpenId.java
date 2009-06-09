package org.jboss.seam.security.openid;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.log.*;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesManager;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.security.Identity;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.FetchRequest;

@Name("org.jboss.seam.security.openid.openid")
@Install(precedence=Install.BUILT_IN, classDependencies="org.openid4java.consumer.ConsumerManager")
@Scope(ScopeType.SESSION)
public class OpenId 
    implements Serializable
{
    private transient LogProvider log = Logging.getLogProvider(OpenId.class);

    String id;
    String validatedId;

    ConsumerManager manager;
    DiscoveryInformation discovered;

    @Create
    public void init()
        throws ConsumerException
    {
        manager = new ConsumerManager();
        discovered = null;
        id = null;
        validatedId = null;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    
    public String returnToUrl()  {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        try {   
            URL returnToUrl;            
            if (request.getServerPort()==80) {
               returnToUrl = new URL("http",   
                     request.getServerName(), 
                     context.getApplication().getViewHandler().getActionURL(context, "/openid.xhtml"));
            } else {
               returnToUrl = new URL("http",   
                     request.getServerName(), 
                     request.getServerPort(),
                     context.getApplication().getViewHandler().getActionURL(context, "/openid.xhtml"));
               
            }
            return returnToUrl.toExternalForm();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void login() throws IOException {
        validatedId = null;
        String returnToUrl = returnToUrl();

        String url = authRequest(id, returnToUrl);

        if (url != null) {
            Redirect redirect = Redirect.instance();
            redirect.captureCurrentView();

            FacesManager.instance().redirectToExternalURL(url);
        }
    }

    // --- placing the authentication request ---
    @SuppressWarnings("unchecked")
    protected String authRequest(String userSuppliedString, String returnToUrl)
        throws IOException
    {
        try {
            // perform discovery on the user-supplied identifier
            List discoveries = manager.discover(userSuppliedString);
            
            // attempt to associate with the OpenID provider
            // and retrieve one service endpoint for authentication
            discovered = manager.associate(discoveries);
            
            //// store the discovery information in the user's session
            // httpReq.getSession().setAttribute("openid-disc", discovered);

            // obtain a AuthRequest message to be sent to the OpenID provider
            AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

            // Attribute Exchange example: fetching the 'email' attribute
            FetchRequest fetch = FetchRequest.createFetchRequest();
            fetch.addAttribute("email",
                               "http://schema.openid.net/contact/email",   // type URI
                               true);                                      // required
            
            // attach the extension to the authentication request
            authReq.addExtension(fetch);

            return authReq.getDestinationUrl(true);
        } catch (OpenIDException e)  {
	    log.warn(e);
        }
        
        return null;
    }

    public void verify() 
    {       
        ExternalContext    context = javax.faces.context.FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) context.getRequest();
        
        validatedId = verifyResponse(request);
    }


    public boolean loginImmediately() {
        if (validatedId !=null) {
            Identity.instance().acceptExternallyAuthenticatedPrincipal((new OpenIdPrincipal(validatedId)));
            return true;
        } 

        return false;
    }

    public boolean isValid() {
        return validatedId != null;
    }

    public String getValidatedId() {
        return validatedId;
    }

    @SuppressWarnings("unchecked")
    public String verifyResponse(HttpServletRequest httpReq)
    {
        try {
            // extract the parameters from the authentication response
            // (which comes in as a HTTP request from the OpenID provider)
            ParameterList response =
                new ParameterList(httpReq.getParameterMap());
          
            // extract the receiving URL from the HTTP request
            StringBuffer receivingURL = httpReq.getRequestURL();
            String queryString = httpReq.getQueryString();
            if (queryString != null && queryString.length() > 0)
                receivingURL.append("?").append(httpReq.getQueryString());
            
            
            // verify the response; ConsumerManager needs to be the same
            // (static) instance used to place the authentication request
            VerificationResult verification = manager.verify(receivingURL.toString(),
                                                             response, discovered);
            
            // examine the verification result and extract the verified identifier
            Identifier verified = verification.getVerifiedId();
            if (verified != null) {
//                AuthSuccess authSuccess =
//                    (AuthSuccess) verification.getAuthResponse();
                
//                if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
//                    FetchResponse fetchResp = (FetchResponse) authSuccess
//                        .getExtension(AxMessage.OPENID_NS_AX);
//                    
//                    List emails = fetchResp.getAttributeValues("email");
//                    String email = (String) emails.get(0);
//                }
                
                return verified.getIdentifier();
            }
        } catch (OpenIDException e) {
            // present error to the user
        }
        
        return null;
    }

    public void logout() 
        throws ConsumerException
    {
        init();
    }
}
