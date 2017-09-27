package message_creator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parser that analyzes an .xsd document and retrieves its
 * nodes by using the {@link #getNodeByName(String)} method.
 * @author avonva
 *
 */
public class XsdParser {

	private Document xsd;
	
	public XsdParser(Document xsd) {
		this.xsd = xsd;
	}

	/**
	 * Get a node by its name. Note that the node should be unique in the
	 * file, otherwise just the first will be retrieved
	 * @param nodeName
	 * @return
	 */
	private Element getNodeByName(String nodeName) {
		
		NodeList complexTypes = xsd.getElementsByTagName("xs:complexType");
		
		for (int i = 0; i < complexTypes.getLength(); ++i) {
			Node node = complexTypes.item(i);
			
			if (node instanceof Element) {
				Element element = (Element) node;
				
				String name = element.getAttribute("name");
				
				if (name != null && name.equals(nodeName))
					return element;
			}
		}
		
		return null;
	}
	
	/**
	 * Get all the xs:elements inside of a node. Note that the
	 * node should be unique in the entire document.
	 * @param nodeName
	 * @return
	 */
	public List<XSElement> getNodeElements(String nodeName) {
		
		List<XSElement> out = new ArrayList<>();
		
		Element header = getNodeByName(nodeName);

		NodeList headerNodes = header.getElementsByTagName("xs:element");

		for (int i = 0; i < headerNodes.getLength(); ++i) {
			
			Node node = headerNodes.item(i);
			
			// if we have an xs element
			if (node instanceof Element) {
				
				// get the name of the field
				Element elemNode = (Element) node;
				
				String name = elemNode.getAttribute("name");
				
				// add the element to the list
				XSElement element = new XSElement();
				element.setName(name);
				
				out.add(element);
			}
		}
		
		return out;
	}
	
	/**
	 * Get all the XSElement contained in the header of the xsd
	 * @return
	 */
	public List<XSElement> getHeaderElements() {
		return getNodeElements("headerType");
	}
	
	/**
	 * Get all the XSElement contained in the operation of the xsd
	 * @return
	 */
	public List<XSElement> getOperationElements() {
		return getNodeElements("operationType");
	}
	
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		
		File file = new File("C:\\Users\\avonva\\Desktop\\TseBseReportInterface\\TseBseReportCreator\\temp\\GDE2_message.xsd");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(file);
		
		XsdParser order = new XsdParser(document);
		
		System.out.println(order.getHeaderElements());
		System.out.println(order.getOperationElements());
	}
}
