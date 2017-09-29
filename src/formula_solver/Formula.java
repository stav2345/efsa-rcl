package formula_solver;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app_config.AppPaths;
import app_config.BooleanValue;
import app_config.PropertiesReader;
import table_relations.Relation;
import table_skeleton.TableColumn;
import table_skeleton.TableColumnValue;
import table_skeleton.TableRow;
import xml_catalog_reader.Selection;
import xml_catalog_reader.XmlContents;
import xml_catalog_reader.XmlLoader;

/**
 * Class which models a generic formula which can be inserted in the
 * .xlsx configuration files for creating ReportTable.
 * The formula can also be solved by invoking {@link #solve()}
 * @author avonva
 *
 */
public class Formula {
	
	private static HashMap<Cell, Integer> dependenciesCache;
	
	// regex components
	private static final String NUMBER = "[0-9]{1,13}(\\.[0-9]*)?";
	private static final String INTEGER = "[0-9]+";
	
	private String formula;
	private String solvedFormula;
	private String fieldHeader;
	private TableRow row;
	private TableColumn column;
	private int dependenciesCount;
	
	//private long debugTime;
	
	public Formula(TableRow row, TableColumn column, String fieldHeader) {
		
		if (dependenciesCache == null)
			dependenciesCache = new HashMap<>();
		
		this.row = row;
		this.column = column;
		this.fieldHeader = fieldHeader;
		this.formula = column.getFieldByHeader(fieldHeader);
		this.dependenciesCount = 0;
		evalDependencies();
	}

	/**
	 * Get the number of dependencies in terms of \columnname.field
	 * @return
	 */
	public int getDependenciesCount() {
		return dependenciesCount;
	}
	
	public TableRow getRow() {
		return row;
	}
	
	public TableColumn getColumn() {
		return column;
	}
	
	/**
	 * Get the solved formula. Can be used only after calling
	 * {@link #solve()}, otherwise it returns null.
	 * @return
	 */
	public String getSolvedFormula() {
		return solvedFormula;
	}
	
	public String getFormula() {
		return formula;
	}
	
	/**
	 * Solve the formula. Returns null if the field should be ignored
	 * @return
	 */
	public String solve() {
		
		if (formula == null || formula.isEmpty())
			return "";
		
		String value = formula;
		
		// solve dates
		value = solveDateFormula(value);
		
		print(value, "DATE");
		
		// solve special characters
		value = solveKeywords(value);
		
		print(value, "KEYWORDS");
		
		// solve columns values
		value = solveColumnsFormula(value);

		print(value, "COLUMNS");
		
		// solve relations formulas
		value = solveRelationFormula(value);
		
		print(value, "RELATIONS");
		
		// solve additions
		value = solveAdditions(value);
		
		print(value, "SUM");
		
		// solve if not null
		value = solveConditions(value);
		
		print(value, "CONDITIONS");
		
		// solve the if statements
		value = solveIf(value);
		
		print(value, "IF");
		
		// solve logical comparisons
		value = solveLogicalOperators(value);
		
		print(value, "LOGIC");
		
		// solve padding
		value = solvePadding(value);
		
		print(value, "PADDING");
		
		// solve trims
		value = solveTrims(value);
		
		print(value, "TRIMS");

		this.solvedFormula = value.trim();
		
		return solvedFormula;
	}
	
	private void print(String value, String header) {
		
		//if ((column.equals("progInfo")||column.equals("totSamplesTested")) && fieldHeader.equals("labelFormula"))
		//	System.out.println("column " + column + " " + header + " => " + value);
		//System.out.println("TIME for " + header + " => " 
		//		+ (System.currentTimeMillis() - debugTime)/1000.00 + " seconds");
		
		// update debug time
		//debugTime = System.currentTimeMillis();
	}
	
	private String solveTrims(String value) {
		
		String command = value;
		
		String pattern = "END_TRIM\\s*\\(\\s*.*?\\s*,\\s*" + INTEGER + "\\s*\\)";

		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(value);
		
		while (m.find()) {
			
			String match = m.group();
			
			// get match
			String elements = match.replace("END_TRIM\\s*(", "");
			
			String[] split = elements.split(",");
			
			if (split.length != 2) {
				System.err.println("Wrong END_TRIM formula " + match);
				return command;
			}
			
			String string = split[0].trim();
			String charNumStr = split[1].replace(")", "").trim();
			
			try {
				
				int charNum = Integer.valueOf(charNumStr);
				
				if (charNum > string.length()) {
					charNum = string.length();
				}
				
				String replacement = string.substring(string.length() - charNum, string.length());
				
				command = command.replace(match, replacement);
			}
			catch (NumberFormatException e) {
				return command;
			}
		}
		
		return command;
	}
	
