

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

import jssc.SerialPort;


public class TokenDispenser
{	
	
	private static SerialPort serialPort = null;
	private static SerialPort serialPort1 = null;
	public static Thread thCoinAccept = null;		
	
	
	public TokenDispenserResponse Initialize()//String str)//String strComPort, int iBaudRate)
	{
		
		TokenDispenserResponse objCRDResponse = new TokenDispenserResponse();
		try
		{
			serialPort = new SerialPort("COM2");
			 if(serialPort != null)
		        {
					// opening port
		            if(serialPort.openPort() == true)
		            {
			            serialPort.setParams(SerialPort.BAUDRATE_38400, 	//SerialPort.BAUDRATE_9600,
			                                 SerialPort.DATABITS_8,
			                                 SerialPort.STOPBITS_1,
			                                 SerialPort.PARITY_NONE);		
		            }
			         
		           // obj.res="Initialize COM Success";
		        }
            byte[] byResponse = null;
			
            byte[] byCmd = new byte [] { 0x1D, 0x49, 0x41 };
			
            serialPort.writeBytes(byCmd);
            
            Thread.sleep(3000);
            objCRDResponse.res=serialPort.readHexString();
	 
	           
		}
		catch(Exception e2)
		{
			objCRDResponse.bResponse = false;
			objCRDResponse.strErrDesc = e2.getMessage();
			e2.printStackTrace();
		}
		
		return objCRDResponse;
	}
	
	public String GetNativeLibVersion()
	{
		String response="";
		try
		{
            response="01.00.00";//Native lib Version = 1.1.0
		}
		catch(Exception e2)
		{
			response= "00.00.00";
			//e2.printStackTrace();
		}
		return response;
	}
	
	public String GetTokenDispenserFWVersion()
	{
		String response="";
		try
		{
			
			String command="",line;
			
			
	        command="./ReferenceMain 2";// 1 20";
			Runtime run  = Runtime.getRuntime(); 
		    Process proc = run.exec(command);
		    BufferedReader input =  
		            new BufferedReader  
		              (new InputStreamReader(proc.getInputStream()));  
           while ((line = input.readLine()) != null) 
           {  
        	   response +=line;
           
           }  
		     input.close();  
			//response id SCUM Version 1.1.0
            response="01.00.00";//Native lib Version = 1.1.0
		}
		catch(Exception e2)
		{
			response= "00.00.00";
			//e2.printStackTrace();
		}
		return response;
	}
	
	public String GetTokenDispenserReaderFWVersion()
	{
		String response="";
		try
		{
            response="01.00.00";//GetTokenDispenserReaderFWVersion
		}
		catch(Exception e2)
		{
			response= "00.00.00";
			//e2.printStackTrace();
		}
		return response;
	}
	
	public int ClearJammedToken(int BoxNo,int TokenDest,int Timeout) 
	{
		int response=31;
		TokenDispenserResponse objCRDResponse = new TokenDispenserResponse();
		 try 
		 {
			 String command="",line;
			 objCRDResponse.res="";
			
	        command="./ReferenceMain 3";// 1 20";
			Runtime run  = Runtime.getRuntime(); 
		    Process proc = run.exec(command);
		    BufferedReader input =  
		            new BufferedReader  
		              (new InputStreamReader(proc.getInputStream()));  
         while ((line = input.readLine()) != null) 
         {  
		       objCRDResponse.res +=line;
         
         }  
		     input.close();  
			response=0;
		 }
		 catch (Exception e)
		 {
			 
			response=1;
		}
		return response;
	
	}

	public int EmptyTokenBox(int BoxNo,int TokenDest,int Timeout)
	{
		int response=1;
		try
         {
			if(BoxNo==1)
			{
				EmptyContainer1();
				response=0;
			}
			else if(BoxNo==2)
			{
				EmptyContainer2();
				response=0;
			}
		}
		catch (Exception e)
		{
			response=1;
		}
		return response;
	}


