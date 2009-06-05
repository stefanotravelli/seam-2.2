package org.jboss.seam.test.unit;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesManager;
import org.jboss.seam.mock.MockHttpServletRequest;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.test.unit.component.TestActions;
import org.testng.annotations.Test;

import javax.faces.context.FacesContext;
import javax.faces.render.ResponseStateManager;
import java.util.List;
import java.util.Map;

/**
 * The purpose of this test is to verify the way that page actions are handled. Once
 * a page action triggers a navigation event, subsequent page actions in the chain
 * should be short circuited.
 */
public class PageActionsTest extends AbstractPageTest
{
   @Override
   protected void installComponents(Context appContext)
   {
      super.installComponents(appContext);
      installComponent(appContext, NoRedirectFacesManager.class);
      installComponent(appContext, TestActions.class);
   }

   /**
    * This test verifies that a non-null outcome will short-circuit the page
    * actions. It tests two difference variations. The first variation includes
    * both actions as nested elements of the page node. The second variation has
    * the first action in the action attribute of the page node and the second
    * action as a nested element. Aside from the placement of the actions, the
    * two parts of the test are equivalent.
    */
   @Test(enabled = true)
   public void testShortCircuitOnNonNullOutcome()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      TestActions testActions = TestActions.instance();

      facesContext.getViewRoot().setViewId("/action-test01a.xhtml");
      Pages.instance().preRender(facesContext);
      assertViewId(facesContext, "/pageA.xhtml");
      assertActionCalls(testActions, new String[] { "nonNullActionA" });

      testActions = TestActions.instance();

