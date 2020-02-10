package lipi;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

import Cris.Common;
import Cris.SmartCard;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

/*import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.ParallelPort;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;*/

import java.awt.HeadlessException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;

import javax.swing.JFileChooser;

import org.eclipse.swt.widgets.Control;
//import org.eclipse.ui.forms.widgets.FormToolkit;
//import org.eclipse.ui.internal.registry.CategorizedPageRegistryReader;
//import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Group;

public class MainWindow {

	protected Shell shell;
	private Text txtResponse;

	private String strOSType;

	// private static SerialPort serialPort;

	// private static byte ACK = (byte)0x06;
	// private static byte NACK = (byte)0x15;
	// private static byte STX = (byte)0xF2;
	// private static byte ETX = (byte)0x03;
	// private static byte CMT = (byte)0x43;
	// private static byte CRC = (byte)0x00;
	//private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());

	private SmartCard objSmartCardDispenser;
	private SmartCard objSmartCardReaderWriter;

	/*
	 * private CommPortIdentifier portIdentifier = null; private CommPort commPort =
	 * null; private SerialPort serialPort = null; private ParallelPort parallelPort
	 * = null;
	 */

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			MainWindow window = new MainWindow();
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
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public static byte GetBCC(byte[] p, int n) {
		byte ch;
		int i;
		byte crc = (byte) 0x00;

		for (i = 0; i < n; i++) {
			ch = p[i];
			crc = (byte) (ch ^ crc);
		}
		return crc;
	}

	public static String GetErrorDesc(byte byE1, byte byE0) {
		String strRes = "";

		switch (String.format("%02x%02x", byE1, byE0)) {
		case "0000":
			strRes = "Reception of undefined command";
			break;
		case "0001":
			strRes = "Command parameter error";
			break;
		}

		return strRes;
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(943, 697);
		shell.setText("SWT Application");

		strOSType = System.getProperty("os.name").toLowerCase();
		shell.setLayout(null);

		txtResponse = new Text(shell, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		txtResponse.setBounds(10, 372, 907, 250);
		txtResponse.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));

