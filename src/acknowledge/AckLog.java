package acknowledge;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import webservice.GetAck;

/**
 * Log obtained from the {@link GetAck} request
 * @author avonva
 *
 */
public class AckLog {

	private Document log;
	
	public AckLog(Document log) {
		this.log = log;
	}
	
	/**
	 * Get the message val res code
	 * @return
	 */
	public MessageValResCode getMessageValResCode() {
		
		String code = getFirstNodeText("messageValResCode");
		
		if (code == null || code.isEmpty())
			return null;
		
		return MessageValResCode.fromString(code);
	}
	
	/**
	 * Get the operation res code
	 * @return
	 */
	public OkCode getOpResCode() {
		
		String code = getFirstNodeText("opResCode");
		
		if (code == null || code.isEmpty())
			return null;
		
		return OkCode.fromString(code);
	}
	
	/**
	 * Get the error message that is attached to the
	 * operation node. Only present if the {@link #getOpResCode()}
	 * is {@link OkCode#KO}.
	 * @return
	 */
	public String getOpComment() {
		return getFirstNodeText("opResLog");
	}
	
	/**
	 * Get the retrieved dataset id
	 * @return
	 */
	public String getDatasetId() {
		return getFirstNodeText("datasetId");
	}
	
	/**
	 * Get the status of the dataset
	 * @return
	 */
	public AckDatasetStatus getDatasetStatus() {
		
		String code = getFirstNodeText("datasetStatus");
		
		if (code == null || code.isEmpty())
			return null;
		
		return AckDatasetStatus.fromString(code);
	}
	
	/**
	 * Get the text of the first encountered node with name
	 * equal to {@code nodeName}.
	 * @param nodeName
	 * @return
	 */
	public String getFirstNodeText(String nodeName) {
		
		NodeList messageCodeList = log.getElementsByTagName(nodeName);
		
		// if no message code return
		if (messageCodeList.getLength() == 0)
			return null;
		
		Node messageCodeNode = messageCodeList.item(0);
		
		String code = messageCodeNode.getTextContent();
		
		return code;
	}
	
	@Override
	public String toString() {
		return "messageResValCode=" + getMessageValResCode() 
			+ "; opResCode=" + getOpResCode()
			+ "; datasetId=" + getDatasetId()
			+ "; datasetStatus=" + getDatasetStatus();
	}
}
