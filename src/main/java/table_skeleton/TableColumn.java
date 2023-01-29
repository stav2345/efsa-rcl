package table_skeleton;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import app_config.BooleanValue;
import formula.Formula;
import formula.FormulaException;
import formula.FormulaSolver;
import table_dialog.TableView;
import table_relations.Relation;
import xlsx_reader.TableHeaders.XlsxHeader;
import xlsx_reader.TableSchema;
import xml_catalog_reader.Selection;
import xml_catalog_reader.SelectionList;
import xml_catalog_reader.XmlContents;
import xml_catalog_reader.XmlLoader;

/**
 * Column class which models the property of a single column of a {@link TableView}
 * @author avonva
 * @author shahaal
 *
 */
public class TableColumn implements Comparable<TableColumn> {
	
	private static final Logger LOGGER = LogManager.getLogger(TableColumn.class);
	
	private String id;           // key which identifies the column
	private String code;         // code of the column
	private String label;        // column name showed to the user
	private String xmlTag;       // xml tag for creating the .xml
	private String tip;          // tip to help the user
	private ColumnType type;     // integer, string, picklist...
	private String mandatory;    // the column needs to be filled?
	private String editable;     // if the user can edit the value
	private String visible;      // if the user can view the column
	private String picklistKey;  // code of the list from which we pick the selectable values
	private String picklistFilter;  // filter the data of the picklist using a code
	private String defaultCode;  // 
	private String codeFormula;
	private String defaultValue; // default value of the column
	private String labelFormula;
	private String putInOutput;  // if the column value should be exported in the .xml
	private int order;           // order of visualization, only for visible columns
	private String naturalKey;   // if the column is part of a natural key or not
	
	/**
	 * Create a column
	 * @param key column key
	 * @param label column name
	 * @param editable if true, the column values can be edited
	 * @param type the column type
	 */
	public TableColumn(String id, String code, String label, String xmlTag, String tip, 
			ColumnType type, String mandatory, String editable, String visible,
			String defaultCode, String codeFormula, String defaultValue, String labelFormula,
			String putInOutput, int order, String naturalKey) {
		
		this.id = id;
		this.code = code;
		this.label = label;
		this.xmlTag = xmlTag;
		this.tip = tip;
		this.type = type;
		this.mandatory = mandatory;
		this.editable = editable;
		this.visible = visible;
		this.defaultCode = defaultCode;
		this.codeFormula = codeFormula;
		this.defaultValue = defaultValue;
		this.labelFormula = labelFormula;
		this.putInOutput = putInOutput;
		this.order = order;
		this.naturalKey = naturalKey;
	}
	
	public enum ColumnType {
		INTEGER("integer"),
		U_INTEGER("u_integer"), // unsigned integer
		STRING("string"),
		PICKLIST("picklist"),  // catalogues values
		PASSWORD("password"),
		FOREIGNKEY("foreignKey");  // relation with other tables
		
		private String headerName;
		
		private ColumnType(String headerName) {
			this.headerName = headerName;
		}
		
		/**
		 * Get the enumerator that matches the {@code text}
		 * @param text
		 * @return
		 */
		public static ColumnType fromString(String text) {
			
			for (ColumnType b : ColumnType.values()) {
				if (b.headerName.equalsIgnoreCase(text)) {
					return b;
				}
			}
			return ColumnType.STRING;
		}
	}
	
	/**
	 * Get a column field by its header
	 * @param header
	 * @return
	 */
	public String getFieldByHeader(String header) {
		
		XlsxHeader h = null;
		try {
			h = XlsxHeader.fromString(header);  // get enum from string
		}
		catch(IllegalArgumentException e) {
			LOGGER.error("Error in accessing column by header ",e);
			e.printStackTrace();
			return null;
		}
		
		String value = null;
		
		switch (h) {
		case ID:
			value = this.id;
			break;
		case CODE:
			value = this.code;
			break;
		case LABEL:
			value = this.label;
			break;
		case TIP:
			value = this.tip;
			break;
		case TYPE:
			value = this.type.toString();
			break;
		case MANDATORY:
			value = this.mandatory;
			break;
		case EDITABLE:
			value = this.editable;
			break;
		case VISIBLE:
			value = this.visible;
			break;
		case PICKLIST_KEY:
			value = this.picklistKey;
			break;
		case PICKLIST_FILTER:
			value = this.picklistFilter;
			break;
		case DEFAULT_VALUE:
			value = this.defaultValue;
			break;
		case DEFAULT_CODE:
			value = this.defaultCode;
			break;
		case CODE_FORMULA:
			value = this.codeFormula;
			break;
		case LABEL_FORMULA:
			value = this.labelFormula;
			break;
		case PUT_IN_OUTPUT:
			value = this.putInOutput;
			break;
		case ORDER:
			value = String.valueOf(this.order);
			break;
		case XML_TAG:
			value = this.getXmlTag();
			break;
		case NATURAL_KEY:
			value = this.naturalKey;
			break;
		default:
			break;
		}
		
		return value;
	}
	
