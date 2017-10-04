package dataset;

public class Operation {
	
	public enum OperationNode {

		OP_TYPE("opType"),
		DATASET_ID("datasetId"),
		SENDER_DATASET_ID("senderDatasetId"),
		DC_CODE("dcCode"),
		DC_TABLE("dcTable"),
		ORG_CODE("orgCode"),
		OP_COM("opCom");
		
		private String headerName;
		
		private OperationNode(String headerName) {
			this.headerName = headerName;
		}
		
		public String getHeaderName() {
			return headerName;
		}
		
		/**
		 * Get the enumerator that matches the {@code text}
		 * @param text
		 * @return
		 */
		public static OperationNode fromString(String text) {
			
			for (OperationNode b : OperationNode.values()) {
				if (b.headerName.equalsIgnoreCase(text)) {
					return b;
				}
			}
			return null;
		}
	}
	
	private String opType;
	private String datasetId;
	private String senderDatasetId;
	private String dcCode;
	private String dcTable;
	private String orgCode;
	private String opCom;
	
	public Operation(String opType, String datasetId, String senderDatasetId, String dcCode, String dcTable, String orgCode, String opCom) {
		this.opType = opType;
		this.datasetId = datasetId;
		this.senderDatasetId = senderDatasetId;
		this.dcCode = dcCode;
		this.dcTable = dcTable;
		this.orgCode = orgCode;
		this.opCom = opCom;
	}
	
	public String getOpType() {
		return opType;
	}
	public String getDatasetId() {
		return datasetId;
	}
	public String getSenderDatasetId() {
		return senderDatasetId;
	}
	public String getDcCode() {
		return dcCode;
	}
	public String getDcTable() {
		return dcTable;
	}
	public String getOrgCode() {
		return orgCode;
	}
	public String getOpCom() {
		return opCom;
	}
	
	@Override
	public String toString() {
		return "Operation opType=" + opType
				+ ";datasetId=" + datasetId
				+ ";senderDatasetId=" + senderDatasetId
				+ ";dcCode=" + dcCode
				+ ";dcTable=" + dcTable
				+ ";orgCode=" + orgCode
				+ ";opCom=" + opCom;
	}
}
