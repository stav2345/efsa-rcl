package webservice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import app_config.AppPaths;
import release.HttpManager;
import release.ProxyConfigException;
import sun.net.www.protocol.http.AuthCacheImpl;
import sun.net.www.protocol.http.AuthCacheValue;
import user.User;
import zip_manager.ZipManager;

/**
 * Abstract class used to create soap requests and to process soap responses
 * @author avonva
 *
 */

public abstract class SOAPRequest {

	private String namespace;
	private SOAPError error;  // error, if occurred

	/**
	 * Set the url where we make the request and the namespace of the request
	 * @param url
	 * @param namespace
	 */
	public SOAPRequest(String namespace) {
		this.namespace = namespace;
	}
	
	private Proxy getProxy() throws ProxyConfigException {
		return HttpManager.getProxy();
	}
	
	private URL getEndpoint(String stringUrl) throws MalformedURLException, ProxyConfigException {
		
		Proxy proxy = getProxy();
		
		URL endpoint = new URL(null, stringUrl, new URLStreamHandler() {
			
	        protected URLConnection openConnection(URL url) throws IOException {
	        	
	            // The url is the parent of this stream handler, so must
	            // create clone
	            URL clone = new URL(url.toString());

	            URLConnection connection = null;
	            
	            // set the proxy if needed
	            if (proxy == null) {
	                connection = clone.openConnection();
	            } else
	                connection = clone.openConnection(proxy);
	            
	            connection.setConnectTimeout(30000);
	            connection.setReadTimeout(30000);
	            return connection;
	        }
	    });
		
		return endpoint;
	}
	
	/**
	 * Create the request and get the response. Process the response and return the results.
	 * @param soapConnection
	 * @return
	 * @throws MySOAPException
	 */
	public Object makeRequest(String url) throws MySOAPException {

		try {
			// Connect to the url
			SOAPConnectionFactory connectionFactory = SOAPConnectionFactory.newInstance();
			
			// open the connection
			SOAPConnection soapConnection = connectionFactory.createConnection();
	
			// create the request message
			SOAPMessage request = createRequest(soapConnection);
	
			SOAPMessage response;
			try {
				
				URL endpoint = getEndpoint(url);
				
				// get the response
				response = soapConnection.call(request, endpoint);
				
			} catch (MalformedURLException | ProxyConfigException e) {
				e.printStackTrace();
				
				System.err.println("ERROR OCCURRED. Proceeding with NO_PROXY.");
				// get the response with no proxy
				response = soapConnection.call(request, url);
			}
			
			// close the soap connection
			soapConnection.close();
			
			// parse the response and get the result
			return processResponse(response);
		}
		catch (SOAPException e) {
			
			// parse error codes
			throw new MySOAPException(e);
		}
	}
	
	/**
	 * Get the error type if it occurred
	 * @return
	 */
	public SOAPError getSOAPError() {
		return this.error;
	}
	
	/**
	 * Create the standard structure of a SOAPMessage, including the
	 * authentication block
	 * @param username
	 * @param password
	 * @return
	 * @throws SOAPException
	 */

	public SOAPMessage createTemplateSOAPMessage(String prefix) throws SOAPException {
		
		// create the soap message
		MessageFactory msgFactory = MessageFactory.newInstance();
		SOAPMessage soapMsg = msgFactory.createMessage();
		SOAPPart soapPart = soapMsg.getSOAPPart();
		
		// add the content type header
		soapMsg.getMimeHeaders().addHeader("Content-Type", "text/xml;charset=UTF-8");
		
		// reset the cache of the authentication
		AuthCacheValue.setAuthCache(new AuthCacheImpl());
		
		// set the username and password for the https connection
		// in order to be able to authenticate me to the DCF
		Authenticator myAuth = new Authenticator() {
			
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {

				User user = User.getInstance();
				
				if (!user.isLogged())
					return null;
				
				//System.out.println("Connecting with " + user.getUsername() + " " + user.getPassword());
				
				return new PasswordAuthentication(user.getUsername(), 
						user.getPassword().toCharArray());
			}
		};
		
		// set the default authenticator
		Authenticator.setDefault(myAuth);

		// create the envelope and name it
		SOAPEnvelope envelope = soapPart.getEnvelope();
		envelope.addNamespaceDeclaration(prefix, namespace);

		// return the message
		return soapMsg;
	}
	
	/**
	 * Get an xml document starting from a string text formatted as xml
	 * @param xml
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws Exception
	 */
	public static Document getDocument(String xml) 
			throws ParserConfigurationException, SAXException, IOException {

		// create the factory object to create the document object
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    
	    // get the builder from the factory
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    
	    // Set the input source (the text string)
	    InputSource is = new InputSource(new StringReader(xml));
	    
	    // get the xml document and return it
	    return builder.parse(is);
	}
	
