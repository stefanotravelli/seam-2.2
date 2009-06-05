package org.jboss.seam.excel.jxl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.imageio.ImageIO;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.WritableCell;
import jxl.write.WritableHyperlink;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.jboss.seam.core.Interpolator;
import org.jboss.seam.document.DocumentData;
import org.jboss.seam.document.DocumentData.DocumentType;
import org.jboss.seam.excel.ExcelWorkbook;
import org.jboss.seam.excel.ExcelWorkbookException;
import org.jboss.seam.excel.WorksheetItem;
import org.jboss.seam.excel.ui.UICell;
import org.jboss.seam.excel.ui.UIColumn;
import org.jboss.seam.excel.ui.UIHyperlink;
import org.jboss.seam.excel.ui.UIImage;
import org.jboss.seam.excel.ui.UILink;
import org.jboss.seam.excel.ui.UIWorkbook;
import org.jboss.seam.excel.ui.UIWorksheet;
import org.jboss.seam.excel.ui.command.Command;
import org.jboss.seam.excel.ui.command.UIGroupColumns;
import org.jboss.seam.excel.ui.command.UIGroupRows;
import org.jboss.seam.excel.ui.command.UIMergeCells;
import org.jboss.seam.excel.ui.command.UIRowPageBreak;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

/**
 * Class that encapsulates the JExcelApi Workbook and Worksheet concepts and
 * internal state
 * 
 * @author Nicklas Karlsson (nickarls@gmail.com)
 * @author Daniel Roth (danielc.roth@gmail.com)
 */
public class JXLExcelWorkbook implements ExcelWorkbook {
    private static final int CELL_DEFAULT_HEIGHT = 17;
    private static final int CELL_DEFAULT_WIDTH = 64;

    private Log log = Logging.getLog(getClass());

    // The maximum number of columns allowed by the Excel specification
    private static final int MAX_COLUMNS = 255;

    // The maximum number of columns allowed by the Excel specification. This
    // will be worked around in future versions of this class by automatically
    // creating new sheets
    private static final int MAX_ROWS = 65535;

    // The default worksheet naming base
    private static final String DEFAULT_WORKSHEET_NAME = "Sheet{0}";

    // The temporary array of data which represents the binary worksheet. This
    // will be passed on to the DocumentStore
    private ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

    // The JExcelAPI abstraction of a workbook. There will only be one per
    // instance of this class
    private WritableWorkbook workbook;

    // The JExcelAPI abstraction of a worksheet. This also represents the
    // current
    // worksheet begin worked on
    private WritableSheet worksheet;

    // The row index to start from. Used for placing a data block at another
    // location than the default top-left (0, 0)
    private int startRowIndex = 0;

    // The current index of the row being worked on (the row where the next cell
    // will be added)
    private int currentRowIndex = 0;

    // The column index to start from. Used for placing a data block at another
    // location than the default top-left (0, 0)
    private int startColumnIndex = 0;

    // The current index of the column being worked on (the column where the
    // next
    // cell will be added)
    private int currentColumnIndex = 0;

    /*
     * The current index of the worksheet being worked on. It's not that
     * important right now (we are moving forward linearly, but later when be
     * support worksheets with more that 65k rows we have to keep track on where
     * we are because starting with the next column could mean jumping back
     * several worksheets. For this we will also require some sort of low- and
     * high-indexes for the current worksheet when we add support for multiple
     * user-defined worksheets in the workbook.
     */
    private int currentWorksheetIndex = 0;

    /**
     * The maximum row index we have seen. Used for determining where to place
     * the worksheet footer (if any)
     */
    private int maxRowIndex;

    private JXLHelper jxlHelper = new JXLHelper();

    /**
     * Moves the row pointer to the next row. Used internally when adding data
     * 
     */
    private void nextRow() {
        if (log.isTraceEnabled()) {
            log.trace("Moving from row #0 to #1", currentRowIndex,
                    currentRowIndex + 1);
        }
        currentRowIndex++;
        if (currentRowIndex >= MAX_ROWS) {
            throw new ExcelWorkbookException(Interpolator.instance()
                    .interpolate("Excel only supports {0} rows", MAX_COLUMNS));
        }
    }

    /**
     * Moves the internal column pointer to the next column, called by the tag
     * to indicate that a new column has been started. If the pointer exceeds
     * the maximum allowed, throws an exception. Resets the styles and row
     * indexes etc.
     * 
     */
    public void nextColumn() {
        if (log.isTraceEnabled()) {
            log.trace("Moving from column #0 to #1", currentColumnIndex,
                    currentColumnIndex + 1);
        }
        currentColumnIndex++;
        if (currentColumnIndex > MAX_COLUMNS) {
            throw new ExcelWorkbookException(Interpolator.instance()
                    .interpolate("Excel doesn't support more than {0} columns",
                            MAX_COLUMNS));
        }
        if (currentRowIndex > maxRowIndex) {
            maxRowIndex = currentRowIndex;
        }
        currentRowIndex = startRowIndex;
    }

