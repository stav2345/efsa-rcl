package acknowledge;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dataset.DatasetStatus;
import webservice.GetAck;

/**
 * Log obtained from the {@link GetAck} request
 * @author avonva
 *
 */
public class AckLog {

	private InputStream rawLog;
	private Document log;
	
	public AckLog(Document log) {
		this.log = log;
	}
	
	public void setRawLog(InputStream rawLog) {
		this.rawLog = rawLog;
	}
	
	public InputStream getRawLog() {
		return rawLog;
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
	
	public boolean isOk() {
		
		OkCode ok = getOpResCode();
		
		if(ok == null)
			return false;
		
		return ok == OkCode.OK;
	}
	
	/**
	 * Get the error message that is attached to the
	 * operation node. Only present if the {@link #getOpResCode()}
	 * is {@link OkCode#KO}.
	 * @return
	 */
	public Collection<String> getOpResLog() {
		return getNodesText("opResLog");
	}
	
	public boolean hasErrors() {
		return !getOpResLog().isEmpty();
	}
	
	/**
	 * Get the type of error if present
	 * @return
	 */
	public OpResError getOpResError() {
		
		Collection<String> errors = getOpResLog();
		
		OpResError error = OpResError.NONE;
		
		for (String errorText : errors) {
			
			OpResError current = OpResError.fromString(errorText);
			
			if (current.priorTo(error))
				error = current;
		}
		
		return error;
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
	public DatasetStatus getDatasetStatus() {
		
		String code = getFirstNodeText("datasetStatus");
		
		if (code == null || code.isEmpty())
			return null;
		
		return DatasetStatus.fromString(code);
	}
	
	/**
	 * Get the text of the first encountered node with name
	 * equal to {@code nodeName}.
	 * @param nodeName
	 * @return
	 */
	public Collection<String> getNodesText(String nodeName) {
		
		Collection<String> text = new ArrayList<>();
		
		NodeList nodes = log.getElementsByTagName(nodeName);
		
		for (int i = 0; i < nodes.getLength(); ++i) {
			text.add(nodes.item(i).getTextContent());
		}

		return text;
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
