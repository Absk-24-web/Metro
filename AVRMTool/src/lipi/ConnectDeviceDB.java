package lipi;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

import Cris.GlobalMembers;
import jssc.SerialPortList;

import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class ConnectDeviceDB {

	protected Shell shlConnectDeviceDB;
	private Text txtPortsAvlbl;
	private Text txtPortSelected;
	private Text txtClearanceMode;

	public int iPortSelected;
	public int iClearanceModeSelected;
	public boolean bFormResult = false;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ConnectDeviceDB window = new ConnectDeviceDB();
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
		shlConnectDeviceDB.open();
		shlConnectDeviceDB.layout();
		while (!shlConnectDeviceDB.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlConnectDeviceDB = new Shell();
		shlConnectDeviceDB.setSize(277, 383);
		shlConnectDeviceDB.setText("Select Connect Parameters");

		Label lblPortsAvailable = new Label(shlConnectDeviceDB, SWT.NONE);
		lblPortsAvailable.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblPortsAvailable.setBounds(10, 10, 189, 20);
		lblPortsAvailable.setText("Serial Ports Available");

		txtPortsAvlbl = new Text(shlConnectDeviceDB, SWT.BORDER);
		txtPortsAvlbl.setEditable(false);
		txtPortsAvlbl.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtPortsAvlbl.setBounds(10, 36, 239, 100);
		
		String[] strPortNames = SerialPortList.getPortNames();
		
		if (GlobalMembers.IsWindows()) {
			for(int iIndex = 0; iIndex < strPortNames.length; iIndex++)
			{
				txtPortsAvlbl.append(strPortNames[iIndex] + "\r\n");
			}
		}
		else
		{
			txtPortsAvlbl.append("1) /dev/ttyS0\r\n");
			txtPortsAvlbl.append("2) /dev/ttyS1\r\n");
			txtPortsAvlbl.append("3) /dev/ttyUSB0\r\n");
			txtPortsAvlbl.append("4) /dev/ttyUSB1\r\n");
		}

		Label lblSelectPort = new Label(shlConnectDeviceDB, SWT.NONE);
		lblSelectPort.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblSelectPort.setBounds(10, 142, 84, 20);
		lblSelectPort.setText("Select Port");

		txtPortSelected = new Text(shlConnectDeviceDB, SWT.BORDER);
		txtPortSelected.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtPortSelected.setBounds(106, 139, 76, 25);

		Label lblChannelClearanceMode = new Label(shlConnectDeviceDB, SWT.NONE);
		lblChannelClearanceMode.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblChannelClearanceMode.setBounds(10, 181, 241, 80);
		lblChannelClearanceMode.setText(
				"Channel Clearance Mode:\r\n0- Retain in the channel\r\n1- Send to rejection bin\r\n2- Return from mouth of the device");

		Label lblSelectClearanceMode = new Label(shlConnectDeviceDB, SWT.NONE);
		lblSelectClearanceMode.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblSelectClearanceMode.setBounds(10, 267, 161, 20);
		lblSelectClearanceMode.setText("Select Clearance Mode");

		txtClearanceMode = new Text(shlConnectDeviceDB, SWT.BORDER);
		txtClearanceMode.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtClearanceMode.setBounds(173, 264, 76, 25);

		Button btnOk = new Button(shlConnectDeviceDB, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					iPortSelected = Integer.parseInt(txtPortSelected.getText());
					iClearanceModeSelected = Integer.parseInt(txtClearanceMode.getText());
					bFormResult = true;					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				shlConnectDeviceDB.close();
			}
		});
		btnOk.setBounds(47, 306, 75, 25);
		btnOk.setText("OK");

		Button btnCancel = new Button(shlConnectDeviceDB, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				bFormResult = false;
				iPortSelected = -1;
				iClearanceModeSelected = -1;
				shlConnectDeviceDB.close();
			}
		});
		btnCancel.setBounds(137, 306, 75, 25);
		btnCancel.setText("Cancel");

	}
}
