package providers;

import formula.FormulaException;
import table_skeleton.TableColumn;
import table_skeleton.TableRow;
import xlsx_reader.TableHeaders.XlsxHeader;

public interface IFormulaService {
	
	/**
	 * Solve a formula of a row regarding a specific column field (e.g. codeFormula,
	 * labelFormula, mandatory...)
	 * @param row
	 * @param column
	 * @param columnProperty
	 * @return
	 */
	public String solve(TableRow row, TableColumn column, XlsxHeader columnProperty) throws FormulaException;
	
	/**
	 * Update all the code and label formulas of the row
	 * @param row
	 */
	public void updateFormulas(TableRow row);
	
	/**
	 * Initialize the row with the default values
	 * note that this will override all the values of the row
	 * with their default values!
	 */
	public void initialize(TableRow row);
	
	/**
	 * Initialize the values of the row solving the
	 * default values formulas
	 * @param row
	 * @param colId
	 */
	public void initialize(TableRow row, String colId);
}
