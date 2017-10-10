package dataset;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;

import table_skeleton.TableRow;
import webservice.GetDataset;
import webservice.GetDatasetList;
import webservice.MySOAPException;

/**
 * Dcf dataset that is downloaded using the {@link GetDatasetList}
 * request.
 * @author avonva
 *
 */

public class Dataset implements Comparable<Dataset> {
	
	private Header header;
	private Operation operation;
	private Collection<TableRow> rows;
	
	private String id;
	private String senderId;
	private DatasetStatus status;

	private static final String COUNTRY = "[a-zA-Z][a-zA-Z]";
	private static final String YEAR = "\\d\\d";
	private static final String MONTH = "\\d\\d";
	private static final String VERSION = "(\\.((0\\d)|(\\d\\d)))?";  // either .01, .02 or .10, .50 (always two digits)
	
	public static final String VALID_SENDER_ID_PATTERN = COUNTRY + YEAR + MONTH + VERSION;
	
	public Dataset() {
		this.rows = new ArrayList<>();
	}
	
	/**
	 * Get the sender id from a composed sender id of the dataset if possible.
	 * Only usable with senderId in format CountryYearMonth.Version (as FR1705.01)
	 * in the example, this would return FR1705
	 * @return
	 */
	public String getDecomposedSenderId() {
		
		String[] split = splitSenderId();
		if (split == null)
			return this.senderId;
		
		return split[0];
	}
	
	/**
	 * Get the version of the dataset if possible.
	 * Only usable with senderId in format CountryYearMonth.Version (as FR1705.01)
	 * @return
	 */
	public String getVersion() {
		
		String[] split = splitSenderId();
		if (split == null)
			return null;
		
		return split[1];
	}
	
	private String[] splitSenderId() {
		return splitSenderId(this.senderId);
	}
	
	public static String[] splitSenderId(String senderId) {
		
		if (senderId == null)
			return null;
		
		if (!senderId.matches(VALID_SENDER_ID_PATTERN))
			return null;
		
		String[] split = senderId.split("\\.");
		
		if (split.length != 2)
			return null;
		
		return split;
	}
	
	/**
	 * Given an empty dataset with just the id, senderId and status
	 * (e.g. a dataset downloaded with {@link GetDatasetList})
	 * populate it with the header/operation and the rows by
	 * sending the {@link GetDataset} request to the DCF
	 * @return
	 * @throws SOAPException 
	 */
	public Dataset populate() throws MySOAPException {
		
		GetDataset req = new GetDataset(id);
		File file = req.getDatasetFile();
		
		if (file == null)
			return null;

		try {
			
			DatasetParser parser = new DatasetParser(file);
			Dataset populatedDataset = parser.parse();
			populatedDataset.setId(id);
			populatedDataset.setSenderId(senderId);
			populatedDataset.setStatus(status);
			return populatedDataset;
			
		} catch (FileNotFoundException | XMLStreamException e) {
			e.printStackTrace();
		}
		
		return this;
	}
	
	public void addRow(TableRow row) {
		this.rows.add(row);
	}
	
	public void setHeader(Header header) {
		this.header = header;
	}
	
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
	public Operation getOperation() {
		return operation;
	}
	
	public Header getHeader() {
		return header;
	}
	
	/**
	 * Set the dataset id
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Set the dataset sender id (as IT1708.01)
	 * note that the version is contained in it
	 * @param senderId
	 */
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
	
	/**
	 * Set the dataset status
	 * @param status
	 */
	public void setStatus(DatasetStatus status) {
		this.status = status;
	}
	
	/**
	 * Get the dataset id
	 * @return
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Get the dataset sender id (id given
	 * by the data provider)
	 * @return
	 */
	public String getSenderId() {
		return senderId;
	}
	
	public Collection<TableRow> getRows() {
		return rows;
	}
	
	/**
	 * Get the dataset status
	 * @return
	 */
	public DatasetStatus getStatus() {
		return status;
	}

	/**
	 * Check if the dataset can be edited or not
	 * @return
	 */
	public boolean isEditable() {

		if (status == null)
			return false;

		return status.isEditable();
	}

	@Override
	public String toString() {
		return "Dataset: id=" + id 
				+ ";senderId=" + senderId 
				+ ";status=" + status
				+ " " + header
				+ " " + operation;
	}
	
	@Override
	public boolean equals(Object arg0) {
		
		if (arg0 instanceof Dataset) {
			return this.senderId.equalsIgnoreCase(((Dataset) arg0).getSenderId());
		}
		
		return super.equals(arg0);
	}

	@Override
	public int compareTo(Dataset arg0) {
		
		String senderId = arg0.getDecomposedSenderId();
		String version = arg0.getVersion();
		
		String mySenderId = this.getDecomposedSenderId();
		String myVersion = this.getVersion();
		
		if (senderId == null || mySenderId == null)
			return 0;
		
		if (!senderId.equalsIgnoreCase(mySenderId))
			return mySenderId.compareTo(senderId);
		
		if (version == null && myVersion == null)
			return 0;
		
		// if the other don't have version i am last
		if (version == null && myVersion != null)
			return 1;
		
		// if i don't have version i am previous
		if (version != null && myVersion == null)
			return -1;
		
		return myVersion.compareTo(version);
	}
}
