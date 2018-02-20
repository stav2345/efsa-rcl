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
}
