package report;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import org.xml.sax.SAXException;

import acknowledge.Ack;
import amend_manager.ReportXmlBuilder;
import dataset.Dataset;
import dataset.DatasetList;
import dataset.DatasetStatus;
import dataset.IDataset;
import message.MessageConfigBuilder;
import message.SendMessageException;
import message_creator.OperationType;
import table_skeleton.TableRow;
import webservice.MySOAPException;

/**
 * Generic EFSA report which contains the basic functionalities
 * that are needed.
 * @author avonva
 *
 */
public interface EFSAReport extends IDataset {

	/**
	 * Get all the report records (all the records, not only the
	 * direct children of the report). Note that the records
	 * defined here are the records which will be exported in
	 * a .xml file.
	 * @return
	 */
	public Collection<TableRow> getRecords();
	
	/**
	 * Export the report into a file. Suggestion: use then {@link ReportXmlBuilder}
	 * to export the data into the file.
	 * @param messageConfig configuration to create the report message
	 */
	public File export(MessageConfigBuilder messageConfig) 
			throws IOException, ParserConfigurationException, SAXException, ReportException;

	/**
	 * Send the report (DCF call)
	 * @param file file which contains the .xml report to send
	 * @param opType the operation type required in the exported file
	 * @throws SOAPException
	 * @throws SendMessageException
	 */
	public void send(File file, OperationType opType) throws SOAPException, SendMessageException;

	/**
	 * Get an acknowledgment of the report (DCF call)
	 * @return
	 * @throws MySOAPException
	 */
	public Ack getAck() throws MySOAPException;
	
	/**
	 * Get all the datasets related to this report
	 * @return
	 * @throws MySOAPException
	 * @throws ReportException
	 */
	public DatasetList<Dataset> getDatasets() throws MySOAPException, ReportException;
	
	/**
	 * get the last dataset related to this report
	 * @return
	 * @throws MySOAPException
	 * @throws ReportException
	 */
	public IDataset getDataset() throws MySOAPException, ReportException;
	
	/**
	 * Force the report to be editable
	 */
	public void makeEditable();
	
	/**
	 * Create a new version of the report
	 * @return
	 */
	public EFSAReport createNewVersion();
	
	/**
	 * Get the message id related to the report if present
	 * @return
	 */
	public String getMessageId();
	
	/**
	 * Get the dataset id related to the report if present
	 * @return
	 */
	public String getDatasetId();
	
	/**
	 * Get the current version of the report
	 * @return
	 */
	public String getVersion();
	
	/**
	 * Get all the report versions that are present locally
	 * @return
	 */
	public ReportList getAllVersions();
	
	/**
	 * Get the previous local version of the report if present
	 * @return
	 */
	public EFSAReport getPreviousVersion();
	
	/**
	 * Check if the version of the report is the first one
	 * @return
	 */
	public boolean isBaselineVersion();
	
	/**
	 * Delete all the report versions from
	 * the database
	 * @return true if ok
	 */
	public boolean deleteAllVersions();
	
	/**
	 * Get the sender dataset id related to the report
	 * this information should be always present
	 * @return
	 */
	public String getSenderId();
	
	/**
	 * Get the current local status of the report
	 * @return
	 */
	public DatasetStatus getStatus();
}
