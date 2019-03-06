package xml_catalog_reader;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import table_skeleton.TableCell;

/**
 * Class which models a single node of a configuration .xml file
 * It also represents a single cell of the report (i.e. a value of a column of the report table)
 * @author avonva && shahaal
 *
 */
public class Selection {
	
	private static final Logger LOGGER = LogManager.getLogger(Selection.class);
	
	private String listId;       // list in which the selection is present (BSE/SCRAPIE...)
	private String code;         // code of the selection (identifies a value of the list)
	private String description;  // label of the selection
	private HashMap<String, String> data;         // optional data
	
	public Selection() {
		data = new HashMap<>();
	}
	
	/**
	 * shahaal
	 * used when cloning rows
	 * @param code
	 * @param label
	 */
	public Selection(TableCell cell) {
		this.code=cell.getCode();
		this.description = cell.getLabel();
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
		else if (arg0 instanceof TableCell) {
			TableCell other = (TableCell) arg0;
			return other != null && other.getCode() != null 
					&& other.getCode().equals(code);
		}
		else
			return super.equals(arg0);
	}
	
	public void print() {
		LOGGER.debug("Code=" + code + ";value=" + description + ";listId=" + listId);
	}
	
	@Override
	public String toString() {
		return "<" + XmlNodes.SELECTION + " " + XmlNodes.SELECTION_CODE_ATTR + "=" + code + ">" 
					+ "<" + XmlNodes.DESCRIPTION + ">" + description + "</" + XmlNodes.DESCRIPTION + ">"
			+ "</" + XmlNodes.SELECTION + ">";
	}
}
