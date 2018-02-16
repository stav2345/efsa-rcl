package report;

import ack.DcfAck;
import dataset.Dataset;
import dataset.DatasetList;
import global_utils.Message;
import soap.DetailedSOAPException;

public interface IReportService {
	
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
