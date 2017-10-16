package table_skeleton;

import java.util.Collection;
import java.util.HashMap;

import duplicates_detector.Checkable;
import formula.Formula;
import formula.FormulaException;
import formula.FormulaSolver;
import table_database.TableDao;
import xlsx_reader.TableHeaders.XlsxHeader;
import xlsx_reader.TableSchema;
import xml_catalog_reader.Selection;
import xml_catalog_reader.XmlLoader;

/**
 * Generic element of a {@link Report}.
 * @author avonva
 *
 */
public class TableRow implements Checkable {

	public enum RowStatus {
		OK,
		MANDATORY_MISSING
	};

	private HashMap<String, TableColumnValue> values;
	private TableSchema schema;
	
	/**
	 * Careful use
	 */
	public TableRow() {
		this.values = new HashMap<>();
	}
	
	/**
	 * Careful use
	 */
	public void setSchema(TableSchema schema) {
		this.schema = schema;
	}
	
	/**
	 * Careful use
	 */
	public TableRow(HashMap<String, TableColumnValue> values, TableSchema schema) {
		this.schema = schema;
		this.values = values;
	}
	
	/**
	 * Create a report row
	 * @param schema columns properties of the row
	 */
	public TableRow(TableSchema schema) {
		this.values = new HashMap<>();
		this.schema = schema;
	}
	
	/**
	 * Create a clone of a row
	 * @param row
	 */
	public TableRow(TableRow row) {
		this.values = new HashMap<>(row.values);
		this.schema = row.getSchema();
	}
	
	/**
	 * Initialize a row with an element already inserted in it
	 * @param schema
	 * @param initialColumnId
	 * @param initialValue
	 */
	public TableRow(TableSchema schema, String initialColumnId, TableColumnValue initialValue) {
		this(schema);
		this.put(initialColumnId, initialValue);
	}
	
	/**
	 * Set the database id
	 * @param id
	 */
	public void setId(int id) {
		
		String index = String.valueOf(id);
		TableColumnValue idValue = new TableColumnValue();
		idValue.setCode(index);
		idValue.setLabel(index);
		
		// put directly into the values and not with this.put
		// to avoid to put the id into the changes hashmap
		this.values.put(schema.getTableIdField(), idValue);
	}
	
	/**
	 * Get the database id if present
	 * otherwise return -1
	 * @return
	 */
	public int getId() {
		
		int id = -1;
		
		try {
			
			TableColumnValue value = this.values.get(schema.getTableIdField());
			
			if (value != null && value.getCode() != null)
				id = Integer.valueOf(value.getCode());
			
		} catch (NumberFormatException e) {}
		
		return id;
	}
	
	/**
	 * Get the parent of this table row
	 * @param parentSchema schema of the parent (also identifies the type of
	 * parent required)
	 * @return
	 */
	public TableRow getParent(TableSchema parentSchema) {
		
		// open the child dao
		TableDao dao = new TableDao(parentSchema);

		// get parent using the id contained in the row
		TableRow parent = dao.getById(this.getNumLabel(parentSchema.getTableIdField()));
		
		return parent;
	}
	
	/**
	 * Get the rows defined in the child table that are related to
	 * this parent row.
	 * @param childSchema the schema of the child table
	 * @return
	 */
	public Collection<TableRow> getChildren(TableSchema childSchema) {
		
		// open the child dao
		TableDao dao = new TableDao(childSchema);
		
		// get parent table name using the relation
		String parentTable = this.getSchema().getSheetName();
		
		// get the rows of the children related to the parent
		Collection<TableRow> children = dao.getByParentId(parentTable, this.getId());
		
		return children;
	}
	
	
	/**
	 * Get a string variable value from the data
	 * @param key
	 * @return
	 */
	public TableColumnValue get(String key) {
		return values.get(key);
	}
	
	/**
	 * Get the label of an element of the row
	 * @param row
	 * @param field
	 * @return
	 */
	public String getLabel(String field) {
		return getField(field, true);
	}
	
	/**
	 * Get the label in numeric format
	 * @param field
	 * @return
	 */
	public int getNumLabel(String field) {
		return getNumField(field, true);
	}
	
	public int getNumCode(String field) {
		return getNumField(field, false);
	}
	
