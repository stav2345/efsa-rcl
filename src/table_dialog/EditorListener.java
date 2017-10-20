package table_dialog;

import table_skeleton.TableRow;

public interface EditorListener {
	public void editStarted();
	public void editEnded(TableRow row, boolean changed);
}
