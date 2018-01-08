package report;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import org.xml.sax.SAXException;

import ack.DcfAck;
import amend_manager.ReportXmlBuilder;
import dataset.DatasetList;
import dataset.IDataset;
import dataset.RCLDatasetStatus;
import message.MessageConfigBuilder;
import message.SendMessageException;
import message_creator.OperationType;
import progress_bar.ProgressListener;
import soap.MySOAPException;
import table_skeleton.TableRow;

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
	public File export(MessageConfigBuilder messageConfig, ProgressListener progressListener) 
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
	 * Submit the report to the DCF
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws SendMessageException
	 * @throws MySOAPException
	 * @throws ReportException
	 */
	public void submit() throws IOException, 
		ParserConfigurationException, SAXException, SendMessageException, 
		MySOAPException, ReportException;
	
	/**
	 * Reject the report in the DCF
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws SendMessageException
	 * @throws MySOAPException
	 * @throws ReportException
	 */
	public void reject() throws IOException, 
		ParserConfigurationException, SAXException, SendMessageException, 
		MySOAPException, ReportException;
	
	/**
	 * Get an acknowledgment of the report (DCF call)
	 * @return
	 * @throws MySOAPException
	 */
	public DcfAck getAck() throws MySOAPException;
	
	/**
	 * Update the status of the report with the one contained in the ack
	 * @return
	 * @throws MySOAPException
	 */
	public RCLDatasetStatus updateStatusWithAck(DcfAck ack);
	
	/**
	 * Get all the datasets related to this report
	 * @return
	 * @throws MySOAPException
	 * @throws ReportException
	 */
	public DatasetList getDatasets() throws MySOAPException, ReportException;
	
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
	public EFSAReport amend();
	
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
	 * Set the dataset id of the dataset related to the report
	 * @param id
	 */
	public void setDatasetId(String id);
	
	/**
	 * Get the default configuration used to export the report
	 * @param opType
	 * @return
	 */
	public MessageConfigBuilder getDefaultExportConfiguration(OperationType opType);
	
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
	 * Get the current local status of the report
	 * @return
	 */
	public RCLDatasetStatus getRCLStatus();
	
	/**
	 * Update the dataset status
	 * @param status
	 */
	public void setStatus(String status);
	
	/**
	 * Update the dataset status
	 * @param status
	 */
	public void setStatus(RCLDatasetStatus status);
	
	/**
	 * Align the report status with the one in the DCF
	 * if possible
	 * @return the new status
	 */
	public RCLDatasetStatus alignStatusWithDCF() throws MySOAPException, ReportException;
	
	/**
	 * Get the sender dataset id related to the report.
	 * This information should be always present in the report.
	 * @return
	 */
	public String getSenderId();
	
	/**
	 * Check if the report is correct or not
	 * @return
	 */
	public boolean isValid();
}
