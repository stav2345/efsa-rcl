package dataset;

public interface IDataset {
	public String getDatasetId();
	public String getSenderId();
	public String getVersion();
	public String getDecomposedSenderId();
	public DatasetStatus getStatus();
}
