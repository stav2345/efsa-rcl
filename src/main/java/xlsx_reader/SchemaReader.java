package xlsx_reader;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Row;

import table_skeleton.TableColumnBuilder;
import xlsx_reader.TableHeaders.XlsxHeader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Read the configuration of tables from the .xlsx. Output all the columns in
 * the {@code schema} variable, accessible by {@link #getSchema()}.
 * 
 * @author avonva
 * @author shahaal
 *
 */
public class SchemaReader extends XlsxReader {
	
	private static final Logger LOGGER = LogManager.getLogger(SchemaReader.class);

	public TableSchema schema;
	private TableColumnBuilder builder;

	public SchemaReader(String filename) throws IOException {
		super(filename);
		this.schema = new TableSchema();
		this.builder = new TableColumnBuilder();
	}

	@Override
	public void read(String sheetName) throws IOException {
		this.schema.setSheetName(sheetName);
		super.read(sheetName);
	}

	@Override
	public void processCell(String header, String value) {

		XlsxHeader h = null;
		try {
			h = XlsxHeader.fromString(header); // get enum from string
		} catch (IllegalArgumentException e) {
			LOGGER.error("Error in processing xls cell: " + e);
			e.printStackTrace();
			return;
		}

		if (h == null || value == null)
			return;

		switch (h) {
		case ID:
			builder.setId(value);
			break;
		case CODE:
			builder.setCode(value);
			break;
		case LABEL:
			builder.setLabel(value);
			break;
		case XML_TAG:
			builder.setXmlTag(value);
			break;
		case TIP:
			builder.setTip(value);
			break;
		case TYPE:
			builder.setType(value);
			break;
		case MANDATORY:
			builder.setMandatory(value);
			break;
		case EDITABLE:
			builder.setEditable(value);
			break;
		case VISIBLE:
			builder.setVisible(value);
			break;
		case PICKLIST_KEY:
			builder.setPicklistKey(value);
			break;
		case PICKLIST_FILTER:
			builder.setPicklistFilter(value);
			break;
		case DEFAULT_VALUE:
			builder.setDefaultValue(value);
			break;
		case CODE_FORMULA:
			builder.setCodeFormula(value);
			break;
		case LABEL_FORMULA:
			builder.setLabelFormula(value);
			break;
		case DEFAULT_CODE:
			builder.setDefaultCode(value);
			break;
		case PUT_IN_OUTPUT:
			builder.setPutInOutput(value);
			break;
		case ORDER:
			int order;
			try {
				order = Integer.valueOf(value);
			} catch (NumberFormatException e) {
				LOGGER.error("Error in integer conversion: " + e);
				e.printStackTrace();
				order = -1;
			}
			builder.setOrder(order);
			break;
		case NATURAL_KEY:
			builder.setNaturalKey(value);
			break;
		default:
			break;
		}
	}

	public TableSchema getSchema() {
		return schema;
	}

	@Override
	public void startRow(Row row) {
		this.builder = new TableColumnBuilder();
	}

	@Override
	public void endRow(Row row) {
		schema.add(builder.build());
		builder = null;
	}
}
