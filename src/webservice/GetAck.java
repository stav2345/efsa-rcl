package webservice;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import acknowledge.Ack;
import acknowledge.AckLog;
import acknowledge.FileState;

/**
 * Get acknowledge request
 * @author avonva
 *
 */
public class GetAck extends SOAPRequest {

	private static final String URL = "https://dcf-elect.efsa.europa.eu/elect2/";
	private static final String NAMESPACE = "http://dcf-elect.efsa.europa.eu/";
	
	private String messageId;
	
	/**
	 * Get an ack for the chosen message
	 * @param messageId
	 */
	public GetAck(String messageId) {
		super(NAMESPACE);
		this.messageId = messageId;
	}
	
	/**
	 * Get the ack of the message
	 * @return
	 * @throws SOAPException
	 */
	public Ack getAck() throws SOAPException {
		
		Object response = makeRequest(URL);
		
		if (response == null)
			return null;
		
		return (Ack) response;
	}

	@Override
	public SOAPMessage createRequest(SOAPConnection con) throws SOAPException {
		
		// create the standard structure and get the message
		SOAPMessage request = createTemplateSOAPMessage("dcf");

		SOAPBody soapBody = request.getSOAPPart().getEnvelope().getBody();
		
		SOAPElement soapElem = soapBody.addChildElement("GetAck", "dcf");

		SOAPElement arg = soapElem.addChildElement("messageId");
		arg.setTextContent(this.messageId);

		// save the changes in the message and return it
		request.saveChanges();
		
		return request;
	}

	@Override
	public Object processResponse(SOAPMessage soapResponse) throws SOAPException {
		
		// get the state from the response
		FileState state = extractState(soapResponse);
		
		if (state == null) {
			System.err.println("No state found for message: " + messageId);
			return null;
		}
		
		AckLog log = null;
		
		// no attachment in these cases
		if (state != FileState.FAIL && state != FileState.OTHER ) {
			
			log = extractAcklog(soapResponse);
			
			if (log == null) {
				System.err.println("No log found for message: " + messageId);
				return null;
			}
		}
		
		// create the ack object
		Ack ack = new Ack(state, log);
		
		return ack;
	}
	
	/**
	 * Extract the ack state from the response
	 * @param soapResponse
	 * @return
	 * @throws SOAPException
	 */
	private FileState extractState(SOAPMessage soapResponse) throws SOAPException {
		
		// get state
		NodeList children = soapResponse.getSOAPPart()
				.getEnvelope().getBody().getElementsByTagName("fileState");

		if (children.getLength() == 0)
			return null;

		Node stateNode = children.item(0);

		String stateText = stateNode.getTextContent();

		if (stateText.isEmpty())
			return null;

		// get the state from the response
		FileState state = FileState.fromString(stateText);
		
		return state;
	}
	
	/**
	 * Extract the ack log from the attachment
	 * @param soapResponse
	 * @return
	 * @throws SOAPException
	 */
	private AckLog extractAcklog(SOAPMessage soapResponse) throws SOAPException {
		
		Document attachment;
		try {
			
			attachment = getFirstXmlAttachment(soapResponse);
			
			if (attachment == null)
				return null;
			
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			return null;
		}
		
		// get the ack from the attachment
		AckLog log = new AckLog(attachment);
		
		return log;
	}
}
