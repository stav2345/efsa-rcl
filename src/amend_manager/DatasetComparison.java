package amend_manager;

public class DatasetComparison {

	private String rowId;
	private String version;
	private String xmlRecord;
	private AmendType amType;
	private String isNullified;
	
	private StringBuilder xmlRecordBuilder;
	
	public DatasetComparison(String rowId, 
			String version, String xmlRecord, AmendType amType, String isNullified) {
		this.rowId = rowId;
		this.version = version;
		this.xmlRecord = xmlRecord;
		this.amType = amType;
		this.isNullified = isNullified;
	}
	
	public DatasetComparison(String rowId, String version, String xmlRecord) {
		this(rowId, version, xmlRecord, null, null);
	}
	
	/**
	 * Use this constructor only with {@link RowParser}
	 * and populate the object using the setter methods
	 */
	public DatasetComparison() {
		this.xmlRecordBuilder = new StringBuilder();
	}
	
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public void setXmlRecord(String xmlRecord) {
		this.xmlRecord = xmlRecord;
	}
	
	/**
	 * Add a node to the construction of the xml
	 * can only be used if {@link #DatasetComparison()}
	 * constructor was called
	 * @param node
	 */
	public void addXmlNode(String node) {
		
		if (xmlRecordBuilder == null)
			return;
		
		this.xmlRecordBuilder.append(node);
	}
	
	/**
	 * Save the xml built with {@link #addXmlNode(String)}
	 * into the {@link #xmlRecord} variable
	 */
	public void buildXml() {
		
		if (xmlRecordBuilder == null)
			return;
		
		this.xmlRecord = this.xmlRecordBuilder.toString();
	}
	
	public void setAmType(AmendType amType) {
		this.amType = amType;
	}
	
	public void setIsNullified(String isNullified) {
		this.isNullified = isNullified;
	}
	
	public String getRowId() {
		return rowId;
	}
	public String getVersion() {
		return version;
	}
	public String getXmlRecord() {
		return xmlRecord;
	}
	public AmendType getAmType() {
		return amType;
	}
	public String getIsNullified() {
		return isNullified;
	}
	
	@Override
	public String toString() {
		return "rowId=" + rowId
				+ ";version=" + version
				+ ";xmlRecord=" + xmlRecord.substring(0,10)
				+ ";amType=" + amType.getCode();
	}
}
