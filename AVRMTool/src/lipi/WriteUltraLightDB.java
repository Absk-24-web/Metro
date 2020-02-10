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

public class WriteUltraLightDB {

	protected Shell shlWriteultralight;
	private Text txtAddress;
	private Text txtAPDU;

	public int iAddress;
	public String strAPDU;
	public boolean bFormResult = false;
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			WriteUltraLightDB window = new WriteUltraLightDB();
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
		shlWriteultralight.open();
		shlWriteultralight.layout();
		while (!shlWriteultralight.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlWriteultralight = new Shell();
		shlWriteultralight.setSize(329, 222);
		shlWriteultralight.setText("WriteUltraLight");
		
		Label lblWriteAddress = new Label(shlWriteultralight, SWT.NONE);
		lblWriteAddress.setText("Write Address");
		lblWriteAddress.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblWriteAddress.setBounds(10, 13, 119, 20);
		
		txtAddress = new Text(shlWriteultralight, SWT.BORDER);
		txtAddress.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtAddress.setBounds(144, 10, 76, 25);
		
		Button btnOK = new Button(shlWriteultralight, SWT.NONE);
		btnOK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					iAddress = Integer.parseInt(txtAddress.getText());
					strAPDU = txtAPDU.getText();
					bFormResult = true;					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				shlWriteultralight.close();
			}
		});
		btnOK.setText("OK");
		btnOK.setBounds(66, 149, 75, 25);
		
		Button btnCancel = new Button(shlWriteultralight, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				iAddress = -1;
				strAPDU = "";
				bFormResult = false;
				shlWriteultralight.close();
			}
		});
		btnCancel.setText("Cancel");
		btnCancel.setBounds(156, 149, 75, 25);
		
		Label label_1 = new Label(shlWriteultralight, SWT.NONE);
		label_1.setText("Enter APDU");
		label_1.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		label_1.setBounds(10, 50, 132, 20);
		
		txtAPDU = new Text(shlWriteultralight, SWT.BORDER);
		txtAPDU.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtAPDU.setBounds(10, 74, 291, 69);
	}
}