	private String solvePadding(String value) {
		
		String command = value;
		
		String pattern = "ZERO_PADDING\\(.*?," + INTEGER + "\\)";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(value);
		
		while (m.find()) {
			
			String match = m.group();
			
			// get match
			String elements = match.replace("ZERO_PADDING(", "");
			
			String[] split = elements.split(",");
			
			if (split.length != 2) {
				System.err.println("Wrong ZERO_PADDING formula " + match);
				return command;
			}
			
			String string = split[0].trim();
			String charNumStr = split[1].replace(")", "").trim();
			
			try {
				
				int charNum = Integer.valueOf(charNumStr);
				
				// make zero padding if necessary
				if (charNum > string.length()) {
					
					int paddingCount = charNum - string.length();
					for(int i = 0; i < paddingCount; ++i) {
						string = "0" + string;
					}
				}
				
				command = command.replace(match, string);
			}
			catch (NumberFormatException e) {
				return command;
			}
		}
		
		return command;
	}
	
	/**
	 * Solve logical operations
	 * @param value
	 * @return
	 */
	private String solveConditions(String value) {
		
		String command = value;
		
		String pattern = "IF_NOT_NULL\\(.*?,.*?,.*?\\)";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(value);
		
		// if found
		while (m.find()) {

			String match = m.group();
			
			// get match
			String elements = match.replace("IF_NOT_NULL(", "");
			
			String[] split = elements.split(",");
			
			if (split.length != 3) {
				System.err.println("Wrong IF_NOT_NULL formula " + match);
				return value;
			}

			String condition = split[0].trim();
			String trueCond = split[1].trim();
			String falseCond = split[2].replace(")", "").trim();
			
			// if we have a not null value
			if(!condition.isEmpty())
				command = command.replace(match, trueCond);
			else
				command = command.replace(match, falseCond);
		}
		
		return command;
	}
	
	/**
	 * Solve if statements
	 * @param value
	 * @return
	 */
	private String solveIf(String value) {
		
		String command = value;
		
		String pattern = "IF\\(.+?,.*?,.*?\\)";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(value);
		
		// if found
		while(m.find()) {

			String match = m.group();
			
			// get match
			String elements = match.replace("IF(", "");
			
			String[] split = elements.split(",");
			
			if (split.length != 3) {
				System.err.println("Wrong IF formula " + match);
				return value;
			}

			String condition = split[0].trim();
			String trueCond = split[1].trim();
			String falseCond = split[2].replace(")", "").trim();
			
			// if we have a true value
			if(BooleanValue.isTrue(condition))
				command = command.replace(match, trueCond);
			else
				command = command.replace(match, falseCond);
		}
		
		return command;
	}
	
	/**
	 * Solve additions. Syntax SUM(x,y,z,....)
	 * @param value
	 * @return
	 */
	private String solveAdditions(String value) {
		
		String result = value;
		
		String pattern = "SUM\\("  + NUMBER + "," + NUMBER + "(," + NUMBER + ")*\\)";
		
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(value);
		
		// if there is a sum
		while (m.find()) {
			
			String elements = m.group().replace("SUM(", "").replace(")", "");
			StringTokenizer st = new StringTokenizer(elements, ",");
			
			// compute the sum
			double sum = 0;
			while(st.hasMoreTokens()) {
				String next = st.nextToken().trim();
				sum = sum + Double.valueOf(next);
			}
			
			// cast to integer if it is an integer
			if ((sum == Math.floor(sum)) && !Double.isInfinite(sum)) {
				// convert result to string
				result = String.valueOf((int)sum);
			}
			else {
				result = String.valueOf(sum);
			}
		}
		
		return result;
	}
	