    /**
     * Checks if the workbook contains a sheet
     * 
     * @param name
     *            The name to look for
     * @return true if found, false otherwise
     */
    private boolean workbookContainsSheet(String name) {
        if (log.isTraceEnabled()) {
            log.trace("Checking if workbook contains sheet named #0", name);
        }
        if (workbook == null) {
            throw new ExcelWorkbookException(
                    "Can't search for sheets before creating a workbook");
        }
        boolean found = false;
        for (String sheetName : workbook.getSheetNames()) {
            if (sheetName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("Result: #0", found);
        }
        return found;
    }

    /**
     * Creates a new worksheet (or selects one if it exists) in the workbook.
     * Will require a rework for auto-renaming when support for auto-adding of
     * new worksheets if there are more than 65k rows. Resets the internal state
     * (row- and column indexes, current styles etc)
     * 
     * @param uiWorksheet
     *            The worksheet to create or select in the workbook
     */
    public void createOrSelectWorksheet(UIWorksheet uiWorksheet) {
        if (workbook == null) {
            throw new ExcelWorkbookException(
                    "You cannot create a worksheet before creating a workbook");
        }
        if (log.isDebugEnabled()) {
            log
                    .debug(
                            "Creating worksheet named #0 starting at column #1 and row #2",
                            uiWorksheet.getName(),
                            uiWorksheet.getStartColumn(), uiWorksheet
                                    .getStartRow());
        }
        if (workbookContainsSheet(uiWorksheet.getName())) {
            if (log.isTraceEnabled()) {
                log.trace("Sheet found, selecting");
            }
            worksheet = workbook.getSheet(uiWorksheet.getName());
        } else {
            if (log.isTraceEnabled()) {
                log.trace("Sheet not found, creating");
            }
            String name = uiWorksheet.getName() != null ? uiWorksheet.getName()
                    : Interpolator.instance().interpolate(
                            DEFAULT_WORKSHEET_NAME, currentWorksheetIndex + 1);
            worksheet = workbook.createSheet(name, currentWorksheetIndex);
        }

        jxlHelper.applyWorksheetSettings(worksheet, uiWorksheet);
        currentWorksheetIndex++;
        startColumnIndex = uiWorksheet.getStartColumn() == null ? 0
                : uiWorksheet.getStartColumn();
        currentColumnIndex = startColumnIndex;
        startRowIndex = uiWorksheet.getStartRow() == null ? 0 : uiWorksheet
                .getStartRow();
        currentRowIndex = startRowIndex;
        maxRowIndex = currentRowIndex;
    }

    /**
     * Creates and adds a data cell to the worksheet using the data cell format.
     * If the cell format is null, initializes the cell format. Finally moves
     * the internal pointer to the next row.
     * 
     * @param uiCell
     *            The cell to be created and added to the workbook
     * @param the
     *            type (header or data) of the cell
     */
    private void addCell(UICell uiCell) {
        if (log.isTraceEnabled()) {
            log.trace("Adding a cell with data #1 at column #2 and row #3",
                    uiCell.getValue(), currentColumnIndex, currentRowIndex);
        }
        if (worksheet == null) {
            throw new ExcelWorkbookException(
                    "Can't add cells before creating worksheet");
        }

        // Determine where to really place the cell
        int useRow = uiCell.getRow() != null ? uiCell.getRow()
                : currentRowIndex;
        int useColumn = uiCell.getColumn() != null ? uiCell.getColumn()
                : currentColumnIndex;

        CellInfo cellInfo = jxlHelper.getCellInfo(uiCell);
        WritableCell cell = JXLHelper.createCell(useColumn, useRow, cellInfo
                .getCellType(), uiCell.getValue(), cellInfo.getCellFormat());
        if (cellInfo.getCellFeatures() != null) {
            cell.setCellFeatures(cellInfo.getCellFeatures());
        }
        try {
            worksheet.addCell(cell);
        } catch (WriteException e) {
            throw new ExcelWorkbookException("Could not add cell", e);
        }
        // Only increase row if cell had no explicit placing
        if (uiCell.getColumn() == null && uiCell.getRow() == null) {
            nextRow();
        }
    }

    /**
     * Returns the binary data from the internal representation of the workbook
     * 
     * @return the data
     * @throws ExcelWorkbookException
     *             If there is a problem producing the binary data
     */
    public byte[] getBytes() {
        if (log.isTraceEnabled()) {
            log.trace("Returning bytes from workbook");
        }
        if (workbook == null) {
            throw new ExcelWorkbookException(
                    "You can't get workbook data before creating a workbook");
        }
        // You will get an IndexOutOfBoundException if trying to write a
        // workbook
        // without sheets,
        // creating a dummy. Could also throw an exception...
        if (workbook.getSheets().length == 0) {
            if (log.isTraceEnabled()) {
                log.trace("Creating dummy sheet");
            }
            workbook.createSheet("dummy", 0);
        }
        try {
            workbook.write();
            workbook.close();
        } catch (WriteException e) {
            throw new ExcelWorkbookException(
                    "There was an exception writing the workbook", e);
        } catch (IOException e) {
            throw new ExcelWorkbookException(
                    "There was an exception closing the workbook", e);
        }
        return byteStream.toByteArray();
    }

    /**
     * Intitializes a new workbook. Must be called first. Not that pretty but
     * the API has different constructors for all permutations of workbook
     * settings and template usage
     * 
     * @param uiWorkbook
     *            UIn Workbook to create
     * @throws ExcelWorkbookException
     *             if there were any errors creating the workbook
     */
    public void createWorkbook(UIWorkbook uiWorkbook) {
        String urlString = uiWorkbook.getTemplateURI();
        InputStream templateStream = null;
        if (urlString != null) {
            try {
                if (urlString.indexOf("://") < 0) {
                    templateStream = getClass().getResourceAsStream(urlString);
                } else {
                    templateStream = new URL(urlString).openStream();
                }
            } catch (Exception e) {
                throw new ExcelWorkbookException(
                        "Could not handle template URI", e);
            }
        }
        WorkbookSettings workbookSettings = null;
        if (uiWorkbook.hasSettings()) {
            workbookSettings = jxlHelper.createWorkbookSettings(uiWorkbook);
        }
        if (log.isDebugEnabled()) {
            log.debug("Creating workbook with creation type #0", uiWorkbook
                    .getCreationType());
        }
        // The joys of multiple constructors and no setters...
        try {
            switch (uiWorkbook.getCreationType()) {
            case WITH_SETTNGS_AND_TEMPLATE:
                workbook = Workbook.createWorkbook(byteStream, Workbook
                        .getWorkbook(templateStream), workbookSettings);
                break;
            case WITH_SETTINGS_WITHOUT_TEMPLATE:
                workbook = Workbook
                        .createWorkbook(byteStream, workbookSettings);
                break;
            case WITHOUT_SETTINGS_WITH_TEMPLATE:
                workbook = Workbook.createWorkbook(byteStream, Workbook
                        .getWorkbook(templateStream));
                break;
            case WITHOUT_SETTINGS_OR_TEMPLATE:
                workbook = Workbook.createWorkbook(byteStream);
                break;
            }
        } catch (Exception e) {
            throw new ExcelWorkbookException("Could not create workbook", e);
        }
        if (uiWorkbook.getWorkbookProtected() != null) {
            workbook.setProtected(uiWorkbook.getWorkbookProtected());
        }
        currentWorksheetIndex = workbook.getNumberOfSheets();
    }

    /**
     * Gets the document type of the data for the DocumentStore
     * 
     * @return the document type (Excel workbook)
     */
    public DocumentType getDocumentType() {
        return new DocumentData.DocumentType("xls", "application/vnd.ms-excel");
    }

    /**
     * Applies column settings for the current column
     * 
     * @param uiColumn
     *            the UI column to inspect for settings
     */
    public void applyColumnSettings(UIColumn uiColumn) {
        if (worksheet == null) {
            throw new ExcelWorkbookException(
                    "You can't set column settings before creating a worksheet");
        }
        jxlHelper.applyColumnSettings(uiColumn, worksheet, currentColumnIndex);
    }

    /**
     * Adds an image to the worksheet. First converts it to PNG since it's what
     * the library wants. If starting rows or columns are given, uses them,
     * otherwise uses the current indexes. If column- and rowspannings are
     * given, uses them, otherwise tries to determine them from the image
     * dimensions and default cell dimensions.
     * 
     * @param uiImage
     *            The image to add
     */
    private void addImage(UIImage uiImage) {
        if (worksheet == null) {
            throw new ExcelWorkbookException(
                    "Can't add an image before creating a worksheet");
        }

        BufferedImage image = null;
        ByteArrayOutputStream pngStream = null;
        try {
            image = ImageIO.read(new URI(uiImage.getURI()).toURL());
            pngStream = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", pngStream);
        } catch (Exception e) {
            throw new ExcelWorkbookException("Could not load or process image",
                    e);
        }

        int useStartColumn = uiImage.getStartColumn() == null ? currentColumnIndex
                : uiImage.getStartRow();
        int useStartRow = uiImage.getStartRow() == null ? currentRowIndex
                : uiImage.getStartRow();
        double estimatedRowSpan = (double) image.getHeight()
                / (double) CELL_DEFAULT_HEIGHT;
        double estimatedColSpan = (double) image.getWidth()
                / (double) CELL_DEFAULT_WIDTH;
        double useColumnSpan = uiImage.getColumnSpan() == null ? estimatedRowSpan
                : uiImage.getColumnSpan();
        double useRowSpan = uiImage.getRowSpan() == null ? estimatedColSpan
                : uiImage.getRowSpan();

        worksheet.addImage(new WritableImage(useStartColumn, useStartRow,
                useColumnSpan, useRowSpan, pngStream.toByteArray()));
    }

    /**
     * Creates a hyperlink to an URL in the worksheet
     * 
     * @param column
     *            The target column of the link (if null, defaults to current
     *            column)
     * @param row
     *            The target row of the link (if null, defaults to current row)
     * @param url
     *            The target URL
     */
    private void addHyperlink(UIHyperlink uiHyperlink) {
        if (worksheet == null) {
            throw new ExcelWorkbookException(
                    "Can't add a hyperlink before creating a worksheet");
        }

        int useStartColumn = uiHyperlink.getStartColumn() == null ? currentColumnIndex
                : uiHyperlink.getStartColumn();
        int useStartRow = uiHyperlink.getStartRow() == null ? currentRowIndex
                : uiHyperlink.getStartRow();
        int useEndColumn = uiHyperlink.getEndColumn() == null ? useStartColumn
                : uiHyperlink.getEndColumn();
        int useEndRow = uiHyperlink.getEndRow() == null ? useStartRow
                : uiHyperlink.getEndRow();
        String useDescription = uiHyperlink.getDescription() == null ? uiHyperlink
                .getURL()
                : uiHyperlink.getDescription();
        URL useURL = null;

        try {
            useURL = new URL(uiHyperlink.getURL());
        } catch (MalformedURLException e) {
            throw new ExcelWorkbookException("Bad url", e);
        }
        try {
            worksheet.addHyperlink(new WritableHyperlink(useStartColumn,
                    useStartRow, useEndColumn, useEndRow, useURL,
                    useDescription));
        } catch (Exception e) {
            throw new ExcelWorkbookException("Could not add hyperlink", e);
        }
    }

    /**
     * Adds an item (cell, image, hyperlink) to add to the worksheet
     * 
     * @param item
     *            The item to add
     */
    public void addItem(WorksheetItem item) {
        if (!((UIComponent) item).isRendered()) {
            return;
        }
        if (item.getValue() == null || "".equals(item.getValue())) {
            if (item.getColumn() == null && item.getRow() == null) {
                nextRow();
            }
            return;
        }
        switch (item.getItemType()) {
        case cell:
            addCell((UICell) item);
            break;
        case hyperlink:
            addHyperlink((UIHyperlink) item);
            break;
        case image:
            addImage((UIImage) item);
            break;
        default:
            throw new ExcelWorkbookException(Interpolator.instance()
                    .interpolate("Unknown item type {0}", item.getItemType()));
        }
    }

    /**
     * Executes a command for a worksheet
     * 
     * @param command
     *            The command to execute
     */
    public void executeCommand(Command command) {
        switch (command.getCommandType()) {
        case merge_cells:
            mergeCells((UIMergeCells) command);
            break;
        case group_columns:
            groupColumns((UIGroupColumns) command);
            break;
        case group_rows:
            groupRows((UIGroupRows) command);
            break;
        case add_row_pagebreak:
            addRowPageBreak((UIRowPageBreak) command);
            break;
        default:
            throw new ExcelWorkbookException(
                    Interpolator.instance().interpolate("Unknown command #0",
                            command.getCommandType()));
        }
    }

    /**
     * Adds a row page break to the worksheet
     * 
     * @param command
     *            the page break command to interpret
     */
    private void addRowPageBreak(UIRowPageBreak command) {
        if (log.isTraceEnabled()) {
            log.trace("Adding row page break #0", command);
        }
        if (worksheet == null) {
            throw new ExcelWorkbookException(
                    "Can't add row page breaks before creating a worksheet");
        }
        int useRow = command.getRow() != null ? command.getRow()
                : currentRowIndex;
        worksheet.addRowPageBreak(useRow);
    }

    /**
     * Groups worksheet rows
     * 
     * @param command
     *            The group command to interpret
     */
    private void groupRows(UIGroupRows command) {
        if (log.isTraceEnabled()) {
            log.trace("Grouping rows #0", command);
        }
        if (worksheet == null) {
            throw new ExcelWorkbookException(
                    "Can't group rows before creating a worksheet");
        }
        if (command.getStartRow() == null || command.getEndRow() == null) {
            throw new ExcelWorkbookException(
                    "Must define starting and ending rows when grouping rows");
        }
        boolean collapse = command.getCollapse() == null ? false : command
                .getCollapse();
        try {
            worksheet.setRowGroup(command.getStartRow(), command.getEndRow(),
                    collapse);
        } catch (Exception e) {
            throw new ExcelWorkbookException("Could not group columns", e);
        }
    }

    /**
     * Groups columns in the worksheet
     * 
     * @param command
     *            The group command to interpret
     */
    private void groupColumns(UIGroupColumns command) {
        if (log.isTraceEnabled()) {
            log.trace("Grouping columns #0", command);
        }
        if (worksheet == null) {
            throw new ExcelWorkbookException(
                    "Can't group columns before creating a worksheet");
        }
        if (command.getStartColumn() == null || command.getEndColumn() == null) {
            throw new ExcelWorkbookException(
                    "Must define starting and ending columns when grouping columns");
        }
        // JExcelAPI bug workaround
        for (int i = command.getStartColumn(); i <= command.getEndColumn(); i++) {
            worksheet.setColumnView(i, new CellView());
        }
        boolean collapse = command.getCollapse() == null ? false : command
                .getCollapse();
        try {
            worksheet.setColumnGroup(command.getStartColumn(), command
                    .getEndColumn(), collapse);
        } catch (Exception e) {
            throw new ExcelWorkbookException("Could not group columns", e);
        }
    }

    /**
     * Merge cells in the worksheet
     * 
     * @param command
     *            The merge command to interpret
     */
    private void mergeCells(UIMergeCells command) {
        if (log.isTraceEnabled()) {
            log.trace("Merging cells #0", command);
        }
        if (worksheet == null) {
            throw new ExcelWorkbookException(
                    "Can't merge cells before creating a worksheet");
        }
        if (command.getStartColumn() == null || command.getStartRow() == null
                || command.getEndColumn() == null
                || command.getEndRow() == null) {
            throw new ExcelWorkbookException(
                    "All start/end columns/rows must be set when merging cells");
        }
        try {
            worksheet
                    .mergeCells(command.getStartColumn(),
                            command.getStartRow(), command.getEndColumn(),
                            command.getEndRow());
        } catch (Exception e) {
            throw new ExcelWorkbookException("Couldn't merge cells", e);
        }
    }

    /**
     * Places an item in the worksheet footer
     * 
     * @param item
     *            The item to add
     * @param colspan
     *            The number of columns to span
     */
    public void addWorksheetFooter(WorksheetItem item, int colspan) {
        currentColumnIndex = startColumnIndex;
        currentRowIndex = maxRowIndex;
        UIMergeCells mergeCommand = new UIMergeCells();
        mergeCommand.setStartColumn(currentColumnIndex);
        mergeCommand.setStartRow(currentRowIndex);
        mergeCommand.setEndColumn(currentColumnIndex + colspan - 1);
        mergeCommand.setEndRow(currentRowIndex);
        executeCommand(mergeCommand);
        addItem(item);
    }

    /**
     * Places an item in the worksheet header
     * 
     * @param item
     *            The item to add
     * @param colspan
     *            The number of columns to span
     */
    public void addWorksheetHeader(WorksheetItem item, int colspan) {
        UIMergeCells mergeCommand = new UIMergeCells();
        mergeCommand.setStartColumn(currentColumnIndex);
        mergeCommand.setStartRow(currentRowIndex);
        mergeCommand.setEndColumn(currentColumnIndex + colspan - 1);
        mergeCommand.setEndRow(currentRowIndex);
        executeCommand(mergeCommand);
        addItem(item);
        startRowIndex++;
    }

    /**
     * Sets stylesheets for the workbook
     * 
     * @param stylesheets
     *            The stylesheet to register
     */
    public void setStylesheets(List<UILink> stylesheets) {
        try {
            jxlHelper.setStylesheets(stylesheets);
        } catch (Exception e) {
            throw new ExcelWorkbookException("Could not parse stylesheet", e);
        }
    }

}
