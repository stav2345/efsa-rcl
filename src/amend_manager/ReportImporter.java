package amend_manager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import dataset.Dataset;
import dataset.DatasetList;
import dataset.IDataset;
import dataset.NoAttachmentException;
import formula.FormulaException;
import progress_bar.ProgressListener;
import report.Report;
import soap.MySOAPException;
import table_skeleton.TableRow;
import table_skeleton.TableRowList;
import table_skeleton.TableVersion;

public abstract class ReportImporter {

	private ProgressListener progressListener;
	
	private TableRowList newVersions;
	private TableRowList oldVersions;
	private DatasetList datasetVersions;
	private String senderDatasetId;
	private String rowIdField;
	private String versionField;
	
	private int processedDatasets;
	
	/**
	 * Download and import a dataset using all its versions to
	 * manage the amendments.
	 * @param datasetVersions
	 * @param rowIdField name of the field which is the identified of the dataset
	 * @param versionField name of the field which contains the dataset version
	 * using the format value.version, as FR1704.01 (senderDatasetId.version)
	 */
	public ReportImporter(DatasetList datasetVersions, 
			String rowIdField, String versionField) {
		
		this.processedDatasets = 1;
		
		this.datasetVersions = datasetVersions;
		this.rowIdField = rowIdField;
		this.versionField =  versionField;
		
		// get the sender id of the dataset versions
		if (!datasetVersions.isEmpty()) {
			senderDatasetId = datasetVersions.get(0).getDecomposedSenderId();
		}
		else {
			throw new IllegalArgumentException("Cannot download an empty dataset list");
		}
	}
	
