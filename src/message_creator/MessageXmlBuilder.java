package message_creator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import amend_manager.DatasetComparison;
import app_config.AppPaths;
import message.MessageConfigBuilder;
import table_skeleton.TableCell;
import table_skeleton.TableColumn;
import table_skeleton.TableRow;

/**
 * Export a collection of {@link TableRow} into an .xml file
 * which contains also an header and an operation block.
 * The header and the operation blocks are defined in the 
 * {@link AppPaths#MESSAGE_CONFIG_SHEET} schema. 
 * @author avonva
 *
 */
public class MessageXmlBuilder implements AutoCloseable {

	private static final Logger LOGGER = LogManager.getLogger(MessageXmlBuilder.class);
	
	private int rowCounter;       // number of processed rows	

	private File file;            // file to create
	private MessageConfigBuilder messageConfig; // configuration to create the message
	private Document gde2Xsd;     // schema of a generic message

	private PrintWriter writer;   // writer of the file


	/**
	 * Export a dataset into the selected file
	 * default operation type = Insert
	 * @param file
	 * @param data list of records that needs to be exported
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public MessageXmlBuilder(File file, MessageConfigBuilder messageConfig) 
			throws ParserConfigurationException, SAXException, IOException {
		
		// get the gde2 .xsd
		XmlReader reader = new XmlReader(AppPaths.MESSAGE_GDE2_XSD);
		this.gde2Xsd = reader.parse();

		this.file = file;
		this.messageConfig = messageConfig;
		this.writer = new PrintWriter(file, "UTF-8");
	}

	/**
	 * Create an empty report (i.e. no dataset, just header/operation)
	 * @return
	 * @throws IOException
	 */
	public File exportEmpty() throws IOException {
		return export(new ArrayList<>());
	}
	
	/**
	 * Export a collection of records into an xml
	 * @throws IOException
	 * @return a handle to the exported file
	 */
	public File export(Collection<DatasetComparison> data) throws IOException {

		// print the header
		printMessage(data);
		
		// close the writer
		writer.close();
		
		return file;
	}
	
	/**
	 * Print the entire message
	 * @param root
	 * @throws IOException
	 */
	private void printMessage(Collection<DatasetComparison> data) throws IOException {
		
		// add xml header
		writer.println("<?xml version='1.0' encoding='UTF-8'?>");
		
		// add first node
		writer.println("<message>");

		// print the header
		printHeader();
		
		// print the payload with the dataset
		printPayload(data);
		
		writer.println("</message>");
	}
	
	/**
	 * print the payload of the message
	 * @throws IOException 
	 */
	private void printPayload(Collection<DatasetComparison> data) throws IOException {
		writer.println("<payload>");
		printOperation();
		printDataset(data);
		writer.println("</payload>");
	}
	
	/**
	 * Print the header of the message
	 * @throws IOException
	 */
	private void printHeader() throws IOException {
		XsdParser parser = new XsdParser(gde2Xsd);
		List<XSElement> headerNodes = parser.getHeaderElements();
		printElementList(headerNodes, "header");
	}
	
	/**
	 * Print the header of the message
	 * @throws IOException
	 */
	private void printOperation() throws IOException {
		XsdParser parser = new XsdParser(gde2Xsd);
		List<XSElement> opNodes = parser.getOperationElements();
		printElementList(opNodes, "operation");
	}
	
	/**
	 * Print in the writer a list of elements
	 * @param list list of elements to be printed
	 * @param nodeName node that will contain all the node of the list in the .xml
	 * @throws IOException
	 */
	private void printElementList(List<XSElement> list, String nodeName) throws IOException {
		
		StringBuilder sb = new StringBuilder();
		
		// get the configuration of the element
		TableRow config = messageConfig.getMessageConfig();
		
		// for each element of the .xsd (in order!)
		for (XSElement element : list) {

			String elementName = element.getName();
			
			if (elementName == null) {
				LOGGER.warn("Null element name found in the xsd file");
				continue;
			}
			
			// get the schema of the column
			TableColumn column = config.getSchema().getById(elementName);
			
			if (column == null) {
				LOGGER.warn("No column found in the message config schema for xsd field " + elementName);
				continue;
			}
			
			if (!column.isPutInOutput(config)) {
				LOGGER.debug("Skipping " + elementName + " since it should not be put in output");
				continue;
			}

			// get the configuration element
			// using the xml node as match
			TableCell value = config.get(elementName);

			if (value == null || value.isEmpty()) {
				LOGGER.warn("No value found for " + elementName 
						+ ". Make sure that it is a not mandatory field for opType " 
						+ messageConfig.getOpType());
				continue;
			}
			
			String nodeValue = value.getLabel();
			
			if (nodeValue == null || nodeValue.isEmpty()) {
				LOGGER.debug("Skipping " + elementName + " since it is empty");
				continue;
			}

			// append the value of the configuration to the xml node
			sb.append(getXmlNode(elementName, nodeValue));
		}
		
		String text = getXmlNode(nodeName, sb.toString());
		
		writer.println(text);
	}
	
	/**
	 * Print the dataset
	 * @throws IOException 
	 */
	private void printDataset(Collection<DatasetComparison> data) throws IOException {
		
		writer.println("<dataset>");
		
		for (DatasetComparison comp : data) {
			print(comp);
		}
		
		writer.println("</dataset>");
	}
	
	/**
	 * Print a single row with its elements
	 * @param row
	 */
	private void print(DatasetComparison row) {

		rowCounter++;
		
		StringBuilder sb = new StringBuilder();
		sb.append(rowCounter)
			.append(" - Exported row id=")
			.append(row.getRowId());
		
		LOGGER.debug(sb.toString());
		
		// print the row nodes into the file
		writer.println(getXmlNode("result", row.getXmlRecord()));
	}
	
	/**
	 * Create a single xml node with the text content
	 * @param nodeName
	 * @param textContent
	 * @return
	 */
	private String getXmlNode(String nodeName, String textContent) {
		
		StringBuilder node = new StringBuilder();
		
		// create the node
		node.append("<")
			.append(nodeName)
			.append(">")
			.append(textContent)
			.append("</")
			.append(nodeName)
			.append(">");
		
		return node.toString();
	}

	@Override
	public void close() throws IOException {
		this.writer.close();
	}
}
