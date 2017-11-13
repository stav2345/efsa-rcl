package table_database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import app_config.AppPaths;
import table_relations.Relation;
import table_skeleton.TableColumn;
import table_skeleton.TableColumnValue;
import table_skeleton.TableRow;
import table_skeleton.TableRowList;
import xlsx_reader.TableSchema;
import xml_catalog_reader.XmlContents;
import xml_catalog_reader.XmlLoader;

/**
 * Dao which communicates with the database and all the tables
 * that follow a {@link TableSchema}. These tables are automatically
 * generated starting from the excel file, therefore the dao
 * adapts the query using their structure.
 * @author avonva
 *
 */
public class TableDao {

	private String tableName;
	private TableSchema schema;
	private TableSchema completeSchema;
	
	public TableDao(TableSchema schema) {
		this.tableName = "APP." + schema.getSheetName();
		this.completeSchema = schema;
		this.schema = schema; // TODO save just the needed information
		//this.schema = schema.getNonCompositeColumns();  // we save just the editable columns
		this.schema.sortById();
	}
	
	/**
	 * Get the query needed to add a row to the table
	 * @return
	 */
	private String getAddQuery() {
		
		StringBuilder query = new StringBuilder();
		query.append("insert into " + tableName + " (");
		
		// set the columns names
		Iterator<TableColumn> iterator = schema.iterator();
		while (iterator.hasNext()) {
			
			TableColumn col = iterator.next();
			
			query.append(col.getId());
			
			// append the comma if there is another field
			if(iterator.hasNext())
				query.append(",");
			else
				query.append(")");  // else, close statement
		}
		
		// values statement
		query.append(" values (");
		
		// add the ?
		iterator = schema.iterator();
		while (iterator.hasNext()) {
			
			// go to the next
			TableColumn col = iterator.next();
			
			query.append("?");
			
			// append the comma if there is another field
			if(iterator.hasNext())
				query.append(",");
			else
				query.append(")");  // else, close statement
		}

		return query.toString();
	}
	
	/**
	 * Get the query needed to update a row to the table
	 * @return
	 */
	private String getUpdateQuery() {
		
		StringBuilder query = new StringBuilder();
		query.append("update " + tableName + " set ");
		
		// set the columns names
		Iterator<TableColumn> iterator = schema.iterator();
		while (iterator.hasNext()) {
			
			TableColumn col = iterator.next();
			
			query.append(col.getId());
			query.append(" = ?");
			
			// append the comma if there is another field
			if(iterator.hasNext())
				query.append(",");
		}
		
		query.append(" where ").append(schema.getTableIdField()).append(" = ?");

		return query.toString();
	}
	
