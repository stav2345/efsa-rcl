package formula;

import app_config.AppPaths;
import table_relations.Relation;
import table_skeleton.TableCell;
import table_skeleton.TableRow;

/**
 * Class to manage and solve a RELATION{parentTableName,parentColumnId.code/label} statement
 * @author avonva
 *
 */
public class RelationFormula implements IFormula {

	private String formula;
	private String parentTable;
	private String parentColumnId;
	private String parentFieldType;

	public RelationFormula(String formula) throws FormulaException {
		this.formula = formula;
		compile();
	}

	public String getParentTable() {
		return parentTable;
	}
	public String getParentColumnId() {
		return parentColumnId;
	}
	public String getParentFieldType() {
		return parentFieldType;
	}
	
	@Override
	public String getUnsolvedFormula() {
		return formula;
	}

	@Override
	public void compile() throws FormulaException {

		String innerFields = formula.replace("RELATION{", "").replace("}", "");

		// get operands by splitting with comma
		String[] split = innerFields.split(",");

		if (split.length != 2) {
			throw new FormulaException("Wrong RELATION statement, found " + formula);
		}

		// identify parent table
		this.parentTable = split[0];

		// get the required field (column.code/label)
		String field = split[1];

		// field is name.code or name.label
		split = field.split("\\.");

		if (split.length != 2) {
			throw new FormulaException("Wrong RELATION statement, need .code or .label, found " + field + " in " + formula);
		}

		// get the field name of the parent
		this.parentColumnId = split[0].replace(" ", "");
		this.parentFieldType = split[1].replace(" ", "");
	}
	
	@Override
	public String solve() throws FormulaException {
		throw new FormulaException("Not supported");
	}

	@Override
	public String solve(TableRow row) throws FormulaException {
		
		// get the relation with the parent
		Relation r = row.getSchema().getRelationByParentTable(parentTable);

		if (r == null) {
			throw new FormulaException("No such relation found in the " + AppPaths.RELATIONS_SHEET 
					+ ". Relation required: " + parentTable);
		}

		// get the parent foreign key from the child row
		TableCell colVal = row.get(r.getForeignKey());

		if (colVal == null) {
			return "";
			//throw new FormulaException("Formula: " + formula + ": No parent data found for " + r + " in the row " + row);
		}

		// get from the child row the foreign key for the parent
		String foreignKey = row.get(r.getForeignKey()).getCode();

		// if no foreign key => error
		if (foreignKey == null || foreignKey.isEmpty()) {
			throw new FormulaException("No foreign key found for " + r + " in the row " + row);
		}

		// get the parent row using the foreign key
		TableRow parent = r.getParentValue(Integer.valueOf(foreignKey));
		
		if (parent == null) {
			throw new FormulaException("No relation value found for " + foreignKey + "; relation " + r);
		}

		// get the required field and put it into the formula
		TableCell parentValue = parent.get(parentColumnId);

		if (parentValue == null) {
			throw new FormulaException("No parent data value found for " + parentColumnId 
					+ " in the row " + row + " with parent " + parent);
		}
		
		String solvedFormula = null;
		
		switch(parentFieldType) {
		case "code":
			solvedFormula = parentValue.getCode();
			break;
		case "label":
			solvedFormula = parentValue.getLabel();
			break;
		default:
			throw new FormulaException("Field type " + parentFieldType + " not recognized");
		}
		
		return solvedFormula;
	}
}
