package formula;

import table_skeleton.TableRow;

/**
 * Solve keywords related to a {@link TableRow}
 * @author avonva
 *
 */
public class RowKeywordFormula implements IFormula {

	public static final String ROW_ID_KEYWORD = "{rowId}";
	
	private String formula;
	
	public RowKeywordFormula(String formula) throws FormulaException {
		this.formula = formula;
	}
	
	@Override
	public String getUnsolvedFormula() {
		return formula;
	}

	@Override
	public void compile() throws FormulaException {}

	@Override
	public String solve(TableRow row) throws FormulaException {
		
		String solvedFormula = null;
		
		switch(formula) {
		case ROW_ID_KEYWORD:  // replace row id statement with the actual row id
			solvedFormula = formula.replace(ROW_ID_KEYWORD, String.valueOf(row.getDatabaseId()));
			break;
		default:
			throw new FormulaException("Keyword " + formula + " not recognized");
		}
		
		return solvedFormula;
	}

	@Override
	public String solve() throws FormulaException {
		throw new FormulaException("Not supported");
	}

}
