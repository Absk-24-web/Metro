package Cris;

import java.util.logging.Level;

import jssc.SerialPort;

public class Common 
{	
	private static SerialPort serialPort = null;	
	public int GetVendorId()
	{
		return GlobalMembers.iVendorId;
	}
	
	public String GetAVRMApiVersion()
	{
		String strReturnVal = "";
		try
		{
			strReturnVal = GlobalMembers.strAVRMApiVersion;
		}
		catch(Exception e2)
		{
			strReturnVal = "00.00.00";
			//objCRDResponse.strErrDesc = e2.getMessage();
			e2.printStackTrace();
		}
		return strReturnVal;
	}
	
	public int SetLoggingLevel(int iLogLevel)
	{
		int iRetVal = -1;
		try
		{
			switch(iLogLevel)
			{
				case 40: GlobalMembers.objLogLevel = Level.OFF; break;
				case 41: GlobalMembers.objLogLevel = Level.SEVERE; break;
				case 42: GlobalMembers.objLogLevel = Level.WARNING; break;
				case 43: GlobalMembers.objLogLevel = Level.INFO; break;
				case 44: GlobalMembers.objLogLevel = Level.CONFIG; break;
				case 45: GlobalMembers.objLogLevel = Level.FINE; break;
				case 46: GlobalMembers.objLogLevel = Level.FINER; break;
				case 47: GlobalMembers.objLogLevel = Level.FINEST; break;
				default: GlobalMembers.objLogLevel = Level.ALL; break;
			}
			
			iRetVal = 0;
		}
		catch(Exception e2)
		{
			iRetVal = 1;
			//objCRDResponse.strErrDesc = e2.getMessage();
			e2.printStackTrace();
		}
		return iRetVal;
	}
	
	public int GetLoggingLevel()
	{
		int iRetVal = -1;
		try
		{
			if(GlobalMembers.objLogLevel == Level.OFF)
			{
				iRetVal = 40;
			}
			else if(GlobalMembers.objLogLevel == Level.SEVERE)
			{
				iRetVal = 41;
			}
			else if(GlobalMembers.objLogLevel == Level.WARNING)
			{
				iRetVal = 42;
			}
			else if(GlobalMembers.objLogLevel == Level.INFO)
			{
				iRetVal = 43;
			}
			else if(GlobalMembers.objLogLevel == Level.CONFIG)
			{
				iRetVal = 44;
			}
			else if(GlobalMembers.objLogLevel == Level.FINE)
			{
				iRetVal = 45;
			}
			else if(GlobalMembers.objLogLevel == Level.FINER)
			{
				iRetVal = 46;
			}
			else if(GlobalMembers.objLogLevel == Level.FINEST)
			{
				iRetVal = 47;
			}
			else if(GlobalMembers.objLogLevel == Level.ALL)
			{
				iRetVal = 47;
			}
			//iRetVal = 0;
		}
		catch(Exception e2)
		{
			iRetVal = 1;
			//objCRDResponse.strErrDesc = e2.getMessage();
			e2.printStackTrace();
		}
		return iRetVal;
	}
	
	public boolean SetHostName(String HostName)
	{
		GlobalMembers.strHostName = HostName;
		return true;
	}
	
	public static Level GetLogLevel()
	{
		return GlobalMembers.objLogLevel;
	}	
}
