package report;

import java.util.Comparator;

import dataset.IDataset;

public class VersionComparator implements Comparator<IDataset> {

	@Override
	public int compare(IDataset arg0, IDataset arg1) {
		
		String mySenderId = arg0.getSenderId();
		String myVersion = arg0.getVersion();
		
		String senderId = arg1.getSenderId();
		String version = arg1.getVersion();
		
		if (senderId.isEmpty() || mySenderId.isEmpty())
			return 0;

		if (!senderId.equalsIgnoreCase(mySenderId))
			return senderId.compareTo(mySenderId);
		
		if (version.isEmpty() && myVersion.isEmpty())
			return 0;

		// if the other don't have version i am last
		if (version.isEmpty() && !myVersion.isEmpty())
			return -1;

		// if i don't have version i am previous
		if (!version.isEmpty() && myVersion.isEmpty())
			return 1;

		return version.compareTo(myVersion);
	}
}
