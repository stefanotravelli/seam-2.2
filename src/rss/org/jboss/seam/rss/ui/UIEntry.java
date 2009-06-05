package org.jboss.seam.rss.ui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.faces.context.FacesContext;

import org.jboss.seam.contexts.Contexts;

import yarfraw.core.datamodel.ChannelFeed;
import yarfraw.core.datamodel.ItemEntry;
import yarfraw.core.datamodel.Person;
import yarfraw.core.datamodel.Text;
import yarfraw.core.datamodel.Text.TextType;

/**
 *atomEntry = element atom:entry { atomCommonAttributes, (atomAuthor* &
 * atomCategory* & atomContent? & atomContributor* & atomId & atomLink* &
 * atomPublished? & atomRights? & atomSource? & atomSummary? & atomTitle &
 * atomUpdated & extensionElement*) }
 */

public class UIEntry extends SyndicationComponent
{
   private static final String COMPONENT_TYPE = "org.jboss.seam.rss.ui.UIEntry";

   private String uid;
   private String title;
   private String link;
   private String author;
   private String summary;
   private TextType textFormat = TextType.html;
   private Date published;
   private Date updated;

   @Override
   public String getFamily()
   {
      return COMPONENT_TYPE;
   }

   private Text makeText(String textString) {
      Text text = new Text(textFormat);
      text.setText(textString);
      return text;
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public void encodeBegin(FacesContext facesContext) throws IOException
   {
      ChannelFeed channelFeed = (ChannelFeed) Contexts.getEventContext().get(FEED_IMPL_KEY);

      ItemEntry itemEntry = new ItemEntry();
      itemEntry.setUid(getUid());
      itemEntry.setTitle(makeText(getTitle()));
      itemEntry.addLink(getLink());
      String author = getAuthor();
      if (author != null)
      {
         Person authorPerson = new Person();
         authorPerson.setName(author);
         itemEntry.addAuthorOrCreator(authorPerson);
      }
      itemEntry.setDescriptionOrSummary(makeText(getSummary()));
      if (getUpdated() != null) {
         itemEntry.setUpdatedDate(getUpdated(), new SimpleDateFormat(ATOM_DATE_FORMAT));
      }
      if (getPublished() != null) {
         itemEntry.setPubDate(getPublished(), new SimpleDateFormat(ATOM_DATE_FORMAT));
      }

      channelFeed.addItem(itemEntry);
   }

   public String getTitle()
   {
      return (String) valueOf("title", title);
   }

   public void setTitle(String title)
   {
      this.title = title;
   }

   public String getLink()
   {
      return (String) valueOf("link", link);
   }

   public void setLink(String link)
   {
      this.link = link;
   }

   public String getAuthor()
   {
      return (String) valueOf("author", author);
   }

   public void setAuthor(String author)
   {
      this.author = author;
   }

   public String getSummary()
   {
      return (String) valueOf("summary", summary);
   }

   public void setSummary(String summary)
   {
      this.summary = summary;
   }

   public Date getPublished()
   {
      return (Date) valueOf("published", published);
   }

   public void setPublished(Date published)
   {
      this.published = published;
   }

   public Date getUpdated()
   {
      return (Date) valueOf("updated", updated);
   }

   public void setUpdated(Date updated)
   {
      this.updated = updated;
   }

   public String getUid()
   {
      return (String) valueOf("uid", uid);
   }

   public void setUid(String uid)
   {
      this.uid = uid;
   }

   public TextType getTextFormat()
   {
      return textFormat;
   }

   public void setTextFormat(TextType textFormat)
   {
      this.textFormat = textFormat;
   }

}
