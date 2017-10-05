package formula;

import table_skeleton.TableRow;

public interface IFormula {
	
	/**
	 * Get the unresolved formula
	 * @return
	 */
	public String getUnsolvedFormula();
	
	/**
	 * Compile the formula to extract the important pieces of information
	 */
	public void compile() throws FormulaException;
	
	/**
	 * Solve the formula using the row information
	 * @param row
	 * @return the solved formula
	 */
	public String solve(TableRow row) throws FormulaException;
	
	/**
	 * Solve the formula
	 * @return
	 */
	public String solve() throws FormulaException;
}
