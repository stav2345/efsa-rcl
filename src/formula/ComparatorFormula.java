package formula;

import app_config.BooleanValue;
import table_skeleton.TableRow;

/**
 * Manage comparison formulas as (op1==op2), (op1!=op2), (op1ORop2), (op1ANDop2)
 * Note that without brackets it doesn't work
 * @author avonva
 *
 */
public class ComparatorFormula implements IFormula {

	public static final String EQUAL = "==";
	public static final String DISEQUAL = "!=";
	
	private String formula;
	private String operator;  // as ==, !=
	private String leftOperand;
	private String rightOperand;
	
	public ComparatorFormula(String formula, String operator) throws FormulaException {
		this.formula = formula;
		this.operator = operator;
		compile();
	}
	
	public String getOperator() {
		return operator;
	}
	
	@Override
	public String getUnsolvedFormula() {
		return formula;
	}

	@Override
	public void compile() throws FormulaException {

		String[] split = formula.split(operator);
		
		if (split.length > 2) {
			throw new FormulaException("Wrong comparison for " + formula);
		}

		if (split.length == 1) {
			boolean missingLeft = split[0].startsWith("==");
			
			if (missingLeft) {
				this.leftOperand = "";
				this.rightOperand = split[0].trim().replace(")", "");
			}
			else {
				this.leftOperand = split[0].trim().replace("(", "");
				this.rightOperand = "";
			}
		}
		else {
			// extract operands
			this.leftOperand = split[0].trim().replace("(", "");
			this.rightOperand = split[1].trim().replace(")", "");
		}
	}

	@Override
	public String solve() throws FormulaException {
		
		boolean comparison = false;
		
		switch(operator) {
		case EQUAL:
			comparison = leftOperand.equalsIgnoreCase(rightOperand);
			break;
		case DISEQUAL:
			comparison = !leftOperand.equalsIgnoreCase(rightOperand);
			break;
		default:
			throw new FormulaException("Operator " + operator + " not supported!");
		}

		// get a string value for the boolean
		String stringComp = comparison ? BooleanValue.getTrueValue() : BooleanValue.getFalseValue();
		
		return stringComp;
	}
	
	@Override
	public String solve(TableRow row) throws FormulaException {
		throw new FormulaException("Not supported");
	}
}