	public TokenDispenserResponse EmptyContainer1()
	{
		TokenDispenserResponse objCRDResponse = new TokenDispenserResponse();
		
		try 
		{
			
		    String command="", line;;
		    objCRDResponse.res="";
			//command = "//Desktop//TokenDispenser//TeuIntegratedBinaryAndLibrary//TeuIntegratedBinaryAndLibrary//Teu_Library_2//Build//ReferenceMain 3 1 30";
	        command="./ReferenceMain 8";// 1 20";
			Runtime run  = Runtime.getRuntime(); 
		    Process proc = run.exec(command);
		    BufferedReader input =  
		            new BufferedReader  
		              (new InputStreamReader(proc.getInputStream()));  
            while ((line = input.readLine()) != null) 
            {  
            	  objCRDResponse.res +=line;
            }  
		     input.close();  
			
           
		}
		catch(Exception e2)
		{
			objCRDResponse.bResponse = false;
			objCRDResponse.strErrDesc = e2.getMessage();
			e2.printStackTrace();
		}
		
		return objCRDResponse;
	}

	public TokenDispenserResponse EmptyContainer2()
	{
		TokenDispenserResponse objCRDResponse = new TokenDispenserResponse();
		
		try 
		{
			
		    String command="", line;;
		    objCRDResponse.res="";
			//command = "//Desktop//TokenDispenser//TeuIntegratedBinaryAndLibrary//TeuIntegratedBinaryAndLibrary//Teu_Library_2//Build//ReferenceMain 3 1 30";
	        command="./ReferenceMain 9";// 1 20";
			Runtime run  = Runtime.getRuntime(); 
		    Process proc = run.exec(command);
		    BufferedReader input =  
		            new BufferedReader  
		              (new InputStreamReader(proc.getInputStream()));  
            while ((line = input.readLine()) != null) 
            {  
            	  objCRDResponse.res +=line;
            }  
		     input.close();  
			
           
		}
		catch(Exception e2)
		{
			objCRDResponse.bResponse = false;
			objCRDResponse.strErrDesc = e2.getMessage();
			e2.printStackTrace();
		}
		
		return objCRDResponse;
	}

	public TokenDispenserResponse DebugContainer1() 
	{
		TokenDispenserResponse objCRDResponse = new TokenDispenserResponse();
		 try 
		 {
			 String command="",line;
			 objCRDResponse.res="";
			
	        command="./ReferenceMain 14";// 1 20";
			Runtime run  = Runtime.getRuntime(); 
		    Process proc = run.exec(command);
		    BufferedReader input =  
		            new BufferedReader  
		              (new InputStreamReader(proc.getInputStream()));  
         while ((line = input.readLine()) != null) 
         {  
		       objCRDResponse.res +=line;
         
         }  
		     input.close();  
			
		 }
		 catch (Exception e)
		 {
			 
			e.printStackTrace();
		}
		return objCRDResponse;
	
	}

	public TokenDispenserResponse DebugContainer2() 
	{
		TokenDispenserResponse objCRDResponse = new TokenDispenserResponse();
		 try 
		 {
			 String command="",line;
			 objCRDResponse.res="";
			
	        command="./ReferenceMain 15";// 1 20";
			Runtime run  = Runtime.getRuntime(); 
		    Process proc = run.exec(command);
		    BufferedReader input =  
		            new BufferedReader  
		              (new InputStreamReader(proc.getInputStream()));  
         while ((line = input.readLine()) != null) 
         {  
		       objCRDResponse.res +=line;
         
         }  
		     input.close();  
			
		 }
		 catch (Exception e)
		 {
			 
			e.printStackTrace();
		}
		return objCRDResponse;
	
	}

	public TokenDispenserResponse Start1()
	{
		TokenDispenserResponse objCRDResponse = new TokenDispenserResponse();
		 try 
		 {
			 String command="",line;
			 objCRDResponse.res="";
			
	        command="./ReferenceMain 10";// 1 20";
			Runtime run  = Runtime.getRuntime(); 
		    Process proc = run.exec(command);
		    BufferedReader input =  
		            new BufferedReader  
		              (new InputStreamReader(proc.getInputStream()));  
         while ((line = input.readLine()) != null) 
         {  
		       objCRDResponse.res +=line;
         
         }  
		     input.close();  
			
		 }
		 catch (Exception e)
		 {
			 
			e.printStackTrace();
		}
		return objCRDResponse;
	
	}

