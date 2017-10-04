package dataset;

public class HeaderBuilder {

	private String type;
	private String version;
	private String senderMessageId;
	private String senderOrgCode;
	private String receiverOrgCode;
	
	public void setType(String type) {
		this.type = type;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public void setSenderMessageId(String senderMessageId) {
		this.senderMessageId = senderMessageId;
	}
	public void setSenderOrgCode(String senderOrgCode) {
		this.senderOrgCode = senderOrgCode;
	}
	public void setReceiverOrgCode(String receiverOrgCode) {
		this.receiverOrgCode = receiverOrgCode;
	}
	public Header build() {
		return new Header(type, version, senderMessageId, senderOrgCode, receiverOrgCode);
	}
}
