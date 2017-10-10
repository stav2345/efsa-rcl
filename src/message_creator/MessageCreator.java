package message_creator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import app_config.AppPaths;
import table_list.TableMetaData;
import table_relations.Relation;
import table_skeleton.TableColumn;
import table_skeleton.TableColumnValue;
import table_skeleton.TableRow;
import xlsx_reader.TableSchema;
import xlsx_reader.TableSchemaList;

/**
 * Class used to write the content of a {@link TableRow} object 
 * with all its children tables into an .xml file. The export is
 * created with flat records, that is, each records corresponds to
 * a leaf of the family tree of the {@link TableRow}, in which
 * the parents data are replicated for each leaf (as SSD2 does).
 * @author avonva
 *
 */
public abstract class MessageCreator {
	
	/*private Document xsdModel;
	
	public DatasetXmlCreator(Document xsdModel) {
		this.xsdModel = xsdModel;
	}*/

	private int rowCounter;       // number of processed rows
	private File file;            // file to create
	private PrintWriter writer;   // writer of the file
	
	private OperationType opType; // operation which needs to be done for the dataset
	
	private Document gde2Xsd;
	
	/**
	 * Export a dataset into the selected file
	 * default operation type = Insert
	 * default data collection code = the one set in the properties
	 * @param file
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public MessageCreator(File file) throws ParserConfigurationException, SAXException, IOException {
		
		// get the gde2 .xsd
		XmlReader reader = new XmlReader(AppPaths.MESSAGE_GDE2_XSD);
		this.gde2Xsd = reader.parse();

		this.file = file;
		this.writer = new PrintWriter(file);

		this.opType = OperationType.INSERT;
	}
	
	/**
	 * Set the operation type for the report
	 * @param opType
	 */
	public void setOpType(OperationType opType) {
		this.opType = opType;
	}
	
	/**
	 * Create an empty report (i.e. no dataset, just header/operation)
	 * @return
	 * @throws IOException
	 */
	public File exportEmpty() throws IOException {
		return export(null);
	}
	
	/**
	 * Export a {@link TableRow} object with all its children tables.
	 * Note that each record will be created as a flat one, 
	 * in the sense that all the parent information will be
	 * replicated at the leaves level. This means that in the end we will have
	 * as number of records the number of leaves.
	 * @param root
	 * @throws IOException
	 * @return a handle to the exported file
	 */
	public File export(TableRow root) throws IOException {

		// print the header
		printMessage(root);
		
		// close the writer
		writer.close();
		
		return file;
	}
	
	/**
	 * Print the entire message
	 * @param root
	 * @throws IOException
	 */
	private void printMessage(TableRow root) throws IOException {
		
		// add xml header
		writer.println("<?xml version='1.0' encoding='UTF-8'?>");
		
		// add first node
		writer.println("<message>");

		// print the header
		printHeader();
		
		// print the payload with the dataset
		printPayload(root);
		
		writer.println("</message>");
	}
	
