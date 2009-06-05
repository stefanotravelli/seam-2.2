package org.jboss.seam.pdf.ui;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.seam.pdf.ITextUtils;
import org.jboss.seam.ui.graphicImage.GraphicImageResource;
import org.jboss.seam.ui.graphicImage.GraphicImageStore;
import org.jboss.seam.ui.graphicImage.GraphicImageStore.ImageWrapper;
import org.jboss.seam.ui.graphicImage.Image.Type;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.Dataset;

import com.lowagie.text.*;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.AsianFontMapper;

public abstract class UIChart extends ITextComponent {
    private Image image = null;
    private JFreeChart chart = null;

    private byte[] imageData;

    private int height = 300;
    private int width = 400;

    private boolean legend;
    private boolean is3D = false;

    private String title;
    private String borderBackgroundPaint;
    private String borderPaint;
    private String borderStroke;
    private boolean borderVisible = true;

    private String plotBackgroundPaint;
    private Float plotBackgroundAlpha;
    private Float plotForegroundAlpha;
    private String plotOutlineStroke;
    private String plotOutlinePaint;

    protected Dataset dataset;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return (String) valueBinding("title", title);
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return (Integer) valueBinding(FacesContext.getCurrentInstance(), "height", height);
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return (Integer) valueBinding(FacesContext.getCurrentInstance(), "width", width);
    }

    public void setLegend(boolean legend) {
        this.legend = legend;
    }

    public boolean getLegend() {
        return (Boolean) valueBinding("legend", legend);
    }

    public void setIs3D(boolean is3D) {
        this.is3D = true;
    }

    public boolean getIs3D() {
        return (Boolean) valueBinding("is3D", is3D);
    }

    public void setBorderBackgroundPaint(String backgroundPaint) {
        this.borderBackgroundPaint = backgroundPaint;
    }

    public String getBorderBackgroundPaint() {
        return (String) valueBinding(FacesContext.getCurrentInstance(), "borderBackgroundPaint", borderBackgroundPaint);
    }

    public void setBorderPaint(String borderPaint) {
        this.borderPaint = borderPaint;
    }

    public String getBorderPaint() {
        return (String) valueBinding(FacesContext.getCurrentInstance(), "borderPaint", borderPaint);
    }

    public void setBorderStroke(String borderStroke) {
        this.borderStroke = borderStroke;
    }

    public String getBorderStroke() {
        return (String) valueBinding(FacesContext.getCurrentInstance(), "borderStroke", borderStroke);
    }

    public void setBorderVisible(boolean borderVisible) {
        this.borderVisible = borderVisible;
    }

    public boolean getBorderVisible() {
        return (Boolean) valueBinding(FacesContext.getCurrentInstance(), "borderVisible", borderVisible);
    }

    public void setPlotBackgroundAlpha(Float plotBackgroundAlpha) {
        this.plotBackgroundAlpha = plotBackgroundAlpha;
    }

    public Float getPlotBackgroundAlpha() {
        return (Float) valueBinding(FacesContext.getCurrentInstance(), "plotBackgroundAlpha", plotBackgroundAlpha);
    }

    public void setPlotBackgroundPaint(String plotBackgroundPaint) {
        this.plotBackgroundPaint = plotBackgroundPaint;
    }

    public String getPlotBackgroundPaint() {
        return (String) valueBinding(FacesContext.getCurrentInstance(), "plotBackgroundPaint", plotBackgroundPaint);
    }

    public void setPlotForegroundAlpha(Float plotForegroundAlpha) {
        this.plotForegroundAlpha = plotForegroundAlpha;
    }

    public Float getPlotForegroundAlpha() {
        return (Float) valueBinding(FacesContext.getCurrentInstance(), "plotForegroundAlpha", plotForegroundAlpha);
    }

    public void setPlotOutlinePaint(String plotOutlinePaint) {
        this.plotOutlinePaint = plotOutlinePaint;
    }

    public String getPlotOutlinePaint() {
        return (String) valueBinding(FacesContext.getCurrentInstance(), "plotOutlinePaint", plotOutlinePaint);
    }

    public void setPlotOutlineStroke(String plotOutlineStroke) {
        this.plotOutlineStroke = plotOutlineStroke;
    }

    public String getPlotOutlineStroke() {
        return (String) valueBinding(FacesContext.getCurrentInstance(), "plotOutlineStroke", plotOutlineStroke);
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public Dataset getDataset() {
        return (Dataset) valueBinding(FacesContext.getCurrentInstance(), "dataset", dataset);
    }
    
    public void setChart(JFreeChart chart) {
       this.chart = chart;
   }

   public JFreeChart getChart() {
       return (JFreeChart) valueBinding(FacesContext.getCurrentInstance(), "chart", chart);
   }
    
    @Override
    public void restoreState(FacesContext context, Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);

        height = (Integer) values[1];
        width = (Integer) values[2];
        borderBackgroundPaint = (String) values[3];
        borderPaint = (String) values[4];
        borderStroke = (String) values[5];
        borderVisible = (Boolean) values[6];
        plotBackgroundPaint = (String) values[7];
        plotBackgroundAlpha = (Float) values[8];
        plotForegroundAlpha = (Float) values[9];
        plotOutlineStroke = (String) values[10];
        plotOutlinePaint = (String) values[11];
        title = (String) values[12];
        is3D = (Boolean) values[13];
        legend = (Boolean) values[14];
    }

    @Override
    public Object saveState(FacesContext context) {
        Object[] values = new Object[15];

        values[0] = super.saveState(context);
        values[1] = height;
        values[2] = width;
        values[3] = borderBackgroundPaint;
        values[4] = borderPaint;
        values[5] = borderStroke;
        values[6] = borderVisible;
        values[7] = plotBackgroundPaint;
        values[8] = plotBackgroundAlpha;
        values[9] = plotForegroundAlpha;
        values[10] = plotOutlineStroke;
        values[11] = plotOutlinePaint;
        values[12] = title;
        values[13] = is3D;
        values[14] = legend;

        return values;
    }

    public static Paint findColor(String name) {
        if (name == null || name.length() == 0) {
            return null;
        }
        UIComponent component = FacesContext.getCurrentInstance().getViewRoot()
                .findComponent(name);

        if (component != null) {
            if (component instanceof UIColor) {
                return ((UIColor) component).getPaint();
            } else {
                throw new RuntimeException();
            }
        }

        return ITextUtils.colorValue(name);
    }

    public static Stroke findStroke(String id) {
        if (id == null || id.length() == 0) {
            return null;
        }

        UIComponent component = FacesContext.getCurrentInstance().getViewRoot().findComponent(id);

        if (component instanceof UIStroke) {
            return ((UIStroke) component).getStroke();
        } else {
            throw new RuntimeException();

        }
    }

    public abstract JFreeChart createChart(FacesContext context);

    @Override
    public void createITextObject(FacesContext context) {

        if (getBorderBackgroundPaint() != null) {
            chart.setBackgroundPaint(findColor(getBorderBackgroundPaint()));
        }

        if (getBorderPaint() != null) {
            chart.setBorderPaint(findColor(getBorderPaint()));
        }

        if (getBorderStroke() != null) {
            chart.setBorderStroke(findStroke(getBorderStroke()));
        }

        chart.setBorderVisible(getBorderVisible());

        configurePlot(chart.getPlot());

        try {
            UIDocument doc = (UIDocument) findITextParent(getParent(),
                    UIDocument.class);
            if (doc != null) {
                PdfWriter writer = (PdfWriter) doc.getWriter();
                PdfContentByte cb = writer.getDirectContent();
                PdfTemplate tp = cb.createTemplate(getWidth(), getHeight());

                UIFont font = (UIFont) findITextParent(this, UIFont.class);

                DefaultFontMapper fontMapper;
                if (font == null) {
                    fontMapper = new DefaultFontMapper();
                } else {
                    fontMapper = new AsianFontMapper(font.getName(), font.getEncoding());
                }

                Graphics2D g2 = tp.createGraphics(getWidth(), getHeight(), fontMapper);
                chart.draw(g2, new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
                g2.dispose();

                image = new ImgTemplate(tp);
            } else {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                ChartUtilities.writeChartAsJPEG(stream, chart, getWidth(),
                        getHeight());

                imageData = stream.toByteArray();
                stream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void configurePlot(Plot plot) {
        if (getPlotBackgroundAlpha() != null) {
            plot.setBackgroundAlpha(getPlotBackgroundAlpha());
        }
        if (getPlotForegroundAlpha() != null) {
            plot.setForegroundAlpha(getPlotForegroundAlpha());
        }
        if (getPlotBackgroundPaint() != null) {
            plot.setBackgroundPaint(findColor(getPlotBackgroundPaint()));
        }
        if (getPlotOutlinePaint() != null) {
            plot.setOutlinePaint(findColor(getPlotOutlinePaint()));
        }
        if (getPlotOutlineStroke() != null) {
            plot.setOutlineStroke(findStroke(getPlotOutlineStroke()));
        }
    }

    public PlotOrientation plotOrientation(String orientation) {
        if (orientation != null && orientation.equalsIgnoreCase("horizontal")) {
            return PlotOrientation.HORIZONTAL;
        } else {
            return PlotOrientation.VERTICAL;
        }
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        dataset = getDataset();
        // bypass super to avoid createITextObject() before the chart is ready
        if (dataset == null) {
            dataset = createDataset();
        }
        
        chart = getChart();
        if (chart == null) {
            chart = createChart(context);
        }
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        // call create here so that we'll have a valid chart
        createITextObject(context);

        if (imageData != null) {
            ResponseWriter response = context.getResponseWriter();
            response.startElement("img", null);
            GraphicImageStore store = GraphicImageStore.instance();
            String key = store.put(new ImageWrapper(imageData, Type.IMAGE_JPEG));
            String url = context.getExternalContext().getRequestContextPath()
                    + GraphicImageResource.GRAPHIC_IMAGE_RESOURCE_PATH + "/"
                    + key + Type.IMAGE_JPEG.getExtension();

            response.writeAttribute("src", url, null);

            response.writeAttribute("height", getHeight(), null);
            response.writeAttribute("width", getWidth(), null);

            response.endElement("img");
        }

        super.encodeEnd(context);
    }

    @Override
    public Object getITextObject() {
        return image;
    }

    @Override
    public void handleAdd(Object arg0) {
        throw new RuntimeException("No children allowed");
    }

    @Override
    public void removeITextObject() {
        image = null;
        chart = null;
    }

    public abstract Dataset createDataset();
}
