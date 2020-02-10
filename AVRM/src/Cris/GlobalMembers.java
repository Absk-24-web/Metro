package Cris;

import java.util.logging.Level;

public abstract class GlobalMembers 
{
	public static int iVendorId = 2;
	public static String strAVRMApiVersion = "1.0.1";
	public static String strSCardNativeLibVersion = "1.0.1";
	public static String strSCardReaderNativeLibVersion = "1.0.1";
	
	public static String strHostName = "";
	
	public static String strLogFileDirPath = "";
	
	public static Level objLogLevel;
	
	public static String LogDirPath()
	{
		String strOS = System.getProperty("os.name").toLowerCase();
				
		if(strOS.indexOf("win") >= 0)
		{
			strLogFileDirPath = "D:\\AVRMLogs";
		}
		else
		{
			strLogFileDirPath = "/usr/AVRMLogs";
		}
		return strLogFileDirPath;
	}
	
	public static boolean IsWindows()
	{
		boolean bret = false;
		if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
		{
			bret = true;
		}
		else
		{
			bret = false;
		}
		return bret;
	}
}
