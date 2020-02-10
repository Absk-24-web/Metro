import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import Cris.Security;
import Cris.UPS;
import jssc.SerialPort;

public class MainWindow 
{

	protected Shell shell;
	private Text txtResponseSheet;
	public Security objSecurity=new Security();
	public UPS objUPS=new UPS();
	private String strOSType;
	private static SerialPort serialPort;
	private Text textSecurityPort;
	private Text textUpsPort;
	
	public static void main(String[] args)
	{
		try 
		{
			MainWindow window = new MainWindow();
			window.open();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}	
	
	/**
	 * Open the window.
	 */
	public void open()
	{
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
	}
	
	public static byte GetBCC(byte []p,int n)
	{
		byte  ch;
		int	i; 
		byte crc = (byte)0x00;
	
		for(i=0;i<n;i++)
		{
			ch = p[i];				
			crc = (byte)(ch ^ crc);
		}		
		return crc;
	}
	
	public static String GetErrorDesc(byte byE1, byte byE0)
	{
		String strRes="";
		
		switch(String.format("%02x%02x", byE1, byE0))
		{
			case "0000": strRes = "Reception of undefined command";	break;
			case "0001": strRes = "Command parameter error";	break;
		}
		
		return strRes;
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() 
	{
		shell = new Shell();
		shell.setSize(new Point(550, 503));
		shell.setSize(550, 600);
		shell.setText("SWT Application");
		
		strOSType = System.getProperty("os.name").toLowerCase();
		
		txtResponseSheet = new Text(shell, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		//txtResponse.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		txtResponseSheet.setBounds(10, 366, 507, 143);
		
		Label lblResponse = new Label(shell, SWT.NONE);
		lblResponse.setText("Response");
		//lblResponse.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblResponse.setBounds(10, 327, 72, 25);
		//if(strOSType.indexOf("win") >= 0)
			//cmbCOMPort.setItems(new String[] {"Select", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"});
		//else
			//cmbCOMPort.setItems(new String[] {"Select", "/dev/ttyS0", "/dev/ttyS1", "/dev/ttyS2", "/dev/ttyS3", "/dev/ttyS4", "/dev/ttyS5", "/dev/ttyS6", "/dev/ttyS7", "/dev/ttyS8", "/dev/ttyS9"});
		
		Label lblStatusVal = new Label(shell, SWT.NONE);
		lblStatusVal.setText("");
		//lblStatusVal.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		lblStatusVal.setBounds(10, 528, 507, 28);
		
		Button btnClear = new Button(shell, SWT.NONE);		
		btnClear.setText("Clear");
		//btnClear.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		btnClear.setBounds(430, 330, 87, 30);
		
		Button btnDisableAlarm = new Button(shell, SWT.NONE);
		btnDisableAlarm.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				int res= objSecurity.DisableAlarm(0, 100);
				//txtResponseSheet.setText("");
				//txtResponseSheet.setText("******SecurityDevice*******\r\n");
				txtResponseSheet.append("\r\nDisableAlarm Response :- "+res);	
			}
		});
		btnDisableAlarm.setText("DisableAlarm");
		//btnReset.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		btnDisableAlarm.setBounds(358, 105, 159, 46);
		//formToolkit.adapt(btnLivePacket, true, true);
		
		Button btnSecurityConnect = new Button(shell, SWT.NONE);
		btnSecurityConnect.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				int PortId=Integer.parseInt(textSecurityPort.getText().toString());
				int res= objSecurity.ConnectDevice(PortId, 100);
				txtResponseSheet.setText("");
				txtResponseSheet.setText("\r\n******SecurityDevice*******\r\n");
				txtResponseSheet.append("\r\nConnect Security Response :- "+res);
			}
		});
		btnSecurityConnect.setText("Connect Device");
		//btnInitializeDevice.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		//btnInitializeDevice.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		btnSecurityConnect.setBounds(31, 53, 159, 46);
		
		Button btnSecurityGetDoorStatus = new Button(shell, SWT.NONE);
		btnSecurityGetDoorStatus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				int res= objSecurity.GetDoorStatus(0);
				//txtResponseSheet.setText("");
				//txtResponseSheet.setText("******SecurityDevice*******\r\n");
				txtResponseSheet.append("\r\nGetDoorStatus Response :- "+res);
			}
		});
		btnSecurityGetDoorStatus.setText("GetDoorStatus");
		//btnAskversion.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		btnSecurityGetDoorStatus.setBounds(358, 53, 159, 46);
		
		Button btnSecurityDisconnect = new Button(shell, SWT.NONE);
		btnSecurityDisconnect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				int res= objSecurity.DisConnectDevice(100);
				//txtResponseSheet.setText("");
				//txtResponseSheet.setText("******SecurityDevice*******\r\n");
				txtResponseSheet.append("\r\nDisconnect Response :- "+res);
			}
		});
		btnSecurityDisconnect.setText("Disconnect Device");
		btnSecurityDisconnect.setBounds(31, 105, 159, 46);
		
		Button btnSecurityNativeLib = new Button(shell, SWT.NONE);
		btnSecurityNativeLib.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				String res= objSecurity.GetNativeLibVersion();
				//txtResponseSheet.setText("");
				//txtResponseSheet.setText("******SecurityDevice*******\r\n");
				txtResponseSheet.append("\r\nGetNativeLibVersion Response :- "+res);
			}
		});
		btnSecurityNativeLib.setText("GetNativeVersion");
		btnSecurityNativeLib.setBounds(193, 53, 159, 46);
		
		Button btnSecurityFWVersion = new Button(shell, SWT.NONE);
		btnSecurityFWVersion.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				String res= objSecurity.GetSecurityDevFWVersion();
				//txtResponseSheet.setText("");
				//txtResponseSheet.setText("******SecurityDevice*******\r\n");
				txtResponseSheet.append("\r\nGetSecurityDevFWVersion Response :- "+res);
			}
		});
		btnSecurityFWVersion.setText("SecurityFWVersion");
		btnSecurityFWVersion.setBounds(196, 105, 159, 46);
		
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setBounds(384, 10, 133, 25);
		lblNewLabel.setText("SECURITY TOOL");
		
		Button btnUpsFwversion = new Button(shell, SWT.NONE);
		btnUpsFwversion.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				String res= objUPS.GetUPSFWVersion();
				//txtResponseSheet.setText("");
				//txtResponseSheet.setText("******SecurityDevice*******\r\n");
				txtResponseSheet.append("\r\nGetUPSFWVersion Response :- "+res);
			}
		});
		btnUpsFwversion.setText("GetUPSFWVersion");
		btnUpsFwversion.setBounds(193, 215, 159, 46);
		
		Button btnUpsGetNative = new Button(shell, SWT.NONE);
		btnUpsGetNative.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				String res= objUPS.GetNativeLibVersion();
				//txtResponseSheet.setText("");
				//txtResponseSheet.setText("******SecurityDevice*******\r\n");
				txtResponseSheet.append("\r\nGetNativeLibVersion UPS Response :- "+res);
			}
		});
		btnUpsGetNative.setText("GetNativeVersion");
		btnUpsGetNative.setBounds(358, 215, 159, 46);
		
		Button btnUpsStatus = new Button(shell, SWT.NONE);
		btnUpsStatus.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				int res= objUPS.GetUPStatus();
				//txtResponseSheet.setText("");
				//txtResponseSheet.setText("******SecurityDevice*******\r\n");
				txtResponseSheet.append("\r\nGeUPSStatus Response :- "+res);
			}
		});
		btnUpsStatus.setText("UPS Status");
		btnUpsStatus.setBounds(193, 267, 159, 46);
		
		Button btnBatteryStatus = new Button(shell, SWT.NONE);
		btnBatteryStatus.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				int res= objUPS.GetBatteryStatus();
				//txtResponseSheet.setText("");
				//txtResponseSheet.setText("******SecurityDevice*******\r\n");
				txtResponseSheet.append("\r\nGetBatteryStatus UPS Response :- "+res);
			}
		});
		btnBatteryStatus.setText("Battery Status");
		btnBatteryStatus.setBounds(358, 267, 159, 46);
		
		Button btnUpsConnect = new Button(shell, SWT.NONE);
		btnUpsConnect.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				int PortId=Integer.parseInt(textUpsPort.getText().toString());
				int res= objUPS.ConnectDevice(PortId, 100);
				//txtResponseSheet.setText("");
				txtResponseSheet.setText("\r\n******UPS Device*******\r\n");
				txtResponseSheet.append("\r\nConnect UPS Response :- "+res);
			}
		});
		btnUpsConnect.setText("Connect Device");
		btnUpsConnect.setBounds(31, 215, 159, 46);
		
		Button btnUpsDisconnect = new Button(shell, SWT.NONE);
		btnUpsDisconnect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				int res= objUPS.DisConnectDevice(100);
				//txtResponseSheet.setText("");
				//txtResponseSheet.setText("******SecurityDevice*******\r\n");
				txtResponseSheet.append("\r\nDisconnect UPS Response :- "+res);
			}
			
		});
		btnUpsDisconnect.setText("Disconnect Device");
		btnUpsDisconnect.setBounds(31, 267, 159, 46);
		
		Label lblUpsTool = new Label(shell, SWT.NONE);
		lblUpsTool.setText("UPS TOOL");
		lblUpsTool.setBounds(397, 173, 120, 36);
		
		textSecurityPort = new Text(shell, SWT.BORDER);
		textSecurityPort.setBounds(167, 7, 120, 40);
		
		Label lblNewLabel_1 = new Label(shell, SWT.NONE);
		lblNewLabel_1.setBounds(55, 10, 78, 25);
		lblNewLabel_1.setText("Enter Port");
		
		Label label = new Label(shell, SWT.NONE);
		label.setText("Enter Port");
		label.setBounds(31, 173, 102, 36);
		
		textUpsPort = new Text(shell, SWT.BORDER);
		textUpsPort.setBounds(167, 173, 120, 36);
		
		Button btnExit = new Button(shell, SWT.NONE);
		btnExit.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				System.exit(0);
			}
			
		});
		btnExit.setText("EXIT");
		btnExit.setBounds(77, 319, 150, 41);
		shell.setTabList(new Control[]{btnClear, txtResponseSheet});
		
		// Clear button functioning
		btnClear.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				try
				{
					txtResponseSheet.setText("");
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});
	}
}