	/**
	 * Resolve all columns dependencies ($column_name.(code|label))
	 * with the columns values
	 * @param value
	 * @return
	 * @throws IOException 
	 */
	private String solveColumnsFormula(String value) {
		
		String command = value;
		String pattern = "\\$\\w+?\\.(code|label)";
		Pattern p = Pattern.compile(pattern);

		Matcher m = p.matcher(command);
		
		// for each match
		while (m.find()) {
			
			String match = m.group();
			String[] split = match.split("\\.");
			
			// get column id and required field
			if (split.length != 2) {
				System.err.println("Wrong column formula, found " + match);
				return command;
			}
			
			String colId = split[0].replace("$", "");
			String field = split[1];  // required field
			
			// get the row value for the required column
			TableColumnValue colValue = this.row.get(colId);
			TableColumn col = this.row.getSchema().getById(colId);
			
			if (col == null) {
				System.err.println("No column found for " + colId);
				return command;
			}
			
			String replacement = null;
			
			// if code get code
			if (field.equals("code")) {
				if (colValue != null)
					replacement = colValue.getCode();
				else
					replacement = col.getCodeFormula();
			}
			else {
				// otherwise use value
				if (colValue != null)
					replacement = colValue.getLabel();
				else
					replacement = col.getLabelFormula();
			}

			if (replacement == null)
				replacement = "";
			
			// replace value
			command = command.replace(match, replacement);
		}

		return command;
	}
	
	/**
	 * Solve all the RELATION(parent, field) statements
	 * @param value
	 * @return
	 */
	private String solveRelationFormula(String value) {
		
		String command = value;
		
		Pattern p = Pattern.compile("RELATION\\{.+?,.+?\\}");
		Matcher m = p.matcher(command);
		
		// found a relation keyword
		while (m.find()) {
			
			// remove useless pieces
			String hit = m.group();
			
			String match = hit.replace("RELATION{", "").replace("}", "");
			
			// get operands by splitting with comma
			String[] split = match.split(",");
			
			if (split.length != 2) {
				System.err.println("Wrong RELATION statement, found " + hit);
				continue;
			}
			
			String parentId = split[0];
			String field = split[1];
			
			// field is name.code or name.label
			split = field.split("\\.");
			
			if (split.length != 2) {
				System.err.println("Need .code or .label, found " + field + " in the match " + hit);
				continue;
			}
			
			// get the field name of the parent
			String fieldName = split[0].replace(" ", "");
			
			// get the relation with the parent
			Relation r = row.getSchema().getRelationByParentTable(parentId);
			
			if (r == null) {
				System.err.println("No such relation found in the " 
						+ AppPaths.RELATIONS_SHEET 
						+ ". Relation required: " 
						+ parentId);
				continue;
			}
			
			TableColumnValue colVal = row.get(r.getForeignKey());
			
			if (colVal == null) {
				System.err.println("No parent data found for " + r + " in the row " + row);
				System.err.println("Involved code " + match + " of the complete formula: " + command);
				continue;
			}
			
			// get from the child row the foreign key for the parent
			String foreignKey = row.get(r.getForeignKey()).getCode();
			
			if (foreignKey == null || foreignKey.isEmpty())
				continue;

			// get the row using the foreign key
			TableRow row = r.getParentValue(Integer.valueOf(foreignKey));
			
			if (row == null) {
				System.err.println("No relation value found for " + foreignKey + "; relation " + r);
				continue;
			}
			
			// get the required field and put it into the formula
			TableColumnValue parentValue = row.get(fieldName);
			
			if (parentValue == null) {
				System.err.println("No parent data value found for " + fieldName);
				continue;
			}

			// apply keywords
			match = match.replace(fieldName + ".code", parentValue.getCode());
			match = match.replace(fieldName + ".label", parentValue.getLabel());
			
			// remove also useless part
			match = match.replace(parentId + ",", "");
			
			// replace in the final string
			command = command.replace(hit, match);
		}

		return command;
	}
	
	/**
	 * Solve the logical operators
	 * @param value
	 * @return
	 */
	private String solveLogicalOperators(String value) {
		
		String command = solveLogicalOp(value, "==");
		command = solveLogicalOp(command, "!=");
		
		return command;
	}
	
	private String solveLogicalOp(String value, String op) {
		
		String command = value;
		
		String operand = "(.+?)";
		String pattern = "\\(" + operand + "\\s*" + op + "\\s*" + operand + "\\)";
		
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(value);
		
		while (m.find()) {

			// extract operands from the match
			String match = m.group();
			
			String[] split = match.split(op);
			String leftOp = split[0].trim().replace("(", "");
			String rightOp = split[1].trim().replace(")", "");;
			
			boolean comparison = leftOp.equalsIgnoreCase(rightOp);
			
			// invert the boolean if we are searching
			// for differences
			if (op.equals("!="))
				comparison = !comparison;
			
			// set the correct value
			String result = comparison ? BooleanValue.getTrueValue() : BooleanValue.getFalseValue();

			// replace match with the logical result
			command = value.replace(match, result);
		}

		return command;
	}
	
