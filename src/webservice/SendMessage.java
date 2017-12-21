package webservice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.NodeList;

import message.MessageResponse;
import message.TrxCode;

/**
 * SendMessage request to the dcf
 * @author avonva
 *
 */
public class SendMessage extends SOAPRequest {

	private static final String URL = "https://dcf-elect.efsa.europa.eu/elect2/";
	private static final String NAMESPACE = "http://dcf-elect.efsa.europa.eu/";
	
	private File file;
	
	/**
	 * Send the {@code file} to the dcf
	 * as message.
	 * @param file
	 */
	public SendMessage(File file) {
		super(NAMESPACE);
		this.file = file;
	}
	
	/**
	 * Send a dataset to the dcf
	 * @param filename
	 */
	public MessageResponse send() throws MySOAPException {
		
		Object response = makeRequest(URL);
		
		if (response == null)
			return null;
		
		return (MessageResponse) response;
	}

	@Override
	public SOAPMessage createRequest(SOAPConnection con) throws SOAPException {
		
		// create the standard structure and get the message
		SOAPMessage request = createTemplateSOAPMessage("dcf");

		SOAPBody soapBody = request.getSOAPPart().getEnvelope().getBody();
		
		SOAPElement soapElem = soapBody.addChildElement("SendMessage", "dcf");

		SOAPElement trxFileMessage = soapElem.addChildElement("trxFileMessage");
		
		SOAPElement fileHandler = trxFileMessage.addChildElement("fileHandler");

		SOAPElement arg = trxFileMessage.addChildElement("fileName");
		arg.setTextContent(this.file.getName());

		// add attachment to fileHandler node
		Path path = Paths.get(this.file.getAbsolutePath());
		
		// read the file as byte array and encode it in base64
		try {
			byte[] data = Files.readAllBytes(path);
			byte[] encodedData = Base64.getEncoder().encode(data);
			fileHandler.setValue(new String(encodedData));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// save the changes in the message and return it
		request.saveChanges();

		return request;
	}

	@Override
	public Object processResponse(SOAPMessage soapResponse) throws SOAPException {
		
		// extract the information
		String messageId = extractMessageId(soapResponse);
		TrxCode trxCode = extractTrxState(soapResponse);
		String trxErr = extractTrxError(soapResponse);
		
		// create the response
		MessageResponse messageResponse = new MessageResponse(messageId, trxCode, trxErr);
		
		return messageResponse;
	}
	
	/**
	 * Extract the message id from the response
	 * @param soapResponse
	 * @return
	 * @throws SOAPException
	 */
	private String extractMessageId(SOAPMessage soapResponse) throws SOAPException {
		
		NodeList msgId = soapResponse.getSOAPPart()
				.getEnvelope().getBody().getElementsByTagName("messageId");
		
		if (msgId.getLength() == 0)
			return null;
		
		return msgId.item(0).getTextContent();
	}
	
	/**
	 * Extract the trx state from the response
	 * @param soapResponse
	 * @return
	 * @throws SOAPException
	 */
	private TrxCode extractTrxState(SOAPMessage soapResponse) throws SOAPException {
		
		NodeList trxStateList = soapResponse.getSOAPPart()
				.getEnvelope().getBody().getElementsByTagName("trxState");
		
		if (trxStateList.getLength() == 0)
			return null;
		
		String trxState = trxStateList.item(0).getTextContent();
		
		return TrxCode.fromString(trxState);
	}
	
	/**
	 * Extract the trx error if present from the response
	 * @param soapResponse
	 * @return
	 * @throws SOAPException
	 */
	private String extractTrxError(SOAPMessage soapResponse) throws SOAPException {
		
		NodeList msgId = soapResponse.getSOAPPart()
				.getEnvelope().getBody().getElementsByTagName("trxErr");
		
		if (msgId.getLength() == 0)
			return null;
		
		return msgId.item(0).getTextContent();
	}
}
