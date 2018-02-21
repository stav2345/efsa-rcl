package providers;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import formula.Formula;
import formula.FormulaException;
import formula.FormulaSolver;
import table_skeleton.TableCell;
import table_skeleton.TableColumn;
import table_skeleton.TableRow;
import xlsx_reader.TableHeaders.XlsxHeader;

public class FormulaService implements IFormulaService {

	private static final Logger LOGGER = LogManager.getLogger(FormulaService.class);
	
	private ITableDaoService daoService;
	
	public FormulaService(ITableDaoService daoService) {
		this.daoService = daoService;
	}
	

	@Override
	public void initialize(TableRow row) {
		for (TableColumn col : row.getSchema()) {
			initialize(row, col.getId());
		}
	}
	
	@Override
	public void initialize(TableRow row, String colId) {
		
		TableColumn col = row.getSchema().getById(colId);
		
		// skip foreign keys
		if (col.isForeignKey())
			return;
		
		TableCell sel = new TableCell();
		FormulaSolver solver = new FormulaSolver(row, daoService);

		try {
			Formula label = solver.solve(col, XlsxHeader.DEFAULT_VALUE.getHeaderName());
			sel.setLabel(label.getSolvedFormula());
		} catch (FormulaException e) {
			e.printStackTrace();
			LOGGER.error("Cannot solve formula for column=" + colId, e);
		}

		try {
			Formula code = solver.solve(col, XlsxHeader.DEFAULT_CODE.getHeaderName());
			
			if (col.getPicklistKey() == null || col.getPicklistKey().isEmpty())
				sel.setCode(code.getSolvedFormula());
			else
				sel = row.getTableColumnValue(code.getSolvedFormula(), col.getPicklistKey());
			
		} catch (FormulaException e) {
			e.printStackTrace();
			LOGGER.error("Cannot solve formula for column=" + colId, e);
		}

		row.put(col.getId(), sel);
	}
	
	@Override
	public String solve(TableRow row, TableColumn column, XlsxHeader columnProperty) throws FormulaException {

		FormulaSolver solver = new FormulaSolver(row, daoService);
		
		Formula formula = solver.solve(column, columnProperty.getHeaderName());

		return formula.getSolvedFormula();
	}
	
	@Override
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
