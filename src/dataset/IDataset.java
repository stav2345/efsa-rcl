package dataset;

public interface IDataset {
	public String getSenderId();
	public String getVersion();
	public String getDecomposedSenderId();
	public DatasetStatus getStatus();
}
