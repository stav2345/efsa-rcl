package providers;

import table_skeleton.TableRow;

public interface IFormulaService {
	
	/**
	 * Update all the code and label formulas of the row
	 * @param row
	 */
	public void updateFormulas(TableRow row);
}