	/**
	 * Check if the id is a foreignKey for a parent table of 
	 * the current table
	 * @param id
	 * @return
	 * @throws IOException
	 */
	private boolean isRelationId(String id) throws IOException {
		
		if (schema.getRelations() == null)
			return false;
		
		for (Relation r : schema.getRelations()) {
			if(r.getForeignKey().equals(id))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Set the parameter of the statement using the row values
	 * and the table name
	 * @param row
	 * @param stmt
	 * @throws SQLException
	 */
	private void setParameters(TableRow row, PreparedStatement stmt, boolean setWhereId) throws SQLException {

		int currentIndex = 1;
		
		for (int i = 0; i < schema.size(); ++i) {
			
			TableColumn col = schema.get(i);
			
			TableColumnValue colValue = row.get(col.getId());
			
			if (colValue == null) {
				System.err.println("Missing value for " + col.getId() 
					+ " in table " + row.getSchema().getSheetName() + ". Putting an empty value.");
				
				colValue = new TableColumnValue();
			}

			// save always the code
			String value = colValue.getCode();
			
			// if no code is found, use the label
			if (value.isEmpty())
				value = colValue.getLabel();

			// If we have a relation ID => then convert into integer
			try {
				
				if (isRelationId(col.getId()))
					stmt.setInt(currentIndex, Integer.valueOf(value));
				else {
					stmt.setString(currentIndex, value);
				}
				
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
				System.err.println("Wrong integer field " + col.getId() + " with value " + value);
			}
			
			// increase current index
			currentIndex++;
		}
		
		// set also the id of the row
		if (setWhereId) {
			stmt.setInt(currentIndex, row.getId());
		}
	}

	
	/**
	 * Add a new row to the table
	 * @param row
	 * @return
	 */
	public int add(TableRow row) {
		
		int id = -1;
		
		try (Connection con = Database.getConnection(); 
				PreparedStatement stmt = con.prepareStatement(getAddQuery(), 
						Statement.RETURN_GENERATED_KEYS);) {
			
			// set the row values in the parameters
			setParameters(row, stmt, false);
			
			// insert the element
			stmt.executeUpdate();
			
			// get the newly generated id
			try (ResultSet rs = stmt.getGeneratedKeys();) {
				if (rs.next()) {
					id = rs.getInt(1);
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (id != -1) {
			System.out.println("Row " + id + " successfully added in " + tableName);
		}
		else {
			System.err.println("Errors in adding " + row + " to " + tableName);
		}
		
		return id;
	}
	
	/**
	 * Add a new row to the table
	 * @param row
	 * @return
	 */
	public boolean update(TableRow row) {
		
		boolean ok = true;
		
		try (Connection con = Database.getConnection(); 
				PreparedStatement stmt = con.prepareStatement(getUpdateQuery());) {
			
			// set the row values in the parameters
			// with the where id included
			setParameters(row, stmt, true);
			
			// insert the element
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			ok = false;	
		}
		
		if (ok) {
			System.out.println("Row " + row.getId() + " successfully updated in " + tableName);
		}
		else {
			System.err.println("Errors in updating " + row + " for " + tableName);
		}
		
		return ok;
	}

	/**
	 * Delete all the rows from the table
	 * @param row
	 * @return
	 */
	public boolean deleteAll() {
		
		boolean ok = true;
		
		String query = "delete from " + tableName;
		
		try (Connection con = Database.getConnection(); 
				PreparedStatement stmt = con.prepareStatement(query);) {
			
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
			ok = false;
		}
		
		if (ok) {
			System.out.println("All rows successfully deleted from " + tableName);
		}
		else {
			System.err.println("Cannot delete all rows from " + tableName);
		}
		
		return ok;
	}
	
	/**
	 * Remove all the rows where the parent id is equal to {@code parentId} in the parent table {@code parentTable}
	 * @param row
	 * @return
	 */
	public boolean deleteByParentId(String parentTable, int parentId) {

		boolean ok = true;
		
		Relation r = schema.getRelationByParentTable(parentTable);

		String query = "delete from " + tableName + " where " + r.getForeignKey() + " = ?";
		
		try (Connection con = Database.getConnection(); 
				PreparedStatement stmt = con.prepareStatement(query);) {
			
			// set the id of the parent
			stmt.setInt(1, parentId);

			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
			ok = false;
		}
		
		return ok;
	}
	
	/**
	 * Get a row from the result set
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public TableRow getByResultSet(ResultSet rs) throws SQLException {
		
		// here we need all the columns because we also
		// compute composite fields
		TableRow row = new TableRow(completeSchema);
		
		// put the id
		int id = rs.getInt(schema.getTableIdField());
		
		TableColumnValue sel = new TableColumnValue();
		sel.setCode(String.valueOf(id));
		sel.setLabel(String.valueOf(id));
		row.put(schema.getTableIdField(), sel);
		
		for (TableColumn column : schema) {
			
			TableColumnValue selection = null;
			
			// create foreign key if necessary
			if (column.isForeignKey()) {
				
				// the foreign key is an integer id
				int value = rs.getInt(column.getId());
				
				selection = new TableColumnValue();
				
				// we don't need the description for foreign id
				selection.setCode(String.valueOf(value));
				
				row.put(column.getId(), selection);
			}
			else {
				
				String value = rs.getString(column.getId());

				// if no value go to the next field
				if (value == null)
					continue;
				
				// if we have a picklist, we need both code and description
				if (column.isPicklist()) {
					
					String code = String.valueOf(value);

					// get the description from the .xml using the code
					if (code != null && !code.isEmpty()) {

						XmlContents contents = XmlLoader.getByPicklistKey(column.getPicklistKey());
						
						if (contents == null) {

							System.err.println("IMPORTANT: Check that the picklist " 
									+ column.getPicklistKey() + " is in your " + AppPaths.XML_FOLDER
									+ " folder. Note that also the root node of the xml should have "
									+ "the name " + column.getPicklistKey() + ". Putting an empty value.");
							
							selection = new TableColumnValue();
						}
						else if (contents.getElementByCode(code) == null) {
							
							System.err.println("IMPORTANT: The element " 
									+ code + " is missing in the picklist " + column.getPicklistKey() 
									+ " in the " + AppPaths.XML_FOLDER
									+ " folder. Putting an empty value.");
							
							selection = new TableColumnValue();
						}
						else {
							selection = new TableColumnValue(contents.getElementByCode(code));
						}
					}
					else
						selection = new TableColumnValue();
				}
				else {

					// if simple element, then it is sufficient the
					// description (which is the label)
					selection = new TableColumnValue();
					selection.setCode(String.valueOf(value));
					selection.setLabel(String.valueOf(value));
				}
			}

			// set also the id of the row
			row.setId(rs.getInt(schema.getTableIdField()));

			if (selection.getLabel().isEmpty())
				selection.setLabel(selection.getCode());

			// insert the element into the row
			row.put(column.getId(), selection);
		}

		// solve automatic fields
		row.updateFormulas();
		
		return row;
	}
	
	public TableRowList getByParentId(String parentTable, int parentId) {
		return getByParentId(parentTable, parentId, "asc");
	}
	
	/**
	 * Get all the rows that has as parent the {@code parentId} in the parent table {@code parentTable}
	 * @param row
	 * @return
	 */
	public TableRowList getByParentId(String parentTable, int parentId, String order) {
		
		TableRowList rows = new TableRowList(schema);

		Relation r = schema.getRelationByParentTable(parentTable);

		String query = "select * from " + tableName + " where " + r.getForeignKey() 
			+ " = ? order by " + schema.getTableIdField() + " " + order;
		
		try (Connection con = Database.getConnection(); 
				PreparedStatement stmt = con.prepareStatement(query);) {
			
			// set the id of the parent
			stmt.setInt(1, parentId);
			
			try (ResultSet rs = stmt.executeQuery();) {
				
				while (rs.next()) {

					TableRow row = getByResultSet(rs);
	
					if (row != null)
						rows.add(row);
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return rows;
	}
	
	/**
	 * Get all the rows from the table
	 * @param row
	 * @return
	 */
	public TableRowList getAll() {
		
		TableRowList rows = new TableRowList(schema);

		String query = "select * from " + tableName + " order by " + schema.getTableIdField() + " asc";
		
		try (Connection con = Database.getConnection(); 
				PreparedStatement stmt = con.prepareStatement(query);) {
			
			try (ResultSet rs = stmt.executeQuery();) {
				while (rs.next()) {
					
					TableRow row = getByResultSet(rs);
					if (row != null)
						rows.add(row);
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return rows;
	}
	
	/**
	 * Remove a row by its id
	 * @param rowId
	 * @return
	 */
	public boolean delete(int rowId) {

		boolean ok = true;
		
		String query = "delete from " + tableName + " where " + schema.getTableIdField() + " = ?";
		
		try (Connection con = Database.getConnection(); 
				PreparedStatement stmt = con.prepareStatement(query);) {
			
			stmt.setInt(1, rowId);
			
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
			ok = false;
		}
		
		if (ok) {
			System.out.println("Row " + rowId + " successfully deleted from " + tableName);
		}
		else {
			System.out.println("Row " + rowId + " cannot be deleted from " + tableName);
		}
		
		return ok;
	}
	
	public boolean delete(TableRowList list) {

		boolean ok = true;
		
		String query = "delete from " + tableName + " where " + schema.getTableIdField() + " = ?";
		
		try (Connection con = Database.getConnection(); 
				PreparedStatement stmt = con.prepareStatement(query);) {
			
			for (TableRow row : list) {
				stmt.setInt(1, row.getId());

				stmt.addBatch();
			}
			
			stmt.executeBatch();
			
		} catch (SQLException e) {
			e.printStackTrace();
			ok = false;
		}
		
		return ok;
	}

	/**
	 * Delete all the records by a database field
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public boolean deleteByStringField(String fieldName, String value) {

		boolean ok = true;
		
		String query = "delete from " + tableName + " where " + fieldName + " = ?";
		
		try (Connection con = Database.getConnection(); 
				PreparedStatement stmt = con.prepareStatement(query);) {
			
			stmt.setString(1, value);
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
			ok = false;
		}
		
		if (ok) {
			System.out.println("Rows with " + fieldName + " = " 
					+ value + " successfully deleted from " + tableName);
		}
		else {
			System.out.println("Rows with " + fieldName + " = " 
					+ value + " cannot be deleted from " + tableName);
		}
		
		return ok;
	}
	
	/**
	 * Get the row by its id
	 * @param id
	 * @return
	 */
	public TableRow getById(int id) {
		
		TableRow row = null;

		String query = "select * from " + tableName + " where " + schema.getTableIdField() + " = ?";
		
		try (Connection con = Database.getConnection(); 
				PreparedStatement stmt = con.prepareStatement(query);) {
			
			stmt.setInt(1, id);
			
			try (ResultSet rs = stmt.executeQuery();) {
				if (rs.next()) {
					row = getByResultSet(rs);
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return row;
	}

	/**
	 * Get the all the rows that matches the fieldName with value
	 * @param id
	 * @return
	 */
	public TableRowList getByStringField(String fieldName, String value) {
		
		TableRowList rows = new TableRowList(schema);

		String query = "select * from " + tableName + " where " + fieldName 
				+ " = ? order by " + schema.getTableIdField() + " asc";
		
		try (Connection con = Database.getConnection(); 
				PreparedStatement stmt = con.prepareStatement(query);) {
			
			stmt.setString(1, value);
			
			try (ResultSet rs = stmt.executeQuery();) {
				while (rs.next()) {
					TableRow row = getByResultSet(rs);
					rows.add(row);
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return rows;
	}
}
