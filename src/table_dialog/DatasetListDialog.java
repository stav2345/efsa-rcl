package table_dialog;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

import dataset.Dataset;
import dataset.DatasetList;
import warn_user.Warnings;

public class DatasetListDialog {

	private Shell parent;
	private Shell dialog;
	private String title;
	private String okBtnText;
	private DatasetList list;
	private Dataset selectedDataset;
	
	public DatasetListDialog(Shell parent, String title, String okBtnText, DatasetList list) {
		this.parent = parent;
		this.title = title;
		this.okBtnText = okBtnText;
		this.list = list;
		create();
	}
	
	private void create() {
		
		this.dialog = new Shell(parent, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
		this.dialog.setText(title);
		this.dialog.setImage(parent.getImage());
		
		dialog.setLayout(new GridLayout(1, false));
		dialog.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TableViewer datasetList = new TableViewer(dialog, SWT.BORDER | SWT.SINGLE
				| SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.NONE);
		datasetList.getTable().setHeaderVisible(true);
		datasetList.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		datasetList.setContentProvider(new DatasetContentProvider());
		
		// Add the column to the parent table
		TableViewerColumn idCol = new TableViewerColumn(datasetList, SWT.NONE);
		idCol.getColumn().setText("Id");
		idCol.setLabelProvider(new DatasetLabelProvider("id"));
		idCol.getColumn().setWidth(100);
		
		TableViewerColumn senderIdCol = new TableViewerColumn(datasetList, SWT.NONE);
		senderIdCol.getColumn().setText("Sender id");
		senderIdCol.setLabelProvider(new DatasetLabelProvider("senderId"));
		senderIdCol.getColumn().setWidth(100);
		
		TableViewerColumn statusCol = new TableViewerColumn(datasetList, SWT.NONE);
		statusCol.getColumn().setText("DCF Status");
		statusCol.setLabelProvider(new DatasetLabelProvider("status"));
		statusCol.getColumn().setWidth(100);

		datasetList.setInput(list);

		// ok button to select a dataset
		Button okBtn = new Button(dialog, SWT.PUSH);
		okBtn.setText(okBtnText);
		
		okBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				IStructuredSelection selection = (IStructuredSelection) datasetList.getSelection();
				if (selection.isEmpty()) {
					Warnings.warnUser(dialog, "Error", "No dataset was selected");
					return;
				}
				
				selectedDataset = (Dataset) selection.getFirstElement();
				
				dialog.close();
			}
		});
		
		dialog.pack();
		dialog.open();
		
		// Event loop
		while ( !dialog.isDisposed() ) {
			if ( !dialog.getDisplay().readAndDispatch() )
				dialog.getDisplay().sleep();
		}
	}
	
	public Shell getDialog() {
		return dialog;
	}
	
	/**
	 * Get the selected dataset
	 * @return
	 */
	public Dataset getSelectedDataset() {
		return selectedDataset;
	}
}
