package message_creator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Read an .xml file and return the related document
 * @author avonva
 *
 */
public class XmlReader {

	private InputStream stream;
	
	public XmlReader(String filename) throws FileNotFoundException {
		this(new File(filename));
	}
	
	public XmlReader(File file) throws FileNotFoundException {
		this(new FileInputStream(file));
	}
	
	public XmlReader(InputStream stream) {
		this.stream = stream;
	}
	
	/**
	 * Parse an .xml file and return it as document
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Document parse() throws ParserConfigurationException, SAXException, IOException {
		
        // parse the document
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(stream);
        
        stream.close();
        
        return doc;
	}
}
