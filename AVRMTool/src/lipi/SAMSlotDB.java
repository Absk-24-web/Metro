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

public class SAMSlotDB {

	protected Shell shlSamSlotOptions;
	private Text txtSAMSlot;
	private Text txtPowerState;

	public int iSAMSlot;
	public int iPowerState;
	public boolean bFormResult = false;
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			SAMSlotDB window = new SAMSlotDB();
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
		shlSamSlotOptions.open();
		shlSamSlotOptions.layout();
		while (!shlSamSlotOptions.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlSamSlotOptions = new Shell();
		shlSamSlotOptions.setSize(246, 299);
		shlSamSlotOptions.setText("SAM Slot Options");
		
		Label lblSamSlot = new Label(shlSamSlotOptions, SWT.NONE);
		lblSamSlot.setText("SAM Slot-\r\n1) First SAM slot\r\n2) Second SAM slot\r\n");
		lblSamSlot.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblSamSlot.setBounds(10, 10, 210, 66);
		
		Label lblSelectSamSlot = new Label(shlSamSlotOptions, SWT.NONE);
		lblSelectSamSlot.setText("Select SAM Slot");
		lblSelectSamSlot.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblSelectSamSlot.setBounds(10, 83, 119, 20);
		
		txtSAMSlot = new Text(shlSamSlotOptions, SWT.BORDER);
		txtSAMSlot.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtSAMSlot.setBounds(135, 80, 76, 25);
		
		Button btnOK = new Button(shlSamSlotOptions, SWT.NONE);
		btnOK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					iSAMSlot = Integer.parseInt(txtSAMSlot.getText());
					iPowerState = Integer.parseInt(txtPowerState.getText());
					bFormResult = true;					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				shlSamSlotOptions.close();
			}
		});
		btnOK.setText("OK");
		btnOK.setBounds(39, 226, 75, 25);
		
		Button btnCancel = new Button(shlSamSlotOptions, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				iSAMSlot = -1;
				iPowerState = -1;
				bFormResult = false;
				shlSamSlotOptions.close();
			}
		});
		btnCancel.setText("Cancel");
		btnCancel.setBounds(129, 226, 75, 25);
		
		Label lblPowerState = new Label(shlSamSlotOptions, SWT.NONE);
		lblPowerState.setText("Power State-\r\n0) Power OFF/Deactivate\r\n1) Power ON/Activate\r\n");
		lblPowerState.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblPowerState.setBounds(10, 112, 210, 66);
		
		Label lblSelectPowerState = new Label(shlSamSlotOptions, SWT.NONE);
		lblSelectPowerState.setText("Select Power State");
		lblSelectPowerState.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblSelectPowerState.setBounds(10, 185, 122, 20);
		
		txtPowerState = new Text(shlSamSlotOptions, SWT.BORDER);
		txtPowerState.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtPowerState.setBounds(135, 182, 76, 25);

	}

}
