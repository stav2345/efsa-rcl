package formula;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormulaFinder {
	
	// regex components
	private static final String NUMBER = "[0-9]{1,13}(\\.[0-9]*)?";
	private static final String INTEGER = "[0-9]+";
	private static final String LETTER = "[a-zA-Z]";
	private static final String STRING = "(" + LETTER + ")+";
	private static final String VARIABLE = "((" + NUMBER + ")|(" + LETTER + "))+";
	
	public static final String RELATION_REGEX = "RELATION\\{.+?,.+?\\}";
	
	/**
	 * Find the formulas related to a function
	 * @param text
	 * @param functionName check functions defined in {@link FunctionFormula}
	 * @return
	 * @throws FormulaException
	 */
	public static FormulaList findFunctionFormulas(String text, String functionName) throws FormulaException {
		
		FormulaList formulas = new FormulaList();
		
		// regex to match function names
		StringBuilder regex = new StringBuilder();
		
		// function name
		regex.append(functionName);
		
		String operand = ".*?";
		
		// regex to match functions that have from 1 parameter to n parameters
		
		regex.append("\\(")  // open function bracket
			.append("(").append(operand).append(",)*?")
			.append(operand)
			.append("\\)");  // close function bracket
		
		String pattern = regex.toString();
		
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(text);

		while (m.find()) {
			String formula = m.group();
			FunctionFormula funcFormula = new FunctionFormula(formula);
			formulas.add(funcFormula);
		}

		return formulas;
	}
	
	/**
	 * Find all the functions formulas
	 * @param text
	 * @return
	 * @throws FormulaException
	 */
	/*@Deprecated
	public static FormulaList findFunctionFormulas(String text) throws FormulaException {
		
		FormulaList formulas = new FormulaList();
		
		// regex to match function names
		StringBuilder regex = new StringBuilder();
		
		// function name
		regex.append("(")
			.append(FunctionFormula.IF).append("|")
			.append(FunctionFormula.IF_NOT_NULL).append("|")
			.append(FunctionFormula.END_TRIM).append("|")
			.append(FunctionFormula.ZERO_PADDING).append("|")
			.append(FunctionFormula.SUM).append(")");
		
		String operand = ".*?";
		
		// regex to match functions that have from 1 parameter to n parameters
		
		regex.append("\\(")  // open function bracket
			.append("(").append(operand).append(",)*?")
			.append(operand)
			.append("\\)");  // close function bracket
		
		String pattern = regex.toString();
		
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(text);

		FunctionFormula funcFormula = new FunctionFormula(formula);
		formulas.add(funcFormula);

		return formulas;
	}*/
	
	/**
	 * Find the formulas for columns (%columnId.code/label)
	 * @return
	 * @throws FormulaException 
	 */
	public static FormulaList findColumnFormulas(String text) 
			throws FormulaException {
		
		FormulaList colFormulas = new FormulaList();
		
		String pattern = "\\%\\w+?\\.(code|label)";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(text);
		
		// compile all the column formulas
		while (m.find()) {
			String formula = m.group();
			ColumnFormula colFormula = new ColumnFormula(formula);
			colFormulas.add(colFormula);
		}
		
		return colFormulas;
	}
	
	/**
	 * Find the formulas for relations
	 * @param text
	 * @return
	 * @throws FormulaException 
	 */
	public static FormulaList findRelationFormulas(String text) 
			throws FormulaException {
		
		FormulaList relFormulas = new FormulaList();
		
		Pattern p = Pattern.compile(RELATION_REGEX);
		
		Matcher m = p.matcher(text);
		
		// compile all the relation formulas
		while (m.find()) {
			String formula = m.group();
			RelationFormula relFormula = new RelationFormula(formula);
			relFormulas.add(relFormula);
		}
		
		return relFormulas;
	}
	
	/**
	 * Find all the logical comparator formulas (as (a==b), (a!=b))
	 * @param text
	 * @param operator
	 * @return
	 * @throws FormulaException
	 */
	public static FormulaList findComparatorFormulas(String text, String operator) 
			throws FormulaException {
		
		FormulaList formulas = new FormulaList();
		
		String operand = "(" + VARIABLE + ")";
		String pattern = "\\(" + operand + "\\s*" + operator + "\\s*" + operand + "\\)";
		
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(text);

		while (m.find()) {
			String formula = m.group();
			ComparatorFormula compFormula = new ComparatorFormula(formula, operator);
			formulas.add(compFormula);
		}
		
		return formulas;
	}
	
	/**
	 * Search special keywords
	 * @param text
	 * @return
	 * @throws FormulaException
	 */
	public static FormulaList findKeywordFormulas(String text) 
			throws FormulaException {
		
		FormulaList formulas = new FormulaList();

		// search for keywords
		StringBuilder sb = new StringBuilder();
		sb.append("(")
			.append(toRegex(KeywordFormula.APP_NAME_KEYWORD)).append("|")
			.append(toRegex(KeywordFormula.APP_VERSION_KEYWORD)).append("|")
			.append(toRegex(KeywordFormula.APP_DC_CODE_KEYWORD)).append("|")
			.append(toRegex(KeywordFormula.APP_DC_TABLE_KEYWORD)).append("|")
			.append(toRegex(KeywordFormula.NULL_KEYWORD)).append("|")
			.append(toRegex(KeywordFormula.CONCAT_KEYWORD)).append("|")
			.append(toRegex(KeywordFormula.TODAY_KEYWORD)).append("|")
			.append(toRegex(KeywordFormula.LAST_MONTH_CODE_KEYWORD)).append("|")
			.append(toRegex(KeywordFormula.LAST_MONTH_LABEL_KEYWORD)).append("|")
			.append(toRegex(KeywordFormula.LAST_MONTH_YEAR_CODE_KEYWORD)).append("|")
			.append(toRegex(KeywordFormula.LAST_MONTH_YEAR_LABEL_KEYWORD))
			.append(")");

		String pattern = sb.toString();
		
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(text);
		
		while (m.find()) {
			String formula = m.group();
			KeywordFormula keyFormula = new KeywordFormula(formula);
			formulas.add(keyFormula);
		}
		
		return formulas;
	}
	
	/**
	 * Search keywords which are dependent on the row values
	 * @param text
	 * @return
	 * @throws FormulaException
	 */
	public static FormulaList findRowKeywordFormulas(String text) 
			throws FormulaException {
		
		FormulaList formulas = new FormulaList();
		
		// search for keywords
		StringBuilder sb = new StringBuilder();
		sb.append("(")
			.append(toRegex(RowKeywordFormula.ROW_ID_KEYWORD))
			.append(")");
		
		String pattern = sb.toString();
		
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(text);
		
		while (m.find()) {
			String formula = m.group();
			RowKeywordFormula keyFormula = new RowKeywordFormula(formula);
			formulas.add(keyFormula);
		}
		
		return formulas;
	}
	
	/**
	 * Convert a string into a regex by escaping special characters
	 * @param formula
	 * @return
	 */
	private static String toRegex(String formula) {
		return formula.replace("{", "\\{")
				.replace("}", "\\}")
				.replace("|", "\\|")
				.replace(".", "\\.");
	}
}
