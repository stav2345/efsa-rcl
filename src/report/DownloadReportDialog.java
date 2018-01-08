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
import i18n_messages.Messages;
import soap.GetDatasetList;
import table_dialog.DatasetListDialog;

/**
 * Show the list of dcf datasets. A {@link GetDatasetList} is performed
 * for each data collection year considered.
 * @author avonva
 *
 */
public class DownloadReportDialog extends DatasetListDialog {
	
	private DatasetList allDatasets;
	private DatasetList downloadableDatasets;
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
		this.allDatasets = new DatasetList();
		this.downloadableDatasets = new DatasetList();
	}
	
	public void loadDatasets() {
		
		Shell parent = getParent();
		
		parent.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
		
		// prepare downloadableDatasets and allDatasets lists
		initDatasets(validSenderIdPattern);
		
		// sort the datasets
		this.downloadableDatasets.sort();
		
		this.setList(downloadableDatasets);
		
		parent.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
	}

	/**
	 * Get a list of all the dcf datasets which are downloadable.
	 * All the data collections starting from the starting year are considered.
	 * @param validSenderIdPattern pattern that the sender id field of a
	 * dataset must follow to be considered downloadable (used to filter
	 * the datasets)
	 * @return
	 */
	private void initDatasets(String validSenderIdPattern) {
		
		Collection<String> dcCodes = new ArrayList<>();
		dcCodes.add(PropertiesReader.getTestDataCollectionCode()); // add test dc
		
		Calendar today = Calendar.getInstance();
		int currentYear = today.get(Calendar.YEAR);
		int startingYear = PropertiesReader.getDataCollectionStartingYear();
		
		// if other years are needed
		if (currentYear >= startingYear) {
			
			// add also the other years
			for (int i = currentYear; i >= startingYear; --i) {	
				dcCodes.add(PropertiesReader.getDataCollectionCode(String.valueOf(i)));
			}
		}

		
		// for each data collection get the datasets
		for (String dcCode : dcCodes) {
			
			DatasetList output = new DatasetList();
			
			GetDatasetList req = new GetDatasetList(dcCode, output);
			
			try {
				
				req.getList();
				
				allDatasets.addAll(output.getDownloadableDatasets(validSenderIdPattern));
				downloadableDatasets.addAll(
						output.getDownloadableDatasetsLatestVersions(validSenderIdPattern));
				
			} catch (SOAPException e) {
				e.printStackTrace();
			}
		}
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
		
		System.out.println("Selected dataset senderId" + senderId);
		System.out.println("Filtering in " + allDatasets);
		if (senderId == null)
			return null;
		
		// get all the versions of the dataset
		return this.allDatasets.filterByDecomposedSenderId(senderId);
	}
}
