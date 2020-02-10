
package Cris;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jxfs.control.IJxfsBaseControl;
import com.jxfs.control.cdr.IJxfsCDRConst;
import com.jxfs.control.cdr.JxfsATM;
import com.jxfs.control.cdr.JxfsCashInOrder;
import com.jxfs.control.cdr.JxfsCurrency;
import com.jxfs.control.cdr.JxfsCurrencyCode;
import com.jxfs.control.cdr.JxfsDenomination;
import com.jxfs.control.cdr.JxfsDenominationItem;
import com.jxfs.control.cdr.JxfsDeviceStatus;
import com.jxfs.control.cdr.JxfsDispenseRequest;
import com.jxfs.events.IJxfsIntermediateListener;
import com.jxfs.events.JxfsException;
import com.jxfs.events.JxfsIntermediateEvent;
import com.jxfs.events.JxfsOperationCompleteEvent;
import com.jxfs.general.IJxfsConst;
import com.jxfs.general.JxfsDeviceManager;
import com.jxfs.general.JxfsRemoteDeviceInformation;
import com.mei.bnr.Bnr;
import com.mei.bnr.cashunit.DenominationInfo;
import com.mei.bnr.cashunit.IDenominationInfo;
import com.mei.bnr.enums.IntermediateStackerStatus;
import com.mei.bnr.enums.SafeDoorStatus;
import com.mei.bnr.enums.ShutterStatus;
import com.mei.bnr.enums.TransportStatus;
import com.mei.bnr.event.handlers.OperationCompleteEventHandler;
import com.mei.bnr.event.handlers.StatusEventHandler;
import com.mei.bnr.exception.BnrException;
import com.mei.bnr.jxfs.device.DeviceType;
import com.mei.bnr.jxfs.device.IDevice;
import com.mei.bnr.jxfs.device.IDeviceProvider;
import com.mei.bnr.jxfs.device.IIdentification.ModuleIdentificationEnum;
import com.mei.bnr.jxfs.device.MEIModuleIdentification;
import com.mei.bnr.jxfs.device.state.MEIModuleStatus;
import com.mei.bnr.jxfs.drivers.BnrUsbDriver;
import com.mei.bnr.jxfs.service.IDirectIOConsts;
import com.mei.bnr.jxfs.service.SpecificDeviceManager;
import com.mei.bnr.jxfs.service.data.MEIBnrStatus;
import com.mei.bnr.jxfs.service.data.MEICashInOrder;
import com.mei.bnr.jxfs.service.data.MEICashUnit;
import com.mei.bnr.jxfs.util.ISynchronousOperation;
import com.mei.bnr.jxfs.util.MEIJxfsException;
import com.mei.bnr.jxfs.util.SynchronousJxfsOperationHelper;
import com.mei.bnr.jxfs.xmlrpc.parameters.DirectIOModuleIdParameter;
import com.mei.bnr.jxfs.xmlrpc.parameters.DirectIOModuleSetIdentificationParameters;
import com.mei.bnr.state.IStatus;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;


public class Currency 
{
  public static int g_DeviceType = 0;
  public final static long CASH_IN_AMOUNT = 1000;
  public final static String CASH_IN_CURRENCY = "INR";
  public final static int EXPONENT = -2;  
  public static JxfsATM control;  
  public static SynchronousJxfsOperationHelper helper;  
  private static SerialPort serialPort = null;
  public static Thread thCoinAccept = null;
  public static Thread thAcceptCurrency=null;
  public static Boolean isTimeoutOccured=false;
  
  public static OperationCompleteEventHandler obj;
  public Bnr objBnr=new Bnr();
  public static StatusEventHandler    statusChanged;
  public static int g_5Notes = 0;
  public static int g_10Notes = 0;
  public static int g_20Notes = 0;
  public static int g_50Notes = 0;
  public static int g_100Notes = 0;
  public static int g_200Notes = 0;
  public static int g_500Notes = 0;
  public static int g_1000Notes = 0;
  public static int g_2000Notes = 0;
  public static int g_5Coin = 0;
  public static int g_10Coin = 0;
  public static int g_2Coin = 0;
  public static int g_1Coin = 0;
  public static byte[][] AcceptedAmount=new byte[25][2];
  public static Boolean isDeviceConnected=false;
  public static Boolean isCoinEscrowConnected=false;
  public static Boolean isAcceptingCoinRecursive=false;
  public static boolean isStartCoinAcceptOnce = false;
  public static Boolean isStartStopNoteAccept=false;
  public static Boolean isCurrencyAcceptingNote=false;
  public static Thread thNoteAccept = null;	
  
  public static Logger CurrencyLogger = null;
  private static volatile FileHandler fileHandler;
  private static volatile int iPrevDayOfYear = 0;
  public static int TotalAcceptedNotesAmount=0,TotalAcceptedCoinsAmount=0,TotalAmount=0;
  public static int isWhichDeviceSelected=0;
  public static Boolean isCurrencyAccepting=false;
  public static int ret=0;
  public static int retNote=31,retCoin=31;
  public static byte byPreviousValue = 0x00;
  
  public byte[][] getAcceptedAmount()
  {
      return this.AcceptedAmount;
  }
  
