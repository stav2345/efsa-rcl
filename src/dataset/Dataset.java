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

/**
 * Dcf dataset that is downloaded using the {@link GetDatasetList}
 * request.
 * @author avonva
 *
 */

public class Dataset {
	
	private Header header;
	private Operation operation;
	private Collection<TableRow> rows;
	
	private String id;
	private String senderId;
	private DatasetStatus status;
	
	public Dataset() {
		this.rows = new ArrayList<>();
	}
	
	/**
	 * Given an empty dataset with just the id, senderId and status
	 * (e.g. a dataset downloaded with {@link GetDatasetList})
	 * populate it with the header/operation and the rows by
	 * sending the {@link GetDataset} request to the DCF
	 * @return
	 * @throws SOAPException 
	 */
	public Dataset populate() throws SOAPException {
		
		GetDataset req = new GetDataset(id);
		File file = req.getDatasetFile();

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
	 * Set the dataset sender id (as IT1708)
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
}