	public int getNumField(String field, boolean label) {
		
		String value = getField(field, label);
		
		int numValue = Integer.MIN_VALUE;
		try {
			numValue = Integer.valueOf(value);
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		return numValue;
	}
	
	/**
	 * Get the code of an element of the row
	 * @param row
	 * @param field
	 * @return
	 */
	public String getCode(String field) {
		return getField(field, false);
	}
	
	private String getField(String field, boolean label) {
		
		TableColumnValue value = this.get(field);
		
		if (value == null || value.isEmpty())
			return "";
		
		if (label)
			return value.getLabel();
		else
			return value.getCode();
	}
	
	/**
	 * Put a selection into the data
	 * @param key
	 * @param value
	 */
	public void put(String key, TableColumnValue value) {
		values.put(key, value);
	}
	
	/**
	 * Put a string into the data, only for raw columns not picklists
	 * use {@link #put(String, Selection)} for picklists
	 * @param key
	 * @param label
	 */
	public void put(String key, String label) {
		
		if (schema != null && schema.getById(key) != null && schema.getById(key).isPicklist()) {
			System.err.println("Wrong use of ReportRow.put(String,String), "
					+ "use Report.put(String,Selection) instead for picklist columns");
			return;
		}
		
		TableColumnValue row = new TableColumnValue();
		row.setCode(label);
		row.setLabel(label);
		this.put(key, row);
	}
	
	/**
	 * Initialize the row with the default values
	 * note that this will override all the values of the row
	 * with their default values!
	 */
	public void initialize() {
		
		// create a slot for each column of the table
		for (TableColumn col : schema) {

			// skip foreign keys
			if (col.isForeignKey())
				continue;

			TableColumnValue sel = new TableColumnValue();

			FormulaSolver solver = new FormulaSolver(this);
			Formula code = null;
			try {
				code = solver.solve(col, XlsxHeader.DEFAULT_CODE.getHeaderName());
				sel.setCode(code.getSolvedFormula());
			} catch (FormulaException e) {
				e.printStackTrace();
			}
			
			Formula label = null;
			try {
				label = solver.solve(col, XlsxHeader.DEFAULT_VALUE.getHeaderName());
				sel.setLabel(label.getSolvedFormula());
			} catch (FormulaException e) {
				e.printStackTrace();
			}

			this.put(col.getId(), sel);
		}
	}

	/**
	 * Update the code or the value of the row
	 * using a solved formula
	 * @param f
	 * @param fieldHeader
	 */
	public void update(Formula f, String fieldHeader) {
		
		// skip editable columns
		if (f.getColumn().isEditable())
			return;
		
		XlsxHeader h = XlsxHeader.fromString(fieldHeader);
		
		if (h == null)
			return;
		
		TableColumnValue colVal = this.get(f.getColumn().getId());

		if (h == XlsxHeader.CODE_FORMULA && !f.getSolvedFormula().isEmpty()) {
			colVal.setCode(f.getSolvedFormula());
		}
		else if (h == XlsxHeader.LABEL_FORMULA && !f.getSolvedFormula().isEmpty()) {
			colVal.setLabel(f.getSolvedFormula());
			
			// if the field has not a code formula and it is not a picklist, then
			// update the code using the label
			if (!f.getColumn().isPicklist() && f.getColumn().getCodeFormula().isEmpty())
				colVal.setCode(f.getSolvedFormula());
		}
		else // else do nothing
			return;

		this.put(f.getColumn().getId(), colVal);
	}
	
	/**
	 * Update the values of the rows applying the columns formulas
	 * (Compute all the automatic values)
	 */
	public void updateFormulas() {
		
		// solve the formula for default code and default value
		FormulaSolver solver = new FormulaSolver(this);
		
		// note that this automatically updates the row
		// while solving formulas
		try {
			solver.solveAll(XlsxHeader.CODE_FORMULA.getHeaderName());
		} catch (FormulaException e) {
			e.printStackTrace();
		}
		
		try {
			solver.solveAll(XlsxHeader.LABEL_FORMULA.getHeaderName());
		} catch (FormulaException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Save the current row into the database
	 * (this is an insert operation! Multiple calls
	 * create multiple rows). Return the new id
	 * of the database
	 */
	public int save() {
		
		TableDao dao = new TableDao(this.schema);
		int id = dao.add(this);
		this.setId(id);
		
		return id;
	}
	
	/**
	 * Update the row in the database
	 */
	public void update() {
		TableDao dao = new TableDao(this.schema);
		dao.update(this);
	}
	
	/**
	 * Delete permanently the row from the database
	 */
	public void delete() {
		TableDao dao = new TableDao(this.schema);
		dao.delete(this.getId());
	}

	/**
	 * Get the schema of the row
	 * @return
	 */
	public TableSchema getSchema() {
		return schema;
	}
	
	/**
	 * Get the status of the row
	 * @return
	 */
	public RowStatus getRowStatus() {
		
		RowStatus status = RowStatus.OK;
		
		if (!areMandatoryFilled())
			status = RowStatus.MANDATORY_MISSING;
		
		return status;
	}
	
	public TableColumnValue getTableColumnValue(String code, String picklistKey) {
		
		Selection sel = XmlLoader.getByPicklistKey(picklistKey).getElementByCode(code);
		
		if (sel == null) {
			System.err.println("Cannot pick the value " + code + " from list " + picklistKey 
					+ ". Either the list or the element do not exist. Empty element returned instead.");
			return new TableColumnValue();
		}
		
		return new TableColumnValue(sel);
	}
	
	/**
	 * Check if all the mandatory fields are filled
	 * @return
	 */
	public boolean areMandatoryFilled() {
		
		for (TableColumn column : schema) {
			
			if (column.isMandatory(this)) {
				
				TableColumnValue value = this.get(column.getId());
				
				if (value.isEmpty())
					return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Check if equal
	 */
	public boolean sameAs(Checkable arg0) {
		
		if (!(arg0 instanceof TableRow))
			return false;
		
		TableRow other = (TableRow) arg0;
		
		// cannot compare rows with different schema
		if (!this.schema.equals(other.schema))
			return false;
		
		// for each column of the row
		for (String key : this.values.keySet()) {
			
			// skip the id of the table since we do
			// not have a column for that in the schema
			if (key.equals(schema.getTableIdField()))
				continue;
			
			// get the current column object
			TableColumn col = this.schema.getById(key);
			
			// continue searching if we have not a natural key field
			if (!col.isNaturalKey())
				continue;
			
			// here we are comparing a part of the natural key
			TableColumnValue value1 = this.get(key);
			TableColumnValue value2 = other.get(key);
			
			// cannot compare two empty values (it would return
			// equal but actually they simply have a missing value)
			if (value1.isEmpty() && value2.isEmpty())
				continue;
			
			// if a field of the natural key is
			// different then the two rows are different
			if (!value1.equals(value2))
				return false;
		}
		
		// if we have arrived here, all the natural
		// keys are equal, therefore we have the same row
		return true;
	}
	
	/**
	 * Convert the row into an xml
	 * @return
	 */
	public String toXml(boolean addRoot) {
		
		StringBuilder sb = new StringBuilder();
		
		for (TableColumn column : schema) {
			
			String rowValue = this.getCode(column.getId());

			// skip non output columns and non mandatory fields which are empty
			if (!column.isPutInOutput(this) || (!column.isMandatory(this) && rowValue.isEmpty()))
				continue;

			String node = getXmlNode(column.getXmlTag(), this.get(column.getId()).getCode());

			// write the node
			sb.append(node);
		}
		
		// if add root return the wrapped version
		if (addRoot) {
			return getXmlNode("result", sb.toString());
		}
		
		return sb.toString();
	}
	
	/**
	 * Create a single xml node with the text content
	 * @param nodeName
	 * @param textContent
	 * @return
	 */
	private String getXmlNode(String nodeName, String textContent) {
		
		StringBuilder node = new StringBuilder();
		
		// create the node
		node.append("<")
			.append(nodeName)
			.append(">")
			.append(textContent)
			.append("</")
			.append(nodeName)
			.append(">");
		
		return node.toString();
	}
	
	@Override
	public String toString() {
		
		StringBuilder print = new StringBuilder();
		
		print.append("ID: " + getId() + "\n");
		
		for (String key : this.values.keySet()) {
			
			print.append("Column: " + key);

			print.append(" code=" + values.get(key).getCode());
			
			print.append(";value=" + values.get(key).getLabel());

			print.append("\n");
		}
		return print.toString();
	}
}
