package export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Stack;

import app_config.AppPaths;
import app_config.PropertiesReader;
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
public abstract class DatasetXmlCreator {
	
	/*private Document xsdModel;
	
	public DatasetXmlCreator(Document xsdModel) {
		this.xsdModel = xsdModel;
	}*/

	private int rowCounter;      // number of processed rows
	private File file;           // file to create
	private PrintWriter writer;  // writer of the file
	
	public DatasetXmlCreator(String filename) throws FileNotFoundException {
		this(new File(filename));
	}
	
	/**
	 * Export a dataset into the selected file
	 * @param file
	 * @throws FileNotFoundException
	 */
	public DatasetXmlCreator(File file) throws FileNotFoundException {
		this.file = file;
		this.writer = new PrintWriter(file);
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
		writer.println("<message xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
				+ "xsi:noNamespaceSchemaLocation="
				+ "'file:///D:/Work%20in%20progress/GDE_MasterVersioning/GDE-Schemas/SSD/GDE2_message.xsd'>");

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
	 * print the operation node
	 * @throws IOException
	 */
	private void printOperation() throws IOException {
		
		writer.print("<operation>");
		
		// add the operation nodes
		printMetaData("operation", false);
		
		// add action node
		String opType = getXmlNode("opType", "Insert");
		writer.print(opType);
		
		// add data collection node
		String dc = getXmlNode("dcCode", PropertiesReader.getDataCollectionCode());
		writer.print(dc);
		
		// add the comment node with the tool name and version
		String toolName = PropertiesReader.getAppName();
		String version = PropertiesReader.getAppVersion();
		
		String comment = getXmlNode("opCom", "File generated with " + toolName + " - version " + version);
		writer.print(comment);

		writer.println("</operation>");
	}
	
	/**
	 * Print the dataset
	 * @throws IOException 
	 */
	private void printDataset(TableRow root) throws IOException {
		
		writer.println("<dataset>");
		
		Stack<TableRow> nodes = new Stack<>();  // depth-first exploration
		
		// add the root to the stack
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
	
	private void printHeader() throws IOException {
		printMetaData("header", true);
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

		// inject the parents
		for (TableRow parent : getConfigMessageParents()) {
			Relation.injectParent(parent, row);
		}
		
		row.updateFormulas();
		
		return row;
	}
	
	/**
	 * Print a meta data node
	 * @param type
	 * @throws IOException
	 */
	private void printMetaData(String type, boolean addEnvelop) throws IOException {
		
		StringBuilder sb = new StringBuilder();
		
		TableRow config = getMessageConfig();
		
		for (TableColumn column : config.getSchema()) {
			
			// consider only header elements
			if (!column.getPutInOutput().equalsIgnoreCase(type))
				continue;
			
			// get the value of the row
			TableColumnValue value = config.get(column.getId());
			
			// append the xml header node
			sb.append(getXmlNode(column.getXmlTag(), value.getLabel()));
		}
		
		String text = addEnvelop ? getXmlNode(type, sb.toString()) : sb.toString();
		
		writer.println(text);
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
