package amend_manager;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import dataset.Dataset;
import table_skeleton.TableRow;
import xml_catalog_reader.XmlContents;

/**
 * Parser for the .xml dataset and return it into the {@link Dataset} object
 * @author avonva
 *
 */
public class RowParser implements Closeable {

	private String currentNode;
	private TableRow datasetRow;

	private InputStream input;               // input xml
	private XMLEventReader eventReader;      // xml parser
	
	/**
	 * Initialize the parser with a {@link File} object which
	 * contains the path to the file to parse
	 * @param file file which points to the .xml file to parse
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 */
	public RowParser(File file) throws FileNotFoundException, XMLStreamException {
		this(new FileInputStream(file));
	}
	
	/**
	 * Initialize the parser with an {@link InputStream} object
	 * @param input input stream which contains the .xml file to parse
	 * @throws XMLStreamException
	 */
	public RowParser(InputStream input) throws XMLStreamException {
		
		this.input = input;
		this.datasetRow = new TableRow();
		
		// initialize xml parser
		XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty(XMLInputFactory.IS_COALESCING, true);
		this.eventReader = factory.createXMLEventReader(input);
	}

	/**
	 * Parse the .xml document and get the contents in a java object
	 * {@link XmlContents}
	 * @throws XMLStreamException
	 */
	public TableRow parse() throws XMLStreamException {
		
		// for each node of the xml
		while (eventReader.hasNext()) {

			// read the node
			XMLEvent event = eventReader.nextEvent();

			// actions based on the node type
			switch(event.getEventType()) {

			// if starting xml node
			case XMLStreamConstants.START_ELEMENT:
				start(event);
				break;

			// if looking the xml contents
			case XMLStreamConstants.CHARACTERS:
				parseCharacters(event);
				break;

			// if ending xml node
			case  XMLStreamConstants.END_ELEMENT:
				break;
			}
		}
		
		return this.datasetRow;
	}
	
	/**
	 * Parse the a node when it starts
	 * @param event
	 */
	private void start(XMLEvent event) {
		
		StartElement startElement = event.asStartElement();
		this.currentNode = startElement.getName().getLocalPart();
	}
	
	/**
	 * Parse the characters of the xml
	 * @param event
	 */
	private void parseCharacters (XMLEvent event) {
		
		// get the xml node value
		String contents = event.asCharacters().getData();
		
		// cannot parse null content or empty contents
		if (contents == null || currentNode == null || contents.trim().isEmpty())
			return;

		// save the node into the row
		datasetRow.put(currentNode, contents);
	}
	
	/**
	 * Close the parser
	 * @throws IOException 
	 */
	public void close () throws IOException {
		
		if (eventReader != null) {
			try {
				eventReader.close();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}

		eventReader = null;
		
		if (input != null)
			input.close();
	}
}
