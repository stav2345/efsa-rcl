package xml_catalog_reader;

import java.util.HashMap;

import table_skeleton.TableColumnValue;

/**
 * Class which models a single node of a configuration .xml file
 * It also represents a single cell of the report (i.e. a value of a column of the report table)
 * @author avonva
 *
 */
public class Selection {
	
	private String listId;       // list in which the selection is present (BSE/SCRAPIE...)
	private String code;         // code of the selection (identifies a value of the list)
	private String description;  // label of the selection
	private HashMap<String, String> data;         // optional data
	
	public Selection() {
		data = new HashMap<>();
	}
	
	public void setListId(String listId) {
		this.listId = listId;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void addData(String key, String value) {
		this.data.put(key, value);
	}
	
	public String getListId() {
		return listId;
	}
	public String getCode() {
		return code;
	}
	public String getDescription() {
		return description;
	}
	public String getData(String key) {
		return data.get(key);
	}
	
	public Integer getNumData(String key) {
		String data = getData(key);
		if (data == null)
			return null;
		return Integer.valueOf(data);
	}
	
	@Override
	public boolean equals(Object arg0) {
		
		if (arg0 instanceof Selection) {
			Selection other = (Selection) arg0;
			return other.code.equals(code);
		}
		else if (arg0 instanceof TableColumnValue) {
			TableColumnValue other = (TableColumnValue) arg0;
			return other.getCode().equals(code);
		}
		else
			return super.equals(arg0);
	}
	
	public void print() {
		System.out.println("Code=" + code + ";value=" + description + ";listId=" + listId);
	}
	
	@Override
	public String toString() {
		return "<" + XmlNodes.SELECTION + " " + XmlNodes.SELECTION_CODE_ATTR + "=" + code + ">" 
					+ "<" + XmlNodes.DESCRIPTION + ">" + description + "</" + XmlNodes.DESCRIPTION + ">"
			+ "</" + XmlNodes.SELECTION + ">";
	}
}