	/**
	 * Listen to the process progresses
	 * @param progressListener
	 */
	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
	}
	
	/**
	 * Set the progress (call the listener if set)
	 * @param progress
	 */
	private void setProgress(double progress) {
		if (this.progressListener != null)
			this.progressListener.progressChanged(progress);
	}
	
	private void saveOldVersions() {
		this.oldVersions = Report.getAllVersions(this.senderDatasetId);
	}
	
	/**
	 * Delete all the old versions of the report
	 */
	public void deleteOldVersions() {
		this.oldVersions.deleteAll();
	}
	
	public void abort() {
		if (this.newVersions != null)
			this.newVersions.deleteAll();
	}
	
	/**
	 * Import an entire report (composed of several dataset versions)
	 * The amendment is also managed here
	 * @throws MySOAPException
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws FormulaException 
	 * @throws NoAttachmentException 
	 * @throws ParseException 
	 */
	public void importReport() throws MySOAPException, XMLStreamException, IOException, 
		FormulaException, NoAttachmentException, ParseException {
		
		System.out.println("Report downloader started");
		
		// save old versions of the report if present
		saveOldVersions();
		
		// clear cache
		clearTable();

		int k = getLastAcceptedVersion();  // version of last accepted dataset
		int n = getLastExistingVersion();  // version of last dataset
		
		System.out.println("Last version found is " + n 
				+ ", while last ACCEPTED_DWH version found is " + k);
		
		System.out.println(datasetVersions);
		
		// sort the datasets by version ascendent
		datasetVersions.sortAsc();
		
		// in order, import the datasets processing the amendments if needed
		for (IDataset data : datasetVersions) {
			
			Dataset dataset = (Dataset) data;
			
			setProgress(processedDatasets / datasetVersions.size() * 25);
			
			System.out.println("importSingleVersion of " + dataset);
			
			// import the single dataset into db
			importSingleVersion((Dataset) dataset);
			
			setProgress(processedDatasets / datasetVersions.size() * 100);
			processedDatasets++;
			
			// get the dataset version
			int currentVersion = TableVersion.getNumVersion(dataset.getVersion());
			
			System.out.println("The version of the imported dataset is " + currentVersion);
			
			if (currentVersion == k || currentVersion == n) {
				
				if (currentVersion == n)
					System.out.print("which is the last one");
				else
					System.out.print("which is the last accepted one");
				
				System.out.println("; therefore process amendments and create the report");
				
				// populate the dataset with metadata (operation/header)
				Dataset popDataset = dataset.populateMetadata();
				
				// process the dataset header/operation
				TableRow newReport = importDatasetMetadata(popDataset);
				if (this.newVersions == null) {
					this.newVersions = new TableRowList(newReport.getSchema());
				}
				
				newVersions.add(newReport);
				
				// process the amendments of the current dataset
				processAmendments();
				
				// generate local report starting from dataset
				createLocalReport();
				
				// if we have reached the last processable dataset stop
				if (currentVersion == n) {
					break;
				}
			}
		}
		
		// at the end clear the database table
		clearTable();
		
		// delete all the old versions (we don't need them anymore)
		deleteOldVersions();
		
		if (this.progressListener != null)
			this.progressListener.progressCompleted();
		
		System.out.println("Report downloader ended");
	}
	
	/**
	 * Import a single dataset version into the database
	 * @param dataset
	 * @throws MySOAPException
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws NoAttachmentException 
	 */
	private void importSingleVersion(Dataset dataset) throws MySOAPException, 
		XMLStreamException, IOException, NoAttachmentException {
		
		// download the dataset file
		File file = dataset.download();
		
		setProgress(processedDatasets / datasetVersions.size() * 75);
		
		// import the file
		importDatasetFile(file);
	}
	
	/**
	 * Import a dataset file into the comparison table
	 * @param file
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	private void importDatasetFile(File file) throws XMLStreamException, IOException {
		
		if (file == null || !file.exists()) {
			throw new IOException("Cannot find the dataset attachment in the DCF response.");
		}
		
		// parse it to extract the relevant information
		DatasetComparisonParser parser = new DatasetComparisonParser(
				file, rowIdField, versionField);

		// for each dataset comparison insert into the db
		DatasetComparison comp;
		while ((comp = parser.next()) != null) {
			DatasetComparisonDao dao = new DatasetComparisonDao();
			dao.add(comp);
		}

		parser.close();
	}

	/**
	 * Process the amendments of the current processed dataset
	 */
	private void processAmendments() {
		deleteNullifiedRecords();
	}

	/**
	 * Delete all the records which were amended as deleted
	 */
	private void deleteNullifiedRecords() {
		
		StringBuilder query = new StringBuilder();
		query.append("delete from APP.DATASET_COMPARISON ")
			.append("where IS_NULLIFIED = '1'");
		
		DatasetComparisonDao dao = new DatasetComparisonDao();
		dao.executeQuery(query.toString());
	}
	
	/**
	 * Create the local report using the data received up to now
	 * @throws IOException 
	 * @throws XMLStreamException 
	 * @throws FormulaException 
	 * @throws ParseException 
	 */
	private void createLocalReport() throws XMLStreamException, IOException, FormulaException, ParseException {
		
		DatasetComparisonDao dao = new DatasetComparisonDao();
		List<DatasetComparison> list = dao.getAll();
		List<TableRow> rows = new ArrayList<>();
		
		for (DatasetComparison comp : list) {
			TableRow row = getRowFromXml(comp.getXmlRecord());
			rows.add(row);
		}
		
		// import the rows
		importDatasetRows(rows);
	}
	
	/**
	 * Extract the table row from the xml
	 * @param xmlRecord
	 * @return
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	private TableRow getRowFromXml(String xmlRecord) throws XMLStreamException, IOException {
		
		String encoding = StandardCharsets.UTF_8.name();

		// add root to create a well formed xml
		xmlRecord = "<dummy>" + xmlRecord + "</dummy>";
		
		InputStream input = new ByteArrayInputStream(xmlRecord.getBytes(encoding));
		RowParser parser = new RowParser(input);
		
		TableRow row = parser.parse();
		
		parser.close();
		
		return row;
	}
	
	/**
	 * Get the last version of the dataset which is accepted
	 * @return
	 */
	private int getLastAcceptedVersion() {
		Dataset lastAccepted = (Dataset) datasetVersions
				.getLastAcceptedVersion(senderDatasetId);
		
		if (lastAccepted == null)
			return 0;
		
		return Integer.valueOf(lastAccepted.getVersion());
	}
	
	/**
	 * Get the last version of the dataset which is not deleted/rejected
	 * @return
	 */
	private int getLastExistingVersion() {
		Dataset lastExisting = (Dataset) datasetVersions
				.getLastExistingVersion(senderDatasetId);
		
		if (lastExisting == null)
			return 0;
		
		return Integer.valueOf(lastExisting.getVersion());
	}
	
	/**
	 * Clear comparisons table
	 */
	private void clearTable() {
		DatasetComparisonDao dao = new DatasetComparisonDao();
		dao.deleteAll();
	}
	
	/**
	 * Import the dataset header/operation
	 * @param dataset
	 */
	public abstract TableRow importDatasetMetadata(Dataset dataset);
	
	/**
	 * Import into the local database the row (depends on the data collection)
	 * @param row
	 */
	public abstract void importDatasetRows(List<TableRow> row) throws FormulaException, ParseException;
}
