package org.jboss.seam.rss.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.contexts.Contexts;

import yarfraw.core.datamodel.ChannelFeed;
import yarfraw.core.datamodel.FeedFormat;
import yarfraw.core.datamodel.YarfrawException;
import yarfraw.io.FeedWriter;

/*
 * atomFeed =
 element atom:feed {
 atomCommonAttributes,
 (atomAuthor*
 & atomCategory*
 & atomContributor*
 & atomGenerator?
 & atomIcon?
 & atomId
 & atomLink*
 & atomLogo?
 & atomRights?
 & atomSubtitle?
 & atomTitle
 & atomUpdated
 & extensionElement*),
 atomEntry*
 }
 */

public class UIFeed extends SyndicationComponent
{
   private static final String COMPONENT_TYPE = "org.jboss.seam.rss.ui.UIFeed";
   private static final String EXTENSION = "xml";
   private static final String MIMETYPE = "text/xml";

   private boolean sendRedirect = true;

   private String uid;
   private String title;
   private String subtitle;
   private Date updated;
   private String link;
   private FeedFormat feedFormat = FeedFormat.ATOM10;

   @SuppressWarnings("unchecked")
   @Override
   public void encodeBegin(FacesContext facesContext) throws IOException
   {
      ChannelFeed channelFeed = new ChannelFeed();
      channelFeed.setUid(getUid());
      channelFeed.setTitle(getTitle());
      channelFeed.setDescriptionOrSubtitle(getSubtitle());
      if (getUpdated() != null)
      {
         channelFeed.setPubDate(getUpdated(), new SimpleDateFormat(ATOM_DATE_FORMAT));
      }
      channelFeed.addLink(getLink());
      Contexts.getEventContext().set(FEED_IMPL_KEY, channelFeed);
   }

   @Override
   public void encodeEnd(FacesContext facesContext) throws IOException
   {
      ChannelFeed channelFeed = (ChannelFeed) Contexts.getEventContext().get(FEED_IMPL_KEY);
      ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
      try
      {
         FeedWriter.writeChannel(feedFormat, channelFeed, byteStream);
      }
      catch (YarfrawException e)
      {
         /**
          * Was IOException, but 1.5 does not have this constructor
          * http://java.sun.com/j2se/1.5.0/docs/api/java/io/IOException.html
          */
         throw new RuntimeException("Could not create feed", e);
      }
      Writer responseWriter = ((HttpServletResponse) facesContext.getExternalContext().getResponse()).getWriter();
      HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
      response.setContentType(MIMETYPE);
      response.setContentLength(byteStream.size());
      responseWriter.write(byteStream.toString());
      response.flushBuffer();
      facesContext.responseComplete();
   }

   public boolean isSendRedirect()
   {
      return sendRedirect;
   }

   public void setSendRedirect(boolean sendRedirect)
   {
      this.sendRedirect = sendRedirect;
   }

   @Override
   public String getFamily()
   {
      return COMPONENT_TYPE;
   }

   public String getTitle()
   {
      return (String) valueOf("title", title);
   }

   public void setTitle(String title)
   {
      this.title = title;
   }

   public String getSubtitle()
   {
      return (String) valueOf("subtitle", subtitle);
   }

   public void setSubtitle(String subtitle)
   {
      this.subtitle = subtitle;
   }

   public Date getUpdated()
   {
      return (Date) valueOf("updated", updated);
   }

   public void setUpdated(Date updated)
   {
      this.updated = updated;
   }

   public String getLink()
   {
      return (String) valueOf("link", link);
   }

   public void setLink(String link)
   {
      this.link = link;
   }

   public FeedFormat getFeedFormat()
   {
      return (FeedFormat) valueOf("feedFormat", feedFormat);
   }

   public void setFeedFormat(FeedFormat feedFormat)
   {
      this.feedFormat = feedFormat;
   }

   public String getUid()
   {
      return (String) valueOf("uid", uid);
   }

   public void setUid(String uid)
   {
      this.uid = uid;
   }

}
