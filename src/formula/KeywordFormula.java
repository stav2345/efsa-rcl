package formula;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import app_config.AppPaths;
import app_config.PropertiesReader;
import table_skeleton.TableRow;
import xml_catalog_reader.Selection;
import xml_catalog_reader.XmlContents;
import xml_catalog_reader.XmlLoader;

/**
 * Solve keywords
 * @author avonva
 *
 */
public class KeywordFormula implements IFormula {

	public static final String APP_NAME_KEYWORD = "{app.name}";
	public static final String APP_VERSION_KEYWORD = "{app.version}";
	public static final String APP_DC_CODE_KEYWORD = "{app.dcCode}";
	public static final String APP_DC_TEST_CODE_KEYWORD = "{app.dcTestCode}";
	public static final String APP_DC_TABLE_KEYWORD = "{app.dcTable}";
	public static final String NULL_KEYWORD = "null";
	public static final String CONCAT_KEYWORD = "|";
	public static final String TODAY_KEYWORD = "today.timestamp";
	public static final String LAST_MONTH_CODE_KEYWORD = "lastMonth.code";
	public static final String LAST_MONTH_LABEL_KEYWORD = "lastMonth.label";
	public static final String LAST_MONTH_YEAR_CODE_KEYWORD = "lastMonth.year.code";
	public static final String LAST_MONTH_YEAR_LABEL_KEYWORD = "lastMonth.year.label";

	private String formula;

	public KeywordFormula(String formula) throws FormulaException {
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
		throw new FormulaException("Not supported");
	}

	@Override
	public String solve() throws FormulaException {

		String solvedFormula = null;

		switch(formula) {
		case APP_NAME_KEYWORD:
			solvedFormula = formula.replace(APP_NAME_KEYWORD, PropertiesReader.getAppName());
			break;
		case APP_VERSION_KEYWORD:
			solvedFormula = formula.replace(APP_VERSION_KEYWORD, PropertiesReader.getAppVersion());
			break;
		case APP_DC_CODE_KEYWORD:
			solvedFormula = formula.replace(APP_DC_CODE_KEYWORD, PropertiesReader.getDataCollectionCode());
			break;
		case APP_DC_TEST_CODE_KEYWORD:
			solvedFormula = formula.replace(APP_DC_TEST_CODE_KEYWORD, PropertiesReader.getTestDataCollectionCode());
			break;
		case APP_DC_TABLE_KEYWORD:
			solvedFormula = formula.replace(APP_DC_TABLE_KEYWORD, PropertiesReader.getDataCollectionTable());
			break;
		case NULL_KEYWORD:
			solvedFormula = formula.replace(NULL_KEYWORD, "");
			break;
		case CONCAT_KEYWORD:
			solvedFormula = formula.replace(CONCAT_KEYWORD, "");
			break;

		case LAST_MONTH_YEAR_CODE_KEYWORD:
		case LAST_MONTH_YEAR_LABEL_KEYWORD:
		case LAST_MONTH_CODE_KEYWORD:
		case LAST_MONTH_LABEL_KEYWORD:
		case TODAY_KEYWORD:
			solvedFormula = solveDateKeywords(formula);
			break;

		default:
			throw new FormulaException("Keyword " + formula + " not recognized");
		}

		return solvedFormula;
	}

	/**
	 * Solve the keywords related to dates
	 * @param formula
	 * @return
	 * @throws FormulaException
	 */
	private String solveDateKeywords(String formula) throws FormulaException {
		
		// get today date
		Date today = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(today);

		String solvedFormula = null;
		
		switch(formula) {
		
		case TODAY_KEYWORD:
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
			solvedFormula = sdf.format(calendar.getTime());
			break;

		case LAST_MONTH_YEAR_CODE_KEYWORD:
		case LAST_MONTH_YEAR_LABEL_KEYWORD:
			calendar.add(Calendar.MONTH, -1);  // go to last month
			solvedFormula = String.valueOf(calendar.get(Calendar.YEAR));  // get the last month year
			break;

		case LAST_MONTH_CODE_KEYWORD:
		case LAST_MONTH_LABEL_KEYWORD:

			calendar.add(Calendar.MONTH, -1);  // go to last month
			
			// get last month term
			// months start from 0 so to show the correct number
			// we add 1
			String lastMonth = String.valueOf(calendar.get(Calendar.MONTH) + 1);

			// pick the months list
			XmlContents xml = XmlLoader.getByPicklistKey(AppPaths.MONTHS_LIST);

			if (xml == null) {
				throw new FormulaException("Cannot resolve formula lastMonth.code/label, since the " 
						+ AppPaths.MONTHS_LIST + " was not found!");
			}

			Selection monthSel = xml.getList().getSelectionByCode(lastMonth);

			if (formula.equals(LAST_MONTH_CODE_KEYWORD))
				solvedFormula = monthSel.getCode();
			else
				solvedFormula = monthSel.getDescription();

			break;
			
		default:
			break;
		}
		
		return solvedFormula;
	}
}
