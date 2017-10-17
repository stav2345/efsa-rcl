package report;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import javax.xml.soap.SOAPException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import app_config.PropertiesReader;
import dataset.Dataset;
import dataset.DatasetList;
import table_dialog.DatasetListDialog;
import webservice.GetDatasetList;

/**
 * Show the list of dcf datasets
 * @author avonva
 *
 */
public class DownloadReportDialog extends DatasetListDialog {
	
	private DatasetList<Dataset> allDatasets;
	
	/**
	 * 
	 * @param parent
	 * @param validSenderIdPattern pattern that the sender id field of a
	 * dataset must follow to be considered downloadable (used to filter
	 * the datasets)
	 */
	public DownloadReportDialog(Shell parent, String validSenderIdPattern) {
		
		super(parent, "Available reports", "Download");
		
		parent.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
		
		this.allDatasets = getDownloadableDatasets(validSenderIdPattern);
		
		this.setList(allDatasets);
		
		parent.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		
		this.open();
	}

	/**
	 * Get a list of all the dcf datasets which are downloadable
	 * @param validSenderIdPattern pattern that the sender id field of a
	 * dataset must follow to be considered downloadable (used to filter
	 * the datasets)
	 * @return
	 */
	private DatasetList<Dataset> getDownloadableDatasets(String validSenderIdPattern) {
		
		DatasetList<Dataset> allDatasets = new DatasetList<>();
		
		Collection<String> dcCodes = new ArrayList<>();
		dcCodes.add(PropertiesReader.getTestDataCollectionCode()); // add test
		
		Calendar today = Calendar.getInstance();
		int currentYear = today.get(Calendar.YEAR);
		int startingYear = PropertiesReader.getDataCollectionStartingYear();
		
		// if other years are needed
		if (currentYear >= startingYear) {
			
			// add also the other years
			for (int i = currentYear; i <= startingYear; --i) {
				dcCodes.add(PropertiesReader.getDataCollectionCode(String.valueOf(i)));
			}
		}

		// for each data collection get the datasets
		for (String dcCode : dcCodes) {
			
			GetDatasetList req = new GetDatasetList(dcCode);
			
			try {
				
				DatasetList<Dataset> datasets = req.getList();
				allDatasets.addAll(datasets.getDownloadableDatasets(validSenderIdPattern));
				
			} catch (SOAPException e) {
				e.printStackTrace();
			}
		}
		
		return allDatasets;
	}
	
	/**
	 * Get all the versions of the dataset
	 * @return
	 */
	public DatasetList<Dataset> getSelectedDatasetVersions() {

		Dataset dataset = getSelectedDataset();
		
		if (dataset == null) {
			return null;
		}
		
		String senderId = dataset.getDecomposedSenderId();
		
		if (senderId == null)
			return null;
		
		// get all the versions of the dataset
		return this.allDatasets.filterByDecomposedSenderId(senderId);
	}
}
