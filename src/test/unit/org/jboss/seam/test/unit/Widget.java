package org.jboss.seam.test.unit;

import java.util.Map;
import java.util.List;

/**
 * Used for remoting unit tests to test a variety of constraint combinations.
 *
 * @author Shane Bryzak
 */
public class Widget
{
  private String value;
  private String secret;
  private Widget child;
  private Map<String,Widget> widgetMap;
  private List<Widget> widgetList;

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getSecret()
  {
    return secret;
  }

  public void setSecret(String secret)
  {
    this.secret = secret;
  }

  public Widget getChild()
  {
    return child;
  }

  public void setChild(Widget child)
  {
    this.child = child;
  }

  public Map<String,Widget> getWidgetMap()
  {
    return widgetMap;
  }

  public void setWidgetMap(Map<String,Widget> widgetMap)
  {
    this.widgetMap = widgetMap;
  }

  public List<Widget> getWidgetList()
  {
    return widgetList;
  }

  public void setWidgetList(List<Widget> widgetList)
  {
    this.widgetList = widgetList;
  }
}
