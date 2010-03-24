package org.jboss.seam.example.common.test.webdriver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Adds AjaxWebElement functionality to an ordinary WebElement
 * 
 * @author kpiwko
 * 
 */
public class DelegatedWebElement implements AjaxWebElement {

	private int waitTime;
	private WebElement element;

	public DelegatedWebElement(WebElement element) {
		this(element, DEFAULT_WAIT_TIME);
	}

	public DelegatedWebElement(WebElement element, int waitTime) {
		this.element = element;
		this.waitTime = waitTime;
	}

	//@Override
	public void clear() {
		element.clear();
	}
	
	//@Override
	public void clearAndSendKeys(CharSequence...keysToSend) {
		element.clear();
		element.sendKeys(keysToSend);
	}

	//@Override
	public void click() {
		element.click();
	}

	//@Override
	public void clickAndWait() {
		element.click();
		try {
			Thread.sleep(waitTime);
		} catch (InterruptedException e) {
		}
	}

	//@Override
	public AjaxWebElement findElement(By by) {
		return new DelegatedWebElement(element.findElement(by));
	}

	//@Override
	public List<WebElement> findElements(By by) {
		List<WebElement> elements = new ArrayList<WebElement>();
		List<WebElement> original = element.findElements(by);
		if (original == null || original.size() == 0)
			return Collections.emptyList();

		for (WebElement e : original)
			elements.add(new DelegatedWebElement(e));

		return elements;
	}

	//@Override
	public String getAttribute(String name) {
		return element.getAttribute(name);
	}

	@Deprecated
	//@Override
	public String getElementName() {
		return element.getElementName();
	}

	//@Override
	public String getTagName() {
		return element.getTagName();
	}

	//@Override
	public String getText() {
		return element.getText();
	}

	//@Override
	public String getValue() {
		return element.getValue();
	}

	//@Override
	public boolean isEnabled() {
		return element.isEnabled();
	}

	//@Override
	public boolean isSelected() {
		return element.isSelected();
	}

	//@Override
	public void sendKeys(CharSequence... keysToSend) {
		element.sendKeys(keysToSend);
	}

	//@Override
	public void setSelected() {
		element.setSelected();
	}

	//@Override
	public void setWaitTime(int millis) {
		this.waitTime = millis;
	}

	//@Override
	public void submit() {
		element.submit();
	}

	//@Override
	public boolean toggle() {
		return element.toggle();
	}

}
