package report;

import java.util.ArrayList;
import java.util.Collections;

public class ReportList extends ArrayList<EFSAReport> {
	
	private static final long serialVersionUID = -7192586611100343055L;
	
	public void sort() {
		Collections.sort(this, new VersionComparator());
	}
	public void reverse() {
		Collections.reverse(this);
	}
}