		Label lblResponse = new Label(shell, SWT.NONE);
		lblResponse.setBounds(10, 341, 72, 25);
		lblResponse.setText("Response");
		lblResponse.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));

		Label lblStatus = new Label(shell, SWT.NONE);
		lblStatus.setBounds(10, 628, 44, 18);
		lblStatus.setText("Status-");
		lblStatus.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));

		Label lblStatusVal = new Label(shell, SWT.NONE);
		lblStatusVal.setBounds(60, 628, 527, 28);
		lblStatusVal.setText("");
		lblStatusVal.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));

		Button btnClear = new Button(shell, SWT.NONE);
		btnClear.setBounds(698, 336, 219, 30);
		btnClear.setText("Clear");
		btnClear.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnClear, true, true);

		TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
		tabFolder.setBounds(10, 10, 911, 320);
		//formToolkit.adapt(tabFolder);
		//formToolkit.paintBordersFor(tabFolder);

		TabItem tbtmCardDispenser = new TabItem(tabFolder, SWT.NONE);
		tbtmCardDispenser.setText("Card Dispenser");

		Composite composite = new Composite(tabFolder, SWT.NONE);
		tbtmCardDispenser.setControl(composite);
		//formToolkit.paintBordersFor(composite);
		// cmbBaudRate.setVisible(false);

		Button btnConnect = new Button(composite, SWT.NONE);
		btnConnect.setBounds(10, 46, 218, 30);
		btnConnect.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		btnConnect.setText("Connect");
		//formToolkit.adapt(btnConnect, true, true);

		Button btnGetscarddevnativelibversion = new Button(composite, SWT.NONE);
		btnGetscarddevnativelibversion.setBounds(234, 46, 218, 30);
		btnGetscarddevnativelibversion.setText("GetSCardDevNativeLibVersion");
		btnGetscarddevnativelibversion.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnGetscarddevnativelibversion, true, true);

		Button btnGetscarddevfwversion = new Button(composite, SWT.NONE);
		btnGetscarddevfwversion.setBounds(458, 46, 218, 30);
		btnGetscarddevfwversion.setText("GetSCardDevFWVersion");
		btnGetscarddevfwversion.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnGetscarddevfwversion, true, true);

		Button btnGetscardreaderfwversion = new Button(composite, SWT.NONE);
		btnGetscardreaderfwversion.setBounds(682, 46, 218, 30);
		btnGetscardreaderfwversion.setText("GetSCardReaderFWVersion");
		btnGetscardreaderfwversion.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnGetscardreaderfwversion, true, true);

		Button btnGetdevicestatus = new Button(composite, SWT.NONE);
		btnGetdevicestatus.setBounds(10, 82, 218, 30);
		btnGetdevicestatus.setText("GetDeviceStatus");
		btnGetdevicestatus.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnGetdevicestatus, true, true);

		Button btnEnablecardacceptance = new Button(composite, SWT.NONE);
		btnEnablecardacceptance.setBounds(234, 82, 218, 30);
		btnEnablecardacceptance.setText("EnableCardAcceptance");
		btnEnablecardacceptance.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnEnablecardacceptance, true, true);

		Button btnDisablecardacceptance = new Button(composite, SWT.NONE);
		btnDisablecardacceptance.setBounds(458, 82, 218, 30);
		btnDisablecardacceptance.setText("DisableCardAcceptance");
		btnDisablecardacceptance.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnDisablecardacceptance, true, true);

		Button btnRejectcard = new Button(composite, SWT.NONE);
		btnRejectcard.setBounds(458, 118, 218, 30);
		btnRejectcard.setText("RejectCard");
		btnRejectcard.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnRejectcard, true, true);

		Button btnReturncard = new Button(composite, SWT.NONE);
		btnReturncard.setBounds(234, 118, 218, 30);
		btnReturncard.setText("ReturnCard");
		btnReturncard.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnReturncard, true, true);

		Button btnDispensecard = new Button(composite, SWT.NONE);
		btnDispensecard.setBounds(10, 118, 218, 30);
		btnDispensecard.setText("DispenseCard");
		btnDispensecard.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnDispensecard, true, true);

		Button btnIscardinchannel = new Button(composite, SWT.NONE);
		btnIscardinchannel.setBounds(10, 154, 218, 30);
		btnIscardinchannel.setText("IsCardInChannel");
		btnIscardinchannel.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnIscardinchannel, true, true);

		Button btnIscardremoved = new Button(composite, SWT.NONE);
		btnIscardremoved.setBounds(234, 154, 218, 30);
		btnIscardremoved.setText("IsCardRemoved");
		btnIscardremoved.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnIscardremoved, true, true);

		Button btnSamslotpoweronoff = new Button(composite, SWT.NONE);
		btnSamslotpoweronoff.setBounds(458, 154, 218, 30);
		btnSamslotpoweronoff.setText("SAMSlotPowerOnOff");
		btnSamslotpoweronoff.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnSamslotpoweronoff, true, true);

		Button btnXchangeapdu = new Button(composite, SWT.NONE);
		btnXchangeapdu.setBounds(458, 190, 218, 30);
		btnXchangeapdu.setText("XChangeAPDU");
		btnXchangeapdu.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnXchangeapdu, true, true);

		Button btnDeactivatecard = new Button(composite, SWT.NONE);
		btnDeactivatecard.setBounds(234, 190, 218, 30);
		btnDeactivatecard.setText("DeactivateCard");
		btnDeactivatecard.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnDeactivatecard, true, true);

		Button btnActivatecard = new Button(composite, SWT.NONE);
		btnActivatecard.setBounds(10, 190, 218, 30);
		btnActivatecard.setText("ActivateCard");
		btnActivatecard.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnActivatecard, true, true);

		Button btnWriteultralightpage = new Button(composite, SWT.NONE);
		btnWriteultralightpage.setBounds(10, 226, 218, 30);
		btnWriteultralightpage.setText("WriteUltralightPage");
		btnWriteultralightpage.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnWriteultralightpage, true, true);

		Button btnDisconnectDevice = new Button(composite, SWT.NONE);
		btnDisconnectDevice.setBounds(234, 226, 218, 30);
		btnDisconnectDevice.setText("DisConnectDevice");
		btnDisconnectDevice.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnDisconnectDevice, true, true);

		Button btnReadultralightblock = new Button(composite, SWT.NONE);
		btnReadultralightblock.setBounds(682, 190, 218, 30);
		btnReadultralightblock.setText("ReadUltralightBlock");
		btnReadultralightblock.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnReadultralightblock, true, true);

		Button btnResetsam = new Button(composite, SWT.NONE);
		btnResetsam.setBounds(682, 154, 218, 30);
		btnResetsam.setText("ResetSAM");
		btnResetsam.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnResetsam, true, true);

		Button btnCollectcard = new Button(composite, SWT.NONE);
		btnCollectcard.setBounds(682, 118, 218, 30);
		btnCollectcard.setText("CollectCard");
		btnCollectcard.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnCollectcard, true, true);

		Button btnAcceptcard = new Button(composite, SWT.NONE);
		btnAcceptcard.setBounds(682, 82, 218, 30);
		btnAcceptcard.setText("AcceptCard");
		btnAcceptcard.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		//formToolkit.adapt(btnAcceptcard, true, true);

		Button btnCardReaderWriter = new Button(composite, SWT.RADIO);
		btnCardReaderWriter.setText("Card Reader Writer");
		btnCardReaderWriter.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		btnCardReaderWriter.setBounds(458, 10, 218, 20);
		//formToolkit.adapt(btnCardReaderWriter, true, true);

		Button btnCardDispenser = new Button(composite, SWT.RADIO);
		btnCardDispenser.setText("Card Dispenser");
		btnCardDispenser.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		btnCardDispenser.setBounds(234, 10, 218, 20);
		//formToolkit.adapt(btnCardDispenser, true, true);
		btnCardDispenser.setSelection(true);

		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblNewLabel.setBounds(10, 10, 179, 20);
		//formToolkit.adapt(lblNewLabel, true, true);
		lblNewLabel.setText("Card Device Type");
		// formToolkit.adapt(btnBlock, true, true);

		// Open button functioning
		btnConnect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					Common objCommon=new Common();
					lblStatusVal.setText("");
					objCommon.SetLoggingLevel(47);
					if (btnCardDispenser.getSelection() == true) {
						txtResponse.setText("*** Cash Dispenser ***\r\n");
						if (objSmartCardDispenser == null)
							objSmartCardDispenser = new SmartCard(1);
					} else {
						txtResponse.setText("*** Cash Reader Writer ***\r\n");
						if (objSmartCardReaderWriter == null)
							objSmartCardReaderWriter = new SmartCard(0);
					}
					txtResponse.append("ConnectDevice\r\n");

					ConnectDeviceDB objConnect = new ConnectDeviceDB();
					objConnect.open();
					if (objConnect.bFormResult) {
						txtResponse.append("\r\nPort- " + objConnect.iPortSelected + "\r\n" + "ClearanceMode- "
								+ objConnect.iClearanceModeSelected);

						int iRet;
						if (btnCardDispenser.getSelection() == true) {
							iRet = objSmartCardDispenser.ConnectDevice(objConnect.iPortSelected,
									objConnect.iClearanceModeSelected, 10000);
						} else {
							iRet = objSmartCardReaderWriter.ConnectDevice(objConnect.iPortSelected,
									objConnect.iClearanceModeSelected, 10000);
						}
						txtResponse.append("\r\nReturn value- " + iRet);
						
						switch(iRet)
						{
						case 0: txtResponse.append("\r\nDevice connected successfully"); break;
						case 1: txtResponse.append("\r\nChannel clearance failed due to rejection bin full"); break;
						case 2: txtResponse.append("\r\nChannel clearance failed due to return mouth blocked"); break;
						case 3: txtResponse.append("\r\nChannel clearance failed due to unknown reason"); break;
						case 18: txtResponse.append("\r\nOperation timeout occured"); break;
						case 20: txtResponse.append("\r\nDevice already connected"); break;
						case 25: txtResponse.append("\r\nPort doesn't exist"); break;
						case 26: txtResponse.append("\r\nPort doesn't exist"); break;
						case 27: txtResponse.append("\r\nPort doesn't exist"); break;
						case 28: txtResponse.append("\r\nCommunication failure"); break;
						case 29: txtResponse.append("\r\nCommunication failure"); break;
						case 30: txtResponse.append("\r\nCommunication failure"); break;
						case 31: txtResponse.append("\r\nOther error"); break;
						}
					} else {
						txtResponse.append("\r\nParameter not valid");
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		// Clear button functioning
		btnClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					txtResponse.setText("");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnGetscarddevnativelibversion.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("GetSCardDevNativeLibVersion\r\n");

					String strRet = "";
					if (btnCardDispenser.getSelection() == true) {
						strRet = objSmartCardDispenser.GetSCardDevNativeLibVersion();
					} else {
						strRet = objSmartCardReaderWriter.GetSCardDevNativeLibVersion();
					}
					txtResponse.append("\r\nReturn value- " + strRet);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnGetscarddevfwversion.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("GetSCardDevFWVersion\r\n");

					String strRet = "";

					if (btnCardDispenser.getSelection() == true) {
						strRet = objSmartCardDispenser.GetSCardDevFWVersion();
					} else {
						strRet = objSmartCardReaderWriter.GetSCardDevFWVersion();
					}
					txtResponse.append("\r\nReturn value- " + strRet);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnGetscardreaderfwversion.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("GetSCardReaderFWVersion\r\n");

					String strRet = "";
					if (btnCardDispenser.getSelection() == true) {
						strRet = objSmartCardDispenser.GetSCardReaderFWVersion();
					} else {
						strRet = objSmartCardReaderWriter.GetSCardReaderFWVersion();
					}
					txtResponse.append("\r\nReturn value- " + strRet);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnGetdevicestatus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("GetDeviceStatus\r\n");

					SelectComponentDB objComponent = new SelectComponentDB();
					objComponent.open();
					if (objComponent.bFormResult) {
						txtResponse.append("\r\nComponent- " + objComponent.iComponentSelected);

						byte[] byRet = null;
						if (btnCardDispenser.getSelection() == true) {
							byRet = objSmartCardDispenser.GetDeviceStatus(objComponent.iComponentSelected, 10000);
						} else {
							byRet = objSmartCardReaderWriter.GetDeviceStatus(objComponent.iComponentSelected, 10000);
						}
						txtResponse.append("\r\nReturn value- ");
						for (int i = 0; i < byRet.length; i++) {
							txtResponse.append(String.format("%02X ", byRet[i]));
						}
						
						switch(byRet[0])
						{
						case 0: txtResponse.append("\r\nOperation successful"); break;
						case 18: txtResponse.append("\r\nOperation timeout occured"); break;
						case 28: txtResponse.append("\r\nCommunication failure"); break;
						case 29: txtResponse.append("\r\nCommunication failure Reader"); break;
						case 31: txtResponse.append("\r\nOther error"); break;
						}
						
						txtResponse.append("\r\nRFID reader status");
						switch(byRet[1])
						{
						case 0: txtResponse.append("\r\nReady"); break;
						case 1: txtResponse.append("\r\nNot ready"); break;
						}
						
						txtResponse.append("\r\nSAM reader status");
						switch(byRet[2])
						{
						case 0: txtResponse.append("\r\nReady"); break;
						case 1: txtResponse.append("\r\nNot ready"); break;
						}
						
						txtResponse.append("\r\nStacker status");
						switch(byRet[3])
						{
						case 0: txtResponse.append("\r\nEmpty"); break;
						case 1: txtResponse.append("\r\nNearly empty"); break;
						case 2: txtResponse.append("\r\nNearly full"); break;
						case 3: txtResponse.append("\r\nFull"); break;
						}
						
						txtResponse.append("\r\nApprox card count in stacker- " + byRet[4]);
						
						txtResponse.append("\r\nRejection bin status");
						switch(byRet[5])
						{
						case 0: txtResponse.append("\r\nEmpty"); break;
						case 1: txtResponse.append("\r\nNearly empty"); break;
						case 2: txtResponse.append("\r\nNearly full"); break;
						case 3: txtResponse.append("\r\nFull"); break;
						}
						
						txtResponse.append("\r\nApprox card count in rejection bin- " + byRet[6]);
						
						txtResponse.append("\r\nChannel status");
						switch(byRet[7])
						{
						case 0: txtResponse.append("\r\nClear"); break;
						case 1: txtResponse.append("\r\nBlocked"); break;
						}
						
						txtResponse.append("\r\nChannel sensor status");
						switch(byRet[8])
						{
						case 0: txtResponse.append("\r\nClear"); break;
						case 1: txtResponse.append("\r\nBlocked"); break;
						}
						
						txtResponse.append("\r\nCollection bin status");
						switch(byRet[9])
						{
						case 0: txtResponse.append("\r\nEmpty"); break;
						case 1: txtResponse.append("\r\nNearly empty"); break;
						case 2: txtResponse.append("\r\nNearly full"); break;
						case 3: txtResponse.append("\r\nFull"); break;
						}
						
						txtResponse.append("\r\nApprox card count in collection bin- " + byRet[10]);
					} else {
						txtResponse.append("\r\nParameter not valid");
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnEnablecardacceptance.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("EnableCardAcceptance\r\n");

					int iRet = -1;
					if (btnCardDispenser.getSelection() == true) {
						iRet = objSmartCardDispenser.EnableCardAcceptance(10000);
					} else {
						iRet = objSmartCardReaderWriter.EnableCardAcceptance(10000);
					}
					txtResponse.append("\r\nReturn value- " + iRet);
					
					switch(iRet)
					{
					case 0: txtResponse.append("\r\nOperation successful"); break;
					case 1: txtResponse.append("\r\nChannel blocked"); break;
					case 2: txtResponse.append("\r\nInsertion/return mouth blocked"); break;
					case 18: txtResponse.append("\r\nOperation timeout occured"); break;
					case 20: txtResponse.append("\r\nDevice not yet connected"); break;
					case 28: txtResponse.append("\r\nCommunication failure"); break;
					case 31: txtResponse.append("\r\nOther error"); break;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnDisablecardacceptance.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("DisableCardAcceptance\r\n");

					int iRet = -1;
					if (btnCardDispenser.getSelection() == true) {
						iRet = objSmartCardDispenser.DisableCardAcceptance(10000);
					} else {
						iRet = objSmartCardReaderWriter.DisableCardAcceptance(10000);
					}
					txtResponse.append("\r\nReturn value- " + iRet);
					
					switch(iRet)
					{
					case 0: txtResponse.append("\r\nOperation successful"); break;
					case 18: txtResponse.append("\r\nOperation timeout occured"); break;
					case 20: txtResponse.append("\r\nDevice not yet connected"); break;
					case 28: txtResponse.append("\r\nCommunication failure"); break;
					case 31: txtResponse.append("\r\nOther error"); break;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnAcceptcard.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("AcceptCard\r\n");

					int iRet = -1;
					if (btnCardDispenser.getSelection() == true) {
						iRet = objSmartCardDispenser.AcceptCard(10000);
					} else {
						iRet = objSmartCardReaderWriter.AcceptCard(10000);
					}
					txtResponse.append("\r\nReturn value- " + iRet);
					
					switch(iRet)
					{
					case 0: txtResponse.append("\r\nOperation successful"); break;
					case 1: txtResponse.append("\r\nChannel blocked"); break;
					case 2: txtResponse.append("\r\nInsertion/return mouth blocked"); break;
					case 18: txtResponse.append("\r\nOperation timeout occured"); break;
					case 20: txtResponse.append("\r\nDevice not yet connected"); break;
					case 28: txtResponse.append("\r\nCommunication failure"); break;
					case 31: txtResponse.append("\r\nOther error"); break;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnDispensecard.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("DispenseCard\r\n");

					int iRet = -1;
					if (btnCardDispenser.getSelection() == true) {
						iRet = objSmartCardDispenser.DispenseCard(10000);
					} else {
						iRet = objSmartCardReaderWriter.DispenseCard(10000);
					}
					txtResponse.append("\r\nReturn value- " + iRet);
					
					switch(iRet)
					{
					case 0: txtResponse.append("\r\nOperation successful"); break;
					case 1: txtResponse.append("\r\nChannel blocked"); break;
					case 2: txtResponse.append("\r\nInsertion/return mouth blocked"); break;
					case 3: txtResponse.append("\r\nStacker empty"); break;
					case 18: txtResponse.append("\r\nOperation timeout occured"); break;
					case 20: txtResponse.append("\r\nDevice not yet connected"); break;
					case 28: txtResponse.append("\r\nCommunication failure"); break;
					case 31: txtResponse.append("\r\nOther error"); break;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnReturncard.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("ReturnCard\r\n");

					DispenseModeDB objDispenseModeDB = new DispenseModeDB();
					objDispenseModeDB.open();
					if (objDispenseModeDB.bFormResult) {
						txtResponse.append("\r\nDispense Mode- " + objDispenseModeDB.iDispenseModeSelected);

						int iRet = -1;
						if (btnCardDispenser.getSelection() == true) {
							iRet = objSmartCardDispenser.ReturnCard(objDispenseModeDB.iDispenseModeSelected, 10000);
						} else {
							iRet = objSmartCardReaderWriter.ReturnCard(objDispenseModeDB.iDispenseModeSelected, 10000);
						}
						txtResponse.append("\r\nReturn value- " + iRet);
						
						switch(iRet)
						{
						case 0: txtResponse.append("\r\nOperation successful"); break;
						case 1: txtResponse.append("\r\nReturn mouth blocked"); break;
						case 2: txtResponse.append("\r\nNo card in channel"); break;
						case 18: txtResponse.append("\r\nOperation timeout occured"); break;
						case 20: txtResponse.append("\r\nDevice not yet connected"); break;
						case 28: txtResponse.append("\r\nCommunication failure"); break;
						case 31: txtResponse.append("\r\nOther error"); break;
						}
					} else {
						txtResponse.append("\r\nParameter not valid");
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnRejectcard.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("RejectCard\r\n");

					int iRet = -1;
					if (btnCardDispenser.getSelection() == true) {
						iRet = objSmartCardDispenser.RejectCard(10000);
					} else {
						iRet = objSmartCardReaderWriter.RejectCard(10000);
					}
					txtResponse.append("\r\nReturn value- " + iRet);
					
					switch(iRet)
					{
					case 0: txtResponse.append("\r\nOperation successful"); break;
					case 1: txtResponse.append("\r\nRejection bin full"); break;
					case 2: txtResponse.append("\r\nNo card in channel"); break;
					case 18: txtResponse.append("\r\nOperation timeout occured"); break;
					case 20: txtResponse.append("\r\nDevice not yet connected"); break;
					case 28: txtResponse.append("\r\nCommunication failure"); break;
					case 31: txtResponse.append("\r\nOther error"); break;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnCollectcard.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("CollectCard\r\n");

					int iRet = -1;
					if (btnCardDispenser.getSelection() == true) {
						iRet = objSmartCardDispenser.CollectCard(10000);
					} else {
						iRet = objSmartCardReaderWriter.CollectCard(10000);
					}
					txtResponse.append("\r\nReturn value- " + iRet);
					
					switch(iRet)
					{
					case 0: txtResponse.append("\r\nOperation successful"); break;
					case 1: txtResponse.append("\r\nCollection bin full"); break;
					case 2: txtResponse.append("\r\nNo card in channel"); break;
					case 18: txtResponse.append("\r\nOperation timeout occured"); break;
					case 20: txtResponse.append("\r\nDevice not yet connected"); break;
					case 28: txtResponse.append("\r\nCommunication failure"); break;
					case 31: txtResponse.append("\r\nOther error"); break;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnIscardinchannel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("IsCardInChannel\r\n");

					int iRet = -1;
					if (btnCardDispenser.getSelection() == true) {
						iRet = objSmartCardDispenser.IsCardInChannel(10000);
					} else {
						iRet = objSmartCardReaderWriter.IsCardInChannel(10000);
					}
					txtResponse.append("\r\nReturn value- " + iRet);
					
					switch(iRet)
					{
					case 0: txtResponse.append("\r\nNo card in channel"); break;
					case 1: txtResponse.append("\r\nCard found in channel"); break;
					case 18: txtResponse.append("\r\nOperation timeout occured"); break;
					case 20: txtResponse.append("\r\nDevice not yet connected"); break;
					case 28: txtResponse.append("\r\nCommunication failure"); break;
					case 31: txtResponse.append("\r\nOther error"); break;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnIscardremoved.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("IsCardRemoved\r\n");

					int iRet = -1;
					if (btnCardDispenser.getSelection() == true) {
						iRet = objSmartCardDispenser.IsCardRemoved(10000);
					} else {
						iRet = objSmartCardReaderWriter.IsCardRemoved(10000);
					}
					txtResponse.append("\r\nReturn value- " + iRet);
					
					switch(iRet)
					{
					case 0: txtResponse.append("\r\nNot removed"); break;
					case 1: txtResponse.append("\r\nRemoved"); break;
					case 18: txtResponse.append("\r\nOperation timeout occured"); break;
					case 20: txtResponse.append("\r\nDevice not yet connected"); break;
					case 28: txtResponse.append("\r\nCommunication failure"); break;
					case 31: txtResponse.append("\r\nOther error"); break;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnSamslotpoweronoff.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("SAMSlotPowerOnOff\r\n");

					SAMSlotDB objSAMSlotDB = new SAMSlotDB();
					objSAMSlotDB.open();
					if (objSAMSlotDB.bFormResult) {
						txtResponse.append("\r\nSAM Slot- " + objSAMSlotDB.iSAMSlot + "\r\nPowerOnOffState- "
								+ objSAMSlotDB.iPowerState);

						int iRet = -1;
						if (btnCardDispenser.getSelection() == true) {
							iRet = objSmartCardDispenser.SAMSlotPowerOnOff(objSAMSlotDB.iSAMSlot,
									objSAMSlotDB.iPowerState, 10000);
						} else {
							iRet = objSmartCardReaderWriter.SAMSlotPowerOnOff(objSAMSlotDB.iSAMSlot,
									objSAMSlotDB.iPowerState, 10000);
						}
						txtResponse.append("\r\nReturn value- " + iRet);
						
						switch(iRet)
						{
						case 0: txtResponse.append("\r\nOperation successful"); break;
						case 1: txtResponse.append("\r\nOperation failed"); break;
						case 18: txtResponse.append("\r\nOperation timeout occured"); break;
						case 20: txtResponse.append("\r\nDevice not yet connected"); break;
						case 28: txtResponse.append("\r\nCommunication failure"); break;
						case 31: txtResponse.append("\r\nOther error"); break;
						}
					} else {
						txtResponse.append("\r\nParameter not valid");
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnResetsam.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("ResetSAM\r\n");

					ResetSAMDB objResetSAMDB = new ResetSAMDB();
					objResetSAMDB.open();
					if (objResetSAMDB.bFormResult) {
						txtResponse.append("\r\nSAM Slot- " + objResetSAMDB.iSAMSlot + "\r\nReset Type- "
								+ objResetSAMDB.iResetType);

						byte[] byRet = null;
						if (btnCardDispenser.getSelection() == true) {
							byRet = objSmartCardDispenser.ResetSAM(objResetSAMDB.iSAMSlot, objResetSAMDB.iResetType,
									10000);
						} else {
							byRet = objSmartCardReaderWriter.ResetSAM(objResetSAMDB.iSAMSlot, objResetSAMDB.iResetType,
									10000);
						}
						txtResponse.append("\r\nReturn value- ");
						for (int i = 0; i < byRet.length; i++) {
							txtResponse.append(String.format("%02X ", byRet[i]));
						}
						
						txtResponse.append("\r\nStatus of reset");
						switch(byRet[0])
						{
						case 0: txtResponse.append("\r\nOperation successful"); break;
						case 1: txtResponse.append("\r\nOperation failed"); break;
						case 2: txtResponse.append("\r\nNo contact card (SAM) found"); break;
						case 18: txtResponse.append("\r\nOperation timeout occured"); break;
						case 20: txtResponse.append("\r\nDevice not yet connected"); break;
						case 28: txtResponse.append("\r\nCommunication failure"); break;
						case 31: txtResponse.append("\r\nOther error"); break;
						}
						
						txtResponse.append("\r\nATR- ");
						for (int i = 1; i < byRet.length; i++) {
							txtResponse.append(String.format("%02X ", byRet[i]));
						}
					} else {
						txtResponse.append("\r\nParameter not valid");
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnActivatecard.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("ActivateCard\r\n");

					ActivateCardDB objActivateCardDB = new ActivateCardDB();
					objActivateCardDB.open();
					if (objActivateCardDB.bFormResult) {
						txtResponse.append("\r\nCard Type- " + objActivateCardDB.iCardType + "\r\nSAM Slot- "
								+ objActivateCardDB.iSAMSlot);

						byte[] byRet = null;
						if (btnCardDispenser.getSelection() == true) {
							byRet = objSmartCardDispenser.ActivateCard(objActivateCardDB.iCardType,
									objActivateCardDB.iSAMSlot, 10000);
						} else {
							byRet = objSmartCardReaderWriter.ActivateCard(objActivateCardDB.iCardType,
									objActivateCardDB.iSAMSlot, 10000);
						}
						txtResponse.append("\r\nReturn value- ");
						for (int i = 0; i < byRet.length; i++) {
							txtResponse.append(String.format("%02X ", byRet[i]));
						}
						
						txtResponse.append("\r\nStatus of card activation");
						switch(byRet[0])
						{
						case 0: txtResponse.append("\r\nCard found and activated"); break;
						case 1: txtResponse.append("\r\nCard found but activation failed"); break;
						case 2: txtResponse.append("\r\nCard found but it is unsupported"); break;
						case 10: txtResponse.append("\r\nNo card found"); break;
						case 18: txtResponse.append("\r\nOperation timeout occured"); break;
						case 20: txtResponse.append("\r\nDevice not yet connected"); break;
						case 28: txtResponse.append("\r\nCommunication failure"); break;
						case 31: txtResponse.append("\r\nOther error"); break;
						}
						
						txtResponse.append("\r\nType of card found");
						switch(byRet[1])
						{
						case 0: txtResponse.append("\r\nMIFARE DESFire/MIFARE SAM AV1"); break;
						case 1: txtResponse.append("\r\nMIFARE DESFire EV1/MIFARE SAM AV2"); break;
						case 2: txtResponse.append("\r\nMIFARE Ultralight"); break;
						case 10: txtResponse.append("\r\nNo card found"); break;
						case 18: txtResponse.append("\r\nOperation timeout occured"); break;
						case 20: txtResponse.append("\r\nDevice not yet connected"); break;
						case 28: txtResponse.append("\r\nCommunication failure"); break;
						case 31: txtResponse.append("\r\nOther error"); break;
						}
						
						txtResponse.append("\r\nSize of UID- " + byRet[2]);
						txtResponse.append("\r\nUID- ");
						for (int i = 3; i < byRet.length; i++) {
							txtResponse.append(String.format("%02X ", byRet[i]));
						}
					} else {
						txtResponse.append("\r\nParameter not valid");
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnDeactivatecard.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("DeactivateCard\r\n");

					ActivateCardDB objActivateCardDB = new ActivateCardDB();
					objActivateCardDB.open();
					if (objActivateCardDB.bFormResult) {
						txtResponse.append("\r\nCard Type- " + objActivateCardDB.iCardType + "\r\nSAM Slot- "
								+ objActivateCardDB.iSAMSlot);

						int iRet = -1;
						if (btnCardDispenser.getSelection() == true) {
							iRet = objSmartCardDispenser.DeactivateCard(objActivateCardDB.iCardType,
									objActivateCardDB.iSAMSlot, 10000);
						} else {
							iRet = objSmartCardReaderWriter.DeactivateCard(objActivateCardDB.iCardType,
									objActivateCardDB.iSAMSlot, 10000);
						}
						txtResponse.append("\r\nReturn value- " + iRet);
						
						switch(iRet)
						{
						case 0: txtResponse.append("\r\nCard found and deactivated"); break;
						case 1: txtResponse.append("\r\nCard found but deactivation failed"); break;
						case 10: txtResponse.append("\r\nNo card found"); break;
						case 18: txtResponse.append("\r\nOperation timeout occured"); break;
						case 20: txtResponse.append("\r\nDevice not yet connected"); break;
						case 28: txtResponse.append("\r\nCommunication failure"); break;
						case 31: txtResponse.append("\r\nOther error"); break;
						}
					} else {
						txtResponse.append("\r\nParameter not valid");
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnXchangeapdu.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("XChangeAPDU\r\n");

					XChangeApduDB objXChangeApduDB = new XChangeApduDB();
					objXChangeApduDB.open();
					if (objXChangeApduDB.bFormResult) {
						txtResponse.append("\r\nCard Type- " + objXChangeApduDB.iCardType + "\r\nSAM Slot- "
								+ objXChangeApduDB.iSAMSlot + "\r\nAPDU- " + objXChangeApduDB.strAPDU);

						byte[] byHexAPDU = hexStringToByteArray(objXChangeApduDB.strAPDU);

						byte[] byRet = null;
						if (byHexAPDU != null) {
							if (btnCardDispenser.getSelection() == true) {
								byRet = objSmartCardDispenser.XChangeAPDU(objXChangeApduDB.iCardType, byHexAPDU,
										objXChangeApduDB.iSAMSlot, 10000);
							} else {
								byRet = objSmartCardReaderWriter.XChangeAPDU(objXChangeApduDB.iCardType, byHexAPDU,
										objXChangeApduDB.iSAMSlot, 10000);
							}
							txtResponse.append("\r\nReturn value- ");
							for (int i = 0; i < byRet.length; i++) {
								txtResponse.append(String.format("%02X ", byRet[i]));
							}
							
							switch(byRet[0])
							{
							case 0: txtResponse.append("\r\nExecuted successfully"); break;
							case 1: txtResponse.append("\r\nExecution failed"); break;
							case 10: txtResponse.append("\r\nNo card found"); break;
							case 18: txtResponse.append("\r\nOperation timeout occured"); break;
							case 20: txtResponse.append("\r\nDevice not yet connected"); break;
							case 28: txtResponse.append("\r\nCommunication failure"); break;
							case 31: txtResponse.append("\r\nOther error"); break;
							}
							
							txtResponse.append("\r\nResponse- ");
							for (int i = 1; i < byRet.length; i++) {
								txtResponse.append(String.format("%02X ", byRet[i]));
							}
						} else {
							txtResponse.append("\r\nAPDU not proper");
						}
					} else {
						txtResponse.append("\r\nParameter not valid");
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnReadultralightblock.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("ReadUltralightBlock\r\n");

					ReadUltraLightDB objReadUltraLightDB = new ReadUltraLightDB();
					objReadUltraLightDB.open();
					if (objReadUltraLightDB.bFormResult) {
						txtResponse.append("\r\nAddress- " + objReadUltraLightDB.iAddress);

						byte[] byRet = null;
						if (btnCardDispenser.getSelection() == true) {
							byRet = objSmartCardDispenser.ReadUltralightBlock(objReadUltraLightDB.iAddress, 10000);
						} else {
							byRet = objSmartCardReaderWriter.ReadUltralightBlock(objReadUltraLightDB.iAddress, 10000);
						}
						txtResponse.append("\r\nReturn value- ");
						for (int i = 0; i < byRet.length; i++) {
							txtResponse.append(String.format("%02X ", byRet[i]));
						}
						
						switch(byRet[0])
						{
						case 0: txtResponse.append("\r\nReading successful"); break;
						case 1: txtResponse.append("\r\nReading failed"); break;
						case 18: txtResponse.append("\r\nOperation timeout occured"); break;
						case 20: txtResponse.append("\r\nDevice not yet connected"); break;
						case 28: txtResponse.append("\r\nCommunication failure"); break;
						case 31: txtResponse.append("\r\nOther error"); break;
						}
						
						txtResponse.append("\r\nResponse- ");
						for (int i = 1; i < byRet.length; i++) {
							txtResponse.append(String.format("%02X ", byRet[i]));
						}
					} else {
						txtResponse.append("\r\nParameter not valid");
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnWriteultralightpage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("WriteUltralightPage\r\n");

					WriteUltraLightDB objWriteUltraLightDB = new WriteUltraLightDB();
					objWriteUltraLightDB.open();
					if (objWriteUltraLightDB.bFormResult) {
						txtResponse.append("\r\nAddress- " + objWriteUltraLightDB.iAddress + "\r\nAPDU- "
								+ objWriteUltraLightDB.strAPDU);

						byte[] byHexAPDU = hexStringToByteArray(objWriteUltraLightDB.strAPDU);

						int iRet = -1;
						if (byHexAPDU != null) {
							if (btnCardDispenser.getSelection() == true) {
								iRet = objSmartCardDispenser.WriteUltralightPage(objWriteUltraLightDB.iAddress,
										byHexAPDU, 10000);
							} else {
								iRet = objSmartCardReaderWriter.WriteUltralightPage(objWriteUltraLightDB.iAddress,
										byHexAPDU, 10000);
							}
							txtResponse.append("\r\nReturn value- " + iRet);
							
							switch(iRet)
							{
							case 0: txtResponse.append("\r\nWrite successful"); break;
							case 1: txtResponse.append("\r\nWrite failed"); break;
							case 18: txtResponse.append("\r\nOperation timeout occured"); break;
							case 20: txtResponse.append("\r\nDevice not yet connected"); break;
							case 28: txtResponse.append("\r\nCommunication failure"); break;
							case 31: txtResponse.append("\r\nOther error"); break;
							}
						} else {
							txtResponse.append("\r\nAPDU not proper");
						}
					} else {
						txtResponse.append("\r\nParameter not valid");
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnDisconnectDevice.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					lblStatusVal.setText("");

					if (btnCardDispenser.getSelection() == true) {
						txtResponse.append("*** Cash Dispenser ***\r\n");
					} else {
						txtResponse.append("*** Cash Reader Writer ***\r\n");
					}
					txtResponse.append("DisConnectDevice\r\n");

					int iRet = -1;
					if (btnCardDispenser.getSelection() == true) {
						iRet = objSmartCardDispenser.DisConnectDevice(10000);
					} else {
						iRet = objSmartCardReaderWriter.DisConnectDevice(10000);
					}
					txtResponse.append("\r\nReturn value- " + iRet);
					
					switch(iRet)
					{
					case 0: txtResponse.append("\r\nDisconnected successfully"); break;
					case 18: txtResponse.append("\r\nOperation timeout occured"); break;
					case 20: txtResponse.append("\r\nDevice not yet connected"); break;
					case 28: txtResponse.append("\r\nCommunication failure"); break;
					case 31: txtResponse.append("\r\nOther error"); break;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	public static byte[] hexStringToByteArray(String s) {
		try {
			byte[] b = new byte[s.length() / 2];
			for (int i = 0; i < b.length; i++) {
				int index = i * 2;
				int v = Integer.parseInt(s.substring(index, index + 2), 16);
				b[i] = (byte) v;
			}
			return b;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
