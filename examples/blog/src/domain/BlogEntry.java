//$Id$
package domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.apache.solr.analysis.LowerCaseFilterFactory;
import org.apache.solr.analysis.NGramFilterFactory;
import org.apache.solr.analysis.SnowballPorterFilterFactory;
import org.apache.solr.analysis.StandardTokenizerFactory;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.AnalyzerDefs;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;

/**
 * Represents a blog entry.
 *
 * @author Simon Brown
 * @author Sanne Grinovero
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Indexed
@AnalyzerDefs({
   @AnalyzerDef(name = "en",
         tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
         filters = {
               @TokenFilterDef(factory = LowerCaseFilterFactory.class),
               @TokenFilterDef(factory = SnowballPorterFilterFactory.class, params =   {
                       @Parameter(name = "language", value = "English")
                     })
         }),
   @AnalyzerDef(name="ngrams", tokenizer=@TokenizerDef(factory=StandardTokenizerFactory.class),
         filters={
            @TokenFilterDef(factory = LowerCaseFilterFactory.class),
            @TokenFilterDef(factory = NGramFilterFactory.class,
                  params = { @Parameter(name = "minGramSize", value = "3"), @Parameter(name = "maxGramSize", value = "3") })
      })}
   )
public class BlogEntry {

  @Id @Length(min=1, max=20)
  @DocumentId
  private String id;
  
  @NotNull @Length(max=70)
  @Fields({
       @Field(name="title:en", analyzer=@Analyzer(definition="en")),
       @Field(name="title:ngrams", analyzer=@Analyzer(definition="ngrams"))})
  private String title;
  
  @Length(max=200)
  private String excerpt;
  
  @NotNull @Length(max=1400)
  @Fields({
       @Field(name="body:en", analyzer=@Analyzer(definition="en")),
       @Field(name="body:ngrams", analyzer=@Analyzer(definition="ngrams"))})
  private String body;
  
  @NotNull
  private Date date = new Date();
  
  @ManyToOne @NotNull 
  private Blog blog;

  public BlogEntry(Blog blog) {
    this.blog = blog;
  }
  
  BlogEntry() {}

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getExcerpt() {
    return excerpt;
  }

  public String getBody() {
    return body;
  }

  public Date getDate() {
    return date;
  }

   public void setBody(String body)
   {
      this.body = body;
   }
   
   public void setDate(Date date)
   {
      this.date = date;
   }
   
   public void setExcerpt(String excerpt)
   {
      if ( "".equals(excerpt) ) excerpt=null;
      this.excerpt = excerpt;
   }
   
   public void setId(String id)
   {
      this.id = id;
   }
   
   public void setTitle(String title)
   {
      this.title = title;
   }

}
