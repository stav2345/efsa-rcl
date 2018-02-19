package providers;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import formula.FormulaException;
import formula.FormulaSolver;
import table_skeleton.TableRow;
import xlsx_reader.TableHeaders.XlsxHeader;

public class FormulaService implements IFormulaService {

	private static final Logger LOGGER = LogManager.getLogger(FormulaService.class);
	
	private ITableDaoService daoService;
	
	public FormulaService(ITableDaoService daoService) {
		this.daoService = daoService;
	}
	
	/**
	 * Update the values of the rows applying the columns formulas
	 * (Compute all the automatic values)
	 */
	public void updateFormulas(TableRow row) {
		
		// solve the formula for default code and default value
		FormulaSolver solver = new FormulaSolver(row, daoService);
		
		// note that this automatically updates the row
		// while solving formulas
		try {
			solver.solveAll(XlsxHeader.CODE_FORMULA.getHeaderName());
		} catch (FormulaException e) {
			e.printStackTrace();
			LOGGER.error("Cannot solve row formulas", e);
		}
		
		try {
			solver.solveAll(XlsxHeader.LABEL_FORMULA.getHeaderName());
		} catch (FormulaException e) {
			e.printStackTrace();
			LOGGER.error("Cannot solve row formulas", e);
		}
	}
}