	/**
	 * print the payload of the message
	 * @throws IOException 
	 */
	private void printPayload(TableRow root) throws IOException {
		writer.println("<payload>");
		printOperation();
		printDataset(root);
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
		TableRow config = getMessageConfig();
		
		// for each element of the .xsd (in order!)
		for (XSElement element : list) {

			String elementName = element.getName();
			
			if (elementName == null) {
				System.err.println("Null element name found during the export.");
				continue;
			}
			
			// get the schema of the column
			TableColumn column = config.getSchema().getById(elementName);
			
			if (column == null) {
				System.out.println("No column found in the message config schema for xsd field " + elementName);
				continue;
			}
			
			if (!column.isPutInOutput(config)) {
				System.out.println("Skipping " + elementName + " since it should not be put in output");
				continue;
			}

			// get the configuration element
			// using the xml node as match
			TableColumnValue value = config.get(elementName);

			if (value == null || value.isEmpty()) {
				System.err.println("No value found for " + elementName 
						+ ". Make sure that it is a not mandatory field for opType " + opType);
				continue;
			}
			
			String nodeValue = value.getLabel();

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
	private void printDataset(TableRow root) throws IOException {
		
		writer.println("<dataset>");
		
		Stack<TableRow> nodes = new Stack<>();  // depth-first exploration
		
		// add the root to the stack
		// null check is done since it is possible
		// to create an empty report setting the 
		// root to null
		if (root != null)
			nodes.add(root);
		
		// until we have something
		while (!nodes.isEmpty()) {

			// get the current node (this removes it from the stack)
			TableRow currentNode = nodes.pop();
			
			// get the metadata of the parent table
			TableMetaData table = TableMetaData.getTableByName(currentNode
					.getSchema().getSheetName());

			// print the node if needed
			if (table != null && table.isGenerateRecord()) {
				print(currentNode);
			}

			// get all the children of the current node (i.e. the tables that
			// are directly children of the current node, not nephews etc)
			Collection<Relation> relations = currentNode.getSchema().getDirectChildren();
			
			// for each table which is direct child of the current node
			for (Relation r : relations) {

				// get the rows of the children related to the parent
				Collection<TableRow> children = currentNode.getChildren(r.getChildSchema());
				
				// if we have something then add all the children to the
				// stack in order to process them later
				if (!children.isEmpty()) {
					nodes.addAll(children);
				}
			}
		}
		
		writer.println("</dataset>");
	}
	
	/**
	 * Print a single row with its elements
	 * @param row
	 */
	private void print(TableRow row) {

		rowCounter++;
		
		StringBuilder sb = new StringBuilder();
		sb.append(rowCounter)
			.append(" - Exported row id=")
			.append(row.getId())
			.append(" of table ")
			.append(row.getSchema().getTableIdField());
		
		System.out.println(sb.toString());
		
		// update row values before making the output
		row.updateFormulas();
		
		// print the row nodes into the file
		writer.println(getRowXmlNode(row));
	}
	
	/**
	 * Create all the nodes required for a single row
	 * adding the row separation node (<result>)
	 * @param row
	 * @return
	 */
	private String getRowXmlNode(TableRow row) {
		
		StringBuilder sb = new StringBuilder();
		
		for (TableColumn column : row.getSchema()) {

			// skip non output columns
			if (!column.isPutInOutput(row))
				continue;

			String node = getXmlNode(column.getXmlTag(), row.get(column.getId()).getCode());

			// write the node
			sb.append(node);
		}
		
		// envelop all the row nodes into the result node
		return getXmlNode("result", sb.toString());
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
	
	/**
	 * Get the configuration for the export
	 * @return
	 * @throws IOException 
	 */
	private TableRow getMessageConfig() throws IOException {
		
		TableSchema schema = TableSchemaList.getByName(AppPaths.MESSAGE_CONFIG_SHEET);

		// create the header row
		TableRow row = new TableRow(schema);
		row.initialize();

		// add the op type to the row
		row.put(AppPaths.MESSAGE_CONFIG_OP_TYPE, opType.getCode());
		
		Collection<TableRow> parents = getConfigMessageParents();
		
		if (parents != null) {
			
			// inject the parents
			for (TableRow parent : parents) {
				Relation.injectParent(parent, row);
			}
		}
		
		row.updateFormulas();
		
		return row;
	}
	
	/**
	 * get all the parents table that are used in the {@link AppPaths#MESSAGE_CONFIG_SHEET}
	 * to compute the node formulas (i.e. all the tables used in the RELATION statements)
	 * @return
	 */
	public abstract Collection<TableRow> getConfigMessageParents();
	
	
	/**
	 * Get all the elements of a root node
	 * @param root
	 * @return
	 */
	/*private Collection<XSElement> getElements(Element root) {

		Collection<XSElement> list = new ArrayList<>();
		
		NodeList elements = root.getElementsByTagName("xs:element");
		
		for (int i = 0; i < elements.getLength(); ++i) {
			
			Element node = (Element) elements.item(i);
			
			XSElement xsElem = new XSElement();
			
			if (node.hasAttribute("name"))
				xsElem.setName(node.getAttribute("name"));
			
			if (node.hasAttribute("ref"))
				xsElem.setRef(node.getAttribute("ref"));
			
			list.add(xsElem);
		}
		
		return list;
	}*/
	
	/**
	 * Get a list of {@link XSElement} which represents the xml node of the data set
	 * @param datasetNodeName
	 * @return
	 */
	/*private Collection<XSElement> getDatasetSchema(String datasetNodeName) {
		
		Collection<XSElement> datasetElements = new ArrayList<>();
		
		Element main = xsdModel.getDocumentElement();
		
		// get first root
		Node childNode = main.getFirstChild();
		
		// search in the root nodes
		while (childNode.getNextSibling() != null) {
			
			// get next root
			childNode = childNode.getNextSibling();
			
			// get elements only
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				
				// get root node
				Element childElement = (Element) childNode;
				
				// skip element without name
				if (!childElement.hasAttribute("name"))
					continue;
				
				// if dataset node parse children nodes and return them
				// (we do not need to go on, we need just this)
				if (childElement.getAttribute("name").equals(datasetNodeName)) {
					datasetElements = getElements(childElement);
					break;
				}
			}
		}
		
		return datasetElements;
	}*/
}
