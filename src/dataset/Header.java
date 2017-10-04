package dataset;

public class Header {

	public enum HeaderNode {
		TYPE("type"),
		VERSION("version"),
		SENDER_MSG_ID("senderMessageId"),
		SENDER_ORG_CODE("senderOrgCode"),
		RECEIVER_ORG_CODE("receiverOrgCode");
		
		private String headerName;
		
		private HeaderNode(String headerName) {
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
		public static HeaderNode fromString(String text) {
			
			for (HeaderNode b : HeaderNode.values()) {
				if (b.headerName.equalsIgnoreCase(text)) {
					return b;
				}
			}
			return null;
		}
	}
	
	private String type;
	private String version;
	private String senderMessageId;
	private String senderOrgCode;
	private String receiverOrgCode;
	
	public Header(String type, String version, String senderMessageId, String senderOrgCode, String receiverOrgCode) {
		this.type = type;
		this.version = version;
		this.senderMessageId = senderMessageId;
		this.senderOrgCode = senderOrgCode;
		this.receiverOrgCode = receiverOrgCode;
	}
	
	public String getType() {
		return type;
	}
	public String getVersion() {
		return version;
	}
	public String getSenderMessageId() {
		return senderMessageId;
	}
	public String getSenderOrgCode() {
		return senderOrgCode;
	}
	public String getReceiverOrgCode() {
		return receiverOrgCode;
	}
	
	@Override
	public String toString() {
		return "Header: type=" + type
				+ ";version=" + version
				+ ";senderMessageId=" + senderMessageId
				+ ";senderOrgCode=" + senderOrgCode
				+ ";receiverOrgCode=" + receiverOrgCode;
	}
}
