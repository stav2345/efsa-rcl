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
import global_utils.Warnings;
import i18n_messages.Messages;

public class DatasetListDialog {

	private Shell parent;
	private Shell dialog;
	private String title;
	private String okBtnText;
	private Dataset selectedDataset;
	
	private TableViewer table;
	
	public DatasetListDialog(Shell parent, String title, String okBtnText) {
		this.parent = parent;
		this.title = title;
		this.okBtnText = okBtnText;
		create();
	}
	
	public void setList(DatasetList list) {
		table.setInput(list);
	}
	
	private void create() {
		
		this.dialog = new Shell(parent, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
		this.dialog.setText(title);
		this.dialog.setImage(parent.getImage());
		
		dialog.setLayout(new GridLayout(1, false));
		dialog.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		this.table = new TableViewer(dialog, SWT.BORDER | SWT.SINGLE
				| SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.NONE);
		table.getTable().setHeaderVisible(true);
		table.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setContentProvider(new DatasetContentProvider());

		// ok button to select a dataset
		Button okBtn = new Button(dialog, SWT.PUSH);
		okBtn.setText(okBtnText);
		okBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		
		okBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				IStructuredSelection selection = (IStructuredSelection) table.getSelection();
				if (selection.isEmpty()) {
					Warnings.warnUser(dialog, 
							Messages.get("error.title"), 
							Messages.get("dataset.not.selected"));
					return;
				}
				
				selectedDataset = (Dataset) selection.getFirstElement();
				
				dialog.close();
			}
		});
	}
	
	public TableViewer getTable() {
		return table;
	}

	public void addIdCol() {
		// Add the column to the parent table
		TableViewerColumn idCol = new TableViewerColumn(table, SWT.NONE);
		idCol.getColumn().setText(Messages.get("dataset.header.id"));
		idCol.setLabelProvider(new DatasetLabelProvider("id"));
		idCol.getColumn().setWidth(100);
	}
	
	public void addSenderIdCol() {
		TableViewerColumn senderIdCol = new TableViewerColumn(table, SWT.NONE);
		senderIdCol.getColumn().setText(Messages.get("dataset.header.sender.id"));
		senderIdCol.setLabelProvider(new DatasetLabelProvider("senderId"));
		senderIdCol.getColumn().setWidth(100);
	}
	
	public void addStatusCol() {
		TableViewerColumn statusCol = new TableViewerColumn(table, SWT.NONE);
		statusCol.getColumn().setText(Messages.get("dataset.header.status"));
		statusCol.setLabelProvider(new DatasetLabelProvider("status"));
		statusCol.getColumn().setWidth(130);
	}
	
	public void addRevisionCol() {
		TableViewerColumn revisionCol = new TableViewerColumn(table, SWT.NONE);
		revisionCol.getColumn().setText(Messages.get("dataset.header.revision"));
		revisionCol.setLabelProvider(new DatasetLabelProvider("revision"));
		revisionCol.getColumn().setWidth(100);
	}
	
	public void open() {
		
		dialog.open();

		// Event loop
		while ( !dialog.isDisposed() ) {
			if ( !dialog.getDisplay().readAndDispatch() )
				dialog.getDisplay().sleep();
		}
	}
	
	public Shell getParent() {
		return parent;
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