  public Currency()
  {

	    try
	    {
		   System.setProperty("java.library.path", "/lib/" );
		   Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
		   fieldSysPath.setAccessible( true );
		   fieldSysPath.set( null, null );
		   CurrencyLogger = Logger.getLogger(Currency.class.getName());
		   //logger.setLevel(Level.FINE);
		   CurrencyLogger.setLevel(Common.GetLogLevel());
		   //to show logs on console
		   CurrencyLogger.addHandler(new ConsoleHandler());
		}
	    catch (Exception e) 
	    {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
  }	
	
	private void WriteLog(Level objLevel, String strLog)
	{
		try
        {
			FileHandler fh = fileHandler;
			if (fh != null) 
			{
				CurrencyLogger.removeHandler(fh);
		        fh.close(); //Release any file lock.
		    }
			
			//adding custom handler
	        //logger.addHandler(new MyHandler());
	        
			Calendar cal = Calendar.getInstance();
			int iYear = cal.get(Calendar.YEAR);
			int iDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
			
			String strFileName = GlobalMembers.LogDirPath() + File.separator + "AVRMAPI_" + GlobalMembers.strHostName + "Log" + iYear + "." + iDayOfYear;
					
     	   File yourFile = new File(strFileName);
     	   yourFile.getParentFile().mkdirs();
     	   yourFile.createNewFile();
     	
     	 //FileHandler file name with max size and number of log files limit
         fileHandler = new FileHandler(strFileName, true);
         
         fileHandler.setFormatter(new MyFormatter());
         
         //setting custom filter for FileHandler
         fileHandler.setFilter(new MyFilter());
         
         //adding custom handler
         CurrencyLogger.addHandler(fileHandler);
         
         //smartCardlogger.log(objLevel, "prev- " + iPrevDayOfYear);
         //smartCardlogger.log(objLevel, "cur- " + iDayOfYear);
         
         if(iPrevDayOfYear == 0 || iPrevDayOfYear != iDayOfYear)
         {
         	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
         	
         	CurrencyLogger.log(objLevel, "Date: " + simpleDateFormat.format(new Date()));
         	iPrevDayOfYear = iDayOfYear;
         }
         
         CurrencyLogger.log(objLevel, strLog);
     }
     catch (SecurityException | IOException e1) 
	 {
         e1.printStackTrace();
     }
	}
  
	public void WriteReqResLog(String strCommandName, byte[] byReqRes) 
	{
		String strByteReqRes="";
		for (int responseLen = 0; responseLen < byReqRes.length; responseLen++) 
		{			
			strByteReqRes += String.format("%02X ", byReqRes[responseLen]);
		}
		System.out.println(strCommandName + " - " + strByteReqRes);
		WriteLog(Level.FINEST, strCommandName + " - " + strByteReqRes);
		WriteLog(Level.INFO, strCommandName + " - " + strByteReqRes);		
	}
	
	
  public int ConnectDevice(int PortID1,int PortID2,int PortID3,int DeviceType,int EscrowCleareanceMode,int timeout) 
  {
	    try 
	    {
	         //JxfsCashType obj1=new JxfsCashType(6014, 100, 6050,JxfsCash);
	    	if(DeviceType == 1)
	    	{
	    		if (isDeviceConnected)
	    		{
	    			return 20;
	    		}
	    		
    			g_DeviceType = DeviceType;
	    		
		    	if(PortID1 != 51)
		    		return 25; // Port does not exist
				try
				{
					//objBnr.
					IDeviceProvider driver = null;
					String deviceName = null;      
					driver = BnrUsbDriver.getInstance();
					System.out.println("Driver:- "+driver);
					Thread.sleep(400);
					deviceName = getDeviceNameFromBnrUsbDriver();
					Thread.sleep(400);
					System.out.println("DeviceName:- "+deviceName);
					
					initControl(driver, deviceName);
					subscribeEvents();
					control.open();    
					isDeviceConnected=true;
					getSetDateTime();
					makeBnrOperational(EscrowCleareanceMode);
					obj= new OperationCompleteEventHandler(Bnr.operationCompleteEvents);
					return 0;
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
		    		//System.out.println(e.getMessage());
		    		return 28;	
					//ConnectDevice(51, 0, 0,1, 1,100);
				}
	    		
	    	}
	    	else if(DeviceType == 2)
	    	{
	    		if (isCoinEscrowConnected)
    			{
	    			return 21;
    			}
    			try
	    		{
	    			if (PortID2==7 || PortID2 ==6)
	    				serialPort = new SerialPort("/dev/ttyACM1");
	    			else if (PortID2==5)
	    				serialPort = new SerialPort("/dev/ttyACM0");
	    			else
	    				serialPort = new SerialPort("/dev/ttyACM1");
	    			
    		        if(serialPort != null)
    		        {
    					// opening port
    		            if(serialPort.openPort() == true)
    		            {
    			            serialPort.setParams(SerialPort.BAUDRATE_19200, 	//SerialPort.BAUDRATE_9600,
    			                                 SerialPort.DATABITS_8,
    			                                 SerialPort.STOPBITS_1,
    			                                 SerialPort.PARITY_NONE);
    		            }
    		       
	    			
	    			if(GetControllerVersion().toLowerCase().contains("lipi cdc"))
	    			{
    					String strStatus = "";
    			        //Initialization of Coin Acceptor
    			        byte[] byResponse = null;
    					
    			        byte[] byCmd = new byte [] { 0x1B, 0x01, 0x04, 0x06, 0x08 };  //Initialization of Coin Acceptor					
    			        WriteReqResLog(MyFormatter.strSourceMethodName, byCmd);
    					if(serialPort.writeBytes(byCmd))
    					{
    						//Thread.sleep(15000);
    						Thread.sleep(500);
    						byte[] byRes1 = serialPort.readBytes(1, 5000);
    						int iPendingDataLen = byRes1[0];
    						byte[] byRes2 = serialPort.readBytes(iPendingDataLen, 5000);
    						
    						byResponse = new byte[byRes1.length + byRes2.length];
    						System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
    						System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
		    					
    						byte[] byRes3 = new byte[byResponse.length - 2];
    						System.arraycopy(byResponse, 1, byRes3, 0, byResponse.length - 2);
    						isCoinEscrowConnected=true;
    						
    			     		/*	try 
    						{
    						  byte[] byCmdCoinState = new byte [] { 0x1B, 0x0A, 0x03, 0x05, 0x07 };  //Coin Accept Starta
    						  serialPort.writeBytes(byCmdCoinState);
    						  Thread.sleep(500);
    						  byResponse = serialPort.readBytes();
    						  byPreviousValue=byResponse[11];
    						  System.out.print(String.format("%02x ",byPreviousValue));*/
    						  strStatus = new String(byRes3, "UTF-8");
    						  /*  objCRDResponse.strCoinInitStatus="Init Coin Acceptor:- " + strStatus;*/
		    					/*	} 
		    						catch (Exception e) 
		    						{
		    							
		    						}*/
		    						
    						if(strStatus.contains("OK"))
    						{							 
    							return 0;
    						}
    						else
    						{
    							return 22;
    						}
    					}
	    			   }
					}
					else
					{
						return 26;
					}
	    		
	    		}
	    		catch (Exception e)
                {
					return 31;
				}
		    		
    		g_DeviceType = DeviceType;	
	    }
	} 
	catch (Exception e)
	{			
    	/*if(g_DeviceType == 1)
    	{
    		e.printStackTrace();
    		System.out.println(e.getMessage());
    		return 28;
    	}
    	else if(g_DeviceType == 2)
    	{
    		isCoinEscrowConnected=false;
    		return 29;	    		
    	}
    	else
    	{*/
		isDeviceConnected=false;
		isCoinEscrowConnected=false;
		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));			
		WriteLog(Level.FINEST, errors.toString());
    	return 30;
    	
    	//}
	}
    return 28;    
  }
  
  
  public int DisConnectDevice(int DeviceType, int Timeout)
  {
	 if(DeviceType==1)  //Note Acceptor Disconnect
	 {
		 try
		 {
			 if (isDeviceConnected) 
			 {
				 control.cancel(0);
				 IStatus bnrStatus=objBnr.getStatus();
				 if(bnrStatus.getIntermediateStackerStatus() != IntermediateStackerStatus.EMPTY) //Some notes are present in escrow
				 {
					 control.close();
					 isDeviceConnected=false;  
					 return 1;
				 }
				 else 
				 {
					 control.close();
					 isDeviceConnected=false;
					 return 0;
				 }
			 }
			 else
			 {
                return 20; //Device Not Yet Connected
			 }
			
			
		 }
		 catch (Exception e)
		 {
			WriteLog(Level.WARNING, e.getMessage());
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
			return 28;  //Communication Failure
			
		 }
	 }
	 else if(DeviceType==2) //Coin Acceptor DIsconnect
	 {
		try
		{
			if(isCoinEscrowConnected)
			{
				int objResponse=31;
				try 
				{	
					isCoinEscrowConnected=false;
					serialPort.closePort();
					serialPort=null;
					objResponse=0;
					
					switch(objResponse)
					{
						case 0:   // Disconnected Successfully
						{
							objResponse=0;
						}
						break;				
						case 18:		//Operation timeout
						{
							objResponse = 18;
						}
						break;				
						case 20:		// Not yet connected
						{
							objResponse = 20;
						}
						break;
						default: 
							objResponse = 31;//Operation Timeout Occured
						break;
					}
					
				}
				catch(Exception e2)
				{
					objResponse = 28;//Communication Failure
					WriteLog(Level.WARNING, e2.getMessage());
					StringWriter errors = new StringWriter();
					e2.printStackTrace(new PrintWriter(errors));			
					WriteLog(Level.FINEST, errors.toString());			
				}
				WriteLog(Level.FINEST, "Return value- " + objResponse);
				return objResponse;	
			}
			else
			{
				return 21;
			}
		}
		catch (Exception e)
		{
			WriteLog(Level.WARNING, e.getMessage());
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
			return 29;
			
		}
	 }
	return 31;
  }
  
  
  
  private String GetControllerVersion()
  {
		String strControllerVersion = "01.00.00";
		if(serialPort != null)
		{
			byte[] byResponse = null;
	        byte[] byCmd = new byte [] {0x1B, 0x1D, 0x49, 0x01, 0x02 };  //Send Version of Controller					
			
			try
			{
				if(serialPort.writeBytes(byCmd))
				{
					byte[] byRes1 = serialPort.readBytes(1, 50000);
					
					int iPendingDataLen = byRes1[0];
					
					byte[] byRes2 = serialPort.readBytes(iPendingDataLen, 5000);
					
					byResponse = new byte[byRes1.length + byRes2.length];				
										
					System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
					System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
					
					byte[] byRes3 = new byte[byResponse.length - 2];
					
					System.arraycopy(byResponse, 1, byRes3, 0, byResponse.length - 2);
					
					try 
					{
						strControllerVersion = new String(byRes3, "UTF-8");
						//System.out.println("Controller Version - " + strControllerVersion);
						
					} 
					catch (UnsupportedEncodingException e) 
					{
						
					}					
				 }
			} 
			catch (Exception e) 
			{
				WriteLog(Level.WARNING, e.getMessage());
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));			
				WriteLog(Level.FINEST, errors.toString());
				return "00.00.00";
			} 
		}
		
		 return strControllerVersion;
	
}

  public boolean Reset() 
  {	 
	  	boolean bRet=false;
	    try 
	    {
	    	resetBnr(); 
			bRet = true;
		} 
	    catch (Exception e)
	    {
			bRet=false;
			WriteLog(Level.WARNING, e.getMessage());
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
		}    
	    return bRet;
  }
  
  public String GetNativeLibVersion()
  {
	  return "01.00.00";   //Native Version is True
  }
  
  private String  getDeviceNameFromBnrUsbDriver()
  {
	  String deviceName = null;
	  IDevice device = null;
	  
	  BnrUsbDriver bnrUsbDriver = BnrUsbDriver.getInstance();
	  
	  if (isContainsDeviceNames(bnrUsbDriver)) 
	  {
	  	deviceName = getDeviceName(bnrUsbDriver);
	  	device = bnrUsbDriver.getByName(deviceName);
	  }
	  if(device==null)
	  {
			
	  }
	  else if (isBnrNotConnected(device))
	  {
	  	System.err.println("No device found or device not a BNR");
	  	throw new IllegalStateException();
	  }//if
	  return deviceName;
  }
  
  
  public  String GetCurrencyDevFWVersion(int CurrencyType)
  {
	  if(CurrencyType == 1)
	  {    
		  try
		  {
			  //String name=getDeviceNameFromBnrUsbDriver();
			  return "01.00.00";
		  }
		  catch (Exception e)
		  {
			WriteLog(Level.WARNING, e.getMessage());
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
			 return "00.00.00";
		  }
	  }
	  else if(CurrencyType == 2)
	  {
		  try 
		  {
			  String name=GetControllerVersion();
			  return name;
		  } 
		  catch (Exception e)
		  {
				WriteLog(Level.WARNING, e.getMessage());
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));			
				WriteLog(Level.FINEST, errors.toString());
			  return "00.00.00";
		  }
	  }
	  return "00.00.00";

  }//getDeviceNameFromBnrUsbDriver
  
  
  public int IsNoteRemoved(int iTimeout)
  {
	  try 
	  {   if (isDeviceConnected)
	      {
			  IStatus bnrStatus = null;
			  bnrStatus = objBnr.getStatus();  			
			  if(bnrStatus.getShutterStatus() == ShutterStatus.OPEN)
			  {
			    	return 1;			
			  }
			  else if(bnrStatus.getShutterStatus() == ShutterStatus.CLOSED)
			  {
			    	return 0;			
			  }
			  else
			  {
				  return 31;
			  }
	      }
		  else
		  {
               return 20;
		  }
		 
	  } 
	  catch (BnrException e) 
	  {
			WriteLog(Level.WARNING, e.getMessage());
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());  
		    return 28;			
	  }
  }
  
  
  public byte[] DeviceStatus(int DeviceType, int Timeout)
  {
	  byte[] byReturnValue = new byte[12];
	  IStatus bnrStatus = null;
	
	      int i=0;
		  try 
		  {			  		
			    switch (DeviceType) 
			    {
				 case 0:
					 if(isDeviceConnected)
					 {
						 bnrStatus = objBnr.getStatus();  
						 if(bnrStatus.getDeviceStatus() == com.mei.bnr.enums.DeviceStatus.ONLINE && IsNoteRemoved(100)==0)
						    {
						    	byReturnValue[0] = 0;  //Operation Successful			    	
						    	
						    	byReturnValue[1] = 0x00;  //Communication Status (Ready)			    	
						    	
						    	byReturnValue[1] = (byte)(byReturnValue[1] | 0x00); //Readyness			
						    }
						    else
							{
						    	byReturnValue[0] = 28;  //Communication Failure			    	
						    	byReturnValue[1] = 0x01;  //Communication Status (Not Ready)			    	
						    	byReturnValue[1] = (byte)(byReturnValue[1] | 0x02); //(Not Ready)		
						    }
					 }
					 else
					 {
						    byReturnValue[0] = 20;  //Communication Failure			    	
					    	byReturnValue[1] = 0x01;  //Communication Status (Not Ready)			    	
					    	byReturnValue[1] = (byte)(byReturnValue[1] | 0x02); //(Not Ready)
					 }
				
					    if(isCoinEscrowConnected)
					    {
					    	byReturnValue[0] = 0;  //Operation Successful			    	
					    	
					    	byReturnValue[2] = 0x00;  //Communication Status (Ready)			    	
					    	
					    	byReturnValue[2] = (byte)(byReturnValue[2] | 0x00); //Readyness			    	 	
					    }
					    else
					    {
					    	byReturnValue[0] = 21;  //Failure			    	
					    	
					    	byReturnValue[2] = 0x01;  //Communication Status ( Not Ready)			    	
					    	
					    	byReturnValue[2] = (byte)(byReturnValue[2] | 0x02); //Not Ready	
					    }
					    
					    
					break;
				case 1:
				   if(isDeviceConnected)
				   {
					   bnrStatus = objBnr.getStatus();  
					  // I/sNoteRemoved(100);
					   if(bnrStatus.getDeviceStatus() == com.mei.bnr.enums.DeviceStatus.ONLINE && IsNoteRemoved(100)==0)
					    {
					    	byReturnValue[0] = 0;  //Operation Successful			    	
					    	
					    	byReturnValue[1] = 0x00;  //Communication Status (Ready)			    	
					    	
					    	byReturnValue[1] = (byte)(byReturnValue[1] | 0x00); //Readyness			
					    }
					    else
						 {
					    	byReturnValue[0] = 28;  //Communication Failure			    	
					    	
					    	byReturnValue[1] = 0x01;  //Communication Status (Not Ready)			    	
					    	
					    	byReturnValue[1] = (byte)(byReturnValue[1] | 0x02); //(Not Ready)		
					    }
				   }
				   else
				   {
					    byReturnValue[0] = 20;  //Communication Failure			    	
				    	
				    	byReturnValue[1] = 0x01;  //Communication Status (Not Ready)			    	
				    	
				    	byReturnValue[1] = (byte)(byReturnValue[1] | 0x02); //(Not Ready)
				   }
					break;
				case 2:
					  if(isCoinEscrowConnected)
					    {
					    	byReturnValue[0] = 0;  //Operation Successful			    	
					    	
					    	byReturnValue[2] = 0x00;  //Communication Status (Ready)			    	
					    	
					    	byReturnValue[2] = (byte)(byReturnValue[2] | 0x00); //Readyness			    	 	
					    }
					    else
					    {
					    	byReturnValue[0] = 21;  //Failure			    	
					    	
					    	byReturnValue[2] = 0x01;  //Communication Status ( Not Ready)			    	
					    	
					    	byReturnValue[2] = (byte)(byReturnValue[2] | 0x02); //Not Ready	
					    }
					break;

				default:
					break;
				}
                 
			    if(DeviceType==0||DeviceType==1)
			    {
			    	if(isDeviceConnected)
			    	{
			    	 if(bnrStatus.getSafeDoorStatus() == SafeDoorStatus.LOCKED)
			    		 byReturnValue[1] = (byte)(byReturnValue[1] | 0x04);  //Closed
			    	 else
			    		 byReturnValue[1] = (byte)(byReturnValue[1] | 0x00); //Opened
			    	 
			    	 if(bnrStatus.getIntermediateStackerStatus() == IntermediateStackerStatus.EMPTY)
			    		 byReturnValue[1] = (byte)(byReturnValue[1] | 0x00);  //Escrow Empty
			    	 else
			    		 byReturnValue[1] = (byte)(byReturnValue[1] | 0x08); //Escrow Not Empty
			   
			    	 if(bnrStatus.getTransportStatus() == TransportStatus.OK)
			    		 byReturnValue[1] = (byte)(byReturnValue[1] | 0x00); //Transport Status
			    
			    	}
			    	else
			    	{
			    		
			    	}
			     }
			    	 byReturnValue[1] = (byte)(byReturnValue[1] | 0x10); //Collection Box Status (Not Full) 
			    	 byReturnValue[1] = (byte)(byReturnValue[1] | 0x00); //Insertion Slot Status
			    	 byReturnValue[1] = (byte)(byReturnValue[1] | 0x00); //RFU
				    	// if(bnrStatus.getSafeDoorStatus() == SafeDoorStatus.LOCKED)
			    	 byReturnValue[2] = (byte)(byReturnValue[2] | 0x04);  //Closed
			        	// else
			    	 //	 byReturnValue[1] = (byte)(byReturnValue[1] | 0x04); //Opened
			    	 // if(bnrStatus.getIntermediateStackerStatus() == IntermediateStackerStatus.EMPTY)
			    	 byReturnValue[2] = (byte)(byReturnValue[2] | 0x00);  //Escrow Empty
			    	 //else
			    	 //	 byReturnValue[1] = (byte)(byReturnValue[1] | 0x08); //Escrow Not Empty
			    	 byReturnValue[2] = (byte)(byReturnValue[2] | 0x10); //Collection Box Status (Not Full) 
			    	 byReturnValue[2] = (byte)(byReturnValue[2] | 0x00); //Insertion Slot Status
			    	 // if(bnrStatus.getTransportStatus() == TransportStatus.OK)
			    	 byReturnValue[2] = (byte)(byReturnValue[2] | 0x00); //Transport Status
			    	 byReturnValue[2] = (byte)(byReturnValue[2] | 0x00); //RFU
			    	 
			    	 byte by_5notes = (byte)g_5Notes;
			    	 byte by_10notes = (byte)g_10Notes;
			    	 
			    	 byReturnValue[3] = by_10notes;
			    	 byReturnValue[3] <<= 4;
			    	 byReturnValue[3] = (byte)(byReturnValue[3] | by_5notes);
			    	
			    	 byte by_20notes = (byte)g_20Notes;
			    	 byte by_50notes = (byte)g_50Notes;
			    	 
			    	 byReturnValue[4] = by_50notes;
			    	 byReturnValue[4] <<= 4;
			    	 byReturnValue[4] = (byte)(byReturnValue[4] | by_20notes);
			    			 
			    	 byte by_100notes = (byte)g_100Notes;
			    	 byte by_200notes = (byte)g_200Notes;
			    	 
			    	 byReturnValue[5] = by_200notes;
			    	 byReturnValue[5] <<= 4;
			    	 byReturnValue[5] = (byte)(byReturnValue[5] | by_100notes);
			    	 
			    	 byte by_500notes = (byte)g_500Notes;
			    	 byte by_1000notes = (byte)g_1000Notes;
			    	 
			    	 byReturnValue[6] = by_1000notes;
			    	 byReturnValue[6] <<= 4;
			    	 byReturnValue[6] = (byte)(byReturnValue[6] | by_500notes);
			    	 
			    	 byte by_2000notes = (byte)g_2000Notes;
			    	 byReturnValue[7] = by_2000notes;
			    	
			    	 byte by_RFU1 = (byte)0;
			    	 byte by_RFU2 = (byte)0;
			    	 
			    	 byReturnValue[9] = by_RFU2;
			    	 byReturnValue[9] <<= 4;
			    	 byReturnValue[9] = (byte)(byReturnValue[9] | by_RFU1);
			    	
			    	 byte by_5Coins = (byte)g_5Coin;
			    	 byte by_10Coins = (byte)g_10Coin;
			    	 
			    	 byReturnValue[10] = by_10Coins;
			    	 byReturnValue[10] <<= 4;
			    	 byReturnValue[10] = (byte)(byReturnValue[10] | by_5Coins);		    			 
			    			    	 
			    	 byte by_RFU3 = (byte)0;
			    	 byte by_RFU4 = (byte)0;
			    	 
			    	 byReturnValue[11] = by_RFU4;
			    	 byReturnValue[11] <<= 4;
			    	 byReturnValue[11] = (byte)(byReturnValue[11] | by_RFU3);
	
		  }	 
		  catch (Exception e)
		  {
			  byReturnValue[0]=1;
			  WriteLog(Level.WARNING, e.getMessage());
			  StringWriter errors = new StringWriter();
			  e.printStackTrace(new PrintWriter(errors));			
			  WriteLog(Level.FINEST, errors.toString());
		  }
	  return byReturnValue;
  }
  
  public int  GetValidCurrency(int CurrencyType,int Denom,int timeout)
  {
	  	try
	  	{
			if(CurrencyType == 1)
			{
				if(isDeviceConnected)
				{

			  		retNote=31;
				  	MEICashInOrder data = null;
					int insertedAmount = 0;  
					
				    try 
				    {
					  startCashInTransaction();   
					  data = cashIn(Denom, "INR");
					  insertedAmount = (int) data.getDenomination().getAmount();      

					  if (insertedAmount/100 != Denom) 
					  {
						  cashInRollback();
						  insertedAmount = 0;
						  retNote=2; 
					  }
					  else
					  {
						  retNote=0;  
					  }
						  
					  int amount=(int) (insertedAmount/100);
					  switch(amount)
					  {
					     case 5:		  g_5Notes++;
					    	 break;
					     case 10:		  g_10Notes++;
					    	 break;
					     case 20:		  g_20Notes++;
					    	 break;
					     case 50:		  g_50Notes++;
					    	 break;
					     case 100:		  g_100Notes++;
					    	 break;
					     case 200:		  g_200Notes++;
					    	 break;
					     case 500:		  g_500Notes++;
					    	 break;
					     case 1000:		  g_1000Notes++;
					    	 break;
					     case 2000:		  g_2000Notes++;
					    	 break; 	
					  }
				  }
			      catch (Exception e)
				  {
					 retNote= 28;
				  }  
				 return retNote;  
			  	
				}
				else
				{
					return 20;
				}
			}
			else if(CurrencyType == 2)
			{
				if(isCoinEscrowConnected)
				{
			  		int ret=StartCoinAccept(Denom);
			  		return 0;
				}
				else
				{
					return 21;
				}
			}
			return 0;
		}
	  	catch (Exception e)
	  	{
	  		WriteLog(Level.WARNING, e.getMessage());
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
			return 28;
		}
  }
  
  public int AcceptCurrentCurrency(int CurrencyType,int Denom, int Timeout)
  {
	  if(CurrencyType == 1)
	  { 
		  retNote=31;
		  try 
	      {
			 StopNoteAccept();//Calling Explicitly
			 ClearAcceptedAmountArray(1);
             if (isDeviceConnected) 
             {
    			 g_5Notes = 0;
    			 g_10Notes = 0;
    			 g_20Notes = 0;
    			 g_50Notes = 0;
    			 g_100Notes = 0;
    			 g_200Notes = 0;
    			 g_500Notes = 0;
    			 g_1000Notes = 0;
    			 g_2000Notes = 0;
    			  
    			helper.run(new ISynchronousOperation()
    			{
    				public int run(JxfsATM control) 
    				{
    					try 
    					{
    						control.cashInEnd();
    						retNote=0;
    					} 
    					catch (JxfsException e)
    					{
    						retNote=28;
    					}
    					return retNote;
    				}
    			});
			 }
             else
             {
            	 retNote= 20;
            	 //return retNote;
			 }
			
		 } 
	     catch (Exception e) 
	     {
			retNote= 28;
			WriteLog(Level.WARNING, e.getMessage());
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
		 }
		 // return retNote;
	  }
	  else if(CurrencyType == 2)
	  {
		  retCoin=31;
		  try
		  {
			  StopCoinAccept(); //Calling Explicitly
			  g_10Coin=0;g_5Coin=0;
			  byte[] status = new byte[]{ 0x1B, 0x33, 0x36, 0x39, 0x42 };   
			  serialPort.writeBytes(status);
			  Thread.sleep(500);
			  String s=serialPort.readHexString();
			  retCoin=0;
			  ClearAcceptedAmountArray(0);
		  }
		  catch (Exception e)
		  {
			    retCoin=30;
				WriteLog(Level.WARNING, e.getMessage());
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));			
				WriteLog(Level.FINEST, errors.toString());
			  
		  } 
		  //return retCoin;
	  }
	  
	   if(CurrencyType==1)
	   {
		   return retNote;
	   }
	   else
	   {
		  return retCoin;
	   }
  }
  
  
  public int StackAcceptedCurrencies(int Timeout)
  {
	  try 
      {
		   isTimeoutOccured=false;
		   ret=31;
		   g_5Notes = 0;
		   g_10Notes = 0;
		   g_20Notes = 0;
		   g_50Notes = 0;
		   g_100Notes = 0;
		   g_200Notes = 0;
		   g_500Notes = 0;
		   g_1000Notes = 0;
		   g_2000Notes = 0;
		   g_10Coin=0;g_5Coin=0;
           if (isDeviceConnected && isCoinEscrowConnected)
           {
        	   StopNoteAccept();  //Calling Explicity
        	   StopCoinAccept();
        	  
    		   helper.run(new ISynchronousOperation()
    		   {
    				public int run(JxfsATM control) throws JxfsException
    				{
    					 ret=0;
    				     return control.cashInEnd();
    				}//run
    			});
    		   
    	       try
    	       {
    			   byte[] status = new byte[]{ 0x1B, 0x33, 0x36, 0x39, 0x42 };   
    			   serialPort.writeBytes(status);
    			   Thread.sleep(500);
    			   String s=serialPort.readHexString();
    			   ClearAcceptedAmountArray(0);
				   ClearAcceptedAmountArray(1);
    			   ret=0;
    		   }
    	       catch (Exception e)
    	       {
    			   ret=30;
    			   WriteLog(Level.WARNING, e.getMessage());
    			   StringWriter errors = new StringWriter();
    			   e.printStackTrace(new PrintWriter(errors));			
    			   WriteLog(Level.FINEST, errors.toString());
    		   }
		   } 
           else if(isDeviceConnected)
           {
        	  StopNoteAccept();
              ret= 22;
              try
              {
            	  ClearAcceptedAmountArray(1);
				helper.run(new ISynchronousOperation()
				  {
					public int run(JxfsATM control) throws JxfsException
					{
					     return control.cashInEnd();
					}//run
				  });
			} 
            catch (Exception e) 
            {
				ret=28;
				WriteLog(Level.WARNING, e.getMessage());
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));			
				WriteLog(Level.FINEST, errors.toString());
			}
              
              //return ret;
		   }
           else if(isCoinEscrowConnected)
           {
        	   try
    	       {
        		   ClearAcceptedAmountArray(0);
				   //ClearAcceptedAmountArray(1);
        		   StopCoinAccept();
    			   byte[] status = new byte[]{ 0x1B, 0x33, 0x36, 0x39, 0x42 };   
    			   serialPort.writeBytes(status);
    			   Thread.sleep(500);
    			   String s=serialPort.readHexString();
    			   ClearAcceptedAmountArray(0);
				   ClearAcceptedAmountArray(1);
    			   ret=20;
    		   }
    	       catch (Exception e)
    	       {
    			    ret=30;
    			    WriteLog(Level.WARNING, e.getMessage());
    				StringWriter errors = new StringWriter();
    				e.printStackTrace(new PrintWriter(errors));			
    				WriteLog(Level.FINEST, errors.toString());
    		   }
        	   //return ret;
           }
		 return 0;
		} 
	    catch (Exception e) 
	    {
			ret=28;
			WriteLog(Level.WARNING, e.getMessage());
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
		}
	  return ret;
  }
  
  public void ClearAcceptedAmountArray(int DeviceType)
  {
  		for (int j = 0; j < 25; j++) 
  		{
  			AcceptedAmount[j][DeviceType]=0;
  		}  	
  }
  
  
  public int ReturnCurrentCurrency(int CurrencyType,int Timeout)
  {
	   if(CurrencyType == 1)
	   {
		  	try 
		    {
		  		if (isDeviceConnected) 
		  		{
		  			
		  			StopNoteAccept();
		  			isTimeoutOccured=false;
                    ClearAcceptedAmountArray(1);
			  		retNote=31;
		  		    g_5Notes = 0;
				    g_10Notes = 0;
				    g_20Notes = 0;
				    g_50Notes = 0;
				    g_100Notes = 0;
				    g_200Notes = 0;
				    g_500Notes = 0;
				    g_1000Notes = 0;
				    g_2000Notes = 0;
				    g_5Coin=0;
				    g_10Coin=0;
					
					helper.run(new ISynchronousOperation() 
					{
					  public int run(JxfsATM control)
					  {
						  try
						  {
							control.cashInRollback();
							retNote=0;
						  }
						  catch (Exception e)
						  {
	                         retNote=28;						
						  }
						  return retNote;
					  }//run
					});
					
				}
		  		else
		  		{
                   return 20;
				}
		    }
		    catch (Exception e) 
		    {
				retNote=28;
				WriteLog(Level.WARNING, e.getMessage());
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));			
				WriteLog(Level.FINEST, errors.toString());
				return retNote;
			}
	   }
	   else if(CurrencyType == 2)  //COIN
	   {
		   try
		   {
			   StopCoinAccept();
			   isTimeoutOccured=false;
			   g_10Coin=0;g_5Coin=0;
			   ClearAcceptedAmountArray(0);
			   byte[] status = new byte[]{ 0x1B, 0x32, 0x38, 0x39, 0x40 };
			   serialPort.writeBytes(status);
			   Thread.sleep(500);
			   String s=serialPort.readHexString();
			   retCoin=0;
			   return retCoin;
		   }
		   catch (Exception e) 
		   {
			   retCoin=30;
			   WriteLog(Level.WARNING, e.getMessage());
			   StringWriter errors = new StringWriter();
			   e.printStackTrace(new PrintWriter(errors));			
			   WriteLog(Level.FINEST, errors.toString());
			   return retCoin;
		   }
	   }
	   
	   if(CurrencyType==1)
	   {
		   return retNote;
	   }
	   else
	   {
		  return retCoin;
	   }
  }
  
  
  public int ReturnAcceptedCurrency(int Timeout)
  {
	  	try 
	    {  
	  		 isTimeoutOccured=false;
	    	 g_5Notes = 0;
		     g_10Notes = 0;
		     g_20Notes = 0;
		     g_50Notes = 0;
		     g_100Notes = 0;
		     g_200Notes = 0;
		     g_500Notes = 0;
		     g_1000Notes = 0;
		     g_2000Notes = 0;
    	     g_10Coin=0;g_5Coin=0;
    	 	 ret=31;
	  		
    	 	System.out.println("isDeviceConnected Value:- "+ isDeviceConnected);
    	 	System.out.println("isCoinEscrowConnected Value:- "+ isCoinEscrowConnected);
	  		if (isDeviceConnected && isCoinEscrowConnected)
		     { 
	  			
	  			 System.out.println("Both Connected");
	    	     
	  			 StopNoteAccept();
		          
				 helper.run(new ISynchronousOperation() 
				 {
				  public int run(JxfsATM control)  
				  {
					  try
					  {
						control.cashInRollback();
						// Escrow Status
						ret=0;
						try
						  {
							 StopCoinAccept(); //Calling Explicitly before 
						     byte[] status = new byte[]{ 0x1B, 0x32, 0x38, 0x39, 0x40 };
							 serialPort.writeBytes(status);
							 Thread.sleep(500);
							 String s=serialPort.readHexString();
							 ret=0;
							 ClearAcceptedAmountArray(0);
							 ClearAcceptedAmountArray(1);
							 return ret;
						  }
						  catch (Exception e)
						  {
							ret=30;
							return ret;
						  }
					  }
					  catch(Exception e)
					  {
						  ret=28;
						  return ret;
					  }
				  }
				});
			  } 
	          else if(isDeviceConnected)
	          {
	        	 
	        	     StopNoteAccept();
	        	  
	        	    //isCurrencyAccepting=false;
	        		/*helper.run(new ISynchronousOperation() 
					{
					  public int run(JxfsATM control)
					  {
						  try
						  {
							control.cashInRollback();
							ret=22;
						  }
						  catch (Exception e)
						  {
	                         ret=28;						
						  }
						  return ret;
					  }//run
					});*/
	 
	        	     ReturnCurrentCurrency(1, 100);
	        	     System.out.println("Response IS:- 22");
	        	     return 22;
	        	
		      }
	          else if(isCoinEscrowConnected)
	          {
	        	  try
				  {
	        		 System.out.println("Only Coin");
					 StopCoinAccept(); //Calling Explicitly before 
				     byte[] status = new byte[]{ 0x1B, 0x32, 0x38, 0x39, 0x40 };
					 serialPort.writeBytes(status);
					 Thread.sleep(500);
					 String s=serialPort.readHexString();
					 ret=20;//Note Not Connected
					 ClearAcceptedAmountArray(0);
					
					 return ret;
				  }
				  catch (Exception e)
				  {
					ret=30;
					WriteLog(Level.WARNING, e.getMessage());
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));			
					WriteLog(Level.FINEST, errors.toString());
					return ret;
				  }
	          }
	  		
		}
	    catch (Exception e) 
	    {
	    	WriteLog(Level.WARNING, e.getMessage());
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
	    	return 31;
	    }
	   return ret;
	   //return ret;
  }  
  
  public int ClearJammedCurrencies(int CurrencyType,int EscrowClearanceMode,int Timeout)
  {
	  
	  if(CurrencyType==1)
	  {
		  if (isDeviceConnected) 
		  {
			  StopNoteAccept();
			  isTimeoutOccured=false;
			  ClearAcceptedAmountArray(1);
			  retNote=31;
			  if(EscrowClearanceMode==0)
			  {
					try 
				    {
						// Retrieve the operationCompleteEvent
						helper.run(new ISynchronousOperation() 
						{
						  public int run(JxfsATM control) 
						  {
							  try 
							  {
								control.cashInRollback();
								// Escrow 
								retNote=0;
							  }
							  catch (JxfsException e)
							  {
	                            retNote=1;
							  }
							  return retNote;
						  }
						});
					}
				    catch (Exception e) 
				    {
				    	retNote=28;
				    	WriteLog(Level.WARNING, e.getMessage());
						StringWriter errors = new StringWriter();
						e.printStackTrace(new PrintWriter(errors));			
						WriteLog(Level.FINEST, errors.toString());
				    	return retNote;
					}
			  }
			  else if(EscrowClearanceMode==1)
			  {
				  try 
				     {
						helper.run(new ISynchronousOperation()
						{
							public int run(JxfsATM control) 
							{
								try
								{
									control.cashInEnd();
									retNote=0;
								}
								catch (JxfsException e)
								{
									retNote=1;
								}
								return retNote;
							}
						});
					 } 
				     catch (Exception e) 
				     {
			    		WriteLog(Level.WARNING, e.getMessage());
						StringWriter errors = new StringWriter();
						e.printStackTrace(new PrintWriter(errors));			
						WriteLog(Level.FINEST, errors.toString());
						return 31;
					 }
			  }
		  } 
		  else 
		  {
             return 20;
		  }
	  }
	  else if(CurrencyType==2)
	  {
		  if(isCoinEscrowConnected)
		  {

				 StopCoinAccept();
				 isTimeoutOccured=false;
				 ClearAcceptedAmountArray(1);
				 if(EscrowClearanceMode==1)  // Send To Collection Bin Means Accept
				 {
					  try
					  {
						retCoin=31;
						byte[] status = new byte[]{ 0x1B, 0x33, 0x36, 0x39, 0x42 };    // Reject
					    serialPort.writeBytes(status);
					    Thread.sleep(1000);
					    String s=serialPort.readHexString();
					    retCoin=0;
					    return retCoin;			   
					 }
					 catch (Exception e)
					 {
						retCoin=30;
						WriteLog(Level.WARNING, e.getMessage());
						StringWriter errors = new StringWriter();
						e.printStackTrace(new PrintWriter(errors));			
						WriteLog(Level.FINEST, errors.toString());
						return retCoin;
					 } 
				 }
				 else if(EscrowClearanceMode==0)  //Return The Escrowed Coin
				 {
					 try
					 {
					   byte[] status = new byte[]{ 0x1B, 0x32, 0x38, 0x39, 0x40 }; 
					   serialPort.writeBytes(status);
					   Thread.sleep(1000);
					   String s=serialPort.readHexString();
					   retCoin=0;
					   return retCoin;	
					 }
					 catch (Exception e) 
					 {
						 retCoin=30;
						 WriteLog(Level.WARNING, e.getMessage());
						 StringWriter errors = new StringWriter();
						 e.printStackTrace(new PrintWriter(errors));			
						 WriteLog(Level.FINEST, errors.toString());
						 return retCoin;
					 }
				   
				 }
			  
		  }
		  else
		  {
			  return 22;
		  }
	  }
	return 0;
  }
  
  
  public int StartCoinAccept(long Denom)
  {
	  isStartCoinAcceptOnce=true;
	  Boolean isCoinAccepted=false;
	  byte[] byResponse = null;
      byte[] byCmd = new byte [] { 0x1B, 0x0A, 0x03, 0x05, 0x07 };  //Coin Accept Start					
      int iFirstRead=0;
      byte byPreviousValue = 0x00;
      while(true)
	  {
	    	try
		    {		
	    		retCoin=31;
				if(isStartCoinAcceptOnce)
				{
					if(serialPort.writeBytes(byCmd))
					{							
						Thread.sleep(500);
						byResponse = serialPort.readBytes();							
						if(byResponse != null &&  byResponse.length == 22)
						{
							 if (byResponse[11] != byPreviousValue && byResponse[12] != 0x00)
							 {
							 
								switch(byResponse[12])
								{
									case 0x04:   //1 Coin
									{
										 byPreviousValue = byResponse[11];
										 System.out.println("Rs.1 Coin Accepted");
										 retCoin=0;
										 g_1Coin++;
										 isStartCoinAcceptOnce=false;// objInterfaceCoinAcceptor.CoinAcceptedEvent(1);
									     isCoinAccepted=true;
									}
									break;
									
									case 0x02:   //2 Coin
									{
										byPreviousValue = byResponse[11];
										System.out.println("Rs.2 Coin Accepted");
										retCoin=0;
										g_2Coin++;
										isStartCoinAcceptOnce=false;
										isCoinAccepted=true;
										//objInterfaceCoinAcceptor.CoinAcceptedEvent(2);
									}
									break;
									
									case 0x0A:   //5 Coin
									{
										byPreviousValue = byResponse[11];
										System.out.println("Rs.5 Coin Accepted");
										retCoin=0;
										g_5Coin++;
										isStartCoinAcceptOnce=false;
										isCoinAccepted=true;
										//objInterfaceCoinAcceptor.CoinAcceptedEvent(5);
									}
									break;

									case 0x0B:   //10 Coin
									{
										byPreviousValue = byResponse[11];
										System.out.println("Rs.10 Coin Accepted");
										retCoin=0;
										g_10Coin++;
										isStartCoinAcceptOnce=false;
										isCoinAccepted=true;
										//objInterfaceCoinAcceptor.CoinAcceptedEvent(10);
									}
									break;
									
									case 0x08:   //5 Coin
									{
										byPreviousValue = byResponse[11];
										System.out.println("Rs.5 Coin Accepted");
										retCoin=0;
										isStartCoinAcceptOnce=false;
									    isCoinAccepted=true;
										//objInterfaceCoinAcceptor.CoinAcceptedEvent(5);
									}
									break;
									
									case 0x09:   //5 Coin
									{
										byPreviousValue = byResponse[11];
										System.out.println("Rs.5 Coin Accepted");
										retCoin=0;
										g_5Coin++;
										isStartCoinAcceptOnce=false;
										isCoinAccepted=true;
										//objInterfaceCoinAcceptor.CoinAcceptedEvent(5);
									}
									break;
									case 0x06:   //2 Coin
									{
										byPreviousValue = byResponse[11];
										System.out.println("Rs.2 Coin Accepted");
										retCoin=0;
										g_2Coin++;
										isStartCoinAcceptOnce=false;
										isCoinAccepted=true;
										//objInterfaceCoinAcceptor.CoinAcceptedEvent(2);
									}
									break;
								}
							 }
						}
					}
				}
				else
				{
				    if(!isStartCoinAcceptOnce)  //For Breaking The Condition
				    	break;
				    
					return retCoin;
				}	
		}
		catch (SerialPortException | InterruptedException e) 
		{
			WriteLog(Level.WARNING, e.getMessage());
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
		}
	  }
	  return retCoin;
	}
  
  
  public int EnableDenomination(int CurrencyType,int Denomask,int Timeout)
  {
	  int ret=31;
      try 
      {
    	  if(isDeviceConnected)
    	  {

        	  
        	  if(CurrencyType==0||CurrencyType==1)
        	  {
          		   try 
        		   {
          			   ret=0;
    				   Integer a = new Integer(Denomask); 
    				   // Convert Integer number to byte value 
    				   byte b0=(byte) (a & 0xff) ;
    				   byte b1=(byte) (a>>8 & 0xff) ;
    				   //byte b2=(byte) (a>>16 & 0xff) ;
    				   //byte b3=(byte) (a>>24 & 0xff) ;
      	 
    				   int[] n5=new int[9]; int j=1;
    				  // int[] n6=new int[] {5,10,20,50,100,200,500,1000,2000};
    				   for (int i = 0; i < n5.length-1; i++) 
    				   {
    					   if((b0&j)>0)
    					     n5[i]=1;
    					   j=j*2;
    				    }
    				  
    				   if((b1&0x01)>0)
    				   n5[8]=1;
    				  
    				   for (int i = 0; i < n5.length; i++)
    				   {
    					System.out.println(n5[i]);
    				   }
    				  
    				  List<IDenominationInfo> denos = objBnr.queryDenominations();
    				  List<IDenominationInfo> updatedDenos = new ArrayList<IDenominationInfo>();
    				  
    				  DenominationInfo denoToUpdate;
    				    for (IDenominationInfo item : denos)
    				    {
    				    	//byte[] b=Byte()Denomask;
    				        int Amount=item.getCashType().getValue()/100;
    				    	System.out.println("Amount:- "+Amount);
    				        switch (Amount)
    				        {
    				           
	    						case 5:
	    					        denoToUpdate = new DenominationInfo(
	    				            item.getCashType(), 
	    				            (n5[0]==1)?true:false, 
	    				            item.getSecurityLevel(),
	    				            item.getStudyVersion(), 
	    				            item.getBillsetCompatibilityNr(), 
	    				            item.getRecognitionSensorType());
	    					        updatedDenos.add(denoToUpdate);
	    					    	//objBnr.updateDenominations(updatedDenos);
	    					    	System.out.println("5:- "+((n5[0]==1)?true:false));
	    							break;
	    						case 10:
	    					        denoToUpdate = new DenominationInfo(
	    				            item.getCashType(), 
	    				            (n5[1]==1)?true:false, 
	    				            item.getSecurityLevel(),
	    				            item.getStudyVersion(), 
	    				            item.getBillsetCompatibilityNr(), 
	    				            item.getRecognitionSensorType());
	    					        updatedDenos.add(denoToUpdate);
	    					     	//objBnr.updateDenominations(updatedDenos);
	    					    	System.out.println("10:- "+((n5[0]==1)?true:false));
	    					    break;
	    						case 20:
	    					        denoToUpdate = new DenominationInfo(
	    				            item.getCashType(), 
	    				            (n5[2]==1)?true:false, 
	    				            item.getSecurityLevel(),
	    				            item.getStudyVersion(), 
	    				            item.getBillsetCompatibilityNr(), 
	    				            item.getRecognitionSensorType());
	    					        updatedDenos.add(denoToUpdate);
	    					    	//objBnr.updateDenominations(updatedDenos);
	    					    	System.out.println("20:- "+((n5[0]==1)?true:false));
	    					    	break;
	    						case 50:
	    					        denoToUpdate = new DenominationInfo(
	    				            item.getCashType(), 
	    				            (n5[3]==1)?true:false, 
	    				            item.getSecurityLevel(),
	    				            item.getStudyVersion(), 
	    				            item.getBillsetCompatibilityNr(), 
	    				            item.getRecognitionSensorType());
	    					        updatedDenos.add(denoToUpdate);
	    					    	//objBnr.updateDenominations(updatedDenos);
	    					    	System.out.println("50:- "+((n5[0]==1)?true:false));
	    					    	break;
	    						case 100:
	    							System.out.println(" In condition 100:- "+((n5[0]==1)?true:false));
	    					        denoToUpdate = new DenominationInfo(
	    				            item.getCashType(), 
	    				            (n5[4]==1)?true:false, 
	    				            item.getSecurityLevel(),
	    				            item.getStudyVersion(), 
	    				            item.getBillsetCompatibilityNr(), 
	    				            item.getRecognitionSensorType());
	    					        updatedDenos.add(denoToUpdate);
	    					    	//objBnr.updateDenominations(updatedDenos);
	    					    	System.out.println("100:- "+((n5[0]==1)?true:false));
	    					    	break;
	    						case 200:
	    					        denoToUpdate = new DenominationInfo(
	    				            item.getCashType(), 
	    				            (n5[5]==1)?true:false, 
	    				            item.getSecurityLevel(),
	    				            item.getStudyVersion(), 
	    				            item.getBillsetCompatibilityNr(), 
	    				            item.getRecognitionSensorType());
	    					        updatedDenos.add(denoToUpdate);
	    					    	//objBnr.updateDenominations(updatedDenos);
	    					    	System.out.println("200:- "+((n5[0]==1)?true:false));
	    					    	break;
	    						case 500:
	    					        denoToUpdate = new DenominationInfo(
	    				            item.getCashType(), 
	    				            (n5[6]==1)?true:false, 
	    				            item.getSecurityLevel(),
	    				            item.getStudyVersion(), 
	    				            item.getBillsetCompatibilityNr(), 
	    				            item.getRecognitionSensorType());
	    					        updatedDenos.add(denoToUpdate);
	    					    	//objBnr.updateDenominations(updatedDenos);
	    					    	System.out.println("500:- "+((n5[0]==1)?true:false));
	    					    	break;
	    						case 1000:
	    					        denoToUpdate = new DenominationInfo(
	    				            item.getCashType(), 
	    				            (n5[7]==1)?true:false, 
	    				            item.getSecurityLevel(),
	    				            item.getStudyVersion(), 
	    				            item.getBillsetCompatibilityNr(), 
	    				            item.getRecognitionSensorType());
	    					        updatedDenos.add(denoToUpdate);
	    					    	//objBnr.updateDenominations(updatedDenos);
	    					    	System.out.println("1000:- "+((n5[0]==1)?true:false));
	    					    	break;
	    						case 2000:
	    					        denoToUpdate = new DenominationInfo(
	    				            item.getCashType(), 
	    				            (n5[8]==1)?true:false, 
	    				            item.getSecurityLevel(),
	    				            item.getStudyVersion(), 
	    				            item.getBillsetCompatibilityNr(), 
	    				            item.getRecognitionSensorType());
	    					        updatedDenos.add(denoToUpdate);
	    					    	////objBnr.updateDenominations(updatedDenos);
	    					    	System.out.println("2000:- "+((n5[0]==1)?true:false));
	    					    	break;
    						default:  System.out.println("In Default Case");
    							break;
    				}
    				        objBnr.updateDenominations(updatedDenos);
    				      
              	  }
    				    
    			  ret=0;
    			}
        		catch (Exception e)
        		{
    				ret=1;
    				e.printStackTrace();
    			 }

        	  }
        	  else
        	  {
        		  ret=0;
        	  }
    	  }
    	  else
    	  {
    		  ret=20;
    	  }
      }
      catch (Exception e)
      {
		ret =28;
		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));			
		WriteLog(Level.FINEST, errors.toString());
	}
	return ret;
  }

  public boolean AcceptCurrencies(int CurrencyType,int amount,int Timeout)
  {
  	   try
  	   {
  		 IStatus bnrStatus = null;
  		 bnrStatus = objBnr.getStatus();  
  		//EnableDenomination(1, 255, 100);
  	
  		   if(isDeviceConnected || isCoinEscrowConnected)
  		   {
  	  		 TotalAmount=amount;
  	  		 TotalAcceptedCoinsAmount =0;
  	  		 TotalAcceptedNotesAmount=0;
  			 g_500Notes=0;g_50Notes=0;g_5Notes=0;g_1000Notes=0;g_100Notes=0;g_10Notes=0;g_20Notes=0;g_2000Notes=0;
  			 g_200Notes=0;
  			 g_5Coin=0;g_10Coin=0;  		 
  	  		 
  	  		 
  	  		   if(CurrencyType==0) //Both Currency Should Accept
  	  		   {
  	  			   
  	  			   if(isDeviceConnected && isCoinEscrowConnected)
  	  			   {
  	  				   
  	  				 if(bnrStatus.getIntermediateStackerStatus() == IntermediateStackerStatus.EMPTY)
  	  				 {
  	  				   isWhichDeviceSelected=0; //Both
	  	  			   isStartStopNoteAccept = true;
	  	  			   
	  	  			   thNoteAccept  = new Thread(new NoteAcceptThread ());
	  	  			   thNoteAccept.start();

	  	  			   Thread.sleep(200);
	  	  			   isAcceptingCoinRecursive = true;
	  	  			   if (thCoinAccept ==null) 
	  	  			   {
	  	  				   thCoinAccept  = new Thread(new CoinAcceptThread ());
	  	  				   thCoinAccept.start();
	  	  			   }
  	  				 }
  	  				 else 
  	  				 {
  	  					 return false;
  	  				 }

  	  			   }
  	  			   else
  	  			   {
  	  				   return false;
  	  			   }
  	  		   }
  	  		   else if(CurrencyType==1) //Note
  	  		   {
  	  			 if(bnrStatus.getIntermediateStackerStatus() == IntermediateStackerStatus.EMPTY)
	  			 {
  	  				
  	  			   isWhichDeviceSelected=1;
  	  			   isStartStopNoteAccept = true;
  	  			   thNoteAccept  = new Thread(new NoteAcceptThread ());
  	  			   thNoteAccept.start();
  	  		    	//obj.waitOne(10000);
	  			 }
  	  			 else
  	  			 {
  	  				 return false;
  	  			 }
  	  			   
  	  		   }
  	  		   else if(CurrencyType==2)  //Coin
  	  		   {
  	  			   isWhichDeviceSelected=2;
  	  			   isAcceptingCoinRecursive = true;
  	  			   if (thCoinAccept ==null) 
  	  			   {
  	  				  thCoinAccept  = new Thread(new CoinAcceptThread ());
  	  				  thCoinAccept.start();
  	  				  //return true;
  	  			   }
  	  		   }
  	           isTimeoutOccured=false;
  	           isCurrencyAccepting=true;
  	  		   thAcceptCurrency  = new Thread(new AcceptCuurencyThread(Timeout));
  			   thAcceptCurrency.start();
  	  		   //
  	   		   return true;
  		   }
  		   else
  		   {
  			 isStartStopNoteAccept = false;
  			 isAcceptingCoinRecursive=false;
  			 return false;
  		   }
  	   }
  	   catch (Exception e) 
  	   {
  			WriteLog(Level.WARNING, e.getMessage());
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
  	   }
  	return false;
  }

  public int GetAcceptedAmount(byte[][] AccpAmt)
  {
  	try
  	{
			for (int i = 0; i < 2; i++)
			{
				for (int j = 0; j < 25; j++) 
				{
					AccpAmt[j][i]=AcceptedAmount[j][i];
					//System.out.print(AcceptedAmount[j][i]+" ");
				}
				//System.out.println();
			}
			if(isTimeoutOccured && isCurrencyAcceptingNote)
			{
				return 0;
			}
			/*else if(isTimeoutOccured)
            {
            	return 18;
            }*/
			
			if(isStartStopNoteAccept || isAcceptingCoinRecursive) 
			{
				isCurrencyAccepting=true;
				return 0; //Accepting
			}
			else 
			{
				if(isDeviceConnected || isCoinEscrowConnected )
				{
					if(TotalAmount==(TotalAcceptedCoinsAmount+TotalAcceptedNotesAmount))
					{
						//thAcceptCurrency.
						isCurrencyAccepting=false;
						return 1; //Exact
					}
					else if((TotalAcceptedCoinsAmount+TotalAcceptedNotesAmount)>TotalAmount)
					{
						isCurrencyAccepting=false;
						return 2; //Excess
					}
					else if(isTimeoutOccured)
					{
						Thread.sleep(2000);
						return 18;
					}
					else
					{
						return 0;
					}
				}
				else if(!isDeviceConnected)
				{
					return 20;
				}
				else
				{
					return 21;
				}
					
			}
	}
  	catch (Exception e)
  	{
  		WriteLog(Level.WARNING, e.getMessage());
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));			
		WriteLog(Level.FINEST, errors.toString());
  		return 28;
	}
	
  }

  public boolean StopNoteAccept() 
  {
  	try 
  	{
  		 //objBnr.cashInEnd();
  		//System.out.println("In Stop Note Accept :- "+objBnr.cashInRollBack());
  		 
  		 if(isCurrencyAccepting)
  		    control.cancel(0);
  		isCurrencyAcceptingNote=false;
  		isCurrencyAccepting=false;
  		isStartStopNoteAccept=false; 			
  		return true;
  	}
  	catch ( Exception e)
  	{
  		
  	}
  	return false;
  }


  public boolean StopCoinAccept()
  {
  	try 
  	{
  		//System.out.println("StopCoinAccept Called");  		
  		isAcceptingCoinRecursive = false;
  		return true;
  	}
  	catch ( Exception e)
  	{
  		
  	}
  	return false;

  }


  public int[] GetAcceptedNoteCurrencyResponse() 
  {
  	int[] ret=new int[8];
  	ret[0]=g_5Notes;
  	ret[1]=g_10Notes;
  	ret[2]=g_20Notes;
  	ret[3]=g_50Notes;
  	ret[4]=g_100Notes;
  	ret[5]=g_200Notes;
  	ret[6]=g_500Notes;
  	ret[7]=g_2000Notes;
  	return ret;
  }
    
    
  class NoteAcceptThread implements Runnable
  {
  	@Override
  	public void run()
  	{
  		try
  		{
  			int incrementCash=0;
  			TotalAcceptedNotesAmount=0;g_500Notes=0;g_50Notes=0;g_5Notes=0;g_1000Notes=0;g_100Notes=0;g_10Notes=0;g_20Notes=0;g_2000Notes=0;
  			g_200Notes=0;
  			MEICashInOrder data = null;
  			long insertedAmount = 0;  
  			while(true)
  			{
  				if(isStartStopNoteAccept)
  				{
  					 //System.out.println("Total Amount:- "+TotalAmount);
  					 startCashInTransaction();
  					 data = cashIn(100, "INR");
  					 isCurrencyAcceptingNote=true;
  					 insertedAmount = data.getDenomination().getAmount();      
  					 int amount=(int) (insertedAmount/100);
  					 
  					 switch(amount)
  					 {
  				         case 5: g_5Notes++;
  				                 AcceptedAmount[incrementCash][1]=1;
  					    	     break;
  					     case 10: g_10Notes++;
  					     		  AcceptedAmount[incrementCash][1]=2;
  					    	      break;
  					     case 20: g_20Notes++;
  					     		  AcceptedAmount[incrementCash][1]=3;
  					    	      break;
  					     case 50: g_50Notes++;
  					              AcceptedAmount[incrementCash][1]=4;
  					    	      break;
  					     case 100: g_100Notes++;
  					               AcceptedAmount[incrementCash][1]=5;
  					    	       break;
  					     case 200: g_200Notes++;
  					               AcceptedAmount[incrementCash][1]=6;
  					               break;
  					     case 500: g_500Notes++;
  					     		   AcceptedAmount[incrementCash][1]=7;
  					     		   break;
  					     case 1000: g_1000Notes++;
  					      			AcceptedAmount[incrementCash][1]=0;
  					      			break;
  					     case 2000: g_2000Notes++;
  					     			AcceptedAmount[incrementCash][1]=8;
  					     			break;
  					    	
  				   }
  				   incrementCash++;
  				   //Matching Condition
  				   TotalAcceptedNotesAmount=g_5Notes * 5 + g_10Notes * 10 + g_20Notes * 20 + g_50Notes * 50 + g_100Notes * 100 + g_200Notes * 200 + g_500Notes * 500 + g_1000Notes * 1000 + g_2000Notes;
                     if (isWhichDeviceSelected==0)
                     {
                    	 //System.out.println("Total Amount:- "+TotalAmount);
                    	 int total=TotalAcceptedCoinsAmount+TotalAcceptedNotesAmount;
                    	 //System.out.println("Total Note+Coin in Note:- "+total);
                    	 if(TotalAmount==total)
                    	 {
                    		//System.out.println("Amount Matches in Notes");
  						    StopNoteAccept();
  						    StopCoinAccept();
  						    break;
  					   	 }
                    	 else if(total>TotalAmount)  //Stop
					     {
								   //System.out.println("Amount Greater Than in Coin");
								   //control.cancel(0);
								   StopNoteAccept();
								   StopCoinAccept();
								   break;
						}
  				   }
                     else if(isWhichDeviceSelected==1)
                     {
                  	   if(TotalAmount==TotalAcceptedNotesAmount)
      				   {
      					   StopNoteAccept();
      				   }
                  	   else if(TotalAcceptedNotesAmount>TotalAmount)
                  	   {
                  		   StopNoteAccept();
                  	   }
  				   } 
                     isCurrencyAcceptingNote=false;
  			}
  			else
  			{
  				//System.out.println("ELSE .. ");
  				Thread.sleep(500);
  			}
  		  }
  		} 
  		catch (InterruptedException | JxfsException e) 
  		{
  			WriteLog(Level.WARNING, e.getMessage());
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));			
			WriteLog(Level.FINEST, errors.toString());
  		}
  	 }
  }
  
  class AcceptCuurencyThread implements Runnable
  {
    public int Timeout=0;
	public AcceptCuurencyThread(int timeout)
	{
		Timeout=timeout;// TODO Auto-generated constructor stub
	}

	@Override
	public void run()
	{
		   try
		   {
			   int Counter = 0;
			   while(true)
			   {
				   if(Counter == (Timeout/1000 - 1) )
				   {
					   System.out.println("In Timeout Thread If condition");
					   // Timeout					
					   if(isCurrencyAccepting)
					   {
						   if(isWhichDeviceSelected==0)
						   {
							    StopCoinAccept();
								if(!isCurrencyAcceptingNote)
								{
									  StopNoteAccept();
								}
							
						   }
						   else if(isWhichDeviceSelected==1)
						   {
								if(!isCurrencyAcceptingNote)
								{
									  StopNoteAccept();
								}
						   }
						   else
						   {
							   StopCoinAccept();
						   }
						  
						   isTimeoutOccured=true;
					  }
					   else
					  {
						  isTimeoutOccured =false;
					   }			
					    break;
				   }
				   else
				   {
					   	Thread.sleep(1000);
					    System.out.println("In Timeout Thread else condition counter:- "+Counter);
					   	Counter++;
				   }
			   }   
		   }
		   catch (InterruptedException  e)
		   {
				WriteLog(Level.WARNING, e.getMessage());
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));			
				WriteLog(Level.FINEST, errors.toString());
		   }
	}
	  
  }
    
    class CoinAcceptThread implements Runnable 
    {  
      public void run()
    	{
           byte[] byResponse = null;
           byte[] byCmd = new byte [] { 0x1B, 0x0A, 0x03, 0x05, 0x07 };  //Coin Accept Start					
           int iFirstRead=0;
           int incrementCoin=0;
           TotalAcceptedCoinsAmount=0;g_5Coin=0;g_10Coin=0; 
                  
           while(true)
    		 {
    	    	try
    		    {					
    				if(isAcceptingCoinRecursive)
    				{
    					//serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
    					if(serialPort.writeBytes(byCmd))
    					{							
    						try 
    						{
    							Thread.sleep(500);
    						}
    						catch (InterruptedException e) 
    						{
    							// TODO Auto-generated catch block
    							e.printStackTrace();
    						}
    						byResponse = serialPort.readBytes();
    						
    						//for (int i=0;i<byResponse.length;i++)
    						//	System.out.print(String.format("%02x ",byResponse[i] ));
    						//System.out.println();

    						if(byResponse != null &&  byResponse.length == 22)
    						{
    							 if (byResponse[11] != byPreviousValue && byResponse[12] != 0x00)
    							 {
    		    						//for (int i=0;i<byResponse.length;i++)
    		    							//System.out.print(String.format("%02x ",byResponse[i] ));
    								 
    								switch(byResponse[12])
    								{
    									case 0x04:   //1 Coin
    									{
    										 byPreviousValue = byResponse[11];
    										 g_1Coin++;
    									}
    									break;
    									
    									case 0x02:   //2 Coin
    									{
    										byPreviousValue = byResponse[11];
    										g_2Coin++;
    									}
    									break;
    									
    									case 0x0A:   //5 Coin
    									{
    										byPreviousValue = byResponse[11];
    										//System.out.println("incrementCoin:- "+incrementCoin);
    									    AcceptedAmount[incrementCoin][0]=1;
    									    incrementCoin++;
    										g_5Coin++;    										
    									}
    									break;
    									
    									case 0x0B:   //10 Coin
    									{
    										byPreviousValue = byResponse[11];
    										//System.out.println("incrementCoin:- "+incrementCoin);
    									    AcceptedAmount[incrementCoin][0]=2;
    									    incrementCoin++;
    										g_10Coin++;
    										
    									}
    									break;
    									
    									case 0x08:   //5 Coin
    									{
    										byPreviousValue = byResponse[11];
    										//System.out.println("incrementCoin:- "+incrementCoin);
    									    AcceptedAmount[incrementCoin][0]=1;
    									    incrementCoin++;
    										g_5Coin++;
    										
    									}
    									break;
    									
    									case 0x09:   //5 Coin
    									{
    										byPreviousValue = byResponse[11];
    										//System.out.println("incrementCoin:- "+incrementCoin);
    									    AcceptedAmount[incrementCoin][0]=1;
    									    incrementCoin++;
    										g_5Coin++;
    										
    																	
    									}
    									break;
    									case 0x06:   //2 Coin
    									{
    										byPreviousValue = byResponse[11];
    										g_2Coin++;
    										
    									}
    									break;
    								}
    								TotalAcceptedCoinsAmount=g_5Coin * 5 + g_10Coin * 10;
    								// System.out.println("Amount Coin: " + TotalAcceptedCoinsAmount);
    								 if (isWhichDeviceSelected==0)
    			                     {
  	  								   int total=TotalAcceptedCoinsAmount+TotalAcceptedNotesAmount;
  	  								  // System.out.println("Total Coin+Note in Coin:- "+total);
  	  								   if(TotalAmount==total)
  	  								   {
  	  									   System.out.println("Amount Matches in Coin");
  	  									   //control.cancel(0);
  	  									   StopNoteAccept();
  	  									   StopCoinAccept();
  	  									   //break;
  	  								   }
  	  								   else if(total>TotalAmount)  //Stop
  	  								   {
  	  									   System.out.println("Amount Greater Than in Coin");
 	  									   //control.cancel(0);
 	  									   StopNoteAccept();
 	  									   StopCoinAccept();
 	  									   //break;
  	  								   }
  	  							  }
  	  			                  else if(isWhichDeviceSelected==2)
  	  			                  {
  	  			                	 if(TotalAmount==TotalAcceptedCoinsAmount)
  	   							     {
  	   								   StopCoinAccept();
  	   							     }
  	  			                	 else if(TotalAcceptedCoinsAmount>TotalAmount)
  	  			                	 {
  	  			                		 StopCoinAccept();  //Reject or Accept
  	  			                	 }
  	  							  } 
    							 }
    						}
    					}
    				}
    				else
    				{
    					try 
    					{
    						incrementCoin=0;
    						Thread.sleep(500);
    					} 
    					catch (InterruptedException e)
    					{
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
    				}
    		}
    		catch (Exception e) 
    		{
    			WriteLog(Level.WARNING, e.getMessage());
    			StringWriter errors = new StringWriter();
    			e.printStackTrace(new PrintWriter(errors));			
    			WriteLog(Level.FINEST, errors.toString());
    		}
    	 }
      }  
    }
  
    
  //-------------MEI INTERNAL-------------------------------------------------  
  
  /****************************************************************************
   * isContainsDeviceNames
   ***************************************************************************/
  /**
   * Checks that usb driver contains names of devices
   * 
   * @param bnrUsbDriver driver
   * @return true or false, depending of name availability
   * 
   ***************************************************************************/ 
  private boolean isContainsDeviceNames(BnrUsbDriver bnrUsbDriver) 
  {
	  return bnrUsbDriver.getDeviceNames().size() > 0 ? true : false;
  }//isContainsDeviceNames

  /****************************************************************************
   * getDeviceName
   ***************************************************************************/
  /**
   * get device name from bnrUsbDriver
   * 
   * @param bnrUsbDriver driver
   * @return name of device
   * 
   ***************************************************************************/ 
  private String getDeviceName(BnrUsbDriver bnrUsbDriver) 
  {
	  return bnrUsbDriver.getDeviceNames().iterator().next();
  }//bnrUsbDriver
  
  /****************************************************************************
   * isBnrNotConnected
   ***************************************************************************/
  /**
   * Checks that Bnr device not connected
   * 
   * @param device that gets from usbDriver
   * @return TRUE if Bnr NOT connected and FALSE otherwise
   * 
   ***************************************************************************/ 
  private boolean isBnrNotConnected(IDevice device) 
  {
	    boolean deviceNotExist = (device == null) ? true : false;
	    // device.getDeviceType()
	    boolean deviceNotBnr =false;
	      try
	      {
			Thread.sleep(100);
			deviceNotBnr = (device.getDeviceType() != DeviceType.BNR) ? true : false;
			Thread.sleep(100);
		
	      }
	      catch (InterruptedException e)
	      {
	    	deviceNotBnr = (device.getDeviceType() != DeviceType.BNR) ? true : false;
	      }
	    return deviceNotExist || deviceNotBnr;
  }//isBnrNotConnected
  
  /****************************************************************************
   * initControl
   ***************************************************************************/
  /**
   * Initializes control and helper for access to bnr
   * 
   * @param driver for bnr
   * @param deviceName of bnr
   * 
   ***************************************************************************/
  private void initControl(IDeviceProvider driver, String deviceName) 
  {
	    try
	    {      
		      control = initializeAndGetJxfsControl(driver, deviceName);
		      helper = new SynchronousJxfsOperationHelper(control);     	        
	    } 
	    catch (Exception e)
	    {
		      System.err.println("JxfsException occured in JxfsDeviceManager.initialize().");
		      System.err.println(e.toString());
	    } 
	   
  }//initControl
 
  /****************************************************************************
   * initializeAndGetJxfsControl
   ***************************************************************************/
  /**
   * Initialize JxfsDeviceManager and obtain a JxfsATM Control for the
   * device identified by the given name and driver.
   * 
   * @param driver for bnr
   * @param deviceName of bnr
   * @return a JxfsATM control.
   * @throws JxfsException
   *           if an error occurred.
   * 
   ***************************************************************************/
  private JxfsATM initializeAndGetJxfsControl(IDeviceProvider driver, String deviceName) throws JxfsException {

    try 
    {
		JxfsDeviceManager.getReference().initialize("com.mei.bnr.jxfs.service.SpecificDeviceManager,workstation,jxfsClient,lib");
	} 
    catch (Exception e)
    {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
    
    SpecificDeviceManager.getReference().announce(
        new JxfsRemoteDeviceInformation("workstation", driver.getClass().getName()
            + "/" + deviceName, "", "com.jxfs.control.cdr.JxfsATM", "com.mei.bnr.jxfs.service.BnrService", "CDR-" 
            + deviceName, "description"));

    final IJxfsBaseControl control = getBaseControl(driver, deviceName);

    if (!isJxfsControl(control)) 
    {
      System.err.println("Not a JxfsATM control.");
      throw new IllegalStateException();
    }//if
    return (JxfsATM) control;
  }//initializeAndGetJxfsControl
 
  /****************************************************************************
   * getBaseControl
   *************************************************************************/
  /**
   * Returns Jxfs base conrol
   * 
   * @param driver for bnr
   * @param deviceName of bnr
   * @return a Jxfs base control.
   * @throws JxfsException
   *           if an error occurred.
   * 
   ***************************************************************************/
  private IJxfsBaseControl getBaseControl(IDeviceProvider driver, String deviceName) throws JxfsException {
    return JxfsDeviceManager.getReference().getDevice(driver.getClass().getName() + "/" + deviceName + "@workstation");
  }//getBaseControl

  /****************************************************************************
   * isJxfsControl
   ***************************************************************************/
  /**
   * Checks that base control is JxfsATM control
   * 
   * @param control base control
   * @return true or false if JxfsConrol instance of base control
   * 
   ***************************************************************************/
  private boolean isJxfsControl(IJxfsBaseControl control) {
    return control instanceof JxfsATM ? true : false;
  }//isJxfsControl
 
  /****************************************************************************
   * subscribeEvents
   ***************************************************************************/
  /**
   * Subscribe to intermediate event, and check how much bills user
   * inserted during cahIn operation.
   * 
   * 1) integrator can implement custom Observers for Bnr events and subscribe
   *    it during app initialization. After closing Bnr, integrator should
   *    unsubscribe from Bnr events manually (not implemented in this demo).
   *    
   * 2) integrator can implement custom observers on-the-fly using helper and
   *    anonymous classes (implemented in this demo)
   *
   * 
   ***************************************************************************/
  private void subscribeEvents() {
    control.addIntermediateListener(new IJxfsIntermediateListener() {

      public void intermediateOccurred(JxfsIntermediateEvent IE) {
        try {
			MEICashInOrder data = (MEICashInOrder) IE.getData();
			System.out.println("You`ve inserted: " + data.getDenomination().getAmount() + " " + CASH_IN_CURRENCY);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}      
      }//intermediateOccurred
    });    
  }//subscribeEvents
 
  /****************************************************************************
   * getSetDateTime
   ***************************************************************************/
  /**
   * An example method, how to set up date to bnr. We get current OS time,
   * set it into bnr and print to the terminal.
   *   
   * @throws JxfsException 
   *                 if an error occurred.
   * 
   ***************************************************************************/
  private void getSetDateTime() 
  {
   try
   {
	    Date result = null;
	    setCurrentDateTime();
	    result = getBnrDateTime();   
	  
   }
   catch (JxfsException e)
   {}
  }//getSetDateTime

  /****************************************************************************
   * setCurrentDateTime
   ***************************************************************************/
  /**
   * Takes date from OS and, using helper, set it into the bnr
   *   
   * @throws JxfsException 
   *                 if an error occurred.
   * 
   ***************************************************************************/
  public void setCurrentDateTime() throws JxfsException {
    final Date date = GregorianCalendar.getInstance().getTime();
    
    // Set Date Time.
    helper.run(new ISynchronousOperation() {
        public int run(JxfsATM control) throws JxfsException {
          return control.setDateTime(date);
        }//run
      }); 
  }//setCurrentDateTime
 
  /****************************************************************************
   * getBnrDateTime
   ***************************************************************************/
  /**
   * Gets date from bnr
   *   
   * @return date from bnr
   * @throws JxfsException 
   *                 if an error occurred.
   * 
   ***************************************************************************/
  public Date getBnrDateTime() throws JxfsException
  {
    Date result = null;
    JxfsOperationCompleteEvent event = null;
      
     // Run getTime operation
    event = helper.run(new ISynchronousOperation()
    {
        public int run(JxfsATM control) throws JxfsException 
        {
          return control.getDateTime();
        }//run
      });
    
    // Check and return result
    if (event.getResult() == IJxfsConst.JXFS_RC_SUCCESSFUL)
    {
      result = (Date) event.getData();
    } 
    else 
    {
      throw new JxfsException(event.getResult());
    }//if
    return result;
  }//getBnrDateTime
 
  /****************************************************************************
   * makeBnrOperational
   ***************************************************************************/
  /**
   * If bnr status is offline, set it online
   * @throws JxfsException 
   *                  if an error occurred.
   ***************************************************************************/
  private void makeBnrOperational(int EscrowCleareanceMode) throws JxfsException
  {
    try
    {
		JxfsDeviceStatus deviceStatus = getDeviceStatus();    

		if (deviceStatus.isOffLine())
		{
			if(EscrowCleareanceMode == 1)
				resetBnr();      
			Thread.sleep(500);
		}//if    
	}
    catch (Exception e)
    {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }//makeBnrOperational

  /****************************************************************************
   * getDeviceStatus
   ***************************************************************************/
  /**
   * If bnr status is offline, set it online
   * 
   * @return device status
   * @throws JxfsException 
   *                  if an error occurred.
   ***************************************************************************/
  public JxfsDeviceStatus getDeviceStatus() throws JxfsException
  {
    MEIBnrStatus status = null;    
    
    // Run get status operation
    JxfsOperationCompleteEvent event = helper.run(new ISynchronousOperation() {
      public int run(JxfsATM control) throws JxfsException {
        return control.directIO(IDirectIOConsts.JXFS_O_MEI_CDR_GET_STATUS, null);
      }//run
    });
    
    // Check and return result
    if (event.getResult() == IJxfsConst.JXFS_RC_SUCCESSFUL) {      
      status = (MEIBnrStatus) event.getData();
    } else {
      throw new JxfsException(event.getResult());
    }//if
    return status.getDeviceStatus();
  }//getDeviceStatus
 
  /****************************************************************************
   * startCashInTransaction
   ***************************************************************************/
  /**
   * Reset Bnr
   * 
   * @throws JxfsException 
   *                  if an error occurred.
   ***************************************************************************/
  private void resetBnr() 
  {
    
    try
    {
		helper.run(new ISynchronousOperation() {
		  public int run(JxfsATM control) throws JxfsException {
		    return control.reset();
		  }
		});
	}
    catch (JxfsException e)
    {
	}
  }
  

  
  /****************************************************************************
   * startCashInTransaction
   ***************************************************************************/
  /**
   * Start the cash in transaction with the BNR
   * 
   * @throws JxfsException 
   *                  if an error occurred.
   ***************************************************************************/
  private void startCashInTransaction() throws JxfsException
  {
    helper.run(new ISynchronousOperation()
    {
        public int run(JxfsATM control) throws JxfsException 
        {
          return control.cashInStart(IJxfsCDRConst.JXFS_C_CDR_POS_DEFAULT, true);
        }//run
      });
  }//startCashInTransaction
  
  /****************************************************************************
   * cashIn
   ***************************************************************************/
  /**
   * Move inserted notes to the escrow
   * `2
   * @param amount required
   * @param currencyCode required
   * @return information about inserted notes
   * @throws JxfsException 
   *                  if an error occurred.
   ***************************************************************************/
  private MEICashInOrder cashIn(long amount, String currencyCode) throws JxfsException 
  {
	  
    MEICashInOrder result = null;
    JxfsOperationCompleteEvent event = null;
    final JxfsDenomination denomination = new JxfsDenomination(null, amount, 0);
    final JxfsCurrency jxfsCurrency = new JxfsCurrency(new JxfsCurrencyCode(
          currencyCode == null ? "" : currencyCode), 0);
      
    // Run cashIn operation
       event = helper.run(new ISynchronousOperation() 
       {
        public int run(JxfsATM deviceControl) throws JxfsException 
        {
          return ((JxfsATM) deviceControl).cashIn(new JxfsCashInOrder(denomination, jxfsCurrency));
        }//run
      });    
    // If the result is not successful
    if (event.getResult() != IJxfsConst.JXFS_RC_SUCCESSFUL) 
    {
      throw new MEIJxfsException(event.getResult(), event.getData());
    } 
    else 
    {
      result = (MEICashInOrder) event.getData();
    }//if
    return result;
  }//cashIn
  
  /****************************************************************************
   * isDenominational
   ****************************************************************************/
  /**
   * Check if device has enough money to change
   * 
   * @param amountToDenominate required
   * @return true or false, dependable on denomination operation
   * @throws JxfsException 
   *                  if an error occurred.
   ****************************************************************************/
  private boolean isDenominational(final long amountToDenominate) throws JxfsException {
    final int mixNumber = IJxfsCDRConst.JXFS_C_CDR_MIX_ALGORITHM;
    final Vector<JxfsDenominationItem> denominationItems = null;
    
    // Run denomination operation
    JxfsOperationCompleteEvent event = helper.run(new ISynchronousOperation() {
      public int run(JxfsATM control) throws JxfsException {
        return control.denominate(mixNumber, new JxfsDenomination(
            denominationItems, amountToDenominate, 0), new JxfsCurrency(
              new JxfsCurrencyCode(CASH_IN_CURRENCY), EXPONENT));
      }//run
    });
    
    // Check result
    if (event.getResult() == IJxfsCDRConst.JXFS_E_CDR_NOT_DISPENSABLE) {
      return false;
    }//if
    return true;
  }//isDenominational 
  
  /****************************************************************************
   * cashInRollback
   ***************************************************************************/
  /**
   * Roll back the cash in transaction
   * 
   * @throws JxfsException 
   *                  if an error occurred.
   ***************************************************************************/
  public void cashInRollback() 
  {
    try 
    {
		// Retrieve the operationCompleteEvent
		helper.run(new ISynchronousOperation() 
		{
		  public int run(JxfsATM control) throws JxfsException 
		  {
		    return control.cashInRollback();
		  }//run
		});
	}
    catch (Exception e) 
    {
		e.printStackTrace();
	}
  }//cashInRollback
  

 
  /****************************************************************************
   * hasChange
   ***************************************************************************/
  /**
   * Check if bnr should give change or not
   * 
   * @param insertedAmount amount
   * @return true or not, if inserted amount bigger then required
   ***************************************************************************/
  private boolean hasChange(long insertedAmount) {
    return (insertedAmount - CASH_IN_AMOUNT) > 0 ? true : false;
  }//hasChange
 
  /****************************************************************************
   * dispenseAndPresent
   ***************************************************************************/
  /**
   * Dispense and give change to user
   * 
   * @param amountToChange required
   * @throws JxfsException 
   *                  if an error occurred.
   ***************************************************************************/
  private void dispenseAndPresent(long amountToChange) throws JxfsException {
    System.out.println("Take your change");
    dispense(amountToChange);
    present();
  }//dispenseAndPresent
  
  /****************************************************************************
   * dispense
   ***************************************************************************/
  /**
   * Dispenses the amount
   * 
   * @param amountToChange required
   * @throws JxfsException 
   *                  if an error occurred.
   ***************************************************************************/
  private void dispense(final long amountToChange) throws JxfsException {
    final int mixNumber = IJxfsCDRConst.JXFS_C_CDR_MIX_ALGORITHM;
    
    // Run dispense operation
    helper.run(new ISynchronousOperation() {
      public int run(JxfsATM control) throws JxfsException {
        return control.dispense(new JxfsDispenseRequest(mixNumber,
            new JxfsDenomination(null, amountToChange, 0),
            new JxfsCurrency(new JxfsCurrencyCode(CASH_IN_CURRENCY), EXPONENT),
            IJxfsCDRConst.JXFS_C_CDR_POS_DEFAULT));
      }//run
    });
  }//dispense
  
  /****************************************************************************
   * present
   ***************************************************************************/
  /**
   * Present dispensed money
   * 
   * @param amountToChange
   * @throws JxfsException 
   *                  if an error occurred.
   ***************************************************************************/
  private void present() throws JxfsException {
    
    // Run present operation
    helper.run(new ISynchronousOperation() {
      public int run(JxfsATM control) throws JxfsException {
        return control.present();
      }//run
    });
  }//present
  
  /*************************************************************************
   * alertStatisticsMessage
   ***************************************************************/
  /**
   * Print statistics message
   * 
   *************************************************************************/
  private void alertStatisticsMessage() {
    
    System.out.println("\n********************************************************************");
    System.out.println("STATISTICS");
    System.out.println("********************************************************************\n");
  }//alertStatisticsMessage
  
  /****************************************************************************
   * observeCashUnit
   ***************************************************************************/
  /**
   * Print cashUnit statistics
   * 
   * @throws JxfsException 
   *                  if an error occurred.
   ***************************************************************************/
  private void observeCashUnit() throws JxfsException {
    
    System.out.println("\n******************* Cash Units *******************");
    System.out.println(queryCashUnit());
    
  }//observeCashUnit
  
  /****************************************************************************
   * queryCashUnit
   ***************************************************************************/
  /**
   * Get the complete state of all physical and logical cash units in the BNR
   * 
   * @return data about cashUnits
   * @throws JxfsException 
   *                  if an error occurred.
   ***************************************************************************/
  private MEICashUnit queryCashUnit() throws JxfsException {
    MEICashUnit resultedCashUnit = null;
    
    // Retrieve the operationCompleteEvent
    JxfsOperationCompleteEvent event = helper.run(new ISynchronousOperation() {
      public int run(JxfsATM control) throws JxfsException {
        return control.queryCashUnit();
      }//run

    });
    
    // If the result is successful
    if (event.getResult() == IJxfsCDRConst.JXFS_RC_SUCCESSFUL) {
      // Retrieve the MEICashUnit object
      resultedCashUnit = (MEICashUnit) event.getData();
    } else {
      throw new JxfsException(event.getResult());
    }//if
    return resultedCashUnit;
  }//queryCashUnit
  
  /****************************************************************************
   * observeModules
   ***************************************************************************/
  /**
   * Print Modules statistics
   * @throws JxfsException 
   * 
   * @throws JxfsException 
   *                  if an error occurred.
   ***************************************************************************/
  private void observeModules() throws JxfsException {
    System.out
    .println("\n******************* Modules *******************");
    
    ArrayList<Integer> modules = getModules();
    
    for (Integer module : modules) {
      System.out.println("Module " + ModuleIdentificationEnum.getById(module));
      
      MEIModuleIdentification properties = getIdentification(module);
      
      // Set Module Name if not Bundler or Cashbox
      //if (!(properties.getDescription().contains("Bundler")) && !(properties.getDescription().contains("Cashbox"))) {
        //setIdentification(module, "userInfo");
      //}//if
      
      // Save User info to Module
      System.out.println(getStatus(module));
    }//for
    
  }//observeModules
  
  /****************************************************************************
   * getModules
   ***************************************************************************/
  /**
   * Get the list of all available modules.
   * 
   * @return the list of module ids or <code>null</code> if an error occurred.
   * @throws JxfsException
   *           if the command failed
   * 
   ***************************************************************************/
  @SuppressWarnings("unchecked")
  private ArrayList<Integer> getModules() throws JxfsException {
    ArrayList<Integer> modules = null;
    
    JxfsOperationCompleteEvent eventGetModules = helper
        .run(new ISynchronousOperation() {
          public int run(JxfsATM control) throws JxfsException {
            return control.directIO(
              IDirectIOConsts.JXFS_O_MEI_CDR_GET_MODULES, null);
          }//run
        });
    
    if (eventGetModules.getResult() == IJxfsCDRConst.JXFS_RC_SUCCESSFUL) {
      modules = (ArrayList<Integer>) eventGetModules.getData();
    } else {
      throw new JxfsException(eventGetModules.getResult());
    }//if
    return modules;
  }//getModules
  
  /****************************************************************************
   * getIdentification
   ***************************************************************************/
  /**
   * Get module identification for the given module id.
   * 
   * @param moduleId
   *          the given module id.
   * @return a MEIModuleIdentification.
   * @throws JxfsException
   *           if the command failed
   *
   ***************************************************************************/
  private MEIModuleIdentification getIdentification(final int moduleId) throws JxfsException {
    MEIModuleIdentification moduleIdentification = null;
    
    JxfsOperationCompleteEvent event = helper.run(new ISynchronousOperation() {
      public int run(JxfsATM control) throws JxfsException {
        return control.directIO(
            IDirectIOConsts.JXFS_O_MEI_CDR_MODULE_GET_IDENTIFICATION,
            new DirectIOModuleIdParameter(moduleId));
      }//run
    });

    if (event.getResult() == IJxfsConst.JXFS_RC_SUCCESSFUL) {
      moduleIdentification = (MEIModuleIdentification) event.getData();
    } else {
      throw new JxfsException(event.getResult());
    }//if

    return moduleIdentification;
  }//getIdentification
  
  /****************************************************************************
   * setIdentification
   ***************************************************************************/
  /**
   * Set the user identification of a module.
   * 
   * @param moduleId
   *          the module id
   * @param stringIdentification
   *          the string to be written in the BNR. The maximum length is 255
   *          characters.
   * @throws JxfsException
   *           if the command failed
   * 
   ***************************************************************************/
  private void setIdentification(final int moduleId,
      final String stringIdentification) throws JxfsException {
    JxfsOperationCompleteEvent event = helper.run(new ISynchronousOperation() {
      public int run(JxfsATM control) throws JxfsException {
        return control.directIO(
            IDirectIOConsts.JXFS_O_MEI_CDR_MODULE_SET_IDENTIFICATION,
            new DirectIOModuleSetIdentificationParameters(moduleId,
                stringIdentification));
      }//run
    });

    // If the result is not successful
    if (event.getResult() != IJxfsConst.JXFS_RC_SUCCESSFUL) {
      throw new JxfsException(event.getResult());
    }//if
  }//setIdentification
  
  /****************************************************************************
   * getStatus
   ***************************************************************************/
  /**
   * Get the module state for the given module id.
   * 
   * @param moduleId
   *          the given module id.
   * @return a MEIModuleStatus.
   * @throws JxfsException
   *           if the command failed
   ***************************************************************************/
  private MEIModuleStatus getStatus(final int moduleId) throws JxfsException {
    MEIModuleStatus moduleStatus = null;

    // Retrieve the operationCompleteEvent
    JxfsOperationCompleteEvent event = helper.run(new ISynchronousOperation() {
      public int run(JxfsATM control) throws JxfsException {
        return control.directIO(
            IDirectIOConsts.JXFS_O_MEI_CDR_MODULE_GET_STATE,
            new DirectIOModuleIdParameter(moduleId));
      }//run
    });

    // If the result is successful
    if (event.getResult() == IJxfsConst.JXFS_RC_SUCCESSFUL) 
    {
      // Retrieve the MEIModuleStatus object
      moduleStatus = (MEIModuleStatus) event.getData();
    } 
    else
    {
      throw new JxfsException(event.getResult());
    }//if
    return moduleStatus;
  }//getStatus
 
  /****************************************************************************
   * disposeBnrController
   ***************************************************************************/
  /**
   * Close bnrController end exit from application
   * 
   ***************************************************************************/
  private void disposeBnrController()
  {
    JxfsDeviceManager.getReference().shutdown();
  }//disposeBnrController

}//IntegrationSample



 