	public TokenDispenserResponse Stop1() 
	{
		TokenDispenserResponse objCRDResponse = new TokenDispenserResponse();
		 try 
		 {
			 String command="",line;
			 objCRDResponse.res="";
			
	        command="./ReferenceMain 11";// 1 20";
			Runtime run  = Runtime.getRuntime(); 
		    Process proc = run.exec(command);
		    BufferedReader input =  
		            new BufferedReader  
		              (new InputStreamReader(proc.getInputStream()));  
         while ((line = input.readLine()) != null) 
         {  
		       objCRDResponse.res +=line;
         
         }  
		     input.close();  
			
		 }
		 catch (Exception e)
		 {
			 
			e.printStackTrace();
		}
		return objCRDResponse;
	
	}

	public TokenDispenserResponse Start2() 
	{
		TokenDispenserResponse objCRDResponse = new TokenDispenserResponse();
		 try 
		 {
			 String command="",line;
			 objCRDResponse.res="";
			
	        command="./ReferenceMain 12";// 1 20";
			Runtime run  = Runtime.getRuntime(); 
		    Process proc = run.exec(command);
		    BufferedReader input =  
		            new BufferedReader  
		              (new InputStreamReader(proc.getInputStream()));  
         while ((line = input.readLine()) != null) 
         {  
		       objCRDResponse.res +=line;
         
         }  
		     input.close();  
			
		 }
		 catch (Exception e)
		 {
			 
			e.printStackTrace();
		}
		return objCRDResponse;
	
	}

	public TokenDispenserResponse Stop2()
	{
		TokenDispenserResponse objCRDResponse = new TokenDispenserResponse();
		 try 
		 {
			 String command="",line;
			 objCRDResponse.res="";
			
	        command="./ReferenceMain 13";// 1 20";
			Runtime run  = Runtime.getRuntime(); 
		    Process proc = run.exec(command);
		    BufferedReader input =  
		            new BufferedReader  
		              (new InputStreamReader(proc.getInputStream()));  
         while ((line = input.readLine()) != null) 
         {  
		       objCRDResponse.res +=line;
         
         }  
		     input.close();  
			
		 }
		 catch (Exception e)
		 {
			 
			e.printStackTrace();
		}
		return objCRDResponse;
	
	}

	public String Dispence(int no, int amount)
	{
		TokenDispenserResponse objCRDResponse = new TokenDispenserResponse();
		 try 
		 {
			 String command="", line;;
			 objCRDResponse.res="";
			//command = "//Desktop//TokenDispenser//TeuIntegratedBinaryAndLibrary//TeuIntegratedBinaryAndLibrary//Teu_Library_2//Build//ReferenceMain 3 1 30";
	        command="./ReferenceMain 4 "+no+" "+amount;// 1 20";
			Runtime run  = Runtime.getRuntime(); 
		    Process proc = run.exec(command);
		    BufferedReader input =  
		            new BufferedReader  
		              (new InputStreamReader(proc.getInputStream()));  
            while ((line = input.readLine()) != null) 
            {  
		      // System.out.println(line);  
            	  objCRDResponse.res +=line;
            }  
		     input.close();  
			
		 }
		 catch (Exception e)
		 {
			 
			e.printStackTrace();
		}
		return objCRDResponse.res;
	}

	public String GetDeviceStatus() 
	{
		TokenDispenserResponse objCRDResponse = new TokenDispenserResponse();
		 try 
		 {
			 String command="",line;
			 objCRDResponse.res="";
			
	        command="./ReferenceMain 5";// 1 20";
			Runtime run  = Runtime.getRuntime(); 
		    Process proc = run.exec(command);
		    BufferedReader input =  
		            new BufferedReader  
		              (new InputStreamReader(proc.getInputStream()));  
          while ((line = input.readLine()) != null) 
          {  
		       objCRDResponse.res +=line;
          
          }  
		     input.close();  
			
		 }
		 catch (Exception e)
		 {
			 
			e.printStackTrace();
		}
		return objCRDResponse.res;
	
	}

	
	