	/**
	 * Solve all the dates keywords of the formula
	 * @param value
	 * @return
	 */
	private String solveDateFormula(String value) {

		String command = value;
		
		Date today = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		
		// add the today timestamp if needed
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String todayTs = sdf.format(cal.getTime());
		command = command.replace("today.timestamp", todayTs.replace(" ", ""));
		
		// get last month
		cal.add(Calendar.MONTH, -1);
		
		String lastYear = String.valueOf(cal.get(Calendar.YEAR));

		command = command.replace("lastMonth.year.code", lastYear);
		command = command.replace("lastMonth.year.label", lastYear);
		
		// get last month term
		// months start from 0 so to show the correct number
		// we add 1
		String lastMonth = String.valueOf(cal.get(Calendar.MONTH) + 1);
		
		// pick the months list
		XmlContents xml = XmlLoader.getByPicklistKey(AppPaths.MONTHS_LIST);
		
		if (xml == null) {
			System.err.println("Cannot resolve formula lastMonth.code/label, since the " 
					+ AppPaths.MONTHS_LIST + " was not found!");
			return command;
		}
		
		Selection monthSel = xml.getList().getSelectionByCode(lastMonth);

		command = command.replace("lastMonth.code", monthSel.getCode());
		command = command.replace("lastMonth.label", monthSel.getDescription());
		
		return command;
	}

	/**
	 * Resolve concatenation keywords
	 * @param value
	 * @return
	 */
	private String solveKeywords(String value) {

		String result = value.replace("|", "").replace("null", "");
		
		// replace row id statement with the actual row id
		result = result.replace("{rowId}", String.valueOf(row.getId()));

		// app keywords
		result = result.replace("{app.name}", PropertiesReader.getAppName());
		result = result.replace("{app.version}", PropertiesReader.getAppVersion());
		result = result.replace("{app.dcCode}", PropertiesReader.getDataCollectionCode());

		// concatenation keyword
		return result;
	}
	
	@Override
	public String toString() {
		return "Column " + column.getId() + " formula " + formula + " solved " + solvedFormula;
	}
	
	/**
	 * Check if a column has a dependency with another
	 * column of the same table
	 * @param col
	 * @param value
	 * @return
	 */
	private int isDependentBy(TableColumn col, String value) {
		
		if (value == null)
			return 0;
		
		Pattern p = Pattern.compile("\\$" + col.getId() + "\\.(code|label)");
		Matcher m = p.matcher(value);
		
		int counter = 0;
		
		while(m.find())
			counter++;
		
		return counter;
	}
	
	/**
	 * Check dependencies in a recursive manner. This is actually the
	 * computation of the level of the tree of the dependencies.
	 * A column is dependent on the value of another column
	 * if it has in the field a formula with \columnName.code or
	 * \columnName.label
	 */
	private void evalDependencies() {
		
		Cell cell = new Cell(row.getSchema().getSheetName(), column.getId(), fieldHeader);
		Integer cacheDep = dependenciesCache.get(cell);
		
		// use cache if possible
		if (cacheDep != null) {
			this.dependenciesCount = cacheDep;
			return;
		}
		
		int dependencies = 0;
		
		// Check columns dependencies
		for (TableColumn col : row.getSchema()) {
			
			// evaluate the dependency just for different columns
			// this avoid recursive definitions
			if (!col.equals(column)) {
				
				int numOfDep = isDependentBy(col, formula);
				
				// add number of occurrences as dependencies
				dependencies = dependencies + numOfDep;

				// if we have dependencies (i.e. we found a $column...)
				// also evaluate the column to check if there are nested
				// dependencies
				if (numOfDep > 0) {
					
					// evaluate column dependencies recursively
					Formula child = new Formula(row, col, fieldHeader);
					
					// also add the column dependencies to the current
					// number of dependencies
					dependencies = dependencies + child.getDependenciesCount();
				}
			}
		}
		
		this.dependenciesCount = dependencies;
		
		// save cache
		dependenciesCache.put(cell, dependencies);
	}
	
	private class Cell {
		private String tableName;
		private String columnId;
		private String columnHeader;
		
		public Cell(String tableName, String columnId, String columnHeader) {
			this.tableName = tableName;
			this.columnId = columnId;
			this.columnHeader = columnHeader;
		}
		
		@Override
		public boolean equals(Object obj) {
			
			Cell cell = (Cell) obj;
			
			return tableName.equals(cell.tableName)
					&& columnId.equals(cell.columnId)
					&& columnHeader.equals(cell.columnHeader);
		}
	}
}
