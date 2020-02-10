package lipi;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class ResetSAMDB {

	protected Shell shlResetSam;
	private Text txtSAMSlot;
	private Text txtResetType;

	public int iSAMSlot;
	public int iResetType;
	public boolean bFormResult = false;
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ResetSAMDB window = new ResetSAMDB();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlResetSam.open();
		shlResetSam.layout();
		while (!shlResetSam.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlResetSam = new Shell();
		shlResetSam.setSize(252, 306);
		shlResetSam.setText("Reset SAM");
		
		Label label = new Label(shlResetSam, SWT.NONE);
		label.setText("SAM Slot-\r\n1) First SAM slot\r\n2) Second SAM slot\r\n");
		label.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		label.setBounds(10, 10, 210, 66);
		
		Label label_1 = new Label(shlResetSam, SWT.NONE);
		label_1.setText("Select SAM Slot");
		label_1.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		label_1.setBounds(10, 83, 119, 20);
		
		txtSAMSlot = new Text(shlResetSam, SWT.BORDER);
		txtSAMSlot.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtSAMSlot.setBounds(144, 80, 76, 25);
		
		Label lblResetType = new Label(shlResetSam, SWT.NONE);
		lblResetType.setText("Reset Type-\r\n0) Cold reset\r\n1) Worm reset\r\n");
		lblResetType.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblResetType.setBounds(10, 112, 210, 66);
		
		Label lblSelectResetType = new Label(shlResetSam, SWT.NONE);
		lblSelectResetType.setText("Select Reset Type");
		lblSelectResetType.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblSelectResetType.setBounds(10, 185, 132, 20);
		
		txtResetType = new Text(shlResetSam, SWT.BORDER);
		txtResetType.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtResetType.setBounds(144, 182, 76, 25);
		
		Button btnOK = new Button(shlResetSam, SWT.NONE);
		btnOK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					iSAMSlot = Integer.parseInt(txtSAMSlot.getText());
					iResetType = Integer.parseInt(txtResetType.getText());
					bFormResult = true;					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				shlResetSam.close();
			}
		});
		btnOK.setText("OK");
		btnOK.setBounds(39, 226, 75, 25);
		
		Button btnCancel = new Button(shlResetSam, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				iSAMSlot = -1;
				iResetType = -1;
				bFormResult = false;
				shlResetSam.close();
			}
		});
		btnCancel.setText("Cancel");
		btnCancel.setBounds(129, 226, 75, 25);

	}

}
