package table_database;

public class ForeignKey {

	private String tableName;
	private String columnName;
	private String fkName;
	
	public ForeignKey(String tableName, String columnName, String fkName) {
		this.tableName = tableName;
		this.columnName = columnName;
		this.fkName = fkName;
	}
	
	public String getTableName() {
		return tableName;
	}
	public String getColumnName() {
		return columnName;
	}
	public String getName() {
		return fkName;
	}
}
