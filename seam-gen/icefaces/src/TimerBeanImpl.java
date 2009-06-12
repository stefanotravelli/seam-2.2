/*
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * "The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations under
 * the License.
 *
 * The Original Code is ICEfaces 1.5 open source software code, released
 * November 5, 2006. The Initial Developer of the Original Code is ICEsoft
 * Technologies Canada, Corp. Portions created by ICEsoft are Copyright (C)
 * 2004-2006 ICEsoft Technologies Canada, Corp. All Rights Reserved.
 *
 * Contributor(s): _____________________.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"
 * License), in which case the provisions of the LGPL License are
 * applicable instead of those above. If you wish to allow use of your
 * version of this file only under the terms of the LGPL License and not to
 * allow others to use your version of this file under the MPL, indicate
 * your decision by deleting the provisions above and replace them with
 * the notice and other provisions required by the LGPL License. If you do
 * not delete the provisions above, a recipient may use your version of
 * this file under either the MPL or the LGPL License."
 *
 */
package @actionPackage@;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.ScopeType;
import org.jboss.seam.core.Manager;

import javax.faces.context.FacesContext;
import javax.ejb.Stateful;
import javax.ejb.Remove;
import java.util.Date;
import java.text.DateFormat;
import java.io.Serializable;
import javax.faces.event.ActionEvent;

import com.icesoft.faces.webapp.xmlhttp.PersistentFacesState;
import com.icesoft.faces.webapp.xmlhttp.RenderingException;
import com.icesoft.faces.async.render.RenderManager;
import com.icesoft.faces.async.render.IntervalRenderer;
import com.icesoft.faces.async.render.Renderable;

import com.icesoft.faces.context.effects.Effect;
import com.icesoft.faces.context.effects.BlindDown;
import com.icesoft.faces.context.effects.BlindUp;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;

/**
 * @author ICEsoft Technologies, Inc.
 * 
 */

@Name("timer")
@Scope(ScopeType.PAGE)
public class TimerBeanImpl implements Renderable, Serializable {

    @Logger private Log log;

    // expand/contract effects to take place of panelCollapsible
    private Effect currentEffect=new BlindUp();
    private boolean visibility=false;
    private boolean toggledEffect;
    private boolean expanded = false;

    private DateFormat dateFormatter;

    @In
    private RenderManager renderManager;
    private boolean doneSetup;
    private IntervalRenderer ir;
    private PersistentFacesState state = PersistentFacesState.getInstance();
    private String synchronous;

    private int myId; 

    private static int id;


    public PersistentFacesState getState() {
        return state;
    }

    public void renderingException( RenderingException re) {
        if(log.isTraceEnabled() ) { 
           log.trace("*** View obsoleted: " + myId );
        } 
        cleanup();
    }


    public TimerBeanImpl() {
        dateFormatter =  DateFormat.getDateTimeInstance();
        myId = ++id;      
    }


    /** to simulate panelCollapsible */   
    private void buildEffect() {
       if (expanded) {
            currentEffect = new BlindDown();
       } else {
           currentEffect = new BlindUp();
       }
           currentEffect.setSubmit(true);
           currentEffect.setTransitory(false);
           currentEffect.setDuration(.2f);
    }

    public boolean getExpanded() {
        return expanded;
    }
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        visibility = expanded;
        buildEffect();
        currentEffect.setFired(true);
    }
       public boolean isVisibility() {
        return visibility;
    }
    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }
 
    /**
     * Method used to toggle the expanded status of this item This would be
     * called from the front end pages
     *
     * @return "toggleExpanded" for use with faces-config navigation
     */
    public String toggleExpanded() {
        expanded = !expanded;
        buildEffect();
        toggledEffect = true;
        currentEffect.setFired(false);
        currentEffect.setSubmit(true);
        return null;
    }
    /**
     * Gets the effect used when a cell is expanded or contracted
     *
     * @return effect
     */
    public Effect getExpandEffect() {
        if (!toggledEffect) {
            currentEffect.setSubmit(false);
        }
        toggledEffect = false;
        return currentEffect;
    }
    
    public String getCurrentTime() {

        state = PersistentFacesState.getInstance();

        if (!doneSetup) {

            if(log.isTraceEnabled() ) { 
                log.trace("*** new TimerBean renderable... " + myId );
            } 

            FacesContext fc = FacesContext.getCurrentInstance();
            synchronous = (String) fc.getExternalContext().getInitParameterMap().
                    get( "com.icesoft.faces.synchronousUpdate" );
            boolean timed = Boolean.valueOf( (String) fc.getExternalContext().getInitParameterMap().
                    get("org.icesoft.examples.serverClock"));

            if (timed) {
                ir = renderManager.getIntervalRenderer("org.icesoft.clock.clockRenderer");
                ir.setInterval(5000);
                ir.add(this);
                ir.requestRender();
            }
        }
        
        doneSetup = true;
        return dateFormatter.format( new Date( System.currentTimeMillis() ) );
    }

    public String getRenderMode() {
        return  synchronous + " " + myId;        
    }

    // Don't make this begin a conversation as this class is 
    // intended to be used in a footer. 
    // If this method starts a conversation 
    // it can't be added to the foot of applications that don't 
    // expect conversations to already be in progress
    public String getCurrentConversation() {
        Manager m = Manager.instance();
        return m.getCurrentConversationId();
    }

    public String getLongRunning() {
        Manager m = Manager.instance();
        return Boolean.toString( m.isLongRunningConversation() );
    } 

    @Remove
    @Destroy
    public void remove() {
        if(log.isTraceEnabled() ) { 
           log.trace("*** View removed: " + myId );
        } 
        cleanup(); 
    }


    private void cleanup() {
        if (ir != null) {
            ir.remove(this);
            if (ir.isEmpty() ) {
                if(log.isTraceEnabled() ) { 
                   log.trace("*** IntervalRenderer Stopped " );
                } 
                ir.requestStop();
            }
        }
    } 
}
