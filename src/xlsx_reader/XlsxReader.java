package xlsx_reader;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import app_config.BooleanValue;

/**
 * Read the configuration of tables
 * from the .xlsx. Output all the columns in the {@code schema}
 * variable, accessible by {@link #getSchema()}.
 * @author avonva && shahaal
 *
 */

public abstract class XlsxReader implements Closeable {

	private ArrayList<String> headers;
	private String filename;
	private InputStream inputStream;
	private Workbook workbook;
	
	public XlsxReader(String filename) throws IOException {
		this.filename = filename;
		this.inputStream = new FileInputStream(new File(filename));
		this.workbook = new XSSFWorkbook(inputStream);
		this.headers = new ArrayList<>();
	}
	
	/**
	 * Get the number of sheets
	 */
	public int getNumberOfSheets() {
		return this.workbook.getNumberOfSheets();
	}
	
	/**
	 * Get a sheet by name
	 * @param sheetName
	 */
	public Sheet getSheet(String sheetName) {
		return this.workbook.getSheet(sheetName);
	}
	
	/**
	 * Get the sheet at the specified position
	 * @param sheetId
	 */
	public Sheet getSheetAt(int sheetId) {
		return this.workbook.getSheetAt(sheetId);
	}
	
	/**
	 * Get all the workbook sheets
	 * @return
	 */
	public Collection<Sheet> getSheets() {
		
		Collection<Sheet> sheets = new ArrayList<>();
		
		for (int i = 0; i < workbook.getNumberOfSheets(); ++i) {
			sheets.add(workbook.getSheetAt(i));
		}
		
		return sheets;
	}
	
	/**
	 * Read the first sheet
	 */
	public void readFirstSheet() throws IOException {
		String firstSheetName = workbook.getSheetName(0);
		this.read(firstSheetName);
	}
	
	/**
	 * Read the excel workbook
	 * @throws IOException
	 */
	public void read(String sheetName) throws IOException {

		Sheet firstSheet = workbook.getSheet(sheetName);
		Iterator<Row> iterator = firstSheet.iterator();

		while (iterator.hasNext()) {
			
			Row row = iterator.next();
			
			if (row.getRowNum() != 0)
				startRow(row);

			Iterator<Cell> cellIterator = row.cellIterator();
			
			while (cellIterator.hasNext()) {
				
				Cell cell = cellIterator.next();

				// if first row parse the headers
				if (row.getRowNum() == 0) {
					headers.add(cell.getStringCellValue());
					continue;
				}
				
				// else
				
				String value = null;

				switch (cell.getCellType()) {
				case STRING:
					value = cell.getStringCellValue();
					break;
				case BOOLEAN:
					value = String.valueOf(cell.getBooleanCellValue());
					break;
				case NUMERIC:
					
					double number = cell.getNumericCellValue();
					
					// cast to integer if it is an integer
					if ((number == Math.floor(number)) && !Double.isInfinite(number)) {
						value = String.valueOf((int) number);
					}
					else {
						value = String.valueOf(number);
					}
					
					break;
					
				default:
					break;
				}

				processCell(headers.get(cell.getColumnIndex()), value);
			}
			
			// skip headers
			if (row.getRowNum() != 0)
				endRow(row);
		}
	}
	
	/**
	 * Convert a string field into boolean
	 * @param field
	 * @return
	 */
	protected boolean getBoolean(String field) {
		return BooleanValue.isTrue(field);
	}
	
	/**
	 * Get the excel headers ({@link #read(String)}
	 * must be called first
	 * @return
	 */
	public ArrayList<String> getHeaders() {
		return headers;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public void close() throws IOException {
		inputStream.close();
		workbook.close();
	}
	
	/**
	 * Called while the content of a cell is read 
	 * @param header
	 * @param value
	 */
	public abstract void processCell(String header, String value);
	
	/**
	 * Called before a row is read
	 * @param row
	 */
	public abstract void startRow(Row row);
	
	/**
	 * Called after a row is read
	 * @param row
	 */
	public abstract void endRow(Row row);
}
