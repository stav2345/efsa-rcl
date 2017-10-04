package dataset;

public class OperationBuilder {
	private String opType;
	private String datasetId;
	private String senderDatasetId;
	private String dcCode;
	private String dcTable;
	private String orgCode;
	private String opCom;
	
	public void setOpType(String opType) {
		this.opType = opType;
	}
	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}
	public void setSenderDatasetId(String senderDatasetId) {
		this.senderDatasetId = senderDatasetId;
	}
	public void setDcCode(String dcCode) {
		this.dcCode = dcCode;
	}
	public void setDcTable(String dcTable) {
		this.dcTable = dcTable;
	}
	public void setOrgCode(String orgCode) {
		this.orgCode = orgCode;
	}
	public void setOpCom(String opCom) {
		this.opCom = opCom;
	}
	public Operation build() {
		return new Operation(opType, datasetId, senderDatasetId, dcCode, dcTable, orgCode, opCom);
	}
}