	public ResponseDescription ResponseDesc(String data)
	{
		ResponseDescription response=ResponseDescription.Unknown_Response;
		switch(data)
		{
		 case "AA 4E 02 01 00 08 00 99" :   response= ResponseDescription.Container_1_Present;   break;
		 case "AA 4E 02 02 00 08 00 99" :   response= ResponseDescription.Container_2_Present;   break;
		 case "AA 4E 02 00 00 08 00 99" :   response= ResponseDescription.No_Container_Present;   break;
		 case "AA 4E 02 11 00 08 00 99" :   response= ResponseDescription.Both_Container_Present;   break;
	  // case "AA 4E 02 00 00 08 00 99" :   response=ResponseDescription.   break; Enter DMODE  
		 case "AA 44 02 00 00 08 00 99" :   response= ResponseDescription.Enter_NMode;   break;
		 case "AA 44 01 00 01 08 00 99" :   response= ResponseDescription.Solenoid_1_Debug_Response;   break;
		 case "AA 44 01 00 02 08 00 99" :   response= ResponseDescription.Solenoid_2_Debug_Response;   break;
		 case "AA 4E 00 00 00 08 00 99" :   response= ResponseDescription.Not_in_Debug;   break;
		 case "AA 4E 01 03 01 08 76 99" :   response= ResponseDescription.ASK_Validation;   break;
		 case "AA 44 01 03 01 08 00 99" :   response= ResponseDescription.Sensor_3_CutState;   break;
		 case "AA 44 01 04 01 08 00 99" :   response= ResponseDescription.Sensor_4_CutState;   break;
		 case "AA 44 01 05 01 08 00 99" :   response= ResponseDescription.Sensor_5_CutState;   break;
		 case "AA 44 01 01 00 08 00 99" :   response= ResponseDescription.Container_1_Empty;   break;
		 case "AA 44 01 02 00 08 00 99" :   response= ResponseDescription.Container_2_Empty;   break;
		 case "AA 44 01 11 00 08 00 99" :   response= ResponseDescription.Both_Container_Empty;   break;
		 case "AA 4E 01 44 49 53 00 99" :   response= ResponseDescription.No_Dispense;   break;
		 case "AA 44 42 55 5a 45 52 99" :   response= ResponseDescription.Buzzer_Test_Response;   break;
		 case "AA 44 48 31 53 54 41 99" :   response= ResponseDescription.Hopper_1_Start_Response;   break;
		 case "AA 44 48 31 53 54 4F 99" :   response= ResponseDescription.Hopper_1_Stop_Response;   break;
		 case "AA 44 48 32 53 54 41 99" :   response= ResponseDescription.Hopper_2_Start_Response;   break;
		 case "AA 44 48 32 53 54 4F 99" :   response= ResponseDescription.Hopper_2_Stop_Response;   break;
		 case "AA 52 53 49 48 31 08 99" :   response= ResponseDescription.Token_Stuck_Hopper_1;   break;
		 case "AA 52 53 49 48 32 08 99" :   response= ResponseDescription.Token_Stuck_Hopper_2;   break;
		 case "AA 4F 55 54 4f 46 53 99" :   response= ResponseDescription.Out_Of_Service;   break;
		 default                        :   response=ResponseDescription.Unknown_Response;   break;
		
		
		
		}
		return response;
		
	}

	public TokenDispenserResponse GetVersion() 
	{
		TokenDispenserResponse objCRDResponse = new TokenDispenserResponse();
		 try 
		 {
			 String command="",line;
			 objCRDResponse.res="";
			
	        command="./ReferenceMain 2";// 1 20";
			Runtime run  = Runtime.getRuntime(); 
		    Process proc = run.exec(command);
		    BufferedReader input =  
		            new BufferedReader  
		              (new InputStreamReader(proc.getInputStream()));  
           while ((line = input.readLine()) != null) 
           {  
		       objCRDResponse.res +=line;
           
           }  
		     input.close();  
			
		 }
		 catch (Exception e)
		 {
			 
			e.printStackTrace();
		}
		return objCRDResponse;
	
	}

