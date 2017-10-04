package formula;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormulaFinder {
	
	/**
	 * Find the formulas for columns (%columnId.code/label)
	 * @return
	 */
	public static Matcher findColumnFormulas(String text) {
		String pattern = "\\%\\w+?\\.(code|label)";
		Pattern p = Pattern.compile(pattern);
		return p.matcher(text);
	}
	
	/**
	 * Find the formulas for relations
	 * @param text
	 * @return
	 */
	public static Matcher findRelationFormulas(String text) {
		Pattern p = Pattern.compile("RELATION\\{.+?,.+?\\}");
		return p.matcher(text);
	}
}
