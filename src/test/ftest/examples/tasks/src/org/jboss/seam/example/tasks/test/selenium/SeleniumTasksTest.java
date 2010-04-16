/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.seam.example.tasks.test.selenium;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.jboss.seam.example.common.test.selenium.SeamSeleniumTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This is the base class for Tasks functional tests. Uses jQuery library and
 * Selenium to match AJAX updates.
 * 
 * @author kpiwko
 * 
 */
public class SeleniumTasksTest extends SeamSeleniumTest
{

   public static final String LOGIN_URL = "/login.seam";
   public static final String TASKS_URL = "/tasks.seam";
   public static final String LOGIN_USERNAME = "id=login:username";
   public static final String LOGIN_PASSWORD = "id=login:password";
   public static final String LOGIN_SUBMIT = "xpath=//input[@value='Login']";

   public static final String ACTION_BUTTON_FORMATTER = "xpath=//td[contains(., '%s')]/ancestor::tr/descendant::img[@title='%s']";

   public static final String RESOLVE_BTN_TITLE = "Resolve this task";
   public static final String EDIT_BTN_TITLE = "Edit this task";
   public static final String DELETE_BTN_TITLE = "Delete this task";
   public static final String UNDO_BTN_TITLE = "Undo this task";
   public static final String DELETE_BTN_CAT_TITLE = "Delete this category";

   public static final String DEFAULT_USERNAME = "demo";
   public static final String DEFAULT_PASSWORD = "demo";

   public static final String TASKS_LINK = "xpath=//a[.='Tasks']";
   public static final String RESOLVED_LINK = "xpath=//a[.='Resolved tasks']";
   public static final String CATEGORIES_LINK = "xpath=//a[.='Categories']";
   public static final String LOGOUT_LINK = "id=menuLogoutId";

   public static final String EDIT_TASK_DESCRIPTION = "xpath=//form[@id='updateTask']/input[@class='nameField']";
   public static final String EDIT_TASK_CATEGORY = "xpath=//form[@id='updateTask']/select[@id='editTaskCategory']";
   public static final String EDIT_TASK_SUBMIT = "xpath=//form[@id='updateTask']/input[@id='update']";

   public static final String NEW_TASK_DESCRIPTION = "xpath=//form[@id='newTask']/input[@id='editTaskName']";
   public static final String NEW_TASK_CATEGORY = "xpath=//form[@id='newTask']/select[@id='editTaskCategory']";
   public static final String NEW_TASK_SUBMIT = "xpath=//form[@id='newTask']/input[@id='editTaskSubmit']";

   public static final String NEW_CATEGORY_DESCRIPTION = "xpath=//form[@id='newCategoryForm']/input[@id='editCategoryName']";
   public static final String NEW_CATEGORY_SUBMIT = "xpath=//form[@id='newCategoryForm']/input[@id='editCategorySubmit']";

   @BeforeMethod
   @Override
   public void setUp()
   {
      super.setUp();
      browser.open(CONTEXT_PATH + LOGIN_URL);
      browser.type(LOGIN_USERNAME, DEFAULT_USERNAME);
      browser.type(LOGIN_PASSWORD, DEFAULT_PASSWORD);
      navigate(LOGIN_SUBMIT);
      assertTrue(browser.getLocation().contains(TASKS_URL), "Navigation failure. Tasks page expected.");
   }

   @Test(groups = { "school" })
   public void resolveTuringTask()
   {
      String turing = "Build the Turing machine";
      resolveTask(turing);
      buttonMissing(turing, RESOLVE_BTN_TITLE);

      navigate(RESOLVED_LINK);
      buttonPresent(turing, UNDO_BTN_TITLE);
      navigate(LOGOUT_LINK);
   }

   @Test
   public void deleteMilkTask()
   {
      String milk = "Buy milk";
      deleteTask(milk);
      buttonMissing(milk, RESOLVE_BTN_TITLE);

      navigate(RESOLVED_LINK);
      buttonMissing(milk, UNDO_BTN_TITLE);
      navigate(LOGOUT_LINK);
   }

   @Test(groups = { "turtle", "school" })
   public void undoTurtleTask()
   {
      String turtle = "Buy a turtle";
      navigate(RESOLVED_LINK);
      undoTask(turtle);
      buttonMissing(turtle, UNDO_BTN_TITLE);

      navigate(TASKS_LINK);
      buttonPresent(turtle, RESOLVE_BTN_TITLE);
      navigate(LOGOUT_LINK);
   }

   @Test(dependsOnGroups = { "turtle" })
   public void editTurtleTask()
   {
      String turtle = "Buy a turtle";
      String newCategory = "Work";
      String newDescription = "Buy a turtle and take it to work";
      editTask(turtle, newCategory, newDescription);
      buttonPresent(newDescription, RESOLVE_BTN_TITLE);
      navigate(LOGOUT_LINK);
   }

