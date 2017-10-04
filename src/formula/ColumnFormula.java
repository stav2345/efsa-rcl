package formula;

public class ColumnFormula {

	private String columnId;
	private ColumnValueType type;
	
	public enum ColumnValueType {
		CODE,
		LABEL
	};
	
	public ColumnFormula(String columnId, ColumnValueType type) {
		this.columnId = columnId;
		this.type = type;
	}
}
