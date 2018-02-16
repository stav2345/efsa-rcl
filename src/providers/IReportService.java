package providers;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import ack.DcfAck;
import dataset.Dataset;
import dataset.DatasetList;
import global_utils.Message;
import message.MessageConfigBuilder;
import message.MessageResponse;
import message.SendMessageException;
import message_creator.OperationType;
import progress_bar.ProgressListener;
import report.EFSAReport;
import report.Report;
import report.ReportException;
import report.ReportSendOperation;
import soap.DetailedSOAPException;

public interface IReportService {
	
	/**
	 * Check if the report is already present in the db
	 * @param report
	 * @return
	 */
	public boolean isLocallyPresent(String senderDatasetId);
	
	/**
	 * Create a new report
	 * @param report
	 * @return error if something wrong happened
	 * @throws DetailedSOAPException
	 */
	public RCLError create(Report report) throws DetailedSOAPException;
	
	/**
	 * Get the ack of a report using its message id
	 * @param messageId
	 * @return
	 * @throws DetailedSOAPException
	 */
	public DcfAck getAckOf(String messageId) throws DetailedSOAPException;
	
	/**
	 * Get all the dataset related to the report using its
	 * sender dataset id (without version) and its year
	 * to target the correct data collection
	 * @param senderDatasetId
	 * @param dcYear
	 * @return
	 * @throws DetailedSOAPException
	 */
	public DatasetList getDatasetsOf(String senderDatasetId, String dcYear) throws DetailedSOAPException;
	
	/**
	 * Get a dataset from DCF using the dataset id. The list is prefiltered
	 * by the sender dataset id pattern.
	 * @param senderDatasetId sender dataset id of the report (without version)
	 * @param dcYear data collection year
	 * @param datasetId
	 * @return
	 * @throws DetailedSOAPException
	 */
	public Dataset getDatasetById(String senderDatasetId, String dcYear, String datasetId) throws DetailedSOAPException;
	
	/**
	 * Get the latest dataset of the report by computing
	 * the latest version
	 * @param senderDatasetId
	 * @param dcYear
	 * @return
	 * @throws DetailedSOAPException
	 */
	public Dataset getLatestDataset(String senderDatasetId, String dcYear) throws DetailedSOAPException;
	
	/**
	 * Get the latest dataset of the report using its dataset id if possible,
	 * otherwise using the versions
	 * @param report
	 * @return
	 * @throws DetailedSOAPException
	 */
	public Dataset getLatestDataset(EFSAReport report) throws DetailedSOAPException;
	
	/**
	 * Get which send operation will be used if a send action is performed
	 * @param report
	 * @return
	 * @throws DetailedSOAPException
	 * @throws ReportException
	 */
	public ReportSendOperation getSendOperation(EFSAReport report) throws DetailedSOAPException, ReportException;

	/**
	 * Export a report in .xml file
	 * @param report
	 * @param messageConfig
	 * @return
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws ReportException
	 */
	public File export(Report report, MessageConfigBuilder messageConfig) 
			throws IOException, ParserConfigurationException, SAXException, ReportException;
	
	/**
	 * Export a report into an .xml file tracking the progresses
	 * @param report
	 * @param messageConfig
	 * @param progressListener
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ReportException
	 */
	public File export(Report report, MessageConfigBuilder messageConfig, ProgressListener progressListener) 
			throws ParserConfigurationException, SAXException, IOException, ReportException;
	
	/**
	 * Export and send the report with the required operation type. It allows tracking progresses.
	 * @param report
	 * @param opType
	 * @param progressListener
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws SendMessageException
	 * @throws DetailedSOAPException
	 * @throws ReportException
	 */
	public MessageResponse exportAndSend(Report report, OperationType opType, ProgressListener progressListener) 
			throws IOException, ParserConfigurationException, SAXException, SendMessageException, 
			DetailedSOAPException, ReportException;
	
	/**
	 * Export and send the report with the required operation type.
	 * @param report
	 * @param opType
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws SendMessageException
	 * @throws DetailedSOAPException
	 * @throws ReportException
	 */
	public MessageResponse exportAndSend(Report report, OperationType opType) 
			throws DetailedSOAPException, IOException, ParserConfigurationException, 
			SAXException, SendMessageException, ReportException;
	
	
	/**
	 * Refresh the report status
	 * @param report
	 * @return
	 */
	public Message refreshStatus(Report report);
	
	/**
	 * Display an ack
	 * @param messageId
	 * @return
	 */
	public Message displayAck(String messageId);
}