	/**
	 * Load an xml document using a file
	 * @param file
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document getDocument(File file) 
			throws ParserConfigurationException, SAXException, IOException {
		
		// create the factory object to create the document object
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    
	    // get the builder from the factory
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    
	    // get the xml document and return it
	    return builder.parse(file);
	}
	
	/**
	 * Get a document from an input stream
	 * @param input
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document getDocument(InputStream input) 
			throws ParserConfigurationException, SAXException, IOException {
		
		// create the factory object to create the document object
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    
	    // get the builder from the factory
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    
	    // get the xml document and return it
	    return builder.parse(input);
	}
	
	/**
	 * Get the first attachment of the message
	 * @param message
	 * @return
	 * @throws SOAPException
	 */
	public AttachmentPart getFirstAttachmentPart(SOAPMessage message) throws MySOAPException {

		// get the attachment
		Iterator<?> iter = message.getAttachments();
		
		if (!iter.hasNext()) {
			System.err.println("No attachment found for ");
			try {
				message.writeTo(System.err);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SOAPException e) {
				e.printStackTrace();
				throw new MySOAPException(e);
			}
			
			return null;
		}

		// get the response attachment
		AttachmentPart attachment = (AttachmentPart) iter.next();
		
		return attachment;
	}
	
	/**
	 * Write a zipped stream into the disk
	 * @param message message containing the attachment
	 * @param attachmentFormat format of the file in the zip file
	 * @return
	 * @throws SOAPException
	 */
	public File writeZippedAttachment(SOAPMessage message, String attachmentFormat) throws SOAPException {
		
		AttachmentPart attachmentPart = getFirstAttachmentPart(message);
		
		if (attachmentPart == null)
			return null;
		
		// save the unzipped stream in temporary file
		File file = new File(AppPaths.TEMP_FOLDER + "attachment_" 
				+ System.currentTimeMillis() + "." + attachmentFormat);
		
		InputStream stream = attachmentPart.getRawContent();
		
		if (stream == null) {
			System.err.println("No raw contents in the attachment found");
			return null;
		}
		
		// unzip the stream into a file
		ZipManager.unzipStream(stream, file);
		
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return file;
	}
	
	/**
	 * Get the xsd from the attachment
	 * @param response
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassCastException
	 * @throws SOAPException
	 * @throws IOException
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	public Document getXsdAttachment(SOAPMessage response) 
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, 
			ClassCastException, SOAPException, IOException, ParserConfigurationException, SAXException {
		
		//String filename = AppPaths.TEMP_FOLDER + "temp_" + System.currentTimeMillis() + ".xsd";
		
		// write the .xsd file
		//File file = new File(filename);
		//writeAttachment(response, file);
		
		AttachmentPart part = getFirstAttachmentPart(response);
		InputStream stream = part.getRawContent();
		
        // parse the document
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(stream); 
		
		/*// load the schema from the input stream
		XSImplementationImpl impl = new XSImplementationImpl();
		XSLoader schemaLoader = impl.createXSLoader(null);
		
		LSInput input = new DOMInputImpl();
		input.setByteStream(stream);
		
		XSModel model = schemaLoader.load(input);
		
		// delete the temporary file
		//file.delete();
		*/
		stream.close();
		
		return doc;
	}
	
	/**
	 * Write the first attachment of the response
	 * @param response
	 * @param file
	 * @throws SOAPException
	 * @throws IOException
	 */
	public void writeAttachment(SOAPMessage response, File file) 
			throws SOAPException, IOException {
		
		// get the attachment
		AttachmentPart attachment = getFirstAttachmentPart(response);
		
		// read attachment and write it
		InputStream inputStream = attachment.getRawContent();
		OutputStream outputStream = new FileOutputStream(file);

		byte[] buf = new byte[512];
		int num;
		
		// write file
		while ( (num = inputStream.read(buf) ) != -1) {
			outputStream.write(buf, 0, num);
		}
		
		outputStream.close();
		inputStream.close();
	}
	
	/**
	 * get the first xml attachment in a dom document
	 * @param message
	 * @return
	 * @throws SOAPException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Document getFirstXmlAttachment(SOAPMessage message) 
			throws SOAPException, ParserConfigurationException, SAXException, IOException {
		
		// get the stream
		InputStream stream = getFirstRawAttachment(message);
		
		if (stream == null)
			return null;
		
		// parse the stream and get the document
		Document xml = getDocument(stream);
		
		// close the stream
		stream.close();
		
		return xml;
	}
	
	/*
	 * get the attachment raw format
	 */
	public InputStream getFirstRawAttachment(SOAPMessage message) throws MySOAPException {
		
		AttachmentPart part = getFirstAttachmentPart(message);
		
		if (part == null)
			return null;
		
		// get the stream
		InputStream stream;
		try {
			stream = part.getRawContent();
		} catch (SOAPException e) {
			e.printStackTrace();
			throw new MySOAPException(e);
		}
		
		return stream;
	}
	
	/**
	 * Create the request message which will be sent to the web service
	 * @param con
	 * @return
	 */
	public abstract SOAPMessage createRequest(SOAPConnection con) throws SOAPException;

	/**
	 * Process the web service response and return something if needed
	 * @param soapResponse the response returned after sending the soap request
	 * @return a processed object. It can be whatever you want, be aware that you
	 * need to cast it to specify its type.
	 */
	public abstract Object processResponse(SOAPMessage soapResponse) throws SOAPException;
}
