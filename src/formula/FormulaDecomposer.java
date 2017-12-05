package formula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to decompose a compound field into its
 * children fields. In particular, it is needed the formula
 * that was used to generate the compound field and the 
 * actual value of the compound field.
 * The compound field can have different syntax. In particular, a specific
 * function need to be called for the specific syntax.
 * <ul>
 * 	<li>{@link #decomposeSimpleField(String, false)} Data separated by delimiter, for example with $: data1$data2$...</li>
 * 	<li>{@link #decomposeSimpleField(String, true)} Data separated by delimiter with attributes names inside it. For example with $: 
 * 	    attribute1=data1$attribute2=data2$...</li>
 * 	<li>{@link #decomposeFoodexCode(false)} Foodex syntax: baseTerm#facet1$facet2$...</li>
 * 	<li>{@link #decomposeFoodexCode(true)} Foodex syntax with attributes: baseTerm#attr1=facet1$attr2=facet2$...</li>
 * </ul>
 * @author avonva
 *
 */
public class FormulaDecomposer {

	private String formula;
	private String value;
	
	public FormulaDecomposer(String formula, String value) {
		newValue(formula, value);
	}
	
	public void newValue(String formula, String value) {
		this.formula = normalizeFormula(formula);
		this.value = value;
	}
	
	public enum CompoundType {
		
		FOODEX_CODE,
		SIMPLE;
		
		private String separator;
		private boolean attributes;
		
		public void setSeparator(String separator) {
			this.separator = separator;
		}
		public String getSeparator() {
			return separator;
		}
		public void setAttributes(boolean attributes) {
			this.attributes = attributes;
		}
		public boolean isAttributes() {
			return attributes;
		}
	}
	
	private String normalizeFormula(String formula) {
		return formula.replace("|", "");
	}
	
	/**
	 * Split a text a foodex code
	 * @param text
	 * @return
	 */
	private List<AttributeElement> splitAsFoodex(String text, boolean attributes) {
		
		List<AttributeElement> elements = new ArrayList<>();
		
		String[] split = text.split("#");
		
		AttributeElement baseTerm =  new AttributeElement(split[0], split[0]);
		elements.add(baseTerm);

		// if no facets are present return
		if (split.length != 2) {
			return elements;
		}
		
		String facets = split[1];

		// add also all the facets codes
		elements.addAll(splitAsSimple(facets, "$", attributes));
		
		return elements;
	}
	
	/**
	 * Split a text by a separator
	 * @param text
	 * @param separator
	 * @return
	 */
	private List<AttributeElement> splitAsSimple(String text, String separator, boolean attributes) {
		
		List<AttributeElement> elements = new ArrayList<>();
		
		// add also the facets
		StringTokenizer st = new StringTokenizer(text, "$");
		while (st.hasMoreTokens()) {
			
			String name;
			String element = st.nextToken();

			// if we have the attributes (name=value)
			if (attributes) {
				
				String[] split = element.split("=");
				
				if (split.length != 2) {
					System.err.println("Wrong simple split with attributes: " + element);
					continue;
				}
				
				name = split[0];
				// get only the part after the =
				element = split[1];
			}
			else {
				name = element;
			}
			
			elements.add(new AttributeElement(name, element));
		}
		
		return elements;
	}
	
	/**
	 * Decompose a foodex code.
	 * @param attributes if true it parses: baseTerm#attr1=facet1$attr2=facet2$... otherwise: baseTerm#facet1$facet2$...
	 * @return
	 * @throws FormulaException 
	 */
	public HashMap<String, String> decomposeFoodexCode(boolean attributes) throws FormulaException {
		CompoundType type = CompoundType.FOODEX_CODE;
		type.setAttributes(attributes);
		return decompose(type);
	}
	
	/**
	 * Decompose a simple field separated by a separator
	 * @param separator the separator between the fields
	 * @param attributes if true it parses: attr1=value1$attr2=value2$... otherwise: value1$value2$...
	 * @return
	 * @throws FormulaException 
	 */
	public HashMap<String, String> decomposeSimpleField(String separator, boolean attributes) throws FormulaException {
		CompoundType type = CompoundType.SIMPLE;
		type.setSeparator(separator);
		type.setAttributes(attributes);
		return decompose(type);
	}
	
	/**
	 * Search the fields in a formula with relations
	 * @param separator
	 * @return
	 * @throws FormulaException
	 */
	public HashMap<String, String> decomposeRelationFieldAsFoodex() throws FormulaException {
		
		HashMap<String, String> decomposedValues = new HashMap<>();
		
		// find all the attributes names
		List<AttributeElement> elements = splitAsFoodex(value, false);
		elements.remove(0);  // remove base term TODO fix this
		
		// search the relation that is related to the current element
		String pattern = "";
		
		pattern = pattern + FormulaFinder.RELATION_REGEX;
		
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(formula);
		
		for (AttributeElement elem : elements) {
			
			if (m.find()) {

				// get the match
				String match = m.group();
				
				// get the relation formula
				FormulaList list = FormulaFinder.findRelationFormulas(match);
				
				if (list.isEmpty()) {
					continue;
				}
				
				// get the relation formula
				RelationFormula relF = (RelationFormula) list.get(0);
				
				// save in the parent column id the value of the element
				decomposedValues.put(relF.getParentColumnId(), elem.getValue());
			}
		}
		
		return decomposedValues;
	}
	
	/**
	 * Search the fields in a formula with relations
	 * @param separator
	 * @return
	 * @throws FormulaException
	 */
	public HashMap<String, String> decomposeRelationField(String separator, boolean attributes) throws FormulaException {
		
		HashMap<String, String> decomposedValues = new HashMap<>();
		
		// find all the attributes names
		List<AttributeElement> elements = splitAsSimple(value, "$", attributes);
		
		for (AttributeElement elem : elements) {
			
			// search the relation that is related to the current element
			String pattern = "";
			
			// if we have attributes search for the correct name
			// otherwise use the normal order
			if (attributes) {
				pattern = elem.getName() + "\\s*=\\s*";
			}
			
			pattern = pattern + FormulaFinder.RELATION_REGEX;
			
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(formula);

			if (m.find()) {

				// get the match
				String match = m.group();
				
				// get the relation formula
				FormulaList list = FormulaFinder.findRelationFormulas(match);
				
				if (list.isEmpty()) {
					continue;
				}
				
				// get the relation formula
				RelationFormula relF = (RelationFormula) list.get(0);
				
				// save in the parent column id the value of the element
				decomposedValues.put(relF.getParentColumnId(), elem.getValue());
			}
		}
		
		return decomposedValues;
	}
	
	private HashMap<String, String> decompose(CompoundType type) throws FormulaException {
		
		if (type == CompoundType.SIMPLE && type.getSeparator() == null) {
			System.err.println("No separator was found for CompoundType " 
					+ CompoundType.SIMPLE + ". Aborting operation.");
			return null;
		}
		
		List<AttributeElement> formulaElements = new ArrayList<>();
		List<AttributeElement> valueElements = new ArrayList<>();
		switch (type) {
		case FOODEX_CODE:
			
			// no need of formula if we have attributes
			if (!type.isAttributes())
				formulaElements = splitAsFoodex(formula, type.isAttributes());
			
			valueElements = splitAsFoodex(value, type.isAttributes());
			break;
		case SIMPLE:
			
			// no need of formula if we have attributes
			if (!type.isAttributes())
				formulaElements = splitAsSimple(formula, type.getSeparator(), type.isAttributes());
			
			valueElements = splitAsSimple(value, type.getSeparator(), type.isAttributes());
			break;
		default:
			System.err.println("CompoundType not recognized " + type);
			break;
		}

		// if we do not have attributes we need the same size for formulas and value elements
		// otherwise we do not know which variable is related to which value
		if(!type.isAttributes() && valueElements.size() != formulaElements.size()) {
			throw new FormulaException("Cannot parse code: " 
					+ value 
					+ "; since it has a different number of elements than the formula: " 
					+ formula);
		}
		
		HashMap<String, String> decomposedValues = new HashMap<>();
		
		// for each value
		for (int i = 0; i < valueElements.size(); ++i) {
			
			String columnId = null;
			
			// if no attributes, we need the formula to know which
			// variable is where
			if (!type.isAttributes()) {
				// get the value formula
				String currentFormula = formulaElements.get(i).getValue();
				
				// get the column id of the column formula (if it is that)
				columnId = getColumnId(currentFormula);
			}
			else {
				// if we have the attributes, simply get the value from the
				// value string (we have the variables names there!)
				columnId = valueElements.get(i).getName();
			}
			
			// if we have a column id as formula
			if (columnId != null) {
				
				// save the value in the current column id
				String currentVal = valueElements.get(i).getValue();
				decomposedValues.put(columnId, currentVal);
			}
		}
		
		return decomposedValues;
	}
	
	/**
	 * Get the column id of a column formula
	 * @param text
	 * @return
	 */
	private String getColumnId(String text) {
		
		try {
			
			FormulaList list = FormulaFinder.findColumnFormulas(text);
			
			if (list.isEmpty())
				return null;
			
			ColumnFormula formula = (ColumnFormula) list.get(0);
			
			return formula.getColumnId();
			
		} catch (FormulaException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	
	public static void main(String[] args) throws FormulaException {
		String formula2 = "A04MQ#RELATION{SummarizedInformation,source.code}$RELATION{CasesInformation,part.code}$RELATION{SummarizedInformation,prod.code}$RELATION{SummarizedInformation,animage.code}";
		String value2 = "A04MQ#F01.A057B$F02.A06AM$F21.A07RV$F31.A16NK";
		FormulaDecomposer d = new FormulaDecomposer(formula2, value2);
		System.out.println(d.decomposeRelationFieldAsFoodex());
	}
}