   @Test(groups = { "qa" })
   public void createQACategory()
   {
      String category = "JBoss QA";
      navigate(CATEGORIES_LINK);
      newCategory(category);
      buttonPresent(category, DELETE_BTN_CAT_TITLE);
      navigate(LOGOUT_LINK);
   }

   @Test(dependsOnGroups = { "qa" })
   public void createSeleniumTask()
   {
      String description = "Create selenium ftests for all available examples";
      newTask("Work", description);
      buttonPresent(description, RESOLVE_BTN_TITLE);
      navigate(LOGOUT_LINK);
   }

   @Test(dependsOnGroups = { "school" })
   public void deleteSchoolCategory()
   {
      String category = "School";
      navigate(CATEGORIES_LINK);
      buttonPress(category, DELETE_BTN_CAT_TITLE);

      buttonMissing(category, DELETE_BTN_CAT_TITLE);

      // all tasks from category are deleted as well
      navigate(TASKS_LINK);
      buttonMissing("Finish the RESTEasy-Seam integration example", RESOLVE_BTN_TITLE);
      navigate(LOGOUT_LINK);
   }

   /**
    * Presses undo button for given task
    * 
    * @param task The task name
    */
   protected void undoTask(String task)
   {
      buttonPress(task, UNDO_BTN_TITLE);
   }

   /**
    * Presses resolve button for given task
    * 
    * @param task The task name
    */
   protected void resolveTask(String task)
   {
      buttonPress(task, RESOLVE_BTN_TITLE);
   }

   /**
    * Presses delete button for given task
    * 
    * @param task The task name
    */
   protected void deleteTask(String task)
   {
      buttonPress(task, DELETE_BTN_TITLE);
   }

   /**
    * Goes to another page and waits for dynamic reload
    * 
    * @param anchor Locator of the anchor
    */
   protected void navigate(String anchor)
   {
      browser.clickAndWait(anchor);
      browser.waitForAJAXUpdate();
   }

   /**
    * Executes arbitrary button task, e.g. delete, undo, resolve
    * 
    * @param description Name of task or description on which an action is
    *           triggered
    * @param button Type of action to be triggered - title of the button
    */
   protected void buttonPress(String description, String button)
   {
      String btn = buttonPresent(description, button);
      browser.click(btn);
      browser.waitForAJAXUpdate();
   }

   /**
    * Checks whether button is present on page
    * 
    * @param description The task/category associated with button
    * @param button Button type label
    * @return Button locator
    */
   protected String buttonPresent(String description, String button)
   {
      String btn = String.format(ACTION_BUTTON_FORMATTER, description, button);
      assertTrue(browser.isElementPresent(btn), "There should be a '" + button + "' button for: " + description + ".");
      return btn;
   }

   /**
    * Checks whether button is not present on page
    * 
    * @param description The task/category associated with button
    * @param button Button type label
    * @return Button locator
    */
   protected String buttonMissing(String task, String button)
   {
      String btn = String.format(ACTION_BUTTON_FORMATTER, task, button);
      assertFalse(browser.isElementPresent(btn), "There should NOT be a '" + button + "' button for: " + task + ".");
      return btn;
   }

   /**
    * Creates new task
    * 
    * @param category Category of the task
    * @param description Description of the task
    */
   protected void newTask(String category, String description)
   {
      browser.select(NEW_TASK_CATEGORY, String.format("value=%s", category));
      browser.type(NEW_TASK_DESCRIPTION, description);
      browser.click(NEW_TASK_SUBMIT);
      browser.waitForAJAXUpdate();
   }

   /**
    * Edits task
    * 
    * @param task Old description of the task
    * @param newCategory New category
    * @param newDescription Old description of the task
    */
   protected void editTask(String task, String newCategory, String newDescription)
   {
      String btn = buttonPresent(task, EDIT_BTN_TITLE);
      browser.click(btn);
      browser.waitForAJAXUpdate();
      browser.select(EDIT_TASK_CATEGORY, String.format("value=%s", newCategory));
      browser.type(EDIT_TASK_DESCRIPTION, newDescription);
      browser.click(EDIT_TASK_SUBMIT);
      browser.waitForAJAXUpdate();
   }

   /**
    * Creates new category
    * 
    * @param category Category description
    */
   protected void newCategory(String category)
   {
      browser.type(NEW_CATEGORY_DESCRIPTION, category);
      browser.click(NEW_CATEGORY_SUBMIT);
      browser.waitForAJAXUpdate();
   }

}