	public TokenDispenserResponse ReadTokenData() 
	{
		TokenDispenserResponse objCRDResponse = new TokenDispenserResponse();
		 try 
		 {
			 String command="",line;
			 objCRDResponse.res="";
			//command = "//Desktop//TokenDispenser//TeuIntegratedBinaryAndLibrary//TeuIntegratedBinaryAndLibrary//Teu_Library_2//Build//ReferenceMain 3 1 30";
	        command="./ReferenceMain 6";// 1 20";
			Runtime run  = Runtime.getRuntime(); 
		    Process proc = run.exec(command);
		    BufferedReader input =  
		            new BufferedReader  
		              (new InputStreamReader(proc.getInputStream()));  
            while ((line = input.readLine()) != null) 
            {  
		       objCRDResponse.res +=line;
            	//System.out.println(line);  
            }  
		     input.close();  
			
		 }
		 catch (Exception e)
		 {
			 
			e.printStackTrace();
		}
		return objCRDResponse;
	
	}

	public TokenDispenserResponse DeInit()
	{

		TokenDispenserResponse obj=new TokenDispenserResponse();
		try 
		{	
			// writing to port
	        serialPort=null;
	        obj.res="Deinit Device Success";

		}
		catch(Exception e2)
		{
		
		}
		return obj;
	
	}
	
	public int DisConnectDevice(int Timeout) 
	{
		int objResponse=31;
		
		try 
		{	
			if(serialPort.isOpened()) 
			{
				serialPort.closePort();
				objResponse=0;
			}
            Thread.sleep(1000);
           
			switch(objResponse)
			{
				case 0:   //UPS Disconnected Successfully
				{
					objResponse=0;
				}
				break;
				
				case 18:		//Operation timeout
				{
					objResponse = 18;
				}
				break;
				
				case 20:		//UPS Not yet connected
				{
					objResponse = 20;
				}
				break;
				default : 
					objResponse = 31;//Operation Timeout Occured
				
			}
			
		}
		catch(Exception e2)
		{
			objResponse = 28;//Communication Failure
			
		}
		return objResponse;
	
	}

	public TokenDispenserResponse EmptyBothContainer() 
	{
		TokenDispenserResponse objCRDResponse = new TokenDispenserResponse();
		 try 
		 {
			 String command="",line;
			 objCRDResponse.res="";
			
	        command="./ReferenceMain 7";// 1 20";
			Runtime run  = Runtime.getRuntime(); 
		    Process proc = run.exec(command);
		    BufferedReader input =  
		            new BufferedReader  
		              (new InputStreamReader(proc.getInputStream()));  
         while ((line = input.readLine()) != null) 
         {  
		       objCRDResponse.res +=line;
         
         }  
		     input.close();  
			
		 }
		 catch (Exception e)
		 {
			 
			e.printStackTrace();
		}
		return objCRDResponse;
	
	}
}
class TokenDispenserResponse 
{
	public boolean bResponse;
	public byte[] bData;
	public String res;
	//public String resDesc;
	public String strErrDesc;
	ResponseDescription resDesc;
}

 enum ResponseDescription 
 { 
	 Container_1_Present,
	 Container_2_Present,
	 No_Container_Present, 
	 Both_Container_Present,
	 //Enter_DMode,
	 Enter_NMode,
	 Solenoid_1_Debug_Response,
	 Solenoid_2_Debug_Response,
	 Not_in_Debug,
	 ASK_Validation,
	 Sensor_3_CutState,
	 Sensor_4_CutState,
	 Sensor_5_CutState,
	 Container_1_Empty,
	 Container_2_Empty,
	 Both_Container_Empty,
	 No_Dispense,
	 Buzzer_Test_Response,
	 Hopper_1_Start_Response,
	 Hopper_1_Stop_Response,
	 Hopper_2_Start_Response,
	 Hopper_2_Stop_Response,
	 Token_Stuck_Hopper_1,
	 Token_Stuck_Hopper_2,
	 Out_Of_Service,
	 Unknown_Response;
 } 



