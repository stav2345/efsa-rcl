package report;

import app_config.AppPaths;
import app_config.PropertiesReader;
import dataset.DatasetList;
import dataset.IDataset;
import soap.DetailedSOAPException;
import soap_interface.IGetDatasetsList;

public class ReportService {

	private IGetDatasetsList<IDataset> getDatasetsList;
	
	public ReportService(IGetDatasetsList<IDataset> getDatasetsList) {
		this.getDatasetsList = getDatasetsList;
	}
	
	public DatasetList getDatasetsOf(Report report) throws DetailedSOAPException {
		
		DatasetList output = new DatasetList();
		String dcCode = PropertiesReader.getDataCollectionCode(report.getYear());
		getDatasetsList.getList(dcCode, output);
		
		output.filterBySenderId(report.getSenderId() + AppPaths.REPORT_VERSION_REGEX);
		
		return output;
	}
}