	/**
	 * Check if the column is automatically computed using a formula.
	 * @return
	 */
	public boolean isComposite() {
		boolean codeF = codeFormula != null && !codeFormula.isEmpty();
		boolean labelF = labelFormula != null && !labelFormula.isEmpty();
		return codeF || labelF;
	}
	
	/**
	 * Get the id which identifies the column
	 * @return
	 */
	public String getId() {
		return id;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	/**
	 * Get the name of the column which
	 * is shown to the user in the table
	 * @return
	 */
	public String getLabel() {
		return label;
	}
	
	public String getXmlTag() {
		return xmlTag;
	}
	
	public String getEditableFormula() {
		return editable;
	}
	
	/**
	 * Can the user edit the value?
	 * @return
	 */
	public boolean isEditable(TableSchema schema, Collection<TableRow> parents) {
		
		// no parents => no formula to be solved
		if (parents.isEmpty())
			return BooleanValue.isTrue(editable);
		
		TableRow row = new TableRow(schema);
		
		// add parents
		for (TableRow parent : parents)
			Relation.injectParent(parent, row);
		
		return isTrue(row, XlsxHeader.EDITABLE.getHeaderName());
	}
	
	/**
	 * Can the user edit the value?
	 * @return
	 */
	public boolean isEditable(TableRow row) {
		
		return isTrue(row, XlsxHeader.EDITABLE.getHeaderName());
	}
	
	/**
	 * Check if the column is part of a natural key
	 * @return
	 */
	public boolean isNaturalKey() {
		return BooleanValue.isTrue(naturalKey);
	}
	
	/**
	 * Solve a formula for a specific field using the row values
	 * @param row
	 * @param headerName
	 * @return
	 * @throws FormulaException 
	 */
	private String solveFormula(TableRow row, String headerName) throws FormulaException {
		
		FormulaSolver solver = new FormulaSolver(row);
		Formula formula = solver.solve(this, headerName);

		// check the solved formula
		return formula.getSolvedFormula();
	}
	
	/**
	 * Check if a field is true after solving the formula
	 * @param row
	 * @param headerName
	 * @return
	 */
	private boolean isTrue(TableRow row, String headerName) {
		
		String solvedFormula;
		try {
			solvedFormula = solveFormula(row, headerName);
			return BooleanValue.isTrue(solvedFormula);
		} catch (FormulaException e) {
			LOGGER.error("Cannot solve formula", e);
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * Get the type of the field
	 * @return
	 */
	public ColumnType getType() {
		return type;
	}
	
	/**
	 * Set the picklist key
	 * @param listKey
	 */
	public void setPicklistKey(String listKey) {
		this.picklistKey = listKey;
	}
	
	public void setPicklistFilter(String picklistFilter) {
		this.picklistFilter = picklistFilter;
	}
	
	/**
	 * Get the picklist filter
	 * @param row row to evaluate the formula of the filter if required
	 * @return
	 */
	public String getPicklistFilter(TableRow row) {
		
		try {
			return solveFormula(row, XlsxHeader.PICKLIST_FILTER.getHeaderName());
		} catch (FormulaException e) {
			LOGGER.error("Cannot solve formula for picklist filter", e);
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String getCodeFormula() {
		return codeFormula;
	}
	
	public String getLabelFormula() {
		return labelFormula;
	}
	
	/**
	 * Get the standard filter
	 * @return
	 */
	public String getPicklistFilterFormula() {
		return picklistFilter;
	}
	
	/**
	 * Get the key of the list related to the column
	 * It works only with {@link ColumnType#PICKLIST} type.
	 * @return
	 */
	public String getPicklistKey() {
		return picklistKey;
	}
	
	public boolean isPicklist() {
		return type == ColumnType.PICKLIST;
	}
	public boolean isForeignKey() {
		return type == ColumnType.FOREIGNKEY;
	}
	public boolean isPassword() {
		return type == ColumnType.PASSWORD;
	}
	
	/**
	 * Get a list of the available {@link Selection} which
	 * can be set as value for the current column
	 * @param listId
	 * @return
	 */
	public SelectionList getList(TableRow row) {
		
		if (picklistKey == null)
			return null;

		XmlContents contents = XmlLoader.getByPicklistKey(picklistKey);

		if (contents == null) {
			LOGGER.error("Cannot get picklist " + picklistKey + ". The file was not found");
			return null;
		}
		
		// if no filter, then we have a single list,
		// and we get just the first list
		if (picklistFilter == null || picklistFilter.isEmpty()) {
			return contents.getElements().iterator().next();
		}
		
		// otherwise if we have a filter we get the list
		// related to that filter
		SelectionList list = contents.getListById(getPicklistFilter(row));
		
		
		return list;
	}
	/**
	 * Should the column be visualized in the table?
	 * @return
	 */
	public boolean isVisible(TableRow row) {
		
		return isTrue(row, XlsxHeader.VISIBLE.getHeaderName());
	}
	
	public String getVisibleFormula() {
		return visible;
	}
	
	/**
	 * Should the column be visualized in the table?
	 * @return
	 */
	public boolean isVisible(TableSchema schema, Collection<TableRow> parents) {
		
		// no parents => no formula to be solved
		if (parents.isEmpty())
			return BooleanValue.isTrue(visible); 
		
		TableRow row = new TableRow(schema);
		
		// add parents
		for (TableRow parent : parents)
			Relation.injectParent(parent, row);
		
		return isTrue(row, XlsxHeader.VISIBLE.getHeaderName());
	}
	
	/**
	 * Check if the column is mandatory or not
	 * @return
	 */
	public boolean isMandatory(TableRow row) {
		
		// if no data are provided, just check simple field
		if (row == null)
			return BooleanValue.isTrue(mandatory);
		
		// non visible fields are not mandatory,
		// in the sense that they are automatically
		// computed, therefore they are for sure
		// inserted
		if (!this.isVisible(row)) {
			return false;
		}
		
		return isTrue(row, XlsxHeader.MANDATORY.getHeaderName());
	}
	
	/**
	 * Check the mandatory fields without evaluating formulas
	 * @return
	 */
	public boolean isMandatory() {
		return this.isMandatory(null);
	}
	/**
	 * Check if the mandatory field is a formula or not
	 * @return
	 */
	public boolean isConditionallyMandatory() {
		
		boolean isTrue = BooleanValue.isTrue(mandatory);
		boolean isFalse = BooleanValue.isFalse(mandatory);
		
		// if neither true nor false
		return !isTrue && !isFalse;
	}

	public String getMandatoryFormula() {
		return mandatory;
	}
	public void setMandatory(String mandatory) {
		this.mandatory = mandatory;
	}
	
	/**
	 * Check if the column should be put in the output or not
	 * @param row
	 * @return
	 */
	public boolean isPutInOutput(TableRow row) {
		
		return xmlTag != null && !xmlTag.replaceAll(" ", "").isEmpty() 
				&& isTrue(row, XlsxHeader.PUT_IN_OUTPUT.getHeaderName());
	}
	
	public String getPutInOutput() {
		return putInOutput;
	}
	
	public String getCode() {
		return code;
	}
	public String getTip() {
		return tip;
	}
	
	public String getDefaultCode() {
		return defaultCode;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}

	public int getOrder() {
		return order;
	}
	
	@Override
	public boolean equals(Object arg0) {

		// if same key
		if (arg0 instanceof String) {
			return id.equals((String) arg0);
		}
		
		if (arg0 instanceof TableColumn) {
			return id.equals(((TableColumn)arg0).getId());
		}
		
		return super.equals(arg0);
	}
	@Override
	public String toString() {
		return "Column: id=" + id + ";label=" + label;
	}

	@Override
	public int compareTo(TableColumn column) {
		if (this.order == column.getOrder())
			return 0;
		else if (this.order < column.getOrder())
			return -1;
		else
			return 1;
	}
}