      facesContext.getViewRoot().setViewId("/action-test01b.xhtml");
      Pages.instance().preRender(facesContext);
      assertViewId(facesContext, "/pageA.xhtml");
      assertActionCalls(testActions, new String[] { "nonNullActionA" });
   }

   /**
    * This test verifies that because the first action does not result in a
    * navigation, the second action is executed. However, the third action is
    * not called because of the navigation on the second action.
    */
   @Test(enabled = true)
   public void testShortCircuitInMiddle()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      TestActions testActions = TestActions.instance();

      facesContext.getViewRoot().setViewId("/action-test02.xhtml");
      Pages.instance().preRender(facesContext);
      assertViewId(facesContext, "/pageB.xhtml");
      assertActionCalls(testActions, new String[] { "nonNullActionA", "nonNullActionB" });
   }

   /**
    * This test verifies that an action method with a null return can still match
    * a navigation rule and short-circuit the remaining actions. The key is that
    * a navigation rule is matched, not what the return value is.
    */
   @Test(enabled = true)
   public void testShortCircuitOnNullOutcome()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      TestActions testActions = TestActions.instance();

      facesContext.getViewRoot().setViewId("/action-test03.xhtml");
      Pages.instance().preRender(facesContext);
      assertViewId(facesContext, "/pageA.xhtml");
      assertActionCalls(testActions, new String[] { "nullActionA" });
   }

   /**
    * Verify that the first non-null outcome, even if it is to the same view id,
    * will short circuit the action calls.
    */
   @Test(enabled = true)
   public void testShortCircuitOnNonNullOutcomeToSamePage()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      TestActions testActions = TestActions.instance();

      facesContext.getViewRoot().setViewId("/action-test04.xhtml");
      Pages.instance().preRender(facesContext);
      assertViewId(facesContext, "/action-test04.xhtml");
      assertActionCalls(testActions, new String[] { "nullActionA", "nonNullActionB" });
   }

   /**
    * Same as testShortCircuitOnNonNullOutcome except that the navigation rules
    * are redirects rather than renders.
    */
   @Test(enabled = true)
   public void testShortCircuitOnNonNullOutcomeWithRedirect()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      TestActions testActions = TestActions.instance();

      facesContext.getViewRoot().setViewId("/action-test05.xhtml");
      Pages.instance().preRender(facesContext);
      assertViewId(facesContext, "/action-test05.xhtml");
      assertActionCalls(testActions, new String[] { "nonNullActionA" });
      assert Contexts.getEventContext().get("lastRedirectViewId").equals("/pageA.xhtml") : 
         "Expecting a redirect to /pageA.xhtml but redirected to " + Contexts.getEventContext().get("lastRedirectViewId");
      assert facesContext.getResponseComplete() == true : "The response should have been marked as complete";
   }

   /**
    * Verify that only those actions without on-postback="false" are executed when the
    * magic postback parameter (javax.faces.ViewState) is present in the request map.
    */
   @Test(enabled = true)
   public void testPostbackConditionOnPageAction()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      simulatePostback(facesContext);
      TestActions testActions = TestActions.instance();

      facesContext.getViewRoot().setViewId("/action-test06.xhtml");
      Pages.instance().preRender(facesContext);
      assertViewId(facesContext, "/action-test06.xhtml");
      assertActionCalls(testActions, new String[] { "nonNullActionA" });
   }
   
   /**
    * This test is here (and disabled) to demonstrate the old behavior. All page
    * actions would be executed regardless and navigations could cross page
    * declaration boundaries since the view id is changing mid-run (hence
    * resulting in different navigation rule matches)
    */
   @Test(enabled = false)
   public void oldBehaviorTest()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      TestActions testActions = TestActions.instance();

      facesContext.getViewRoot().setViewId("/action-test99a.xhtml");
      Pages.instance().preRender(facesContext);
      assertViewId(facesContext, "/pageB.xhtml");
      assertActionCalls(testActions, new String[] { "nonNullActionA", "nonNullActionB" });
   }

   private void assertViewId(FacesContext facesContext, String expectedViewId)
   {
      String actualViewId = facesContext.getViewRoot().getViewId();
      assert expectedViewId.equals(actualViewId) :
         "Expected viewId to be " + expectedViewId + ", but got " + actualViewId;
   }

   private void assertActionCalls(TestActions testActions, String[] methodNames)
   {
      List<String> actionsCalled = testActions.getActionsCalled();
      assert actionsCalled.size() == methodNames.length :
         "Expected " + methodNames.length + " action(s) to be called, but executed " + actionsCalled.size() + " action(s) instead";
      String expectedMethodCalls = "";
      for (int i = 0, len = methodNames.length; i < len; i++)
      {
         if (i > 0)
         {
            expectedMethodCalls += ", ";
         }
         expectedMethodCalls += methodNames[i];
      }
      String actualMethodCalls = "";
      for (int i = 0, len = actionsCalled.size(); i < len; i++)
      {
         if (i > 0)
         {
            actualMethodCalls += ", ";
         }
         actualMethodCalls += actionsCalled.get(i);
      }

      assert expectedMethodCalls.equals(actualMethodCalls) :
         "Expected actions to be called: " + expectedMethodCalls + "; actions actually called: " + actualMethodCalls;

      Contexts.getEventContext().remove(Component.getComponentName(TestActions.class));
   }

   private void simulatePostback(FacesContext facesContext)
   {
      MockHttpServletRequest request = (MockHttpServletRequest) facesContext.getExternalContext().getRequest();
      request.getParameters().put(ResponseStateManager.VIEW_STATE_PARAM, new String[] { "true" });
      assert facesContext.getRenderKit().getResponseStateManager().isPostback(facesContext) == true;
   }
   
   @Scope(ScopeType.EVENT)
   @Name("org.jboss.seam.core.manager")
   @BypassInterceptors
   public static class NoRedirectFacesManager extends FacesManager {

      @Override
      public void redirect(String viewId, Map<String, Object> parameters, boolean includeConversationId, boolean includePageParams)
      {
         Contexts.getEventContext().set("lastRedirectViewId", viewId);
         // a lot of shit happens we don't need; the important part is that the
         // viewId is not changed on FacesContext, but the response is marked complete
         FacesContext.getCurrentInstance().responseComplete();
      }
      
   }

}
