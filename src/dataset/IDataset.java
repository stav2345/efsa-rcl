package dataset;

public interface IDataset extends IDcfDataset {
	public String getVersion();
	public String getDecomposedSenderId();
	public RCLDatasetStatus getRCLStatus();
}
