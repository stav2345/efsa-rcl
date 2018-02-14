package report;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import app_config.AppPaths;
import app_config.PropertiesReader;
import config.Environment;
import dataset.Dataset;
import dataset.DatasetList;
import dataset.IDataset;
import dataset.RCLDatasetStatus;
import message_creator.OperationType;
import soap.DetailedSOAPException;
import soap_interface.IGetDatasetsList;
import user.User;

public class ReportService {
	
	private static final Logger LOGGER = LogManager.getLogger(ReportService.class);
	
	private Environment env;
	private IGetDatasetsList<IDataset> getDatasetsList;
	
	/**
	 * Initialize the report service with all the dependencies
	 * @param getDatasetsList
	 */
	public ReportService(Environment env, IGetDatasetsList<IDataset> getDatasetsList) {
		this.env = env;
		this.getDatasetsList = getDatasetsList;
	}
	
	/**
	 * Get all the dataset of a report
	 * @param report
	 * @return
	 * @throws ReportException
	 * @throws DetailedSOAPException
	 */
	public DatasetList getDatasets(EFSAReport report) throws ReportException, DetailedSOAPException {
		
		DatasetList output = new DatasetList();

		String senderDatasetId = report.getSenderId();
		
		if (senderDatasetId == null) {
			throw new ReportException("Cannot retrieve the report sender id for " + report);
		}

		getDatasetsList.getList(env, User.getInstance(), 
				PropertiesReader.getDataCollectionCode(report.getYear()), output);
		
		return output.filterBySenderId(senderDatasetId + AppPaths.REPORT_VERSION_REGEX);
	}
	
	/**
	 * Get the dataset related to the report (only metadata!). 
	 * Note that only the newer one will
	 * be returned. If you need all the datasets related to this report use
	 * {@link #getDatasets()}.
	 * @return
	 * @throws ReportException
	 * @throws DetailedSOAPException 
	 */
	public Dataset getLatestDataset(EFSAReport report) throws ReportException, DetailedSOAPException {

		DatasetList datasets = getDatasets(report);
		
		// use the dataset id if we have it
		if (report.getId() != null && !report.getId().isEmpty()) {
			datasets = datasets.filterByDatasetId(report.getId());
		}
		else {
			
			// otherwise use the sender dataset id to filter
			// the old versions and get the last one
			datasets = datasets.filterOldVersions();
		}
		
		return (Dataset) datasets.getMostRecentDataset();
	}
	
	/**
	 * Given a report and its state, get the operation
	 * that is correct for sending it to the dcf.
	 * For example, if the report was never sent then the operation
	 * will be {@link OperationType#INSERT}.
	 * @param report
	 * @return
	 * @throws ReportException 
	 * @throws DetailedSOAPException 
	 */
	public ReportSendOperation getSendOperation(EFSAReport report) throws DetailedSOAPException, ReportException {
		
		OperationType opType = OperationType.NOT_SUPPORTED;
		
		Dataset dataset = this.getLatestDataset(report);
		
		// if no dataset is present => we do an insert
		if (dataset == null) {
			LOGGER.debug("No valid dataset found in DCF, using INSERT as operation");
			return new ReportSendOperation(null, OperationType.INSERT);
		}
		
		// otherwise we check the dataset status
		RCLDatasetStatus status = dataset.getRCLStatus();
		
		LOGGER.debug("Found dataset in DCF in status " + status);
		
		switch (status) {
		case REJECTED_EDITABLE:
		case VALID:
		case VALID_WITH_WARNINGS:
		case REJECTED:
			opType = OperationType.REPLACE;
			break;
		case DELETED:
			opType = OperationType.INSERT;
			break;
		default:
			opType = OperationType.NOT_SUPPORTED;
			//throw new ReportException("No send operation for status " 
			//		+ status + " is supported");
		}
		
		ReportSendOperation operation = new ReportSendOperation(dataset, opType);
		
		return operation;
	}
	
	/**
	 * Align the report status with the one in DCF
	 * @param report
	 * @return
	 * @throws DetailedSOAPException
	 * @throws ReportException
	 */
	public RCLDatasetStatus alignStatusWithDCF(EFSAReport report) throws DetailedSOAPException, ReportException {
		
		// get the dataset related to the report from the
		// GetDatasetList request
		Dataset dataset = this.getLatestDataset(report);
		
		// if no dataset is retrieved
		if (dataset == null) {
			return report.getRCLStatus();
		}

		// if equal, ok
		if (dataset.getRCLStatus() == report.getRCLStatus())
			return report.getRCLStatus();
		
		// if the report is submitted
		if (report.getRCLStatus() == RCLDatasetStatus.SUBMITTED 
				&& (dataset.getRCLStatus() == RCLDatasetStatus.ACCEPTED_DWH 
					|| dataset.getRCLStatus() == RCLDatasetStatus.REJECTED_EDITABLE)) {
			
			report.setStatus(dataset.getRCLStatus());
		}
		else {
	
			// if not in status submitted
			switch(dataset.getRCLStatus()) {
			// if deleted/rejected then make the report editable
			case DELETED:
			case REJECTED:
				
				// put the report in draft (status automatically changed)
				report.makeEditable();
				return dataset.getRCLStatus();
				//break;
				
			// otherwise inconsistent status
			default:
				break;
			}
			return dataset.getRCLStatus();
		}
		
		return report.getRCLStatus();
	}
}
