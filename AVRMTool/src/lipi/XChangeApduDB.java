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

public class XChangeApduDB {

	protected Shell shlXchangeApdu;
	private Text txtCardType;
	private Text txtSAMSlot;
	private Text txtAPDU;

	public int iSAMSlot;
	public int iCardType;
	public String strAPDU;
	public boolean bFormResult = false;
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			XChangeApduDB window = new XChangeApduDB();
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
		shlXchangeApdu.open();
		shlXchangeApdu.layout();
		while (!shlXchangeApdu.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlXchangeApdu = new Shell();
		shlXchangeApdu.setSize(332, 394);
		shlXchangeApdu.setText("XChange APDU");
		
		Label label = new Label(shlXchangeApdu, SWT.NONE);
		label.setText("Card Tech Type-\r\n0) Contact less card\r\n1) Contact card\r\n");
		label.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		label.setBounds(10, 10, 210, 66);
		
		Label label_1 = new Label(shlXchangeApdu, SWT.NONE);
		label_1.setText("Select Card Type");
		label_1.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		label_1.setBounds(10, 83, 119, 20);
		
		txtCardType = new Text(shlXchangeApdu, SWT.BORDER);
		txtCardType.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtCardType.setBounds(144, 80, 76, 25);
		
		Label label_2 = new Label(shlXchangeApdu, SWT.NONE);
		label_2.setText("SAM Slot-\r\n1) First SAM slot\r\n2) Second SAM slot\r\n");
		label_2.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		label_2.setBounds(10, 112, 210, 66);
		
		Label label_3 = new Label(shlXchangeApdu, SWT.NONE);
		label_3.setText("Select SAM Slot");
		label_3.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		label_3.setBounds(10, 185, 132, 20);
		
		txtSAMSlot = new Text(shlXchangeApdu, SWT.BORDER);
		txtSAMSlot.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtSAMSlot.setBounds(144, 182, 76, 25);
		
		Button btnOK = new Button(shlXchangeApdu, SWT.NONE);
		btnOK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					iSAMSlot = Integer.parseInt(txtSAMSlot.getText());
					iCardType = Integer.parseInt(txtCardType.getText());
					strAPDU = txtAPDU.getText();
					bFormResult = true;					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				shlXchangeApdu.close();
			}
		});
		btnOK.setText("OK");
		btnOK.setBounds(70, 322, 75, 25);
		
		Button btnCancel = new Button(shlXchangeApdu, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				iSAMSlot = -1;
				iCardType = -1;
				strAPDU = "";
				bFormResult = false;
				shlXchangeApdu.close();
			}
		});
		btnCancel.setText("Cancel");
		btnCancel.setBounds(160, 322, 75, 25);
		
		Label lblEnterApdu = new Label(shlXchangeApdu, SWT.NONE);
		lblEnterApdu.setText("Enter APDU");
		lblEnterApdu.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblEnterApdu.setBounds(10, 223, 132, 20);
		
		txtAPDU = new Text(shlXchangeApdu, SWT.BORDER);
		txtAPDU.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtAPDU.setBounds(10, 247, 291, 69);

	}
}
