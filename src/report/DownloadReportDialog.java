package report;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import app_config.PropertiesReader;
import dataset.Dataset;
import dataset.DatasetList;
import dataset.IDataset;
import global_utils.Warnings;
import i18n_messages.Messages;
import soap.GetDatasetsList;
import table_dialog.DatasetListDialog;

/**
 * Show the list of dcf datasets. A {@link GetDatasetsList} is performed
 * for each data collection year considered.
 * @author avonva
 *
 */
public class DownloadReportDialog extends DatasetListDialog implements IDownloadReportDialog {

	private static final Logger LOGGER = LogManager.getLogger(DownloadReportDialog.class);
	
	private DatasetList allValidStatusDatasets;
	private DatasetList allValidStatusAndLatestVersionsDatasets;
	private String validSenderIdPattern;
	
	/**
	 * 
	 * @param parent
	 * @param validSenderIdPattern pattern that the sender id field of a
	 * dataset must follow to be considered downloadable (used to filter
	 * the datasets)
	 */
	public DownloadReportDialog(Shell parent, String validSenderIdPattern) {
		super(parent, Messages.get("download.title"), Messages.get("download.button"));
		
		this.validSenderIdPattern = validSenderIdPattern;
	}
	
	public void setDatasetsList(DatasetList datasetsList) {
		
		allValidStatusDatasets = datasetsList.getDownloadableDatasets(validSenderIdPattern);
		
		allValidStatusAndLatestVersionsDatasets = datasetsList.getDownloadableDatasetsLatestVersions(validSenderIdPattern);
		
		allValidStatusAndLatestVersionsDatasets.sort();
		
		setList(allValidStatusAndLatestVersionsDatasets);
	}
	
	public boolean isDuplicated(Dataset d, DatasetList datasets) {
		
		// we are not considering deleted datasets
		List<IDataset> filtered = datasets.stream().filter(new Predicate<IDataset>() {
			@Override
			public boolean test(IDataset arg0) {
				return d.getSenderId().equals(arg0.getSenderId());
			};
		}).collect(Collectors.toList());
		
		return filtered.size() > 1;
	}
	
	/**
	 * Get all the versions of the dataset
	 * @return
	 */
	public DatasetList getSelectedDatasetVersions() {

		Dataset dataset = getSelectedDataset();
		
		if (dataset == null) {
			return null;
		}
		
		
		String senderId = dataset.getDecomposedSenderId();
		LOGGER.debug("Selected dataset with senderId" + senderId);
		
		if (isDuplicated(dataset, allValidStatusDatasets)) {
			
			LOGGER.warn("Duplicated sender dataset id in DCF for senderId=" + senderId);
			
			int val = Warnings.warnUser(getParent(), Messages.get("error.title"), 
					Messages.get("download.duplicate.sender.id", 
							PropertiesReader.getSupportEmail()), 
					SWT.YES | SWT.NO | SWT.ICON_ERROR);
			
			if (val == SWT.NO)
				return null;
		}
		
		if (senderId == null)
			return null;
		
		// get all the versions of the dataset
		return this.allValidStatusDatasets.filterByDecomposedSenderId(senderId);
	}
}
