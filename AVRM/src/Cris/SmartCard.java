package Cris;

import java.io.File;
//import java.io.FileInputStream;
//import java.nio.channels.ByteChannel;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.*;
//import java.util.logging.LogManager;
//import java.util.logging.Logger;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
//import java.net.URLDecoder;
import java.text.SimpleDateFormat;

import jssc.SerialPort;

public class SmartCard {
	private byte ACK = (byte) 0x06;
	private byte NACK = (byte) 0x15;
	private byte STX = (byte) 0xF2;
	private byte ETX = (byte) 0x03;
	private byte CMT = (byte) 0x43;
	private byte CRC = (byte) 0x00;

	private byte PosRes = (byte) 0x50;
	private byte NegRes = (byte) 0x4E;

	private int st0_Pos = 8;
	private int st1_Pos = 9;
	private int st2_Pos = 10;
	private int e1_Pos = 8;
	private int e0_Pos = 9;

	private int ResType_Pos = 5;

	private enum ST0 {
		No_card_in_CRT_571, One_card_in_gate, One_card_on_RF_IC_position
	}

	private enum ST1 {
		No_card_in_stacker, Few_card_in_stacker, Enough_cards_in_card_box
	}

	private enum ST2 {
		Error_card_bin_not_full, Error_card_bin_full
	}

	private enum Device {
		CardDispenser, CardReaderWriter
	}

	private static final byte INIT_CARDHOLD = 0x30;
	private static final byte INIT_ERRORBIN = 0x31;
	private static final byte INIT_NO_MOVEMENT = 0x33;
	// private static final byte INIT_CARDHOLD_RETRACTCOUNT = 0x34;
	// private static final byte INIT_ERRORBIN_RETRACTCOUNT = 0x35;
	// private static final byte INIT_NO_MOVEMENT_RETRACTCOUNT = 0x37;

	private SerialPort serialPort = null;
	private boolean bConnected = false;

	private static Logger smartCardlogger = null;
	private static volatile FileHandler fileHandler;

	private static volatile int iPrevDayOfYear = 0;

	// member variable for holding device type
	private Device objDeviceType;

	/* Variables used for Smart Card RW device */
	private byte ACK_CardRW = (byte) 0x06;
	private byte STX_CardRW = (byte) 0xF2;

	private SerialPort serialPort_CardRW = null;
	// private boolean bConnected_CardRW = false;

	private char[] byGlobalCommand_CardRW = new char[1024];

	private byte Cm_CardRW, Pm_CardRW;
	private byte TxDataLen_CardRW;
	private byte[] TxData_CardRW = new byte[1024];
	private byte[] RxData_CardRW = new byte[1024];
	private byte[] ReType_CardRW = new byte[1];
	private byte[] St0_CardRW = new byte[1];
	private byte[] St1_CardRW = new byte[1];
	private int[] RxDataLen_CardRW = new int[1];
	// private static boolean bProceed_CardRW = false;

	private byte GetBCC(byte[] p, int n) {
		byte ch;
		int i;
		byte crc = (byte) 0x00;

		for (i = 0; i < n; i++) {
			ch = p[i];
			crc = (byte) (ch ^ crc);
		}
		return crc;
	}

	private static String GetErrorDesc(byte byE1, byte byE0) {
		String strRes = "";

		switch (String.format("%02X%02X", byE1, byE0)) {
		case "0000":
			strRes = "Reception of undefined command";
			break;
		case "0001":
			strRes = "Command parameter error";
			break;
		}

		return strRes;
	}

	/*
	 * public SmartCard() { //try //{ //String currentDirectory =
	 * System.getProperty("user.dir"); //String path =
	 * SmartCard.class.getProtectionDomain().getCodeSource().getLocation().getPath()
	 * ; //String decodedPath = URLDecoder.decode(path, "UTF-8");
	 * //LogManager.getLogManager().readConfiguration(new
	 * FileInputStream(decodedPath + "..\\mylogging.properties")); //} //catch
	 * (SecurityException | IOException e1) //{ //e1.printStackTrace(); //}
	 * 
	 * smartCardlogger = Logger.getLogger(SmartCard.class.getName());
	 * 
	 * //logger.setLevel(Level.FINE);
	 * smartCardlogger.setLevel(Common.GetLogLevel());
	 * 
	 * //to show logs on console smartCardlogger.addHandler(new ConsoleHandler()); }
	 */

	public SmartCard(int DeviceType) throws Exception {
		try {
			smartCardlogger = Logger.getLogger(SmartCard.class.getName());

			// logger.setLevel(Level.FINE);
			smartCardlogger.setLevel(Common.GetLogLevel());

			// to show logs on console
			smartCardlogger.addHandler(new ConsoleHandler());

			if (DeviceType < 0 || DeviceType > 1) {
				WriteLog(Level.WARNING, "Argument is not proper");
				throw new IllegalArgumentException("Argument is not proper");
			}

			if (DeviceType == 0) {
				objDeviceType = Device.CardReaderWriter;
				WriteLog(Level.INFO, "CardReaderWriter object created");
			} else if (DeviceType == 1) {
				objDeviceType = Device.CardDispenser;
				WriteLog(Level.INFO, "CardDispenser object created");
			}
		} catch (Exception e2) {
			WriteLog(Level.WARNING, e2.getMessage());

			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}
	}

	private void WriteLog(Level objLevel, String strLog) {
		try {
			FileHandler fh = fileHandler;
			if (fh != null) {
				smartCardlogger.removeHandler(fh);
				fh.close(); // Release any file lock.
			}

			// adding custom handler
			// logger.addHandler(new MyHandler());

			Calendar cal = Calendar.getInstance();
			int iYear = cal.get(Calendar.YEAR);
			int iDayOfYear = cal.get(Calendar.DAY_OF_YEAR);

			String strFileName = GlobalMembers.LogDirPath() + File.separator + "AVRMAPI_" + GlobalMembers.strHostName
					+ "Log" + iYear + "." + iDayOfYear;

			File yourFile = new File(strFileName);
			yourFile.getParentFile().mkdirs();
			yourFile.createNewFile();

			// FileHandler file name with max size and number of log files limit
			fileHandler = new FileHandler(strFileName, true);

			fileHandler.setFormatter(new MyFormatter());

			// setting custom filter for FileHandler
			fileHandler.setFilter(new MyFilter());

			// adding custom handler
			smartCardlogger.addHandler(fileHandler);

			// smartCardlogger.log(objLevel, "prev- " + iPrevDayOfYear);
			// smartCardlogger.log(objLevel, "cur- " + iDayOfYear);

			if (iPrevDayOfYear == 0 || iPrevDayOfYear != iDayOfYear) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");

				smartCardlogger.log(objLevel, "Date: " + simpleDateFormat.format(new Date()));
				iPrevDayOfYear = iDayOfYear;
			}

			smartCardlogger.log(objLevel, strLog);
		} catch (SecurityException | IOException e1) {
			e1.printStackTrace();
		}
	}

	public void WriteReqResLog(String strCommandName, byte[] byReqRes) {
		String strByteReqRes = "";
		for (int responseLen = 0; responseLen < byReqRes.length; responseLen++) {
			strByteReqRes += String.format("%02X ", byReqRes[responseLen]);
		}
		System.out.println(strCommandName + " - " + strByteReqRes);
		WriteLog(Level.FINEST, strCommandName + " - " + strByteReqRes);
		WriteLog(Level.INFO, strCommandName + " - " + strByteReqRes);
	}

	public int ConnectDevice(int PortId, int ChannelClearanceMode, int Timeout) {
		int iReturnVal = -1;
		boolean bProceed = true;

		MyFormatter.strSourceMethodName = "ConnectDevice";
		WriteLog(Level.INFO, "DeviceType: " + objDeviceType.toString() + "; PortId: " + PortId
				+ "; ChannelClearanceMode: " + ChannelClearanceMode + "; Timeout: " + Timeout);

		try {
			if (ChannelClearanceMode < 0 || ChannelClearanceMode > 2) {
				iReturnVal = 3;
				bProceed = false;
				WriteLog(Level.CONFIG, "ChannelClearanceMode is not valid");
			}

			if (objDeviceType == Device.CardDispenser) {
				// device already connected
				if (bConnected) {
					iReturnVal = 20;
					bProceed = false;
					WriteLog(Level.INFO, "Device already connected");
				}

				if (Timeout < 100) {
					Timeout = 100;
					WriteLog(Level.FINE, "Default timeout set");
				}

				if (bProceed) {
					// writing to port
					if (GlobalMembers.IsWindows()) {
						serialPort = new SerialPort("COM" + PortId);
					} else {
						switch (PortId) {
						case 1:
							serialPort = new SerialPort("/dev/ttyS0");
							break;
						case 2:
							serialPort = new SerialPort("/dev/ttyS1");
							break;
						case 3:
							serialPort = new SerialPort("/dev/ttyUSB0");
							break;
						case 4:
							serialPort = new SerialPort("/dev/ttyUSB1");
							break;
						default:
							serialPort = null; {
							WriteLog(Level.WARNING, "Port not valid");
							break;
						}
						}
					}

					if (serialPort != null) {
						WriteLog(Level.FINEST, "Class object initialized");

						// opening port
						if (serialPort.openPort() == true) {
							WriteLog(Level.FINEST, "Port opened");

							serialPort.setParams(SerialPort.BAUDRATE_115200, // SerialPort.BAUDRATE_9600,
									SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

							byte[] byResponse = null;
							byte byClearanceMode = 0x00;
							switch (ChannelClearanceMode) {
							case 0:
								byClearanceMode = INIT_NO_MOVEMENT;
								break;
							case 1:
								byClearanceMode = INIT_ERRORBIN;
								break;
							case 2:
								byClearanceMode = INIT_CARDHOLD;
								break;
							}

							if (bProceed) {
								byte[] byCmd = new byte[] { STX, 0x00, 0x00, 0x03, CMT, 0x30, byClearanceMode, ETX,
										CRC };
								byCmd[byCmd.length - 1] = (byte) GetBCC(byCmd, byCmd.length - 1);
								WriteReqResLog(MyFormatter.strSourceMethodName, byCmd);
								if (serialPort.writeBytes(byCmd)) {
									WriteLog(Level.FINEST, "bytes written on port");

									byte[] byRes1 = serialPort.readBytes(5, Timeout);
									WriteLog(Level.FINEST, "5 bytes read");

									int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
									WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

									byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
									WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

									byResponse = new byte[byRes1.length + byRes2.length];
									System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
									System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);

									// Thread.sleep(3000);
									// byResponse = serialPort.readBytes();
									WriteReqResLog("ConnectDevice RES", byResponse);
									serialPort.writeByte(ACK);

									if (byResponse[0] == ACK) {
										WriteLog(Level.WARNING, "Ack response received");

										if (byResponse[ResType_Pos] == PosRes) {
											WriteLog(Level.FINEST, "Positive response received");
											switch (byResponse[st0_Pos]) {
											case '0':
												WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
												break;
											case '1':
												WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
												break;
											case '2':
												WriteLog(Level.INFO,
														"CardPos- " + ST0.One_card_on_RF_IC_position.toString());
												break;
											}

											switch (byResponse[st1_Pos]) {
											case '0':
												WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
												break;
											case '1':
												WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
												break;
											case '2':
												WriteLog(Level.INFO,
														"Stacker- " + ST1.Enough_cards_in_card_box.toString());
												break;
											}
											switch (byResponse[st2_Pos]) {
											case '0':
												WriteLog(Level.INFO,
														"ErrBin- " + ST2.Error_card_bin_not_full.toString());
												break;
											case '1': {
												iReturnVal = 1;
												bProceed = false;
												WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
											}
												break;
											}

											if (bProceed) {
												iReturnVal = 0;
												bConnected = true;
												WriteLog(Level.WARNING, "Device connected successfully");
											}
										} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
											WriteLog(Level.FINEST, "Negative response received");
											iReturnVal = 28;
											WriteLog(Level.FINEST,
													GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
										} else {
											WriteLog(Level.FINEST, "Other response received");
											iReturnVal = 28;
										}
									} else if (byResponse[0] == NACK && byResponse.length >= 10) {
										WriteLog(Level.WARNING, "Nack response received");
										iReturnVal = 28;// + ChannelClearanceMode;
										WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
									} else {
										iReturnVal = 28;// + ChannelClearanceMode;
										WriteLog(Level.WARNING, "Unknown error");
									}
								} else {
									iReturnVal = 28;// + ChannelClearanceMode;
									WriteLog(Level.WARNING, "Command send fail on serial port");
								}
							}
						} else {
							iReturnVal = 25;// + ChannelClearanceMode;
							WriteLog(Level.WARNING, "Port could not be opened");
						}
					} else {
						iReturnVal = 31;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			} else if (objDeviceType == Device.CardReaderWriter) {
				// device already connected
				if (bConnected) {
					WriteLog(Level.CONFIG, "Device already connected");
					iReturnVal = 20; // Already connected
					bProceed = false;
				}

				if (bProceed) {
					// writing to port
					if (GlobalMembers.IsWindows()) {
						serialPort_CardRW = new SerialPort("COM" + PortId);
					} else {
						switch (PortId) {
						case 1:
							serialPort_CardRW = new SerialPort("/dev/ttyS0");
							break;
						case 2:
							serialPort_CardRW = new SerialPort("/dev/ttyS1");
							break;
						case 3:
							serialPort_CardRW = new SerialPort("/dev/ttyUSB0");
							break;
						case 4:
							serialPort_CardRW = new SerialPort("/dev/ttyUSB1");
							break;
						default:
							serialPort_CardRW = null; {
							WriteLog(Level.WARNING, "Port not valid");
							break;
						}
						}
					}

					if (serialPort_CardRW != null) {
						WriteLog(Level.FINEST, "Class object initialized");

						// opening port
						if (serialPort_CardRW.openPort() == true) {
							WriteLog(Level.FINEST, "Port opened");
							serialPort_CardRW.setParams(SerialPort.BAUDRATE_38400, // SerialPort.BAUDRATE_9600,
									SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);

							int iRes = 0;

							// Clear all variables
							java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
							java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
							java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
							java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
							java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
							java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

							// Command code
							Cm_CardRW = 0x30;

							// Parameter code
							/*
							 * “0”(30h) Eject the card to the gate portion and finish the command even if
							 * the card is not taken out “1”(31h) Capture the card to the rear side “2”(32h)
							 * Re-position the card to the home position in the ICRW “3”(33h) Don’t move
							 * card “4”(34h) eject the card to the front side and respond, function is same
							 * as “0”(30h) “5”(35h) capture the card from rear side, Function is same as
							 * “1”(31h) “6”(36h) retrieve the card to the reader inside, The function is
							 * same as “2”(32h) “8”(38h) To report the initialization parameter
							 */
							switch (ChannelClearanceMode) {
							case 0:
								Pm_CardRW = INIT_NO_MOVEMENT;
								break;
							case 1:
								Pm_CardRW = INIT_ERRORBIN;
								break;
							case 2:
								Pm_CardRW = INIT_CARDHOLD;
								break;
							}

							St0_CardRW[0] = St1_CardRW[0] = 0;

							RxDataLen_CardRW[0] = 0;

							TxDataLen_CardRW = 13;

							TxData_CardRW[0] = 0x33;
							TxData_CardRW[1] = 0x32;
							TxData_CardRW[2] = 0x34;
							TxData_CardRW[3] = 0x31;

							// fm as always zero
							TxData_CardRW[4] = 0x30;

							// Pd
							TxData_CardRW[5] = 0x30;

							// Ty
							TxData_CardRW[6] = 0x30;

							// Ds
							TxData_CardRW[7] = 0x30;

							// Cc
							TxData_CardRW[8] = 0x31;

							// Re
							TxData_CardRW[9] = 0x30;

							TxData_CardRW[10] = 0x30;
							TxData_CardRW[11] = 0x30;
							TxData_CardRW[12] = 0x30;

							iRes = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW,
									ReType_CardRW, St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);

							if (iRes == 0) {
								if (ReType_CardRW[0] == 0x50) {
									WriteLog(Level.WARNING, "Ack response received");
									// iReturnVal = CardEntrySetting(0);// enable card entry
									// if (iReturnVal == 0)
									iReturnVal = 0;
									bConnected = true;
									// else
									// bConnected = false;
								} else {
									WriteLog(Level.WARNING, "Ack response not received");
									bConnected = false;
									iReturnVal = 28;// communication failure
								}
							} else {
								bConnected = false;
								iReturnVal = iRes;// other error
								WriteLog(Level.WARNING, "Unknown error");
							}
						} else {
							bConnected = false;
							iReturnVal = 25;
							WriteLog(Level.WARNING, "Port could not be opened");
						}
					} else {
						bConnected = false;
						iReturnVal = 31;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			}
		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				iReturnVal = 18;
			} else {
				iReturnVal = 31;
			}
			WriteLog(Level.WARNING, e2.getMessage());

			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}
		WriteLog(Level.FINEST, "Return value- " + iReturnVal);

		return iReturnVal;
	}

	public String GetSCardDevNativeLibVersion() {
		String strReturnVal = "";
		try {
			MyFormatter.strSourceMethodName = "GetSCardDevNativeLibVersion";
			WriteLog(Level.INFO, "DeviceType: " + objDeviceType.toString());

			if (objDeviceType == Device.CardDispenser) {
				strReturnVal = GlobalMembers.strSCardNativeLibVersion;
			} else if (objDeviceType == Device.CardReaderWriter) {
				strReturnVal = GlobalMembers.strSCardReaderNativeLibVersion;
			}
		} catch (Exception e2) {
			strReturnVal = "00.00.00";
			WriteLog(Level.WARNING, e2.getMessage());
			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}
		WriteLog(Level.FINEST, "Return value- " + strReturnVal);

		return strReturnVal;
	}

	public String GetSCardDevFWVersion() {
		String strReturnVal = "";
		try {
			MyFormatter.strSourceMethodName = "GetSCardDevFWVersion";
			WriteLog(Level.INFO, "DeviceType: " + objDeviceType.toString());

			if (objDeviceType == Device.CardDispenser) {
				if (serialPort != null) {
					WriteLog(Level.FINEST, "Class object initialized");

					byte[] byResponse = null;

					byte[] byCmd = new byte[] { STX, 0x00, 0x00, 0x03, CMT, (byte) 0xA4, 0x30, ETX, CRC };
					byCmd[byCmd.length - 1] = (byte) GetBCC(byCmd, byCmd.length - 1);

					WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmd);

					if (serialPort.writeBytes(byCmd)) {
						WriteLog(Level.FINEST, "bytes written on port");

						byte[] byRes1 = serialPort.readBytes(5, 5000);
						WriteLog(Level.FINEST, "5 bytes read");

						int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
						WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

						byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, 5000);
						WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

						byResponse = new byte[byRes1.length + byRes2.length];
						System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
						System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);

						WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
						// Thread.sleep(500);
						// byResponse = serialPort.readBytes();

						serialPort.writeByte(ACK);

						if (byResponse[0] == ACK) {
							WriteLog(Level.WARNING, "Ack response received");

							if (byResponse[ResType_Pos] == PosRes) {
								WriteLog(Level.FINEST, "Positive response received");
								switch (byResponse[st0_Pos]) {
								case '0':
									WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
									break;
								case '1':
									WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
									break;
								case '2':
									WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
									break;
								}
								switch (byResponse[st1_Pos]) {
								case '0':
									WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
									break;
								case '1':
									WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
									break;
								case '2':
									WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
									break;
								}
								switch (byResponse[st2_Pos]) {
								case '0':
									WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
									break;
								case '1':
									WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
									break;
								}

								byte[] byRes = Arrays.copyOfRange(byResponse, 11, byResponse.length - 2);
								strReturnVal = new String(byRes);
								WriteLog(Level.WARNING, "Version read- " + strReturnVal);
							} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
								WriteLog(Level.FINEST, "Negative response received");
								strReturnVal = "00.00.00";
								WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
							} else {
								WriteLog(Level.FINEST, "Other response received");
								strReturnVal = "00.00.00";
							}
						} else if (byResponse[0] == NACK && byResponse.length >= 10) {
							WriteLog(Level.WARNING, "Nack response received");
							strReturnVal = "00.00.00";
							WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
						} else {
							strReturnVal = "00.00.00";
							WriteLog(Level.WARNING, "Unknown error");
						}
					} else {
						strReturnVal = "00.00.00";
						WriteLog(Level.WARNING, "Command send fail on serial port");
					}
				} else {
					strReturnVal = "00.00.00";
					WriteLog(Level.WARNING, "Serial class object not created");
				}
			} else if (objDeviceType == Device.CardReaderWriter) {
				strReturnVal = "00.00.00";
			}
		} catch (Exception e2) {
			strReturnVal = "00.00.00";

			WriteLog(Level.WARNING, e2.getMessage());

			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}
		WriteLog(Level.FINEST, "Return value- " + strReturnVal);

		return strReturnVal;
	}

	public String GetSCardReaderFWVersion() {
		String strReturnVal = "";
		try {
			MyFormatter.strSourceMethodName = "GetSCardReaderFWVersion";
			WriteLog(Level.INFO, "DeviceType: " + objDeviceType.toString());

			if (objDeviceType == Device.CardDispenser) {
				if (serialPort != null) {
					WriteLog(Level.FINEST, "Class object initialized");
					byte[] byResponse = null;

					byte[] byCmd = new byte[] { STX, 0x00, 0x00, 0x03, CMT, (byte) 0xA4, 0x32, ETX, CRC };
					byCmd[byCmd.length - 1] = (byte) GetBCC(byCmd, byCmd.length - 1);

					WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmd);

					if (serialPort.writeBytes(byCmd)) {
						WriteLog(Level.FINEST, "bytes written on port");

						byte[] byRes1 = serialPort.readBytes(5, 5000);
						WriteLog(Level.FINEST, "5 bytes read");

						int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
						WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

						byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, 5000);
						WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

						byResponse = new byte[byRes1.length + byRes2.length];
						System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
						System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);

						// Thread.sleep(500);
						// byResponse = serialPort.readBytes();

						WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);

						serialPort.writeByte(ACK);

						if (byResponse[0] == ACK) {
							WriteLog(Level.WARNING, "Ack response received");
							
							if (byResponse[ResType_Pos] == PosRes) {
								WriteLog(Level.FINEST, "Positive response received");
								switch (byResponse[st0_Pos]) {
								case '0':
									WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
									break;
								case '1':
									WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
									break;
								case '2':
									WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
									break;
								}
								switch (byResponse[st1_Pos]) {
								case '0':
									WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
									break;
								case '1':
									WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
									break;
								case '2':
									WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
									break;
								}
								switch (byResponse[st2_Pos]) {
								case '0':
									WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
									break;
								case '1':
									WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
									break;
								}
	
								byte[] byRes = Arrays.copyOfRange(byResponse, 11, byResponse.length - 2);
								strReturnVal = new String(byRes);
								WriteLog(Level.WARNING, "Version read- " + strReturnVal);
							} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
								WriteLog(Level.FINEST, "Negative response received");
								strReturnVal = "00.00.00";
								WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
							} else {
								WriteLog(Level.FINEST, "Other response received");
								strReturnVal = "00.00.00";
							}
						} else if (byResponse[0] == NACK && byResponse.length >= 10) {
							WriteLog(Level.WARNING, "Nack response received");
							strReturnVal = "00.00.00";
							WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
						} else {
							strReturnVal = "00.00.00";
							WriteLog(Level.WARNING, "Unknown error");
						}
					} else {
						strReturnVal = "00.00.00";
						WriteLog(Level.WARNING, "Command send fail on serial port");
					}
				} else {
					strReturnVal = "00.00.00";
					WriteLog(Level.WARNING, "Serial class object not created");
				}
			} else if (objDeviceType == Device.CardReaderWriter) {
				if (serialPort_CardRW != null) {
					int i = 0;

					java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
					java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
					java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
					java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
					java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
					java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

					Cm_CardRW = 0x41;
					Pm_CardRW = 0x31; // DD 52 check sum
					St0_CardRW[0] = St1_CardRW[0] = 0;
					TxDataLen_CardRW = 0;
					RxDataLen_CardRW[0] = 0;

					i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
							St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, 5000);
					if (i == 0) {
						if (ReType_CardRW[0] == 0x50) {
							WriteLog(Level.WARNING, "Ack response received");
							byte[] byRes = Arrays.copyOfRange(RxData_CardRW, 0, RxDataLen_CardRW[0]);
							strReturnVal = new String(byRes);// successfully
							WriteLog(Level.WARNING, "Version read- " + strReturnVal);
						} else {
							WriteLog(Level.WARNING, "Ack response not received");
							strReturnVal = "00.00.00";
						}
					} else {
						strReturnVal = "00.00.00";
						WriteLog(Level.WARNING, "Unknown error");
					}
				} else {
					strReturnVal = "00.00.00";
					WriteLog(Level.WARNING, "Serial class object not created");
				}
			}
		} catch (Exception e2) {
			strReturnVal = "00.00.00";

			WriteLog(Level.WARNING, e2.getMessage());

			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}
		WriteLog(Level.FINEST, "Return value- " + strReturnVal);

		return strReturnVal;
	}

	public byte[] GetDeviceStatus(int ComponentId, int Timeout) {
		byte[] byReturnVal = new byte[11];
		boolean bProceed = true;

		try {
			MyFormatter.strSourceMethodName = "GetDeviceStatus";
			WriteLog(Level.INFO, "DeviceType: " + objDeviceType.toString() + "; ComponentId:" + ComponentId
					+ "; Timeout: " + Timeout);

			if (ComponentId < 0 || ComponentId > 5) {
				byReturnVal[0] = 31;
				bProceed = false;
				WriteLog(Level.CONFIG, "ComponentId is not valid");
			}

			if (Timeout < 100) {
				Timeout = 100;
				WriteLog(Level.FINE, "Default timeout set");
			}

			if (objDeviceType == Device.CardDispenser) {
				if (bProceed) {
					if (serialPort != null) {
						WriteLog(Level.FINEST, "Class object initialized");

						byte[] byResponse = null;

						byte[] byCmd = new byte[] { STX, 0x00, 0x00, 0x03, CMT, (byte) 0x31, 0x31, ETX, CRC };
						byCmd[byCmd.length - 1] = (byte) GetBCC(byCmd, byCmd.length - 1);

						WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmd);

						if (serialPort.writeBytes(byCmd)) {
							WriteLog(Level.FINEST, "bytes written on port");

							byte[] byRes1 = serialPort.readBytes(5, Timeout);
							WriteLog(Level.FINEST, "5 bytes read");

							int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
							WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

							byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
							WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

							byResponse = new byte[byRes1.length + byRes2.length];
							System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
							System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);

							WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);

							// Thread.sleep(500);
							// byResponse = serialPort.readBytes();

							serialPort.writeByte(ACK);

							if (byResponse[0] == ACK) {
								WriteLog(Level.WARNING, "Ack response received");
								
								if (byResponse[ResType_Pos] == PosRes) {
									WriteLog(Level.FINEST, "Positive response received");
									switch (byResponse[st0_Pos]) // channel status
									{
									case '0':
										byReturnVal[7] = 0;
										WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
										break;
									case '1':
										byReturnVal[7] = 1;
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
										break;
									case '2':
										byReturnVal[7] = 1;
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
										break;
									}
									switch (byResponse[st1_Pos]) // stacker status
									{
									case '0':
										byReturnVal[3] = 0;
										WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
										break;
									case '1':
										byReturnVal[3] = 1;
										WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
										break;
									case '2':
										byReturnVal[3] = 3;
										WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
										break;
									}
									switch (byResponse[st2_Pos]) // rejection bin status
									{
									case '0':
										byReturnVal[5] = 0;
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
										break;
									case '1':
										byReturnVal[5] = 3;
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
										break;
									}
									
									//sensor status position starts from 11
									int iSensorStatusStartPos = st2_Pos + 1;
									
									// channel sensor status
									byReturnVal[8] = 0;
									if(byResponse.length >= 21)
									{
										if(byResponse[iSensorStatusStartPos] == 0x31)
										{
											byReturnVal[8] = (byte)(byReturnVal[8] | 0x01);
										}
										if(byResponse[iSensorStatusStartPos + 1] == 0x31)
										{
											byReturnVal[8] = (byte)(byReturnVal[8] | 0x02);
										}
										if(byResponse[iSensorStatusStartPos + 2] == 0x31)
										{
											byReturnVal[8] = (byte)(byReturnVal[8] | 0x04);
										}
										if(byResponse[iSensorStatusStartPos + 3] == 0x31)
										{
											byReturnVal[8] = (byte)(byReturnVal[8] | 0x08);
										}
										
										//sensor position iSensorStatusStartPos + 4 is reserved
										
										if(byResponse[iSensorStatusStartPos + 5] == 0x31)
										{
											byReturnVal[8] = (byte)(byReturnVal[8] | 0x10);
										}
										if(byResponse[iSensorStatusStartPos + 6] == 0x31)
										{
											byReturnVal[8] = (byte)(byReturnVal[8] | 0x20);
										}
										if(byResponse[iSensorStatusStartPos + 7] == 0x31)
										{
											byReturnVal[8] = (byte)(byReturnVal[8] | 0x40);
										}
										if(byResponse[iSensorStatusStartPos + 8] == 0x31)
										{
											byReturnVal[8] = (byte)(byReturnVal[8] | 0x80);
										}
									}
	
									byReturnVal[0] = 0;
	
									// RFID reader status
									byReturnVal[1] = 0;
	
									// SAM reader status
									byReturnVal[2] = 0;
	
									// card count in stacker
									byReturnVal[4] = 0;
	
									// card count in rejection bin
									byReturnVal[6] = 0;
	
									// collection bin status
									byReturnVal[9] = 0;
	
									// card count in collection bin
									byReturnVal[10] = 0;
								} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
									WriteLog(Level.FINEST, "Negative response received");
									byReturnVal[0] = 31;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									WriteLog(Level.FINEST, "Other response received");
									byReturnVal[0] = 31;
								}
							} else if (byResponse[0] == NACK && byResponse.length >= 10) {
								WriteLog(Level.WARNING, "Nack response received");
								byReturnVal[1] = 1;
								WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
							} else {
								byReturnVal[0] = 31;
								WriteLog(Level.WARNING, "Unknown error");
							}
						} else {
							byReturnVal[0] = 28;
							WriteLog(Level.WARNING, "Command send fail on serial port");
						}
					} else {
						byReturnVal[0] = 28;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			} else if (objDeviceType == Device.CardReaderWriter) {
				if (bProceed) {
					if (serialPort_CardRW != null) {
						int i = 0;

						java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

						Cm_CardRW = 0x31;
						Pm_CardRW = 0x31; // DD 52 check sum
						St0_CardRW[0] = St1_CardRW[0] = 0;
						TxDataLen_CardRW = 0;
						RxDataLen_CardRW[0] = 0;

						i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
								St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
						if (i == 0) {
							if (ReType_CardRW[0] == 0x50) {
								WriteLog(Level.WARNING, "Ack response received");
								// byte[] byRes = Arrays.copyOfRange(RxData_CardRW, 0, RxDataLen_CardRW[0]);

								if (RxDataLen_CardRW[0] > 0) {
									byReturnVal[0] = 0;
								} else {
									byReturnVal[0] = 29;
								}

								// RFID reader status
								byReturnVal[1] = 0;

								// SAM reader status
								byReturnVal[2] = 0;

								// stacker
								byReturnVal[3] = 0;

								// card count in stacker
								byReturnVal[4] = 0;

								// rejection bin
								byReturnVal[5] = 0;

								// card count in rejection bin
								byReturnVal[6] = 0;

								// checking channel status
								if ((RxData_CardRW[0] & 0x0F) != 0x00) {
									byReturnVal[7] = 1;
								} else {
									byReturnVal[7] = 0;
								}

								// channel sensor status
								byReturnVal[8] = 0;
								
								java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
								java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
								java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
								java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
								java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
								java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

								Cm_CardRW = 0x31;
								Pm_CardRW = 0x40; // DD 52 check sum
								St0_CardRW[0] = St1_CardRW[0] = 0;
								TxDataLen_CardRW = 0;
								RxDataLen_CardRW[0] = 0;

								//read and fill channel sensor status
								i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
										St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
								if (i == 0) {
									if (ReType_CardRW[0] == 0x50) {
										WriteLog(Level.WARNING, "Ack response received");
										// byte[] byRes = Arrays.copyOfRange(RxData_CardRW, 0, RxDataLen_CardRW[0]);

										if (RxDataLen_CardRW[0] > 0) {
											if(RxData_CardRW[0] == 0x31)	//PSS1
											{
												byReturnVal[8] = (byte)(byReturnVal[8] | 0x01);
											}
											if(RxData_CardRW[1] == 0x31)	//PSS2
											{
												byReturnVal[8] = (byte)(byReturnVal[8] | 0x02);
											}
											if(RxData_CardRW[2] == 0x31)	//PSS3
											{
												byReturnVal[8] = (byte)(byReturnVal[8] | 0x04);
											}
											if(RxData_CardRW[3] == 0x31)	//PSS4
											{
												byReturnVal[8] = (byte)(byReturnVal[8] | 0x08);
											}
											if(RxData_CardRW[4] == 0x31)	//PSS5
											{
												byReturnVal[8] = (byte)(byReturnVal[8] | 0x10);
											}
											if(RxData_CardRW[5] == 0x31)	//KSW
											{
												byReturnVal[8] = (byte)(byReturnVal[8] | 0x20);
											}
										}										
										
									} else {
										WriteLog(Level.WARNING, "Ack response not received");
										byReturnVal[1] = 1;
									}
								} else {
									byReturnVal[0] = 31;
									WriteLog(Level.WARNING, "Unknown error");
								}

								// collection bin status
								byReturnVal[9] = 0;

								// card count in collection bin
								byReturnVal[10] = 0;
							} else {
								WriteLog(Level.WARNING, "Ack response not received");
								byReturnVal[1] = 1;
							}
						} else {
							byReturnVal[0] = 31;
							WriteLog(Level.WARNING, "Unknown error");
						}
					} else {
						byReturnVal[0] = 28;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			}
		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				byReturnVal[0] = 18;
			} else {
				byReturnVal[0] = 31;
			}

			WriteLog(Level.WARNING, e2.getMessage());

			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}
		WriteLog(Level.FINEST, "Return value- " + byReturnVal);
		return byReturnVal;
	}


	public int EnableCardAcceptance(int Timeout) {
		int iReturnVal = -1;
		boolean bProceed = true;

		try {
			MyFormatter.strSourceMethodName = "EnableCardAcceptance";
			WriteLog(Level.INFO, "DeviceType: " + objDeviceType.toString() + "; Timeout: " + Timeout);

			if (bConnected == false) {
				iReturnVal = 20;
				bProceed = false;
				WriteLog(Level.INFO, "Device not yet connected");
			}

			if (Timeout < 100) {
				Timeout = 100;
				WriteLog(Level.FINE, "Default timeout set");
			}

			if (objDeviceType == Device.CardDispenser) {
				if (bProceed) {

					iReturnVal = 31;
					WriteLog(Level.INFO, "Device dont accept cards");

					/*
					 * if (serialPort != null) { WriteLog(Level.FINEST, "Class object initialized");
					 * 
					 * byte[] byResponse = null;
					 * 
					 * // Enable card acceptance byte[] byCmd = new byte[] { STX, 0x00, 0x00, 0x03,
					 * CMT, 0x33, 0x30, ETX, CRC }; byCmd[byCmd.length - 1] = (byte) GetBCC(byCmd,
					 * byCmd.length - 1);
					 * 
					 * if (serialPort.writeBytes(byCmd)) { WriteLog(Level.FINEST,
					 * "bytes written on port");
					 * 
					 * byte[] byRes1 = serialPort.readBytes(5, Timeout); WriteLog(Level.FINEST,
					 * "5 bytes read");
					 * 
					 * int iPendingDataLen = (byRes1[3] * 256) + byRes1[4]; WriteLog(Level.FINEST,
					 * "Data length received- " + iPendingDataLen);
					 * 
					 * byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
					 * WriteLog(Level.FINEST, iPendingDataLen + " bytes read");
					 * 
					 * byResponse = new byte[byRes1.length + byRes2.length];
					 * System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
					 * System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
					 * 
					 * // Thread.sleep(500); // byResponse = serialPort.readBytes();
					 * 
					 * serialPort.writeByte(ACK);
					 * 
					 * if (byResponse[0] == ACK) { WriteLog(Level.WARNING, "Ack response received");
					 * switch (byResponse[st0_Pos]) // channel status { case '0': iReturnVal = 0;
					 * WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString()); break;
					 * case '1': iReturnVal = 2; WriteLog(Level.INFO, "CardPos- " +
					 * ST0.One_card_in_gate.toString()); break; case '2': iReturnVal = 1;
					 * WriteLog(Level.INFO, "CardPos- " +
					 * ST0.One_card_on_RF_IC_position.toString()); break; }
					 * 
					 * switch(byResponse[st1_Pos]) //stacker status { case '0': byReturnVal[3] = 0;
					 * System.out.println(ST1.No_card_in_stacker); break; case '1': byReturnVal[3] =
					 * 1; System.out.println(ST1.Few_card_in_stacker); break; case '2':
					 * byReturnVal[3] = 3; System.out.println(ST1.Enough_cards_in_card_box); break;
					 * } switch(byResponse[st2_Pos]) //rejection bin status { case '0':
					 * byReturnVal[5] = 0; System.out.println(ST2.Error_card_bin_not_full); break;
					 * case '1': byReturnVal[5] = 3; System.out.println(ST2.Error_card_bin_full);
					 * break; }
					 * 
					 * } else if (byResponse[0] == NACK && byResponse.length >= 10) {
					 * WriteLog(Level.WARNING, "Nack response received"); iReturnVal = 1;
					 * WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
					 * } else { iReturnVal = 31; WriteLog(Level.WARNING, "Unknown error"); } } else
					 * { iReturnVal = 28; WriteLog(Level.WARNING,
					 * "Command send fail on serial port"); } } else { iReturnVal = 20;
					 * WriteLog(Level.WARNING, "Serial class object not created"); }
					 */
				}
			} else if (objDeviceType == Device.CardReaderWriter) {
				if (bProceed) {
					if (serialPort_CardRW != null) {
						int i = 0;

						java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

						Cm_CardRW = 0x3A;
						Pm_CardRW = 0x30; // DD 52 check sum
						St0_CardRW[0] = St1_CardRW[0] = 0;
						TxDataLen_CardRW = 1;

						// Enable card entry
						TxData_CardRW[0] = 0x30;

						RxDataLen_CardRW[0] = 0;

						i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
								St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
						if (i == 0) {
							if (ReType_CardRW[0] == 0x50) {
								WriteLog(Level.WARNING, "Ack response received");
								iReturnVal = 0;// successfully
							} else {
								WriteLog(Level.WARNING, "Ack response not received");
								iReturnVal = 28;// communication failure
							}
						} else {
							WriteLog(Level.WARNING, "Unknown error");
							iReturnVal = i;// other error
						}
					} else {
						iReturnVal = 28;// communication failure
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			}
		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				iReturnVal = 18;
			} else {
				iReturnVal = 31;
			}

			WriteLog(Level.WARNING, e2.getMessage());

			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}
		WriteLog(Level.FINEST, "Return value- " + iReturnVal);
		return iReturnVal;
	}

	public int DisableCardAcceptance(int Timeout) {
		int iReturnVal = -1;
		boolean bProceed = true;

		try {
			MyFormatter.strSourceMethodName = "DisableCardAcceptance";
			WriteLog(Level.INFO, "DeviceType: " + objDeviceType.toString() + "; Timeout: " + Timeout);

			if (bConnected == false) {
				iReturnVal = 20;
				bProceed = false;
				WriteLog(Level.INFO, "Device not yet connected");
			}

			if (Timeout < 100) {
				Timeout = 100;
				WriteLog(Level.FINE, "Default timeout set");
			}

			if (objDeviceType == Device.CardDispenser) {
				if (bProceed) {
					if (serialPort != null) {
						WriteLog(Level.FINEST, "Class object initialized");

						byte[] byResponse = null;

						// Disable card acceptance
						byte[] byCmd = new byte[] { STX, 0x00, 0x00, 0x03, CMT, 0x33, 0x31, ETX, CRC };
						byCmd[byCmd.length - 1] = (byte) GetBCC(byCmd, byCmd.length - 1);

						WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmd);

						if (serialPort.writeBytes(byCmd)) {
							WriteLog(Level.FINEST, "bytes written on port");

							byte[] byRes1 = serialPort.readBytes(5, Timeout);
							WriteLog(Level.FINEST, "5 bytes read");

							int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
							WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

							byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
							WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

							byResponse = new byte[byRes1.length + byRes2.length];
							System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
							System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);

							// Thread.sleep(500);
							// byResponse = serialPort.readBytes();

							WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);

							serialPort.writeByte(ACK);

							if (byResponse[0] == ACK) {
								WriteLog(Level.WARNING, "Ack response received");
								
								if (byResponse[ResType_Pos] == PosRes) {
									WriteLog(Level.FINEST, "Positive response received");
									iReturnVal = 0;
									/*
									 * switch(byResponse[st0_Pos]) //channel status { case '0': iReturnVal = 0;
									 * System.out.println(ST0.No_card_in_CRT_571); break; case '1': iReturnVal = 2;
									 * System.out.println(ST0.One_card_in_gate); break; case '2': iReturnVal = 1;
									 * System.out.println(ST0.One_card_on_RF_IC_position); break; }
									 * switch(byResponse[st1_Pos]) //stacker status { case '0': byReturnVal[3] = 0;
									 * System.out.println(ST1.No_card_in_stacker); break; case '1': byReturnVal[3] =
									 * 1; System.out.println(ST1.Few_card_in_stacker); break; case '2':
									 * byReturnVal[3] = 3; System.out.println(ST1.Enough_cards_in_card_box); break;
									 * } switch(byResponse[st2_Pos]) //rejection bin status { case '0':
									 * byReturnVal[5] = 0; System.out.println(ST2.Error_card_bin_not_full); break;
									 * case '1': byReturnVal[5] = 3; System.out.println(ST2.Error_card_bin_full);
									 * break; }
									 */
								} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
									WriteLog(Level.FINEST, "Negative response received");
									iReturnVal = 28;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									WriteLog(Level.FINEST, "Other response received");
									iReturnVal = 28;
								}

							} else if (byResponse[0] == NACK && byResponse.length >= 10) {
								WriteLog(Level.WARNING, "Nack response received");
								iReturnVal = 1;
								WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
							} else {
								iReturnVal = 31;
								WriteLog(Level.WARNING, "Unknown error");
							}
						} else {
							iReturnVal = 28;
							WriteLog(Level.WARNING, "Command send fail on serial port");
						}
					} else {
						iReturnVal = 20;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			} else if (objDeviceType == Device.CardReaderWriter) {
				if (bProceed) {
					if (serialPort_CardRW != null) {
						int i = 0;

						java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

						Cm_CardRW = 0x3A;
						Pm_CardRW = 0x30; // DD 52 check sum
						St0_CardRW[0] = St1_CardRW[0] = 0;
						TxDataLen_CardRW = 1;

						// Disable card entry
						TxData_CardRW[0] = 0x31;

						RxDataLen_CardRW[0] = 0;

						i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
								St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
						if (i == 0) {
							if (ReType_CardRW[0] == 0x50) {
								WriteLog(Level.WARNING, "Ack response received");
								iReturnVal = 0;// successfully
							} else {
								WriteLog(Level.WARNING, "Ack response not received");
								iReturnVal = 28;// communication failure
							}
						} else {
							WriteLog(Level.WARNING, "Unknown error");
							iReturnVal = i;// other error
						}
					} else {
						iReturnVal = 28;// communication failure
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			}
		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				iReturnVal = 18;
			} else {
				iReturnVal = 31;
			}

			WriteLog(Level.WARNING, e2.getMessage());

			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}

		WriteLog(Level.FINEST, "Return value- " + iReturnVal);
		return iReturnVal;
	}

	public int AcceptCard(int Timeout) {
		int iReturnVal = -1;
		boolean bProceed = true;

		try {
			MyFormatter.strSourceMethodName = "AcceptCard";
			WriteLog(Level.INFO, "DeviceType: " + objDeviceType.toString() + "; Timeout: " + Timeout);

			if (bConnected == false) {
				iReturnVal = 20;
				bProceed = false;
				WriteLog(Level.INFO, "Device not yet connected");
			}

			if (Timeout < 100) {
				Timeout = 100;
				WriteLog(Level.FINE, "Default timeout set");
			}

			if (objDeviceType == Device.CardDispenser) {
				if (bProceed) {

					iReturnVal = 31;
					WriteLog(Level.INFO, "Device dont accept cards");

					/*
					 * if (serialPort != null) { WriteLog(Level.FINEST, "Class object initialized");
					 * 
					 * byte[] byResponse = null;
					 * 
					 * // Enable card acceptance byte[] byCmd = new byte[] { STX, 0x00, 0x00, 0x03,
					 * CMT, 0x33, 0x30, ETX, CRC }; byCmd[byCmd.length - 1] = (byte) GetBCC(byCmd,
					 * byCmd.length - 1);
					 * 
					 * if (serialPort.writeBytes(byCmd)) { WriteLog(Level.FINEST,
					 * "bytes written on port");
					 * 
					 * byte[] byRes1 = serialPort.readBytes(5, Timeout); WriteLog(Level.FINEST,
					 * "5 bytes read");
					 * 
					 * int iPendingDataLen = (byRes1[3] * 256) + byRes1[4]; WriteLog(Level.FINEST,
					 * "Data length received- " + iPendingDataLen);
					 * 
					 * byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
					 * WriteLog(Level.FINEST, iPendingDataLen + " bytes read");
					 * 
					 * byResponse = new byte[byRes1.length + byRes2.length];
					 * System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
					 * System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
					 * 
					 * // Thread.sleep(500); // byResponse = serialPort.readBytes();
					 * 
					 * serialPort.writeByte(ACK);
					 * 
					 * if (byResponse[0] == ACK) { WriteLog(Level.WARNING, "Ack response received");
					 * switch (byResponse[st0_Pos]) // channel status { case '0': iReturnVal = 0;
					 * WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString()); break;
					 * case '1': iReturnVal = 2; WriteLog(Level.INFO, "CardPos- " +
					 * ST0.One_card_in_gate.toString()); break; case '2': iReturnVal = 1;
					 * WriteLog(Level.INFO, "CardPos- " +
					 * ST0.One_card_on_RF_IC_position.toString()); break; }
					 * 
					 * switch(byResponse[st1_Pos]) //stacker status { case '0': byReturnVal[3] = 0;
					 * System.out.println(ST1.No_card_in_stacker); break; case '1': byReturnVal[3] =
					 * 1; System.out.println(ST1.Few_card_in_stacker); break; case '2':
					 * byReturnVal[3] = 3; System.out.println(ST1.Enough_cards_in_card_box); break;
					 * } switch(byResponse[st2_Pos]) //rejection bin status { case '0':
					 * byReturnVal[5] = 0; System.out.println(ST2.Error_card_bin_not_full); break;
					 * case '1': byReturnVal[5] = 3; System.out.println(ST2.Error_card_bin_full);
					 * break; }
					 * 
					 * } else if (byResponse[0] == NACK && byResponse.length >= 10) {
					 * WriteLog(Level.WARNING, "Nack response received"); iReturnVal = 1;
					 * WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
					 * } else { iReturnVal = 31; WriteLog(Level.WARNING, "Unknown error"); } } else
					 * { iReturnVal = 28; WriteLog(Level.WARNING,
					 * "Command send fail on serial port"); } } else { iReturnVal = 20;
					 * WriteLog(Level.WARNING, "Serial class object not created"); }
					 */
				}
			} else if (objDeviceType == Device.CardReaderWriter) {
				if (bProceed) {
					if (serialPort_CardRW != null) {
						int i = 0;

						java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

						Cm_CardRW = 0x3A;
						Pm_CardRW = 0x30; // DD 52 check sum
						St0_CardRW[0] = St1_CardRW[0] = 0;
						TxDataLen_CardRW = 1;

						// Enable card entry
						TxData_CardRW[0] = 0x30;

						RxDataLen_CardRW[0] = 0;

						i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
								St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
						if (i == 0) {
							if (ReType_CardRW[0] == 0x50) {
								WriteLog(Level.WARNING, "Ack response received");

								Thread.sleep(Timeout);

								int ret = IsCardInChannel(Timeout);
								if (ret == 1) {
									iReturnVal = 0;// successfully
								} else {
									iReturnVal = 18;// Timeout
									DisableCardAcceptance(Timeout);
								}
							} else {
								WriteLog(Level.WARNING, "Ack response not received");
								iReturnVal = 28;// communication failure
							}
						} else {
							WriteLog(Level.WARNING, "Unknown error");
							iReturnVal = i;// other error
						}
					} else {
						iReturnVal = 28;// communication failure
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			}
		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				iReturnVal = 18;
			} else {
				iReturnVal = 31;
			}

			WriteLog(Level.WARNING, e2.getMessage());

			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}

		WriteLog(Level.FINEST, "Return value- " + iReturnVal);
		return iReturnVal;
	}

	public int DispenseCard(int Timeout) {
		int iReturnVal = -1;
		boolean bProceed = true;

		try {
			MyFormatter.strSourceMethodName = "DispenseCard";
			WriteLog(Level.INFO, "DeviceType: " + objDeviceType.toString() + "; Timeout: " + Timeout);

			if (bConnected == false) {
				iReturnVal = 20;
				bProceed = false;
				WriteLog(Level.INFO, "Device not yet connected");
			}

			if (Timeout < 100) {
				Timeout = 100;
				WriteLog(Level.FINE, "Default timeout set");
			}

			if (objDeviceType == Device.CardDispenser) {
				if (bProceed) {
					if (serialPort != null) {
						WriteLog(Level.FINEST, "Class object initialized");

						byte[] byResponse = null;

						// Dispense card
						byte[] byCmd = new byte[] { STX, 0x00, 0x00, 0x03, CMT, 0x32, 0x32, ETX, CRC };
						byCmd[byCmd.length - 1] = (byte) GetBCC(byCmd, byCmd.length - 1);

						WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmd);

						if (serialPort.writeBytes(byCmd)) {
							WriteLog(Level.FINEST, "bytes written on port");

							byte[] byRes1 = serialPort.readBytes(5, Timeout);
							WriteLog(Level.FINEST, "5 bytes read");

							int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
							WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

							byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
							WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

							byResponse = new byte[byRes1.length + byRes2.length];
							System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
							System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);

							WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
							// Thread.sleep(500);
							// byResponse = serialPort.readBytes();

							serialPort.writeByte(ACK);

							if (byResponse[0] == ACK) {
								WriteLog(Level.WARNING, "Ack response received");
								
								if (byResponse[ResType_Pos] == PosRes) {
									WriteLog(Level.FINEST, "Positive response received");
									switch (byResponse[st0_Pos]) // channel status
									{
									case '0':
										iReturnVal = 31;
										WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
										break;
									case '1':
										iReturnVal = 31;
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
										break;
									case '2':
										iReturnVal = 0;
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
										break;
									}
									switch (byResponse[st1_Pos]) // stacker status
									{
									case '0':
										iReturnVal = 3;
										WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
										break;
									case '2':
										WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
										break;
									}
									/*
									 * switch(byResponse[st2_Pos]) //rejection bin status { case '0': byReturnVal[5]
									 * = 0; System.out.println(ST2.Error_card_bin_not_full); break; case '1':
									 * byReturnVal[5] = 3; System.out.println(ST2.Error_card_bin_full); break; }
									 */
								} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
									WriteLog(Level.FINEST, "Negative response received");
									iReturnVal = 28;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									WriteLog(Level.FINEST, "Other response received");
									iReturnVal = 28;
								}
							} else if (byResponse[0] == NACK && byResponse.length >= 10) {
								WriteLog(Level.WARNING, "Nack response received");
								iReturnVal = 31;
								WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
							} else {
								iReturnVal = 31;
								WriteLog(Level.WARNING, "Unknown error");
							}
						} else {
							iReturnVal = 28;
							WriteLog(Level.WARNING, "Command send fail on serial port");
						}
					} else {
						iReturnVal = 20;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			} else if (objDeviceType == Device.CardReaderWriter) {
				if (bProceed) {

					iReturnVal = 31;
					WriteLog(Level.INFO, "Device dont dispense cards from stacker");
				}
			}
		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				iReturnVal = 18;
			} else {
				iReturnVal = 31;
			}

			WriteLog(Level.WARNING, e2.getMessage());

			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}

		WriteLog(Level.FINEST, "Return value- " + iReturnVal);
		return iReturnVal;
	}

	public int ReturnCard(int DispenseMode, int Timeout) {
		int iReturnVal = -1;
		boolean bProceed = true;

		try {
			MyFormatter.strSourceMethodName = "ReturnCard";
			WriteLog(Level.INFO, "DeviceType: " + objDeviceType.toString() + "; DispenseMode: " + DispenseMode
					+ "; Timeout: " + Timeout);

			if (bConnected == false) {
				iReturnVal = 20;
				bProceed = false;
				WriteLog(Level.INFO, "Device not yet connected");
			} else if (DispenseMode < 0 || DispenseMode > 1) {
				iReturnVal = 31;
				bProceed = false;
				// System.out.println("DispenseMode not valid");
			}

			if (Timeout < 100) {
				Timeout = 100;
				WriteLog(Level.FINE, "Default timeout set");
			}

			if (objDeviceType == Device.CardDispenser) {
				if (bProceed) {
					if (serialPort != null) {
						WriteLog(Level.FINEST, "Class object initialized");

						byte[] byResponse = null;

						byte[] byCmdSensorStatus = new byte[] { STX, 0x00, 0x00, 0x03, CMT, 0x31, 0x31, ETX, CRC };
						byCmdSensorStatus[byCmdSensorStatus.length - 1] = (byte) GetBCC(byCmdSensorStatus,
								byCmdSensorStatus.length - 1);
						WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmdSensorStatus);
						if (serialPort.writeBytes(byCmdSensorStatus)) {
							WriteLog(Level.FINEST, "bytes written on port");

							byte[] byRes1 = serialPort.readBytes(5, Timeout);
							WriteLog(Level.FINEST, "5 bytes read");

							int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
							WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

							byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
							WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

							byResponse = new byte[byRes1.length + byRes2.length];
							System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
							System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
							WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
							// Thread.sleep(500);
							// byResponse = serialPort.readBytes();

							serialPort.writeByte(ACK);

							if (byResponse[0] == ACK) {
								WriteLog(Level.WARNING, "Ack response received");
								
								if (byResponse[ResType_Pos] == PosRes) {
									WriteLog(Level.FINEST, "Positive response received");
									switch (byResponse[st0_Pos]) // channel status
									{
									case '0':
										WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
										break;
									case '2':
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
										break;
									}
									switch (byResponse[st1_Pos]) // stacker status
									{
									case '0':
										WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
										break;
									case '2':
										WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
										break;
									}
									switch (byResponse[st2_Pos]) // rejection bin status
									{
									case '0':
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
										break;
									}
	
									boolean bCardFound = false;
									for (int iPos = 11; iPos <= 17; iPos++) {
										if (byResponse[iPos] == 0x31) {
											bCardFound = true;
											WriteLog(Level.FINEST, "Card found on sensor position- " + (iPos - 10));
											break;
										}
									}
	
									if (bCardFound == false) {
										iReturnVal = 2; // No card in the channel
										WriteLog(Level.WARNING, "Card not found in channel");
										bProceed = false;
									}
								} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
									WriteLog(Level.FINEST, "Negative response received");
									iReturnVal = 28;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									WriteLog(Level.FINEST, "Other response received");
									iReturnVal = 28;
								}
							} else if (byResponse[0] == NACK && byResponse.length >= 10) {
								WriteLog(Level.WARNING, "Nack response received");
								iReturnVal = 31;
								bProceed = false;
								WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
							} else {
								iReturnVal = 31;
								bProceed = false;
								WriteLog(Level.WARNING, "Unknown error");
							}
						} else {
							iReturnVal = 28;
							bProceed = false;
							WriteLog(Level.WARNING, "Command send fail on serial port");
						}

						if (bProceed) {
							byte byDispense = 0x00;

							if (DispenseMode == 0)
								byDispense = 0x30;
							else if (DispenseMode == 1)
								byDispense = 0x39;

							byResponse = null;

							// Move card
							byte[] byCmd = new byte[] { STX, 0x00, 0x00, 0x03, CMT, 0x32, byDispense, ETX, CRC };
							byCmd[byCmd.length - 1] = (byte) GetBCC(byCmd, byCmd.length - 1);

							WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmd);

							if (serialPort.writeBytes(byCmd)) {
								WriteLog(Level.FINEST, "bytes written on port");

								byte[] byRes1 = serialPort.readBytes(5, Timeout);
								WriteLog(Level.FINEST, "5 bytes read");

								int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
								WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

								byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
								WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

								byResponse = new byte[byRes1.length + byRes2.length];
								System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
								System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
								WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
								// Thread.sleep(500);
								// byResponse = serialPort.readBytes();

								serialPort.writeByte(ACK);

								if (byResponse[0] == ACK) {
									WriteLog(Level.WARNING, "Ack response received");
									
									if (byResponse[ResType_Pos] == PosRes) {
										WriteLog(Level.FINEST, "Positive response received");
										switch (byResponse[st0_Pos]) // channel status
										{
										case '0':
											iReturnVal = 0;
											WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
											break;
										case '1':
											iReturnVal = 0;
											WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
											break;
										case '2':
											iReturnVal = 1;
											WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
											break;
										}
										switch (byResponse[st1_Pos]) // stacker status
										{
										case '0':
											iReturnVal = 3;
											WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
											break;
										case '1':
											WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
											break;
										case '2':
											WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
											break;
										}
										/*
										 * switch(byResponse[st2_Pos]) //rejection bin status { case '0': byReturnVal[5]
										 * = 0; System.out.println(ST2.Error_card_bin_not_full); break; case '1':
										 * byReturnVal[5] = 3; System.out.println(ST2.Error_card_bin_full); break; }
										 */
									} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
										WriteLog(Level.FINEST, "Negative response received");
										iReturnVal = 28;
										WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
									} else {
										WriteLog(Level.FINEST, "Other response received");
										iReturnVal = 28;
									}
								} else if (byResponse[0] == NACK && byResponse.length >= 10) {
									WriteLog(Level.WARNING, "Nack response received");
									iReturnVal = 31;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									iReturnVal = 31;
									WriteLog(Level.WARNING, "Unknown error");
								}
							} else {
								iReturnVal = 28;
								WriteLog(Level.WARNING, "Command send fail on serial port");
							}
						}
					} else {
						iReturnVal = 20;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			} else if (objDeviceType == Device.CardReaderWriter) {
				if (bProceed) {
					if (serialPort_CardRW != null) {
						int iRes = -1;

						java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

						Cm_CardRW = 0x31;
						Pm_CardRW = 0x31; // DD 52 check sum
						St0_CardRW[0] = St1_CardRW[0] = 0;
						TxDataLen_CardRW = 0;
						RxDataLen_CardRW[0] = 0;

						iRes = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
								St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
						if (iRes == 0) {
							if (ReType_CardRW[0] == 0x50) {
								WriteLog(Level.WARNING, "Ack response received");

								// checking channel status
								if ((RxData_CardRW[0] & 0x0F) == 0x00) {
									iReturnVal = 2; // no card in channel
									bProceed = false;
								}
							} else {
								WriteLog(Level.WARNING, "Ack response not received");
								iReturnVal = 28;// communication failure
							}
						} else {
							iReturnVal = 31;
							WriteLog(Level.WARNING, "Unknown error");
						}

						if (bProceed) {
							iRes = -1;

							java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
							java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
							java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
							java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
							java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
							java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

							Cm_CardRW = 0x33;

							if (DispenseMode == 0)
								Pm_CardRW = 0x30;
							else if (DispenseMode == 1)
								Pm_CardRW = 0x38;

							St0_CardRW[0] = St1_CardRW[0] = 0;
							TxDataLen_CardRW = 0;
							RxDataLen_CardRW[0] = 0;

							iRes = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW,
									ReType_CardRW, St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
							if (iRes == 0) {
								if (ReType_CardRW[0] == 0x50) {
									WriteLog(Level.WARNING, "Ack response received");
									iReturnVal = 0;
								} else {
									WriteLog(Level.WARNING, "Ack response not received");
									iReturnVal = 28;// communication failure
								}
							} else {
								iReturnVal = 31;
								WriteLog(Level.WARNING, "Unknown error");
							}
						}
					} else {
						iReturnVal = 28;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			}
		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				iReturnVal = 18;
			} else {
				iReturnVal = 31;
			}

			WriteLog(Level.WARNING, e2.getMessage());

			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}

		WriteLog(Level.FINEST, "Return value- " + iReturnVal);
		return iReturnVal;
	}

	public int RejectCard(int Timeout) {
		int iReturnVal = -1;
		boolean bProceed = true;

		try {
			MyFormatter.strSourceMethodName = "RejectCard";
			WriteLog(Level.INFO, "DeviceType: " + objDeviceType.toString() + "; Timeout: " + Timeout);

			if (bConnected == false) {
				iReturnVal = 20;
				bProceed = false;
				WriteLog(Level.INFO, "Device not yet connected");
			}

			if (Timeout < 100) {
				Timeout = 100;
				WriteLog(Level.FINE, "Default timeout set");
			}

			if (objDeviceType == Device.CardDispenser) {
				if (bProceed) {
					if (serialPort != null) {
						WriteLog(Level.FINEST, "Class object initialized");

						byte[] byResponse = null;

						byte[] byCmdSensorStatus = new byte[] { STX, 0x00, 0x00, 0x03, CMT, 0x31, 0x31, ETX, CRC };
						byCmdSensorStatus[byCmdSensorStatus.length - 1] = (byte) GetBCC(byCmdSensorStatus,
								byCmdSensorStatus.length - 1);
						WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmdSensorStatus);
						if (serialPort.writeBytes(byCmdSensorStatus)) {
							WriteLog(Level.FINEST, "bytes written on port");

							byte[] byRes1 = serialPort.readBytes(5, Timeout);
							WriteLog(Level.FINEST, "5 bytes read");

							int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
							WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

							byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
							WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

							byResponse = new byte[byRes1.length + byRes2.length];
							System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
							System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
							WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
							// Thread.sleep(500);
							// byResponse = serialPort.readBytes();

							serialPort.writeByte(ACK);

							if (byResponse[0] == ACK) {
								WriteLog(Level.WARNING, "Ack response received");
								
								if (byResponse[ResType_Pos] == PosRes) {
									WriteLog(Level.FINEST, "Positive response received");
									switch (byResponse[st0_Pos]) // channel status
									{
									case '0':
										WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
										break;
									case '2':
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
										break;
									}
									switch (byResponse[st1_Pos]) // stacker status
									{
									case '0':
										WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
										break;
									case '2':
										WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
										break;
									}
									switch (byResponse[st2_Pos]) // rejection bin status
									{
									case '0':
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
										break;
									}
	
									boolean bCardFound = false;
									for (int iPos = 11; iPos <= 17; iPos++) {
										if (byResponse[iPos] == 0x31) {
											bCardFound = true;
											WriteLog(Level.FINEST, "Card found on sensor position- " + (iPos - 10));
											break;
										}
									}
	
									if (bCardFound == false) {
										iReturnVal = 2; // No card in the channel
										WriteLog(Level.WARNING, "Card not found in channel");
										bProceed = false;
									}
								} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
									WriteLog(Level.FINEST, "Negative response received");
									iReturnVal = 28;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									WriteLog(Level.FINEST, "Other response received");
									iReturnVal = 28;
								}
							} else if (byResponse[0] == NACK && byResponse.length >= 10) {
								WriteLog(Level.WARNING, "Nack response received");
								iReturnVal = 31;
								bProceed = false;
								WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
							} else {
								iReturnVal = 31;
								bProceed = false;
								WriteLog(Level.WARNING, "Unknown error");
							}
						} else {
							iReturnVal = 28;
							bProceed = false;
							WriteLog(Level.WARNING, "Command send fail on serial port");
						}

						if (bProceed) {
							byResponse = null;

							// Move card to reject bin
							byte[] byCmd = new byte[] { STX, 0x00, 0x00, 0x03, CMT, 0x32, 0x33, ETX, CRC };
							byCmd[byCmd.length - 1] = (byte) GetBCC(byCmd, byCmd.length - 1);
							WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmd);
							if (serialPort.writeBytes(byCmd)) {
								WriteLog(Level.FINEST, "bytes written on port");

								byte[] byRes1 = serialPort.readBytes(5, Timeout);
								WriteLog(Level.FINEST, "5 bytes read");

								int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
								WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

								byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
								WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

								byResponse = new byte[byRes1.length + byRes2.length];
								System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
								System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
								WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
								// Thread.sleep(500);
								// byResponse = serialPort.readBytes();

								serialPort.writeByte(ACK);

								if (byResponse[0] == ACK) {
									WriteLog(Level.WARNING, "Ack response received");
									
									if (byResponse[ResType_Pos] == PosRes) {
										WriteLog(Level.FINEST, "Positive response received");
										switch (byResponse[st0_Pos]) // channel status
										{
										case '0':
											iReturnVal = 0;
											WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
											break;
										case '1':
											iReturnVal = 0;
											WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
											break;
										case '2':
											iReturnVal = 1;
											WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
											break;
										}
										switch (byResponse[st1_Pos]) // stacker status
										{
										case '0':
											iReturnVal = 3;
											WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
											break;
										case '1':
											WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
											break;
										case '2':
											WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
											break;
										}
										switch (byResponse[st2_Pos]) // rejection bin status
										{
										case '0':
											iReturnVal = 0;
											WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
											break;
										case '1':
											iReturnVal = 1;
											WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
											break;
										}
									} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
										WriteLog(Level.FINEST, "Negative response received");
										iReturnVal = 28;
										WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
									} else {
										WriteLog(Level.FINEST, "Other response received");
										iReturnVal = 28;
									}
								} else if (byResponse[0] == NACK && byResponse.length >= 10) {
									WriteLog(Level.WARNING, "Nack response received");
									iReturnVal = 31;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									iReturnVal = 31;
									WriteLog(Level.WARNING, "Unknown error");
								}
							} else {
								iReturnVal = 28;
								WriteLog(Level.WARNING, "Command send fail on serial port");
							}
						}
					} else {
						iReturnVal = 20;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			} else if (objDeviceType == Device.CardReaderWriter) {
				if (bProceed) {
					if (serialPort_CardRW != null) {
						int iRes = -1;

						java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

						Cm_CardRW = 0x31;
						Pm_CardRW = 0x31; // DD 52 check sum
						St0_CardRW[0] = St1_CardRW[0] = 0;
						TxDataLen_CardRW = 0;
						RxDataLen_CardRW[0] = 0;

						iRes = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
								St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
						if (iRes == 0) {
							if (ReType_CardRW[0] == 0x50) {
								WriteLog(Level.WARNING, "Ack response received");

								// checking channel status
								if ((RxData_CardRW[0] & 0x0F) == 0x00) {
									iReturnVal = 2; // no card in channel
									bProceed = false;
								}
							} else {
								WriteLog(Level.WARNING, "Ack response not received");
								iReturnVal = 28;// communication failure
							}
						} else {
							iReturnVal = 31;
							WriteLog(Level.WARNING, "Unknown error");
						}

						if (bProceed) {
							iRes = -1;

							java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
							java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
							java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
							java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
							java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
							java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

							Cm_CardRW = 0x33;
							Pm_CardRW = 0x31; // move card to drop rear side

							St0_CardRW[0] = St1_CardRW[0] = 0;
							TxDataLen_CardRW = 0;
							RxDataLen_CardRW[0] = 0;

							iRes = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW,
									ReType_CardRW, St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
							if (iRes == 0) {
								if (ReType_CardRW[0] == 0x50) {
									WriteLog(Level.WARNING, "Ack response received");
									iReturnVal = 0;
								} else {
									WriteLog(Level.WARNING, "Ack response not received");
									iReturnVal = 28;// communication failure
								}
							} else {
								iReturnVal = 31;
								WriteLog(Level.WARNING, "Unknown error");
							}
						}
					} else {
						iReturnVal = 28;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			}
		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				iReturnVal = 18;
			} else {
				iReturnVal = 31;
			}

			WriteLog(Level.WARNING, e2.getMessage());

			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}

		WriteLog(Level.FINEST, "Return value- " + iReturnVal);
		return iReturnVal;
	}

	public int CollectCard(int Timeout) {
		int iReturnVal = -1;
		boolean bProceed = true;

		try {
			MyFormatter.strSourceMethodName = "CollectCard";
			WriteLog(Level.INFO, "DeviceType: " + objDeviceType.toString() + "; Timeout: " + Timeout);

			if (bConnected == false) {
				iReturnVal = 20;
				bProceed = false;
				WriteLog(Level.INFO, "Device not yet connected");
			}

			if (Timeout < 100) {
				Timeout = 100;
				WriteLog(Level.FINE, "Default timeout set");
			}

			if (bProceed) {
				iReturnVal = 31;
				WriteLog(Level.INFO, "Device dont collect cards in collection bin");
			}
		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				iReturnVal = 18;
			} else {
				iReturnVal = 31;
			}

			WriteLog(Level.WARNING, e2.getMessage());

			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}

		WriteLog(Level.FINEST, "Return value- " + iReturnVal);
		return iReturnVal;
	}

	public int IsCardInChannel(int Timeout) {
		int iReturnVal = -1;
		boolean bProceed = true;

		try {
			MyFormatter.strSourceMethodName = "IsCardInChannel";
			WriteLog(Level.INFO, "DeviceType: " + objDeviceType.toString() + "; Timeout: " + Timeout);

			if (bConnected == false) {
				iReturnVal = 20;
				bProceed = false;
				WriteLog(Level.INFO, "Device not yet connected");
			}

			if (Timeout < 100) {
				Timeout = 100;
				WriteLog(Level.FINE, "Default timeout set");
			}

			if (objDeviceType == Device.CardDispenser) {
				if (bProceed) {
					if (serialPort != null) {
						WriteLog(Level.FINEST, "Class object initialized");

						byte[] byResponse = null;

						byte[] byCmdSensorStatus = new byte[] { STX, 0x00, 0x00, 0x03, CMT, 0x31, 0x31, ETX, CRC };
						byCmdSensorStatus[byCmdSensorStatus.length - 1] = (byte) GetBCC(byCmdSensorStatus,
								byCmdSensorStatus.length - 1);
						WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmdSensorStatus);
						if (serialPort.writeBytes(byCmdSensorStatus)) {
							WriteLog(Level.FINEST, "bytes written on port");

							byte[] byRes1 = serialPort.readBytes(5, Timeout);
							WriteLog(Level.FINEST, "5 bytes read");

							int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
							WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

							byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
							WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

							byResponse = new byte[byRes1.length + byRes2.length];
							System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
							System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
							WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
							// Thread.sleep(500);
							// byResponse = serialPort.readBytes();

							serialPort.writeByte(ACK);

							if (byResponse[0] == ACK) {
								WriteLog(Level.WARNING, "Ack response received");
								
								if (byResponse[ResType_Pos] == PosRes) {
									WriteLog(Level.FINEST, "Positive response received");
									switch (byResponse[st0_Pos]) // channel status
									{
									case '0':
										iReturnVal = 0;
										WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
										break;
									case '1':
										iReturnVal = 1;
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
										break;
									case '2':
										iReturnVal = 1;
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
										break;
									}
									switch (byResponse[st1_Pos]) // stacker status
									{
									case '0':
										WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
										break;
									case '2':
										WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
										break;
									}
									switch (byResponse[st2_Pos]) // rejection bin status
									{
									case '0':
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
										break;
									}
								} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
									WriteLog(Level.FINEST, "Negative response received");
									iReturnVal = 28;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									WriteLog(Level.FINEST, "Other response received");
									iReturnVal = 28;
								}
							} else if (byResponse[0] == NACK && byResponse.length >= 10) {
								WriteLog(Level.WARNING, "Nack response received");
								iReturnVal = 31;
								bProceed = false;
								WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
							} else {
								iReturnVal = 31;
								bProceed = false;
								WriteLog(Level.WARNING, "Unknown error");
							}
						} else {
							iReturnVal = 28;
							bProceed = false;
							WriteLog(Level.WARNING, "Command send fail on serial port");
						}
					} else {
						iReturnVal = 20;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			} else if (objDeviceType == Device.CardReaderWriter) {
				if (bProceed) {
					if (serialPort_CardRW != null) {
						int iRes = -1;

						java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

						Cm_CardRW = 0x31;
						Pm_CardRW = 0x31; // DD 52 check sum
						St0_CardRW[0] = St1_CardRW[0] = 0;
						TxDataLen_CardRW = 0;
						RxDataLen_CardRW[0] = 0;

						iRes = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
								St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
						if (iRes == 0) {
							if (ReType_CardRW[0] == 0x50) {
								WriteLog(Level.WARNING, "Ack response received");

								// checking channel status
								if ((RxData_CardRW[0] & 0x0F) == 0x00) {
									iReturnVal = 0; // no card in channel
									WriteLog(Level.INFO, "No card in channel");
								} else {
									iReturnVal = 1; // card found in channel
									WriteLog(Level.INFO, "Card found in channel");
								}
							} else {
								WriteLog(Level.WARNING, "Ack response not received");
								iReturnVal = 28;// communication failure
							}
						} else {
							iReturnVal = 31;
							WriteLog(Level.WARNING, "Unknown error");
						}
					} else {
						iReturnVal = 28;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			}
		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				iReturnVal = 18;
			} else {
				iReturnVal = 31;
			}

			WriteLog(Level.WARNING, e2.getMessage());

			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}

		WriteLog(Level.FINEST, "Return value- " + iReturnVal);
		return iReturnVal;
	}

	public int IsCardRemoved(int Timeout) {
		int iReturnVal = -1;
		boolean bProceed = true;

		try {
			MyFormatter.strSourceMethodName = "IsCardRemoved";
			WriteLog(Level.INFO, "DeviceType: " + objDeviceType.toString() + "; Timeout: " + Timeout);

			if (bConnected == false) {
				iReturnVal = 20;
				bProceed = false;
				WriteLog(Level.INFO, "Device not yet connected");
			}

			if (Timeout < 100) {
				Timeout = 100;
				WriteLog(Level.FINE, "Default timeout set");
			}

			if (objDeviceType == Device.CardDispenser) {
				if (bProceed) {
					if (serialPort != null) {
						WriteLog(Level.FINEST, "Class object initialized");

						byte[] byResponse = null;

						byte[] byCmdSensorStatus = new byte[] { STX, 0x00, 0x00, 0x03, CMT, 0x31, 0x31, ETX, CRC };
						byCmdSensorStatus[byCmdSensorStatus.length - 1] = (byte) GetBCC(byCmdSensorStatus,
								byCmdSensorStatus.length - 1);
						WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmdSensorStatus);
						if (serialPort.writeBytes(byCmdSensorStatus)) {
							WriteLog(Level.FINEST, "bytes written on port");

							byte[] byRes1 = serialPort.readBytes(5, Timeout);
							WriteLog(Level.FINEST, "5 bytes read");

							int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
							WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

							byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
							WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

							byResponse = new byte[byRes1.length + byRes2.length];
							System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
							System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
							WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
							// Thread.sleep(500);
							// byResponse = serialPort.readBytes();

							serialPort.writeByte(ACK);

							if (byResponse[0] == ACK) {
								WriteLog(Level.WARNING, "Ack response received");
								
								if (byResponse[ResType_Pos] == PosRes) {
									WriteLog(Level.FINEST, "Positive response received");
									switch (byResponse[st0_Pos]) // channel status
									{
									case '0':
										iReturnVal = 1;
										WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
										break;
									case '1':
										iReturnVal = 0;
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
										break;
									case '2':
										iReturnVal = 0;
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
										break;
									}
									switch (byResponse[st1_Pos]) // stacker status
									{
									case '0':
										WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
										break;
									case '2':
										WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
										break;
									}
									switch (byResponse[st2_Pos]) // rejection bin status
									{
									case '0':
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
										break;
									}
	
									/*
									 * for(int iPos = 11; iPos <= 17; iPos++) { if(byResponse[iPos] == 0x30) {
									 * iReturnVal = 1; //No card in the channel bProceed = false; break; } }
									 */
								} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
									WriteLog(Level.FINEST, "Negative response received");
									iReturnVal = 28;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									WriteLog(Level.FINEST, "Other response received");
									iReturnVal = 28;
								}
							} else if (byResponse[0] == NACK && byResponse.length >= 10) {
								WriteLog(Level.WARNING, "Nack response received");
								iReturnVal = 31;
								bProceed = false;
								WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
							} else {
								iReturnVal = 31;
								bProceed = false;
								WriteLog(Level.WARNING, "Unknown error");
							}
						} else {
							iReturnVal = 28;
							bProceed = false;
							WriteLog(Level.WARNING, "Command send fail on serial port");
						}
					} else {
						iReturnVal = 20;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			} else if (objDeviceType == Device.CardReaderWriter) {
				if (bProceed) {
					if (serialPort_CardRW != null) {
						int iRes = -1;

						java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

						Cm_CardRW = 0x31;
						Pm_CardRW = 0x31; // DD 52 check sum
						St0_CardRW[0] = St1_CardRW[0] = 0;
						TxDataLen_CardRW = 0;
						RxDataLen_CardRW[0] = 0;

						iRes = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
								St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
						if (iRes == 0) {
							if (ReType_CardRW[0] == 0x50) {
								WriteLog(Level.WARNING, "Ack response received");

								// checking channel status
								if ((RxData_CardRW[0] & 0x01) == 0x01) {
									iReturnVal = 0; // not removed
									WriteLog(Level.INFO, "Card in gate");
								} else {
									iReturnVal = 1; // card removed
									WriteLog(Level.INFO, "Card removed from gate");
								}
							} else {
								WriteLog(Level.WARNING, "Ack response not received");
								iReturnVal = 28;// communication failure
							}
						} else {
							iReturnVal = 31;
							WriteLog(Level.WARNING, "Unknown error");
						}
					} else {
						iReturnVal = 28;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			}
		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				iReturnVal = 18;
			} else {
				iReturnVal = 31;
			}

			WriteLog(Level.WARNING, e2.getMessage());

			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}

		WriteLog(Level.FINEST, "Return value- " + iReturnVal);
		return iReturnVal;
	}

	public int DisConnectDevice(int Timeout) {
		int iReturnVal = -1;
		boolean bProceed = true;

		try {
			MyFormatter.strSourceMethodName = "DisConnectDevice";
			WriteLog(Level.INFO, "DeviceType: " + objDeviceType.toString() + "; Timeout: " + Timeout);

			if (bConnected == false) {
				iReturnVal = 20;
				bProceed = false;
				WriteLog(Level.INFO, "Device not yet connected");
			}

			if (Timeout < 100) {
				Timeout = 100;
				WriteLog(Level.FINE, "Default timeout set");
			}

			if (objDeviceType == Device.CardDispenser) {
				if (bProceed) {
					if (serialPort != null) {
						WriteLog(Level.FINEST, "Class object initialized");

						byte[] byResponse = null;

						byte[] byCmdSensorStatus = new byte[] { STX, 0x00, 0x00, 0x03, CMT, 0x31, 0x31, ETX, CRC };
						byCmdSensorStatus[byCmdSensorStatus.length - 1] = (byte) GetBCC(byCmdSensorStatus,
								byCmdSensorStatus.length - 1);
						WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmdSensorStatus);
						if (serialPort.writeBytes(byCmdSensorStatus)) {
							WriteLog(Level.FINEST, "bytes written on port");

							byte[] byRes1 = serialPort.readBytes(5, Timeout);
							WriteLog(Level.FINEST, "5 bytes read");

							int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
							WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

							byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
							WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

							byResponse = new byte[byRes1.length + byRes2.length];
							System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
							System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
							WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
							// Thread.sleep(500);
							// byResponse = serialPort.readBytes();

							serialPort.writeByte(ACK);

							if (byResponse[0] == ACK) {
								WriteLog(Level.WARNING, "Ack response received");
								
								if (byResponse[ResType_Pos] == PosRes) {
									WriteLog(Level.FINEST, "Positive response received");
									switch (byResponse[st0_Pos]) // channel status
									{
									case '0':
										WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
										break;
									case '1':
										iReturnVal = 1;
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
										break;
									case '2':
										iReturnVal = 1;
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
										break;
									}
									switch (byResponse[st1_Pos]) // stacker status
									{
									case '0':
										WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
										break;
									case '2':
										WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
										break;
									}
									switch (byResponse[st2_Pos]) // rejection bin status
									{
									case '0':
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
										break;
									}
	
									for (int iPos = 11; iPos <= 17; iPos++) {
										if (byResponse[iPos] == 0x31) {
											iReturnVal = 1; // card in the channel
											bProceed = false;
											WriteLog(Level.FINEST, "Card found on sensor position- " + (iPos - 10));
											break;
										}
									}
	
									boolean bRet = serialPort.closePort();
	
									if (bRet) {
										bConnected = false;
										if (iReturnVal != 1)
											iReturnVal = 0;
									} else {
										iReturnVal = 31;
										WriteLog(Level.WARNING, "Port could not be closed");
										// lblStatusVal.setText("Port could not be opened");
									}
								} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
									WriteLog(Level.FINEST, "Negative response received");
									iReturnVal = 28;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									WriteLog(Level.FINEST, "Other response received");
									iReturnVal = 28;
								}

							} else if (byResponse[0] == NACK && byResponse.length >= 10) {
								WriteLog(Level.WARNING, "Nack response received");
								iReturnVal = 31;
								bProceed = false;
								WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
							} else {
								iReturnVal = 31;
								bProceed = false;
								WriteLog(Level.WARNING, "Unknown error");
							}
						} else {
							iReturnVal = 28;
							bProceed = false;
							WriteLog(Level.WARNING, "Command send fail on serial port");
						}
					} else {
						iReturnVal = 20;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			} else if (objDeviceType == Device.CardReaderWriter) {
				if (bProceed) {
					if (serialPort_CardRW != null) {
						int iRes = -1;

						java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

						Cm_CardRW = 0x31;
						Pm_CardRW = 0x31; // DD 52 check sum
						St0_CardRW[0] = St1_CardRW[0] = 0;
						TxDataLen_CardRW = 0;
						RxDataLen_CardRW[0] = 0;

						iRes = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
								St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
						if (iRes == 0) {
							if (ReType_CardRW[0] == 0x50) {
								WriteLog(Level.WARNING, "Ack response received");

								// checking channel status
								if ((RxData_CardRW[0] & 0x0F) != 0x00) {
									iReturnVal = 1; // card found
									WriteLog(Level.INFO, "Card in gate");
								}
							} else {
								WriteLog(Level.WARNING, "Ack response not received");
								iReturnVal = 28;// communication failure
							}
						} else {
							iReturnVal = 31;
							WriteLog(Level.WARNING, "Unknown error");
						}

						// closing port
						if (serialPort_CardRW.closePort() == true) {
							bConnected = false;
							if (iReturnVal != 1)
								iReturnVal = 0;
						} else {
							iReturnVal = 31;
							WriteLog(Level.WARNING, "Port could not be closed");
						}
					} else {
						iReturnVal = 31;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			}
		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				iReturnVal = 18;
			} else {
				iReturnVal = 31;
			}

			WriteLog(Level.WARNING, e2.getMessage());

			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}

		WriteLog(Level.FINEST, "Return value- " + iReturnVal);
		return iReturnVal;
	}

	/*
	 * public String GetSCardReaderNativeLibVersion() { String strReturnVal = "";
	 * try { MyFormatter.strSourceMethodName = "GetSCardReaderNativeLibVersion";
	 * WriteLog(Level.INFO, "called");
	 * 
	 * strReturnVal = GlobalMembers.strSCardReaderNativeLibVersion;
	 * WriteLog(Level.INFO, "Return value- " + strReturnVal); } catch(Exception e2)
	 * { strReturnVal = "00.00.00"; WriteLog(Level.WARNING, e2.getMessage());
	 * 
	 * StringWriter errors = new StringWriter(); e2.printStackTrace(new
	 * PrintWriter(errors)); WriteLog(Level.FINEST, errors.toString()); }
	 * WriteLog(Level.FINEST, "Return value- " + strReturnVal); return strReturnVal;
	 * }
	 */

	///////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * public String GetSCardReaderFWVersion() { String strReturnVal = ""; try {
	 * MyFormatter.strSourceMethodName = "GetSCardReaderFWVersion";
	 * WriteLog(Level.INFO, "called");
	 * 
	 * if(serialPort_CardRW != null) { int i = 0;
	 * 
	 * java.util.Arrays.fill(TxData_CardRW, (byte)0x00);
	 * java.util.Arrays.fill(RxData_CardRW, (byte)0x00);
	 * java.util.Arrays.fill(ReType_CardRW, (byte)0x00);
	 * java.util.Arrays.fill(St0_CardRW, (byte)0x00);
	 * java.util.Arrays.fill(St1_CardRW, (byte)0x00);
	 * java.util.Arrays.fill(RxDataLen_CardRW, 0x00);
	 * 
	 * Cm_CardRW = 0x41; Pm_CardRW = 0x31; //DD 52 check sum St0_CardRW[0] =
	 * St1_CardRW[0] = 0; TxDataLen_CardRW = 0; RxDataLen_CardRW[0] =0;
	 * 
	 * i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW,
	 * ReType_CardRW, St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW,5000);
	 * if(i == 0) { if (ReType_CardRW[0] == 0x50) { WriteLog(Level.WARNING,
	 * "Ack response received"); byte[] byRes = Arrays.copyOfRange(RxData_CardRW, 0,
	 * RxDataLen_CardRW[0]); strReturnVal = new String(byRes);//successfully } else
	 * { WriteLog(Level.WARNING, "Ack response not received"); strReturnVal =
	 * "00.00.00"; } } else { strReturnVal = "00.00.00"; WriteLog(Level.WARNING,
	 * "Unknown error"); } } else { strReturnVal = "00.00.00";
	 * WriteLog(Level.WARNING, "Serial class object not created"); } }
	 * catch(Exception e2) { strReturnVal = "00.00.00"; WriteLog(Level.WARNING,
	 * e2.getMessage()); StringWriter errors = new StringWriter();
	 * e2.printStackTrace(new PrintWriter(errors)); WriteLog(Level.FINEST,
	 * errors.toString()); } WriteLog(Level.FINEST, "Return value- " +
	 * strReturnVal); return strReturnVal; }
	 */

	/////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * CardEntrySetting() : this function is private type for that class. With help
	 * of this function enable or disable card entry Input Param : IsEnableCardEntry
	 * = 0 mean enable 1 mean disable
	 */
	/*
	 * private int CardEntrySetting(int IsEnableCardEntry) { int iReturnVal = 0; try
	 * { MyFormatter.strSourceMethodName = "CardEntrySetting"; WriteLog(Level.INFO,
	 * "IsEnableCardEntry:" + IsEnableCardEntry);
	 * 
	 * if (serialPort_CardRW != null) { int i = 0;
	 * 
	 * java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
	 * java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
	 * java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
	 * java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
	 * java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
	 * java.util.Arrays.fill(RxDataLen_CardRW, 0x00);
	 * 
	 * Cm_CardRW = 0x3A; Pm_CardRW = 0x30; // DD 52 check sum St0_CardRW[0] =
	 * St1_CardRW[0] = 0; TxDataLen_CardRW = 1; if (IsEnableCardEntry == 0) {
	 * WriteLog(Level.INFO, "All Card Enable"); TxData_CardRW[0] = 0x30; } else {
	 * WriteLog(Level.INFO, "All Card disable"); TxData_CardRW[0] = 0x31; }
	 * 
	 * RxDataLen_CardRW[0] = 0;
	 * 
	 * i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW,
	 * ReType_CardRW, St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW,
	 * 5000); if (i == 0) { if (ReType_CardRW[0] == 0x50) { WriteLog(Level.WARNING,
	 * "Ack response received"); iReturnVal = 0;// successfully } else {
	 * WriteLog(Level.WARNING, "Ack response not received"); iReturnVal = 28;//
	 * communication failure } } else { WriteLog(Level.WARNING, "Unknown error");
	 * iReturnVal = i;// other error } } else { iReturnVal = 28;// communication
	 * failure WriteLog(Level.WARNING, "Serial class object not created"); } } catch
	 * (Exception e2) { iReturnVal = 28;// communication failure
	 * WriteLog(Level.WARNING, e2.getMessage()); StringWriter errors = new
	 * StringWriter(); e2.printStackTrace(new PrintWriter(errors));
	 * WriteLog(Level.FINEST, errors.toString()); } WriteLog(Level.FINEST,
	 * "Return value- " + iReturnVal); return iReturnVal; }
	 */

	/////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * SAMSlotPowerOnOff: Power On/Off a specific SAM Slot/Socket Socket Input
	 * Param: SAMSlotId : 1 (First SAM Slot), 2 (First SAM Slot) PowerOnOffState: 0
	 * Power off/Deactivate, 1 Power On/Activate Timeout Output Param: int
	 */
	public int SAMSlotPowerOnOff(int SAMSlotId, int PowerOnOffState, int Timeout) {
		int iReturnVal = 0;
		boolean bProceed = true;

		try {
			MyFormatter.strSourceMethodName = "SAMSlotPowerOnOff";
			WriteLog(Level.INFO,
					"SAMSlotId: " + SAMSlotId + "; PowerOnOffState: " + PowerOnOffState + "; Timeout: " + Timeout);

			if (bConnected == false) {
				WriteLog(Level.WARNING, "Device not yet connected");
				iReturnVal = 20;// Device not yet connected
				bProceed = false;
			} else if ((SAMSlotId < 1) & (SAMSlotId > 4)) {
				WriteLog(Level.WARNING, "Invalid SAMSlotId: " + SAMSlotId);
				iReturnVal = 31;// Other Error
				bProceed = false;
			} else if ((PowerOnOffState < 0) & (PowerOnOffState > 1)) {
				WriteLog(Level.WARNING, "Invalid PowerOnOffState: " + PowerOnOffState);
				iReturnVal = 31;// Other Error
				bProceed = false;
			}

			if (objDeviceType == Device.CardDispenser) {

				if (bProceed) {
					if (serialPort != null) {
						WriteLog(Level.FINEST, "Class object initialized");

						byte[] byResponse = null;
						byte bySAMPort = 0x00;

						if (SAMSlotId == 1) {
							WriteLog(Level.INFO, "First SAM Slot");
							bySAMPort = 0x30;
						} else if (SAMSlotId == 2) {
							WriteLog(Level.INFO, "Second SAM Slot");
							bySAMPort = 0x31;
						} else if (SAMSlotId == 3) {
							WriteLog(Level.INFO, "Third SAM Slot");
							bySAMPort = 0x32;
						} else if (SAMSlotId == 4) {
							WriteLog(Level.INFO, "Third SAM Slot");
							bySAMPort = 0x33;
						}

						byte[] byCmdSamSlotSelect = new byte[] { STX, 0x00, 0x00, 0x04, CMT, 0x52, 0x40, bySAMPort, ETX,
								CRC };
						byCmdSamSlotSelect[byCmdSamSlotSelect.length - 1] = (byte) GetBCC(byCmdSamSlotSelect,
								byCmdSamSlotSelect.length - 1);
						WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmdSamSlotSelect);
						if (serialPort.writeBytes(byCmdSamSlotSelect)) {
							WriteLog(Level.FINEST, "bytes written on port");

							byte[] byRes1 = serialPort.readBytes(5, Timeout);
							WriteLog(Level.FINEST, "5 bytes read");

							int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
							WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

							byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
							WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

							byResponse = new byte[byRes1.length + byRes2.length];
							System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
							System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
							WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
							// Thread.sleep(500);
							// byResponse = serialPort.readBytes();

							serialPort.writeByte(ACK);

							if (byResponse[0] == ACK) {
								WriteLog(Level.WARNING, "Ack response received");
								
								if (byResponse[ResType_Pos] == PosRes) {
									WriteLog(Level.FINEST, "Positive response received");
									switch (byResponse[st0_Pos]) // channel status
									{
									case '0':
										WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
										break;
									case '2':
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
										break;
									}
									switch (byResponse[st1_Pos]) // stacker status
									{
									case '0':
										WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
										break;
									case '2':
										WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
										break;
									}
									switch (byResponse[st2_Pos]) // rejection bin status
									{
									case '0':
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
										break;
									}
								} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
									WriteLog(Level.FINEST, "Negative response received");
									iReturnVal = 28;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									WriteLog(Level.FINEST, "Other response received");
									iReturnVal = 28;
								}

							} else if (byResponse[0] == NACK && byResponse.length >= 10) {
								WriteLog(Level.WARNING, "Nack response received");
								iReturnVal = 31;
								bProceed = false;
								WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
							} else {
								iReturnVal = 31;
								bProceed = false;
								WriteLog(Level.WARNING, "Unknown error");
							}
						} else {
							iReturnVal = 28;
							bProceed = false;
							WriteLog(Level.WARNING, "Command send fail on serial port");
						}

						if (bProceed) {
							byResponse = null;
							byte[] byCmdActivateSamSlot = null;

							if (PowerOnOffState == 0)// Deactive
							{
								WriteLog(Level.INFO, "Power off/Deactivate");
								byCmdActivateSamSlot = new byte[] { STX, 0x00, 0x00, 0x03, CMT, 0x52, 0x31, ETX, CRC };
							} else// active
							{
								WriteLog(Level.INFO, "Power on/Activate");
								// Activate SAM command with Vcc 0x33
								byCmdActivateSamSlot = new byte[] { STX, 0x00, 0x00, 0x04, CMT, 0x52, 0x30, 0x33, ETX,
										CRC };
							}

							byCmdActivateSamSlot[byCmdActivateSamSlot.length - 1] = (byte) GetBCC(byCmdActivateSamSlot,
									byCmdActivateSamSlot.length - 1);
							WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmdActivateSamSlot);
							if (serialPort.writeBytes(byCmdActivateSamSlot)) {
								WriteLog(Level.FINEST, "bytes written on port");

								byte[] byRes1 = serialPort.readBytes(5, Timeout);
								WriteLog(Level.FINEST, "5 bytes read");

								int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
								WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

								byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
								WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

								byResponse = new byte[byRes1.length + byRes2.length];
								System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
								System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
								WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
								// Thread.sleep(500);
								// byResponse = serialPort.readBytes();

								serialPort.writeByte(ACK);

								if (byResponse[0] == ACK) {
									WriteLog(Level.WARNING, "Ack response received");
									
									if (byResponse[ResType_Pos] == PosRes) {
										WriteLog(Level.FINEST, "Positive response received");
										switch (byResponse[st0_Pos]) // channel status
										{
										case '0':
											WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
											break;
										case '1':
											WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
											break;
										case '2':
											WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
											break;
										}
										switch (byResponse[st1_Pos]) // stacker status
										{
										case '0':
											WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
											break;
										case '1':
											WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
											break;
										case '2':
											WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
											break;
										}
										switch (byResponse[st2_Pos]) // rejection bin status
										{
										case '0':
											WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
											break;
										case '1':
											WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
											break;
										}
	
										iReturnVal = 0;
									} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
										WriteLog(Level.FINEST, "Negative response received");
										iReturnVal = 28;
										WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
									} else {
										WriteLog(Level.FINEST, "Other response received");
										iReturnVal = 28;
									}

								} else if (byResponse[0] == NACK && byResponse.length >= 10) {
									WriteLog(Level.WARNING, "Nack response received");
									iReturnVal = 1;
									bProceed = false;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									iReturnVal = 31;
									bProceed = false;
									WriteLog(Level.WARNING, "Unknown error");
								}
							} else {
								iReturnVal = 28;
								bProceed = false;
								WriteLog(Level.WARNING, "Command send fail on serial port");
							}
						}
					} else {
						iReturnVal = 20;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			} else if (objDeviceType == Device.CardReaderWriter) {
				if (bProceed) {
					if (serialPort_CardRW != null) {
						// Step1 Send CMD for Select SAM
						// Step2 Send CMD for Power On/Activate, Power Off/Deactivate

						int i = 0;

						java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

						// Step1 CMD for Select SAM
						// command code
						Cm_CardRW = 0x49;

						// parameter code
						Pm_CardRW = 0x50;

						St0_CardRW[0] = St1_CardRW[0] = 0;

						TxDataLen_CardRW = 1;

						/*
						 * Sel = “0”(30h) : Select SAM1 Sel = “1”(31h) : Select SAM2 Sel = “2”(32h) :
						 * Select SAM3 Sel = “3”(33h) : Select SAM4
						 */
						if (SAMSlotId == 1) {
							WriteLog(Level.INFO, "First SAM Slot");
							TxData_CardRW[0] = 0x30;
						} else if (SAMSlotId == 2) {
							WriteLog(Level.INFO, "Second SAM Slot");
							TxData_CardRW[0] = 0x31;
						} else if (SAMSlotId == 3) {
							WriteLog(Level.INFO, "Third SAM Slot");
							TxData_CardRW[0] = 0x32;
						} else if (SAMSlotId == 4) {
							WriteLog(Level.INFO, "Third SAM Slot");
							TxData_CardRW[0] = 0x33;
						}

						i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
								St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
						if (i == 0) {
							if (ReType_CardRW[0] == 0x50) {
								WriteLog(Level.WARNING, "Ack response received");
								iReturnVal = 0;// Device connected successfully
								java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
								java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
								java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
								java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
								java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
								java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

								// Step2 CMD for Select SAM
								Cm_CardRW = 0x49;// SAM command
								if (PowerOnOffState == 0)// Deactive
								{
									WriteLog(Level.INFO, "Power off/Deactivate");
									Pm_CardRW = 0x41;
									TxDataLen_CardRW = 0;
								} else// active
								{
									WriteLog(Level.INFO, "Power on/Activate");
									Pm_CardRW = 0x40;// Activate SAM command
									TxDataLen_CardRW = 1;
									TxData_CardRW[0] = 0x33;
								}

								St0_CardRW[0] = St1_CardRW[0] = 0;
								i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW,
										ReType_CardRW, St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW,
										Timeout);
								if (i == 0) {
									if (ReType_CardRW[0] == 0x50) {
										WriteLog(Level.WARNING, "Ack response received");
										iReturnVal = 0;// successfully
									} else {
										WriteLog(Level.WARNING, "Ack response not received");
										iReturnVal = 28;// communication failure
									}
								} else {
									iReturnVal = i;// other error
								}
							} else {
								WriteLog(Level.WARNING, "Ack response not received");
								iReturnVal = 28;// communication failure
							}
						} else {
							WriteLog(Level.WARNING, "Unknown error");
							iReturnVal = i;// other error
						}
					} else {
						iReturnVal = 28;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			}
		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				iReturnVal = 18;
			} else {
				iReturnVal = 31;
			}

			WriteLog(Level.WARNING, e2.getMessage());
			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}
		WriteLog(Level.FINEST, "Return value- " + iReturnVal);
		return iReturnVal;
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * ResetSMA: Reset SAM in cold or worm mode. Input Param: SAMSlotId : 1 (First
	 * SAM Slot), 2 (First SAM Slot) Reset Type: 0 Cold, 1 Worm Reset Timeout Output
	 * Param: Byte Array
	 */
	public byte[] ResetSAM(int SAMSlotId, int ResetType, int Timeout) {
		byte[] byReturnVal = new byte[255];
		boolean bProceed = true;

		try {
			MyFormatter.strSourceMethodName = "ResetSAM";
			WriteLog(Level.INFO, "SAMSlotId: " + SAMSlotId + "; ResetType: " + ResetType + "; Timeout: " + Timeout);

			if (bConnected == false) {
				WriteLog(Level.WARNING, "Device not yet connected");
				byReturnVal[0] = 20;// Device not yet connected
				bProceed = false;
			} else if ((SAMSlotId < 1) & (SAMSlotId > 4)) {
				WriteLog(Level.WARNING, "Invalid SAMSlotId: " + SAMSlotId);
				byReturnVal[0] = 31;// Other Error
				bProceed = false;
			} else if ((ResetType < 0) & (ResetType > 1)) {
				WriteLog(Level.WARNING, "Invalid ResetType: " + ResetType);
				byReturnVal[0] = 31;// Other Error
				bProceed = false;
			}

			if (objDeviceType == Device.CardDispenser) {
				if (bProceed) {

					if (serialPort != null) {
						WriteLog(Level.FINEST, "Class object initialized");

						byte[] byResponse = null;
						byte bySAMPort = 0x00;

						if (SAMSlotId == 1) {
							WriteLog(Level.INFO, "First SAM Slot");
							bySAMPort = 0x30;
						} else if (SAMSlotId == 2) {
							WriteLog(Level.INFO, "Second SAM Slot");
							bySAMPort = 0x31;
						} else if (SAMSlotId == 3) {
							WriteLog(Level.INFO, "Third SAM Slot");
							bySAMPort = 0x32;
						} else if (SAMSlotId == 4) {
							WriteLog(Level.INFO, "Third SAM Slot");
							bySAMPort = 0x33;
						}

						byte[] byCmdSamSlotSelect = new byte[] { STX, 0x00, 0x00, 0x04, CMT, 0x52, 0x40, bySAMPort, ETX,
								CRC };
						byCmdSamSlotSelect[byCmdSamSlotSelect.length - 1] = (byte) GetBCC(byCmdSamSlotSelect,
								byCmdSamSlotSelect.length - 1);
						WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmdSamSlotSelect);
						if (serialPort.writeBytes(byCmdSamSlotSelect)) {
							WriteLog(Level.FINEST, "bytes written on port");

							byte[] byRes1 = serialPort.readBytes(5, Timeout);
							WriteLog(Level.FINEST, "5 bytes read");

							int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
							WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

							byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
							WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

							byResponse = new byte[byRes1.length + byRes2.length];
							System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
							System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
							WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
							// Thread.sleep(500);
							// byResponse = serialPort.readBytes();

							serialPort.writeByte(ACK);

							if (byResponse[0] == ACK) {
								WriteLog(Level.WARNING, "Ack response received");
								
								if (byResponse[ResType_Pos] == PosRes) {
									WriteLog(Level.FINEST, "Positive response received");
									switch (byResponse[st0_Pos]) // channel status
									{
									case '0':
										WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
										break;
									case '2':
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
										break;
									}
									switch (byResponse[st1_Pos]) // stacker status
									{
									case '0':
										WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
										break;
									case '2':
										WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
										break;
									}
									switch (byResponse[st2_Pos]) // rejection bin status
									{
									case '0':
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
										break;
									}
								} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
									WriteLog(Level.FINEST, "Negative response received");
									byReturnVal[0] = 28;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									WriteLog(Level.FINEST, "Other response received");
									byReturnVal[0] = 28;
								}

							} else if (byResponse[0] == NACK && byResponse.length >= 10) {
								WriteLog(Level.WARNING, "Nack response received");
								byReturnVal[0] = 31;
								bProceed = false;
								WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
							} else {
								byReturnVal[0] = 31;
								bProceed = false;
								WriteLog(Level.WARNING, "Unknown error");
							}
						} else {
							byReturnVal[0] = 28;
							bProceed = false;
							WriteLog(Level.WARNING, "Command send fail on serial port");
						}

						if (bProceed) {
							byResponse = null;
							byte byResetType = 0x00;
							if (ResetType == 1)// Worm Reset
							{
								byResetType = 0x38;
								WriteLog(Level.INFO, "Worm reset");
							} else// cold reset
							{
								byResetType = 0x30;
								WriteLog(Level.INFO, "Cold reset");
							}

							byte[] byCmdReset = new byte[] { STX, 0x00, 0x00, 0x03, CMT, 0x52, byResetType, ETX, CRC };
							byCmdReset[byCmdReset.length - 1] = (byte) GetBCC(byCmdReset, byCmdReset.length - 1);
							WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmdReset);
							if (serialPort.writeBytes(byCmdReset)) {
								WriteLog(Level.FINEST, "bytes written on port");

								byte[] byRes1 = serialPort.readBytes(5, Timeout);
								WriteLog(Level.FINEST, "5 bytes read");

								int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
								WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

								byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
								WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

								byResponse = new byte[byRes1.length + byRes2.length];
								System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
								System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
								WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
								// Thread.sleep(500);
								// byResponse = serialPort.readBytes();

								serialPort.writeByte(ACK);

								if (byResponse[0] == ACK) {
									WriteLog(Level.WARNING, "Ack response received");
									
									if (byResponse[ResType_Pos] == PosRes) {
										WriteLog(Level.FINEST, "Positive response received");
										switch (byResponse[st0_Pos]) // channel status
										{
										case '0':
											WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
											break;
										case '1':
											WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
											break;
										case '2':
											WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
											break;
										}
										switch (byResponse[st1_Pos]) // stacker status
										{
										case '0':
											WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
											break;
										case '1':
											WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
											break;
										case '2':
											WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
											break;
										}
										switch (byResponse[st2_Pos]) // rejection bin status
										{
										case '0':
											WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
											break;
										case '1':
											WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
											break;
										}
	
										byReturnVal[0] = 0;
									} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
										WriteLog(Level.FINEST, "Negative response received");
										byReturnVal[0] = 28;
										WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
									} else {
										WriteLog(Level.FINEST, "Other response received");
										byReturnVal[0] = 28;
									}

								} else if (byResponse[0] == NACK && byResponse.length >= 10) {
									WriteLog(Level.WARNING, "Nack response received");
									byReturnVal[0] = 1;
									bProceed = false;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									byReturnVal[0] = 31;
									bProceed = false;
									WriteLog(Level.WARNING, "Unknown error");
								}
							} else {
								byReturnVal[0] = 28;
								bProceed = false;
								WriteLog(Level.WARNING, "Command send fail on serial port");
							}
						}
					} else {
						byReturnVal[0] = 20;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			} else if (objDeviceType == Device.CardReaderWriter) {
				if (bProceed) {
					if (serialPort_CardRW != null) {
						// Step1 Send CMD for Select SAM
						// Step2 Send CMD for Power On/Activate, Power Off/Deactivate

						int i = 0;

						java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

						// Step1 CMD for Select SAM
						Cm_CardRW = 0x49;
						Pm_CardRW = 0x50;
						St0_CardRW[0] = St1_CardRW[0] = 0;
						TxDataLen_CardRW = 1;
						if (SAMSlotId == 1) {
							WriteLog(Level.INFO, "First SAM Slot");
							TxData_CardRW[0] = 0x30;
						} else if (SAMSlotId == 2) {
							WriteLog(Level.INFO, "Second SAM Slot");
							TxData_CardRW[0] = 0x31;
						} else if (SAMSlotId == 3) {
							WriteLog(Level.INFO, "Third SAM Slot");
							TxData_CardRW[0] = 0x32;
						} else if (SAMSlotId == 4) {
							WriteLog(Level.INFO, "Third SAM Slot");
							TxData_CardRW[0] = 0x33;
						}

						i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
								St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
						if (i == 0) {
							if (ReType_CardRW[0] == 0x50) {
								WriteLog(Level.WARNING, "Ack response received");
								java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
								java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
								java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
								java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
								java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
								java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

								// Step2 Cold / Worm reset
								Cm_CardRW = 0x49;
								if (ResetType == 1)// Worm Reset
								{
									Pm_CardRW = 0x48;
									TxDataLen_CardRW = 0;
								} else// Cold Reset
								{
									Pm_CardRW = 0x40;
									TxDataLen_CardRW = 1;
									TxData_CardRW[0] = 0x33;
								}

								St0_CardRW[0] = St1_CardRW[0] = 0;

								i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW,
										ReType_CardRW, St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW,
										Timeout);
								if (i == 0) {
									if (ReType_CardRW[0] == 0x50) {
										byReturnVal[0] = 0;// Device connected successfully
										for (int responseLen = 0; responseLen < RxDataLen_CardRW[0]; responseLen++) {
											byReturnVal[responseLen + 1] = RxData_CardRW[responseLen];
										}
										// getATR
									} else {
										byReturnVal[0] = 28;// communication failure
									}
								} else {
									byReturnVal[0] = (byte) i;// other error
								}
							} else {
								WriteLog(Level.WARNING, "Ack response not received");
								byReturnVal[0] = 28;// communication failure
							}
						} else {
							WriteLog(Level.WARNING, "Unknown error");
							byReturnVal[0] = (byte) i;// other error
						}
					} else {
						byReturnVal[0] = 28;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			}
		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				byReturnVal[0] = 18;
			} else {
				byReturnVal[0] = 31;
			}

			WriteLog(Level.WARNING, e2.getMessage());
			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}
		WriteLog(Level.FINEST, "Return");

		return byReturnVal;
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * ActivateCard: Activate a contactless Card and Contact Card. Input Param:
	 * CardTechType : 0 (contact less), 1 (Contact Card) SAMSlotId : 1 (First SAM
	 * Slot), 2 (Second SAM Slot) Timeout Output Param: Byte Array
	 */
	public byte[] ActivateCard(int CardTechType, int SAMSlotId, int Timeout) {
		byte[] byReturnVal = new byte[255];
		boolean bProceed = true;
		try {
			MyFormatter.strSourceMethodName = "ActivateCard";
			WriteLog(Level.INFO,
					"CardTechType: " + CardTechType + "; SAMSlotId: " + SAMSlotId + "; Timeout: " + Timeout);

			if (bConnected == false) {
				WriteLog(Level.WARNING, "Device not yet connected");
				byReturnVal[0] = 20;// Device not yet connected
				bProceed = false;
			} else if (CardTechType < 0 || CardTechType > 1) {
				byReturnVal[0] = 31;
				WriteLog(Level.WARNING, "Invalid CardTechType: " + CardTechType);
				bProceed = false;
			}

			if (CardTechType == 0)// Contactless
			{
				SAMSlotId = 0;// always 0 for Contactless
			} else {
				if (SAMSlotId < 1 || SAMSlotId > 4) {
					byReturnVal[0] = 31;
					WriteLog(Level.WARNING, "Invalid SAMSlotId: " + SAMSlotId);
					bProceed = false;
				}
			}

			if (objDeviceType == Device.CardDispenser) {
				if (bProceed) {
					if (serialPort != null) {
						WriteLog(Level.FINEST, "Class object initialized");

						if (CardTechType == 1)// Slot select for contact card only
						{
							byte[] byResponse = null;
							byte bySAMPort = 0x00;

							if (SAMSlotId == 1) {
								WriteLog(Level.INFO, "First SAM Slot");
								bySAMPort = 0x30;
							} else if (SAMSlotId == 2) {
								WriteLog(Level.INFO, "Second SAM Slot");
								bySAMPort = 0x31;
							} else if (SAMSlotId == 3) {
								WriteLog(Level.INFO, "Third SAM Slot");
								bySAMPort = 0x32;
							} else if (SAMSlotId == 4) {
								WriteLog(Level.INFO, "Third SAM Slot");
								bySAMPort = 0x33;
							} else {
								WriteLog(Level.INFO, "Fifth SAM Slot");
								bySAMPort = 0x35;
							}

							byte[] byCmdSamSlotSelect = new byte[] { STX, 0x00, 0x00, 0x04, CMT, 0x52, 0x40, bySAMPort,
									ETX, CRC };
							byCmdSamSlotSelect[byCmdSamSlotSelect.length - 1] = (byte) GetBCC(byCmdSamSlotSelect,
									byCmdSamSlotSelect.length - 1);
							WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmdSamSlotSelect);
							if (serialPort.writeBytes(byCmdSamSlotSelect)) {
								WriteLog(Level.FINEST, "bytes written on port");

								byte[] byRes1 = serialPort.readBytes(5, Timeout);
								WriteLog(Level.FINEST, "5 bytes read");

								int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
								WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

								byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
								WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

								byResponse = new byte[byRes1.length + byRes2.length];
								System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
								System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
								WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
								// Thread.sleep(500);
								// byResponse = serialPort.readBytes();

								serialPort.writeByte(ACK);

								if (byResponse[0] == ACK) {
									WriteLog(Level.WARNING, "Ack response received");
									
									if (byResponse[ResType_Pos] == PosRes) {
										WriteLog(Level.FINEST, "Positive response received");
										switch (byResponse[st0_Pos]) // channel status
										{
										case '0':
											WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
											break;
										case '1':
											WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
											break;
										case '2':
											WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
											break;
										}
										switch (byResponse[st1_Pos]) // stacker status
										{
										case '0':
											WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
											break;
										case '1':
											WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
											break;
										case '2':
											WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
											break;
										}
										switch (byResponse[st2_Pos]) // rejection bin status
										{
										case '0':
											WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
											break;
										case '1':
											WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
											break;
										}
									} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
										WriteLog(Level.FINEST, "Negative response received");
										byReturnVal[0] = 28;
										WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
									} else {
										WriteLog(Level.FINEST, "Other response received");
										byReturnVal[0] = 28;
									}

								} else if (byResponse[0] == NACK && byResponse.length >= 10) {
									WriteLog(Level.WARNING, "Nack response received");
									byReturnVal[0] = 31;
									bProceed = false;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									byReturnVal[0] = 31;
									bProceed = false;
									WriteLog(Level.WARNING, "Unknown error");
								}
							} else {
								byReturnVal[0] = 28;
								bProceed = false;
								WriteLog(Level.WARNING, "Command send fail on serial port");
							}

							if (bProceed) {
								byte[] byCmdActivate = new byte[] { STX, 0x00, 0x00, 0x04, CMT, 0x52, 0x30, 0x33, ETX,
										CRC };
								byCmdActivate[byCmdActivate.length - 1] = (byte) GetBCC(byCmdActivate,
										byCmdActivate.length - 1);

								WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmdActivate);
								if (serialPort.writeBytes(byCmdActivate)) {
									WriteLog(Level.FINEST, "bytes written on port");

									byte[] byRes1 = serialPort.readBytes(5, Timeout);
									WriteLog(Level.FINEST, "5 bytes read");

									int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
									WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

									byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
									WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

									byResponse = new byte[byRes1.length + byRes2.length];
									System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
									System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);

									WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
									// Thread.sleep(500);
									// byResponse = serialPort.readBytes();

									serialPort.writeByte(ACK);

									if (byResponse[0] == ACK) {
										WriteLog(Level.WARNING, "Ack response received");

										if (byResponse[ResType_Pos] == PosRes) {
											WriteLog(Level.FINEST, "Positive response received");
											byReturnVal[0] = 0;// Device connected successfully
											// byReturnVal[1] = 1;// Type of contact card... currently hardcore
											byReturnVal[2] = (byte) RxDataLen_CardRW[0];
											for (int responseLen = 0; responseLen < RxDataLen_CardRW[0]; responseLen++) {
												byReturnVal[responseLen + 3] = RxData_CardRW[responseLen];
											}
	
											// get SAM Version
											byte[] SAMVersion = new byte[] { (byte) 0x80, 0x60, 0x00, 0x00, 0x00 };
											byte[] SAMVersionRes;
	
											SAMVersionRes = XChangeAPDU(CardTechType, SAMVersion, SAMSlotId, Timeout);
	
											if (SAMVersionRes.length > 3) {
												for (int ii = 0; ii < SAMVersionRes.length; ii++) {
													if (SAMVersionRes[ii] == (byte) 0x90) {
														if (SAMVersionRes[ii - 1] == (byte) 0xa2)
															byReturnVal[1] = 2;
														else if (SAMVersionRes[ii - 1] == (byte) 0xa1)
															byReturnVal[1] = 1;
														else
															byReturnVal[1] = 0;
														break;
													}
												}
											} else
												byReturnVal[1] = 0;
	
											for (int ii = 0; ii < SAMVersionRes.length; ii++) {
												if (SAMVersionRes[ii] == 0x04) {
													if ((ii + 6) < SAMVersionRes.length) {
	
														if (SAMVersionRes[ii + 6] == (byte) 0x80) {
															byReturnVal[2] = 7;
															byReturnVal[3] = SAMVersionRes[ii];
															byReturnVal[4] = SAMVersionRes[ii + 1];
															byReturnVal[5] = SAMVersionRes[ii + 2];
															byReturnVal[6] = SAMVersionRes[ii + 3];
															byReturnVal[7] = SAMVersionRes[ii + 4];
															byReturnVal[8] = SAMVersionRes[ii + 5];
															byReturnVal[9] = SAMVersionRes[ii + 6];
															break;
														}
													}
												}
											}
										} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
											WriteLog(Level.FINEST, "Negative response received");
											byReturnVal[0] = 28;
											WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
										} else {
											WriteLog(Level.FINEST, "Other response received");
											byReturnVal[0] = 28;
										}

									} else if (byResponse[0] == NACK && byResponse.length >= 10) {
										WriteLog(Level.WARNING, "Nack response received");
										byReturnVal[0] = 31;
										bProceed = false;
										WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
									} else {
										byReturnVal[0] = 31;
										bProceed = false;
										WriteLog(Level.WARNING, "Unknown error");
									}
								} else {
									byReturnVal[0] = 28;
									bProceed = false;
									WriteLog(Level.WARNING, "Command send fail on serial port");
								}
							}
						} else // for contactless card
						{
							// check if card is in channel or not
							int retValue = IsCardInChannel(1000);

							if (retValue == 0) {
								byReturnVal[0] = 10;
								bProceed = false;
							}

							if (bProceed) {
								byte[] byResponse = null;
								byte bySet1 = 0x41, bySet2 = 0x42;

								byte[] byCmdActivateContactLess = new byte[] { STX, 0x00, 0x00, 0x05, CMT, 0x60, 0x30,
										bySet1, bySet2, ETX, CRC };
								byCmdActivateContactLess[byCmdActivateContactLess.length - 1] = (byte) GetBCC(
										byCmdActivateContactLess, byCmdActivateContactLess.length - 1);
								WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmdActivateContactLess);
								if (serialPort.writeBytes(byCmdActivateContactLess)) {
									WriteLog(Level.FINEST, "bytes written on port");

									byte[] byRes1 = serialPort.readBytes(5, Timeout);
									WriteLog(Level.FINEST, "5 bytes read");

									int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
									WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

									byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
									WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

									byResponse = new byte[byRes1.length + byRes2.length];
									System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
									System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);

									// Thread.sleep(500);
									// byResponse = serialPort.readBytes();

									WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);

									serialPort.writeByte(ACK);

									if (byResponse[0] == ACK) {
										WriteLog(Level.WARNING, "Ack response received");
										
										if (byResponse[ResType_Pos] == PosRes) {
											WriteLog(Level.FINEST, "Positive response received");
											switch (byResponse[st0_Pos]) // channel status
											{
											case '0':
												WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
												break;
											case '1':
												WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
												break;
											case '2':
												WriteLog(Level.INFO,
														"CardPos- " + ST0.One_card_on_RF_IC_position.toString());
												break;
											}
											switch (byResponse[st1_Pos]) // stacker status
											{
											case '0':
												WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
												break;
											case '1':
												WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
												break;
											case '2':
												WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
												break;
											}
											switch (byResponse[st2_Pos]) // rejection bin status
											{
											case '0':
												WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
												break;
											case '1':
												WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
												break;
											}
	
											byReturnVal[0] = 0;
	
											switch (byResponse[11]) // RTYPE
											{
											case 0x41: {
												byReturnVal[1] = 1; // need confirmation
												WriteLog(Level.FINEST, "ISO_IEC_14443_Type_A_PROTOCOL");
											}
												break;
											case 0x42: {
												byReturnVal[1] = 2; // need confirmation
												WriteLog(Level.FINEST, "ISO_IEC_14443_Type_B_PROTOCOL");
											}
												break;
											case 0x4D: {
												switch (String.format("%02X%02X", byResponse[12], byResponse[13])) // ATQA
												{
												case "0044":
													byReturnVal[1] = 3; // Ultralight card
													WriteLog(Level.FINEST, "MIFARE_ULTRALIGHT_CARD");
													break;
												case "0004":
													byReturnVal[1] = 1; // need confirmation
													WriteLog(Level.FINEST, "MIFARE_S50_1K_CARD");
													break;
												case "0002":
													byReturnVal[1] = 2; // need confirmation
													WriteLog(Level.FINEST, "MIFARE_S70_4K_CARD");
													break;
												}
											}
												break;
											}
	
											// size of UID
											byReturnVal[2] = byResponse[14];
											WriteLog(Level.FINEST, "UID length- " + byResponse[14]);
	
											// UID bytes copied according to length
											System.arraycopy(byResponse, 15, byReturnVal, 3, byResponse[14]);
										} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
											WriteLog(Level.FINEST, "Negative response received");
											byReturnVal[0] = 28;
											WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
										} else {
											WriteLog(Level.FINEST, "Other response received");
											byReturnVal[0] = 28;
										}

									} else if (byResponse[0] == NACK && byResponse.length >= 10) {
										WriteLog(Level.WARNING, "Nack response received");
										byReturnVal[0] = 1;
										bProceed = false;
										WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
									} else {
										byReturnVal[0] = 31;
										bProceed = false;
										WriteLog(Level.WARNING, "Unknown error");
									}
								} else {
									byReturnVal[0] = 28;
									bProceed = false;
									WriteLog(Level.WARNING, "Command send fail on serial port");
								}
							}
						}
					} else {
						byReturnVal[0] = 20;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}

			} else if (objDeviceType == Device.CardReaderWriter) {
				if (bProceed) {
					if (serialPort_CardRW != null) {
						// Step1 Send CMD for Select SAM
						// Step2 Send CMD for Power On/Activate, Power Off/Deactivate

						int i = 0;

						java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

						if (CardTechType == 1)// Slot select for contact card only
						{

							/*
							 * int retValue=IsCardInChannel(1000);
							 * 
							 * if(retValue==0) { byte[] ret=new byte[1]; ret[0]=10; return ret; }
							 */

							// Step1 CMD for Select SAM
							WriteLog(Level.INFO, "Contact Card");
							Cm_CardRW = 0x49;
							Pm_CardRW = 0x50;
							St0_CardRW[0] = St1_CardRW[0] = 0;
							TxDataLen_CardRW = 1;
							if (SAMSlotId == 1) {
								WriteLog(Level.INFO, "First SAM Slot");
								TxData_CardRW[0] = 0x30;
							} else if (SAMSlotId == 2) {
								WriteLog(Level.INFO, "Second SAM Slot");
								TxData_CardRW[0] = 0x31;
							} else if (SAMSlotId == 3) {
								WriteLog(Level.INFO, "Third SAM Slot");
								TxData_CardRW[0] = 0x32;
							} else {
								WriteLog(Level.INFO, "Forth SAM Slot");
								TxData_CardRW[0] = 0x33;
							}

							i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
									St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
							if (i == 0) {
								if (ReType_CardRW[0] == 0x50) {
									WriteLog(Level.WARNING, "Ack response received");
									java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
									java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
									java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
									java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
									java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
									java.util.Arrays.fill(RxDataLen_CardRW, 0x00);
									Cm_CardRW = 0x49;
									Pm_CardRW = 0x40;
									TxDataLen_CardRW = 1;
									TxData_CardRW[0] = 0x33;

									St0_CardRW[0] = St1_CardRW[0] = 0;
									i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW,
											ReType_CardRW, St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW,
											Timeout);
									if (i == 0) {
										if (ReType_CardRW[0] == 0x50) {
											WriteLog(Level.WARNING, "Ack response received");
											byReturnVal[0] = 0;// Device connected successfully
											// byReturnVal[1] = 1;// Type of contact card... currently hardcore
											// byReturnVal[2] = (byte) RxDataLen_CardRW[0];

											for (int responseLen = 0; responseLen < RxDataLen_CardRW[0]; responseLen++) {
												// byReturnVal[responseLen + 3] = RxData_CardRW[responseLen];
											}
											// getUID

											byte[] SAMVersion = new byte[] { (byte) 0x80, 0x60, 0x00, 0x00, 0x00 };
											byte[] SAMVersionRes;
											SAMVersionRes = XChangeAPDU(CardTechType, SAMVersion, SAMSlotId, Timeout);
											if (SAMVersionRes.length > 3) {
												if (SAMVersionRes[SAMVersionRes.length - 3] == (byte) 0xa2)
													byReturnVal[1] = 2;
												else if (SAMVersionRes[SAMVersionRes.length - 3] == (byte) 0xa1)
													byReturnVal[1] = 1;
												else
													byReturnVal[1] = 0;
											} else
												byReturnVal[1] = 0;

											for (int ii = 0; ii < SAMVersionRes.length; ii++) {
												if (SAMVersionRes[ii] == 0x04) {
													if ((ii + 6) < SAMVersionRes.length) {

														if (SAMVersionRes[ii + 6] == (byte) 0x80) {
															byReturnVal[2] = 7;
															byReturnVal[3] = SAMVersionRes[ii];
															byReturnVal[4] = SAMVersionRes[ii + 1];
															byReturnVal[5] = SAMVersionRes[ii + 2];
															byReturnVal[6] = SAMVersionRes[ii + 3];
															byReturnVal[7] = SAMVersionRes[ii + 4];
															byReturnVal[8] = SAMVersionRes[ii + 5];
															byReturnVal[9] = SAMVersionRes[ii + 6];
															break;
														}
													}
												}
											}

										} else {
											WriteLog(Level.WARNING, "Ack response not received");
											byReturnVal[0] = 28;// communication failure
										}
									} else {
										byReturnVal[0] = (byte) i;// other error
									}
								} else {
									WriteLog(Level.WARNING, "Ack response not received");
									byReturnVal[0] = 28;// communication failure
								}
							} else {
								WriteLog(Level.WARNING, "Unknown error");
								byReturnVal[0] = (byte) i;// other error
							}
						} else// Activate Contact less card
						{

							int retValue = IsCardInChannel(1000);

							if (retValue == 0) {
								byte[] ret = new byte[1];
								ret[0] = 10;
								return ret;
							}

							WriteLog(Level.INFO, "Contact less Card");
							java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
							java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
							java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
							java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
							java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
							java.util.Arrays.fill(RxDataLen_CardRW, 0x00);
							Cm_CardRW = (byte) 0x5A;
							Pm_CardRW = 0x30;
							St0_CardRW[0] = St1_CardRW[0] = 0;
							TxDataLen_CardRW = 3;
							RxDataLen_CardRW[0] = 0;

							TxData_CardRW[0] = 0x41;
							TxData_CardRW[1] = 0x42;
							TxData_CardRW[2] = 0x4D;

							i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
									St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
							if (i == 0) {
								if (ReType_CardRW[0] == 0x50) {
									WriteLog(Level.WARNING, "Ack response received");
									byReturnVal[0] = 0;// Device connected successfully

									switch (RxData_CardRW[0]) {
									case 0x4d: // mifare Series Card
									{
										byReturnVal[1] = 1;// Type of contactless card
										byReturnVal[2] = (byte) 7;// RxDataLen_CardRW[0];
										for (int n = 0; n < 7; n++) {
											byReturnVal[3 + n] += RxData_CardRW[n + 4];
										}
									}
										break;
									case 0x41: // type A card
									{
										byReturnVal[1] = 2;// Type of contactless card
										byReturnVal[2] = (byte) 7;// RxDataLen_CardRW[0];
										for (int n = 0; n < 7; n++) {
											byReturnVal[3 + n] += RxData_CardRW[n + 4];
										}
									}
										break;
									case 0x42: // type B card
									{
										byReturnVal[1] = 3;// Type of contactless card
										byReturnVal[2] = (byte) 7;// RxDataLen_CardRW[0];
										for (int n = 0; n < 7; n++) {
											byReturnVal[3 + n] += RxData_CardRW[n + 4];
										}
									}
										break;
									}
								} else {
									WriteLog(Level.WARNING, "Ack response not received");
									byReturnVal[0] = 28;// communication failure
								}
							} else {
								WriteLog(Level.WARNING, "Unknown error");
								byReturnVal[0] = (byte) i;// other error
							}
						}
					} else {
						byReturnVal[0] = 28;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			}
		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				byReturnVal[0] = 18;
			} else {
				byReturnVal[0] = 31;
			}

			WriteLog(Level.WARNING, e2.getMessage());
			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}
		WriteLog(Level.FINEST, "Return");
		byte[] byResNew = new byte[10];
		for (int j = 0; j < byResNew.length; j++) {
			byResNew[j] = byReturnVal[j];
		}
		return byResNew;
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * DeactivateCard: Deactivate an already activated contactless Card and Contact
	 * Card. Input Param: CardTechType : 0 (contact less), 1 (Contact Card)
	 * SAMSlotId : 1 (First SAM Slot), 2 (Second SAM Slot) Timeout Output Param: int
	 */

	public int DeactivateCard(int CardTechType, int SAMSlotId, int Timeout) {
		int nReturnVal = 0;
		boolean bProceed = true;

		try {
			MyFormatter.strSourceMethodName = "DeactivateCard";
			WriteLog(Level.INFO,
					"CardTechType: " + CardTechType + "; SAMSlotId: " + SAMSlotId + "; Timeout: " + Timeout);

			if (bConnected == false) {
				WriteLog(Level.WARNING, "Device not yet connected");
				nReturnVal = 20;// Device not yet connected
				bProceed = false;
			} else if (CardTechType < 0 || CardTechType > 1) {
				nReturnVal = 31;
				WriteLog(Level.WARNING, "Invalid CardTechType: " + CardTechType);
				bProceed = false;
			}

			if (CardTechType == 0)// Contactless
			{
				SAMSlotId = 0;// always 0 for Contactless
			} else {
				if (SAMSlotId < 1 || SAMSlotId > 4) {
					nReturnVal = 31;
					WriteLog(Level.WARNING, "Invalid SAMSlotId: " + SAMSlotId);
					bProceed = false;
				}
			}

			if (objDeviceType == Device.CardDispenser) {

				if (bProceed) {
					if (serialPort != null) {
						WriteLog(Level.FINEST, "Class object initialized");

						if (CardTechType == 1) // Slot select for contact card only
						{
							byte[] byResponse = null;
							byte bySAMPort = 0x00;

							if (SAMSlotId == 1) {
								WriteLog(Level.INFO, "First SAM Slot");
								bySAMPort = 0x30;
							} else if (SAMSlotId == 2) {
								WriteLog(Level.INFO, "Second SAM Slot");
								bySAMPort = 0x31;
							} else if (SAMSlotId == 3) {
								WriteLog(Level.INFO, "Third SAM Slot");
								bySAMPort = 0x32;
							} else {
								WriteLog(Level.INFO, "Fourth SAM Slot");
								bySAMPort = 0x33;
							}

							byte[] byCmdSamSlotSelect = new byte[] { STX, 0x00, 0x00, 0x04, CMT, 0x52, 0x40, bySAMPort,
									ETX, CRC };
							byCmdSamSlotSelect[byCmdSamSlotSelect.length - 1] = (byte) GetBCC(byCmdSamSlotSelect,
									byCmdSamSlotSelect.length - 1);
							WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmdSamSlotSelect);
							if (serialPort.writeBytes(byCmdSamSlotSelect)) {
								WriteLog(Level.FINEST, "bytes written on port");

								byte[] byRes1 = serialPort.readBytes(5, Timeout);
								WriteLog(Level.FINEST, "5 bytes read");

								int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
								WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

								byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
								WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

								byResponse = new byte[byRes1.length + byRes2.length];
								System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
								System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
								WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
								// Thread.sleep(500);
								// byResponse = serialPort.readBytes();

								serialPort.writeByte(ACK);

								if (byResponse[0] == ACK) {
									WriteLog(Level.WARNING, "Ack response received");
									
									if (byResponse[ResType_Pos] == PosRes) {
										WriteLog(Level.FINEST, "Positive response received");
										switch (byResponse[st0_Pos]) // channel status
										{
										case '0':
											WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
											break;
										case '1':
											WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
											break;
										case '2':
											WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
											break;
										}
										switch (byResponse[st1_Pos]) // stacker status
										{
										case '0':
											WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
											break;
										case '1':
											WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
											break;
										case '2':
											WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
											break;
										}
										switch (byResponse[st2_Pos]) // rejection bin status
										{
										case '0':
											WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
											break;
										case '1':
											WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
											break;
										}
									} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
										WriteLog(Level.FINEST, "Negative response received");
										nReturnVal = 28;
										WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
									} else {
										WriteLog(Level.FINEST, "Other response received");
										nReturnVal = 28;
									}

								} else if (byResponse[0] == NACK && byResponse.length >= 10) {
									WriteLog(Level.WARNING, "Nack response received");
									nReturnVal = 31;
									bProceed = false;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									nReturnVal = 31;
									bProceed = false;
									WriteLog(Level.WARNING, "Unknown error");
								}
							} else {
								nReturnVal = 28;
								bProceed = false;
								WriteLog(Level.WARNING, "Command send fail on serial port");
							}

							if (bProceed) {
								byte[] byCmdDeActivate = new byte[] { STX, 0x00, 0x00, 0x03, CMT, 0x52, 0x31, ETX,
										CRC };
								byCmdDeActivate[byCmdDeActivate.length - 1] = (byte) GetBCC(byCmdDeActivate,
										byCmdDeActivate.length - 1);
								WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmdDeActivate);
								if (serialPort.writeBytes(byCmdDeActivate)) {
									WriteLog(Level.FINEST, "bytes written on port");

									byte[] byRes1 = serialPort.readBytes(5, Timeout);
									WriteLog(Level.FINEST, "5 bytes read");

									int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
									WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

									byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
									WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

									byResponse = new byte[byRes1.length + byRes2.length];
									System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
									System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
									WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
									// Thread.sleep(500);
									// byResponse = serialPort.readBytes();

									serialPort.writeByte(ACK);

									if (byResponse[0] == ACK) {
										WriteLog(Level.WARNING, "Ack response received");
										
										if (byResponse[ResType_Pos] == PosRes) {
											WriteLog(Level.FINEST, "Positive response received");

											nReturnVal = 0;// Device connected successfully
											switch (byResponse[st0_Pos]) // channel status
											{
											case '0':
												WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
												break;
											case '1':
												WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
												break;
											case '2':
												WriteLog(Level.INFO,
														"CardPos- " + ST0.One_card_on_RF_IC_position.toString());
												break;
											}
											switch (byResponse[st1_Pos]) // stacker status
											{
											case '0':
												WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
												break;
											case '1':
												WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
												break;
											case '2':
												WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
												break;
											}
											switch (byResponse[st2_Pos]) // rejection bin status
											{
											case '0':
												WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
												break;
											case '1':
												WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
												break;
											}
										} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
											WriteLog(Level.FINEST, "Negative response received");
											nReturnVal = 28;
											WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
										} else {
											WriteLog(Level.FINEST, "Other response received");
											nReturnVal = 28;
										}

									} else if (byResponse[0] == NACK && byResponse.length >= 10) {
										WriteLog(Level.WARNING, "Nack response received");
										nReturnVal = 31;
										bProceed = false;
										WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
									} else {
										nReturnVal = 31;
										bProceed = false;
										WriteLog(Level.WARNING, "Unknown error");
									}
								} else {
									nReturnVal = 28;
									bProceed = false;
									WriteLog(Level.WARNING, "Command send fail on serial port");
								}
							}
						} else // for contactless card
						{
							// check if card is in channel or not
							int retValue = IsCardInChannel(1000);

							if (retValue == 0) {
								nReturnVal = 10;
								bProceed = false;
							}

							if (bProceed) {
								byte[] byResponse = null;
								byte bySet1 = 0x00, bySet2 = 0x00;

								byte[] byCmdDeActivateContactLess = new byte[] { STX, 0x00, 0x00, 0x03, CMT, 0x60, 0x31,
										ETX, CRC };
								byCmdDeActivateContactLess[byCmdDeActivateContactLess.length - 1] = (byte) GetBCC(
										byCmdDeActivateContactLess, byCmdDeActivateContactLess.length - 1);
								WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmdDeActivateContactLess);
								if (serialPort.writeBytes(byCmdDeActivateContactLess)) {
									WriteLog(Level.FINEST, "bytes written on port");

									byte[] byRes1 = serialPort.readBytes(5, Timeout);
									WriteLog(Level.FINEST, "5 bytes read");

									int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
									WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

									byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
									WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

									byResponse = new byte[byRes1.length + byRes2.length];
									System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
									System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
									WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
									serialPort.writeByte(ACK);

									if (byResponse[0] == ACK) {
										WriteLog(Level.WARNING, "Ack response received");
										
										if (byResponse[ResType_Pos] == PosRes) {
											WriteLog(Level.FINEST, "Positive response received");
											switch (byResponse[st0_Pos]) // channel status
											{
											case '0':
												WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
												break;
											case '1':
												WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
												break;
											case '2':
												WriteLog(Level.INFO,
														"CardPos- " + ST0.One_card_on_RF_IC_position.toString());
												break;
											}
											switch (byResponse[st1_Pos]) // stacker status
											{
											case '0':
												WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
												break;
											case '1':
												WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
												break;
											case '2':
												WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
												break;
											}
											switch (byResponse[st2_Pos]) // rejection bin status
											{
											case '0':
												WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
												break;
											case '1':
												WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
												break;
											}
	
											nReturnVal = 0;
										} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
											WriteLog(Level.FINEST, "Negative response received");
											nReturnVal = 28;
											WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
										} else {
											WriteLog(Level.FINEST, "Other response received");
											nReturnVal = 28;
										}

									} else if (byResponse[0] == NACK && byResponse.length >= 10) {
										WriteLog(Level.WARNING, "Nack response received");
										nReturnVal = 1;
										bProceed = false;
										WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
									} else {
										nReturnVal = 31;
										bProceed = false;
										WriteLog(Level.WARNING, "Unknown error");
									}
								} else {
									nReturnVal = 28;
									bProceed = false;
									WriteLog(Level.WARNING, "Command send fail on serial port");
								}
							}
						}
					} else {
						nReturnVal = 28;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}

			} else if (objDeviceType == Device.CardReaderWriter) {
				if (bProceed) {
					if (serialPort_CardRW != null) {

						// Step1 Send CMD for Select SAM
						// Step2 Send CMD for Power On/Activate, Power Off/Deactivate

						int i = 0;

						java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

						if (CardTechType == 1)// Slot select for contact card only
						{
							WriteLog(Level.INFO, "Contact Card");
							// Step1 CMD for Select SAM
							Cm_CardRW = 0x49;
							Pm_CardRW = 0x50;
							St0_CardRW[0] = St1_CardRW[0] = 0;
							TxDataLen_CardRW = 1;
							if (SAMSlotId == 1) {
								WriteLog(Level.INFO, "First SAM Slot");
								TxData_CardRW[0] = 0x30;
							} else if (SAMSlotId == 2) {
								WriteLog(Level.INFO, "Second SAM Slot");
								TxData_CardRW[0] = 0x31;
							} else if (SAMSlotId == 3) {
								WriteLog(Level.INFO, "Third SAM Slot");
								TxData_CardRW[0] = 0x32;
							} else {
								WriteLog(Level.INFO, "Fourth SAM Slot");
								TxData_CardRW[0] = 0x33;
							}

							i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
									St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
							if (i == 0) {
								if (ReType_CardRW[0] == 0x50) {
									WriteLog(Level.WARNING, "Ack response received");
									java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
									java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
									java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
									java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
									java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
									java.util.Arrays.fill(RxDataLen_CardRW, 0x00);
									Cm_CardRW = 0x49;
									Pm_CardRW = 0x41; // Deactivate SAM
									TxDataLen_CardRW = 0;
									TxData_CardRW[0] = 0x00;

									St0_CardRW[0] = St1_CardRW[0] = 0;
									i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW,
											ReType_CardRW, St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW,
											Timeout);
									if (i == 0) {
										if (ReType_CardRW[0] == 0x50) {
											nReturnVal = 0;// Device connected successfully
											// getATR

										} else {
											nReturnVal = 28;// communication failure
										}
									} else {
										nReturnVal = (byte) i;// other error
									}
								} else {
									WriteLog(Level.WARNING, "Ack response not received");
									nReturnVal = 28;// communication failure
								}
							} else {
								WriteLog(Level.WARNING, "Unknown error");
								nReturnVal = (byte) i;// other error
							}
						} else// deactivate Contact less card
						{
							WriteLog(Level.INFO, "Contact less Card");
							java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
							java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
							java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
							java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
							java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
							java.util.Arrays.fill(RxDataLen_CardRW, 0x00);
							Cm_CardRW = (byte) 0x5A;
							Pm_CardRW = 0x31;
							St0_CardRW[0] = St1_CardRW[0] = 0;
							TxDataLen_CardRW = 0;
							RxDataLen_CardRW[0] = 0;

							i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
									St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
							if (i == 0) {
								if (ReType_CardRW[0] == 0x50) {
									WriteLog(Level.WARNING, "Ack response received");
									nReturnVal = 0;// Device connected successfully
								} else {
									WriteLog(Level.WARNING, "Ack response not received");
									nReturnVal = 28;// communication failure
								}
							} else {
								WriteLog(Level.WARNING, "Unknown error");
								nReturnVal = (byte) i;// other error
							}
						}
					} else {
						nReturnVal = 28;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			}
		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				nReturnVal = 18;
			} else {
				nReturnVal = 31;
			}

			WriteLog(Level.WARNING, e2.getMessage());
			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}
		WriteLog(Level.FINEST, "Return value- " + nReturnVal);

		return nReturnVal;
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * DisconnectDevice: Disconnect the device . Input Param: Timeout Output Param:
	 * int
	 */
	/*
	 * public int DisconnectDevice_SmartCard_RW(int Timeout) { int iReturnVal = -1;
	 * boolean bProceed = false;
	 * 
	 * try { MyFormatter.strSourceMethodName = "DisconnectDevice";
	 * WriteLog(Level.INFO, "Timeout: " + Timeout);
	 * 
	 * // device already connected if (bConnected == false) {
	 * WriteLog(Level.WARNING, "Device not yet connected"); iReturnVal = 20;//
	 * Device not yet connected return iReturnVal; }
	 * 
	 * if (bProceed) {
	 * 
	 * if (serialPort_CardRW != null) { // opening port if
	 * (serialPort_CardRW.closePort() == true) {
	 * 
	 * iReturnVal = 0; } else { iReturnVal = 25;// + ChannelClearanceMode;
	 * WriteLog(Level.WARNING, "Port could not be opened"); } } else { iReturnVal =
	 * 31; WriteLog(Level.WARNING, "Serial class object not created"); } } } catch
	 * (Exception e2) { if (e2.getMessage().toLowerCase().contains("timeout")) {
	 * iReturnVal = 18; } else { iReturnVal = 31; }
	 * 
	 * WriteLog(Level.WARNING, e2.getMessage()); StringWriter errors = new
	 * StringWriter(); e2.printStackTrace(new PrintWriter(errors));
	 * WriteLog(Level.FINEST, errors.toString()); } WriteLog(Level.FINEST,
	 * "Return value- " + iReturnVal); return iReturnVal; }
	 */

	/////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * XChangeAPDU: XChange APDU for contactless Card and Contact Card. Input Param:
	 * CardTechType : 0 (contact less), 1 (Contact Card) SAMSlotId : 1 (First SAM
	 * Slot), 2 (Second SAM Slot) Timeout Output Param: Byte Array
	 */

	public byte[] XChangeAPDU(int CardTechType, byte[] CommandAPDU, int SAMSlotId, int Timeout) {
		byte[] byReturnVal = new byte[255];
		boolean bProceed = true;
		byte[] byResNew = null;
		int ResponseLength = 0;
		try {
			MyFormatter.strSourceMethodName = "XChangeAPDU";
			WriteLog(Level.INFO, "CardTechType: " + CardTechType + "CommandAPDU len: " + CommandAPDU.length
					+ "; SAMSlotId: " + SAMSlotId + "; Timeout: " + Timeout);

			if (bConnected == false) {
				WriteLog(Level.WARNING, "Device not yet connected");
				byReturnVal[0] = 20;// Device not yet connected
				bProceed = false;
			} else if (CardTechType < 0 || CardTechType > 1) {
				byReturnVal[0] = 31;
				bProceed = false;
				WriteLog(Level.WARNING, "Invalid CardTechType: " + CardTechType);
			} else if (CommandAPDU.length == 0) {
				byReturnVal[0] = 31;
				bProceed = false;
				WriteLog(Level.WARNING, "CommandAPDU not received properly");
			}

			if (CardTechType == 0)// Contactless
			{
				SAMSlotId = 0;// always 0 for Contactless
			} else {
				if (SAMSlotId < 1 || SAMSlotId > 4) {
					byReturnVal[0] = 31;
					bProceed = false;
					WriteLog(Level.WARNING, "Invalid SAMSlotId: " + SAMSlotId);
				}
			}

			if (objDeviceType == Device.CardDispenser) {
				if (bProceed) {

					if (serialPort != null) {
						if (CardTechType == 1)// Slot select for contact card only
						{
							byte[] byResponse = null;
							byte bySAMPort = 0x00;

							if (SAMSlotId == 1) {
								WriteLog(Level.INFO, "First SAM Slot");
								bySAMPort = 0x30;
							} else if (SAMSlotId == 2) {
								WriteLog(Level.INFO, "Second SAM Slot");
								bySAMPort = 0x31;
							} else if (SAMSlotId == 3) {
								WriteLog(Level.INFO, "Third SAM Slot");
								bySAMPort = 0x32;
							} else {
								WriteLog(Level.INFO, "Fourth SAM Slot");
								bySAMPort = 0x33;
							}

							byte[] byCmdSamSlotSelect = new byte[] { STX, 0x00, 0x00, 0x04, CMT, 0x52, 0x40, bySAMPort,
									ETX, CRC };
							byCmdSamSlotSelect[byCmdSamSlotSelect.length - 1] = (byte) GetBCC(byCmdSamSlotSelect,
									byCmdSamSlotSelect.length - 1);
							WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmdSamSlotSelect);
							if (serialPort.writeBytes(byCmdSamSlotSelect)) {
								WriteLog(Level.FINEST, "bytes written on port");

								byte[] byRes1 = serialPort.readBytes(5, Timeout);
								WriteLog(Level.FINEST, "5 bytes read");

								int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
								WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

								byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
								WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

								byResponse = new byte[byRes1.length + byRes2.length];
								System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
								System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
								WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
								// Thread.sleep(500);
								// byResponse = serialPort.readBytes();

								serialPort.writeByte(ACK);

								if (byResponse[0] == ACK) {
									WriteLog(Level.WARNING, "Ack response received");
									
									if (byResponse[ResType_Pos] == PosRes) {
										WriteLog(Level.FINEST, "Positive response received");
										switch (byResponse[st0_Pos]) // channel status
										{
										case '0':
											WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
											break;
										case '1':
											WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
											break;
										case '2':
											WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
											break;
										}
										switch (byResponse[st1_Pos]) // stacker status
										{
										case '0':
											WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
											break;
										case '1':
											WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
											break;
										case '2':
											WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
											break;
										}
										switch (byResponse[st2_Pos]) // rejection bin status
										{
										case '0':
											WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
											break;
										case '1':
											WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
											break;
										}
									} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
										WriteLog(Level.FINEST, "Negative response received");
										byReturnVal[0] = 28;
										WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
									} else {
										WriteLog(Level.FINEST, "Other response received");
										byReturnVal[0] = 28;
									}

								} else if (byResponse[0] == NACK && byResponse.length >= 10) {
									WriteLog(Level.WARNING, "Nack response received");
									byReturnVal[0] = 31;
									bProceed = false;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									byReturnVal[0] = 31;
									bProceed = false;
									WriteLog(Level.WARNING, "Unknown error");
								}
							} else {
								byReturnVal[0] = 28;
								bProceed = false;
								WriteLog(Level.WARNING, "Command send fail on serial port");
							}

							if (bProceed) {
								byte[] byCmdPreAPDU = new byte[] { STX, 0x00, 0x00, 0x03, CMT, 0x52, 0x39 };
								byte[] byCmdPostAPDU = new byte[] { ETX, CRC };

								byte[] byCmdXchangeAPDU = new byte[9 + CommandAPDU.length];// { STX, 0x00, 0x00, 0x04,
																							// CMT, 0x52, 0x39, 0x33,
																							// ETX, CRC };

								// copy pre APDU in final cmd
								System.arraycopy(byCmdPreAPDU, 0, byCmdXchangeAPDU, 0, 7);

								// set length according to received APDU in cmd
								byCmdXchangeAPDU[3] = (byte) (3 + CommandAPDU.length);

								// copy received APDU
								System.arraycopy(CommandAPDU, 0, byCmdXchangeAPDU, 7, CommandAPDU.length);

								// copy post APDU in final cmd
								System.arraycopy(byCmdPostAPDU, 0, byCmdXchangeAPDU, 7 + CommandAPDU.length, 2);

								byCmdXchangeAPDU[byCmdXchangeAPDU.length - 1] = (byte) GetBCC(byCmdXchangeAPDU,
										byCmdXchangeAPDU.length - 1);

								WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmdXchangeAPDU);

								if (serialPort.writeBytes(byCmdXchangeAPDU)) {
									WriteLog(Level.FINEST, "bytes written on port");

									byte[] byRes1 = serialPort.readBytes(5, Timeout);
									WriteLog(Level.FINEST, "5 bytes read");

									int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
									WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

									byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
									WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

									byResponse = new byte[byRes1.length + byRes2.length];
									System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
									System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
									WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
									// Thread.sleep(500);
									// byResponse = serialPort.readBytes();

									serialPort.writeByte(ACK);

									if (byResponse[0] == ACK) {
										WriteLog(Level.WARNING, "Ack response received");

										if (byResponse[ResType_Pos] == PosRes) {
											WriteLog(Level.FINEST, "Positive response received");
											switch (byResponse[st0_Pos]) // channel status
											{
											case '0':
												WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
												break;
											case '1':
												WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
												break;
											case '2':
												WriteLog(Level.INFO,
														"CardPos- " + ST0.One_card_on_RF_IC_position.toString());
												break;
											}
											switch (byResponse[st1_Pos]) // stacker status
											{
											case '0':
												WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
												break;
											case '1':
												WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
												break;
											case '2':
												WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
												break;
											}
											switch (byResponse[st2_Pos]) // rejection bin status
											{
											case '0':
												WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
												break;
											case '1':
												WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
												break;
											}
	
											byReturnVal[0] = 0;
	
											if (byResponse.length > 12) {
												// option 1 to copy R-APDU
												// System.arraycopy(byResponse, 11, byReturnVal, 1, byResponse.length - 13);
	
												// option 2 to copy R-APDU
												for (int iReturnValIndex = 0, iResIndex = 11; iResIndex < byResponse.length
														- 2; iReturnValIndex++, iResIndex++)
													byReturnVal[1 + iReturnValIndex] = byResponse[iResIndex];
												ResponseLength = byResponse.length - 2;
											}
										} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
											WriteLog(Level.FINEST, "Negative response received");
											byReturnVal[0] = 28;
											WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
										} else {
											WriteLog(Level.FINEST, "Other response received");
											byReturnVal[0] = 28;
										}

									} else if (byResponse[0] == NACK && byResponse.length >= 10) {
										WriteLog(Level.WARNING, "Nack response received");
										byReturnVal[0] = 31;
										bProceed = false;
										WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
									} else {
										byReturnVal[0] = 31;
										bProceed = false;
										WriteLog(Level.WARNING, "Unknown error");
									}
								} else {
									byReturnVal[0] = 28;
									bProceed = false;
									WriteLog(Level.WARNING, "Command send fail on serial port");
								}
							}
						} else// Contact less card
						{
							// check if card is in channel or not
							int retValue = IsCardInChannel(1000);

							if (retValue == 0) {
								byReturnVal[0] = 10;
								bProceed = false;
							}

							if (bProceed) {
								byte[] byResponse = null;

								byte[] byCmdPreAPDU = new byte[] { STX, 0x00, 0x00, 0x03, CMT, 0x60, 0x34 }; // 0x34 for
																												// type
																												// A
								byte[] byCmdPostAPDU = new byte[] { ETX, CRC };

								byte[] byCmdXchangeAPDU = new byte[9 + CommandAPDU.length];// { STX, 0x00, 0x00, 0x04,
																							// CMT, 0x52, 0x39, 0x33,
																							// ETX, CRC };

								// copy pre APDU in final cmd
								System.arraycopy(byCmdPreAPDU, 0, byCmdXchangeAPDU, 0, 7);

								// set length according to received APDU in cmd
								byCmdXchangeAPDU[3] = (byte) (3 + CommandAPDU.length);

								// copy received APDU
								System.arraycopy(CommandAPDU, 0, byCmdXchangeAPDU, 7, CommandAPDU.length);

								// copy post APDU in final cmd
								System.arraycopy(byCmdPostAPDU, 0, byCmdXchangeAPDU, 7 + CommandAPDU.length, 2);

								byCmdXchangeAPDU[byCmdXchangeAPDU.length - 1] = (byte) GetBCC(byCmdXchangeAPDU,
										byCmdXchangeAPDU.length - 1);
								WriteReqResLog(MyFormatter.strSourceMethodName + " CMD ", byCmdXchangeAPDU);
								if (serialPort.writeBytes(byCmdXchangeAPDU)) {
									WriteLog(Level.FINEST, "bytes written on port");

									byte[] byRes1 = serialPort.readBytes(5, Timeout);
									WriteLog(Level.FINEST, "5 bytes read");

									int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
									WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

									byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
									WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

									byResponse = new byte[byRes1.length + byRes2.length];
									System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
									System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
									WriteReqResLog(MyFormatter.strSourceMethodName + " RES ", byResponse);
									serialPort.writeByte(ACK);

									if (byResponse[0] == ACK) {
										WriteLog(Level.WARNING, "Ack response received");
										
										if (byResponse[ResType_Pos] == PosRes) {
											WriteLog(Level.FINEST, "Positive response received");
											switch (byResponse[st0_Pos]) // channel status
											{
											case '0':
												WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
												break;
											case '1':
												WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
												break;
											case '2':
												WriteLog(Level.INFO,
														"CardPos- " + ST0.One_card_on_RF_IC_position.toString());
												break;
											}
											switch (byResponse[st1_Pos]) // stacker status
											{
											case '0':
												WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
												break;
											case '1':
												WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
												break;
											case '2':
												WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
												break;
											}
											switch (byResponse[st2_Pos]) // rejection bin status
											{
											case '0':
												WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
												break;
											case '1':
												WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
												break;
											}
	
											byReturnVal[0] = 0;
	
											if (byResponse.length > 12) {
												// option 1 to copy R-APDU
												// System.arraycopy(byResponse, 11, byReturnVal, 1, byResponse.length - 13);
	
												// option 2 to copy R-APDU
												for (int iReturnValIndex = 0, iResIndex = 11; iResIndex < byResponse.length
														- 2; iReturnValIndex++, iResIndex++)
													byReturnVal[1 + iReturnValIndex] = byResponse[iResIndex];
												ResponseLength = byResponse.length - 2;
											}
										} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
											WriteLog(Level.FINEST, "Negative response received");
											byReturnVal[0] = 28;
											WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
										} else {
											WriteLog(Level.FINEST, "Other response received");
											byReturnVal[0] = 28;
										}

									} else if (byResponse[0] == NACK && byResponse.length >= 10) {
										WriteLog(Level.WARNING, "Nack response received");
										byReturnVal[0] = 1;
										bProceed = false;
										WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
									} else {
										byReturnVal[0] = 31;
										bProceed = false;
										WriteLog(Level.WARNING, "Unknown error");
									}
								} else {
									byReturnVal[0] = 28;
									bProceed = false;
									WriteLog(Level.WARNING, "Command send fail on serial port");
								}
							}
						}
					} else {
						byReturnVal[0] = 28;
						WriteLog(Level.WARNING, "Serial class object not created");
					}

				}
				byResNew = new byte[ResponseLength + 1];
				for (int j = 0; j < byResNew.length; j++) {
					byResNew[j] = byReturnVal[j];
				}

			} else if (objDeviceType == Device.CardReaderWriter) {
				if (bProceed) {
					if (serialPort_CardRW != null) {

						// Step1 Send CMD for Select SAM
						// Step2 Send CMD for Power On/Activate, Power Off/Deactivate

						int i = 0;

						java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

						if (CardTechType == 1)// Slot select for contact card only
						{
							WriteLog(Level.INFO, "Contact Card");
							// Step1 CMD for Select SAM
							Cm_CardRW = 0x49;
							Pm_CardRW = 0x50;
							St0_CardRW[0] = St1_CardRW[0] = 0;
							TxDataLen_CardRW = 1;
							if (SAMSlotId == 1) {
								WriteLog(Level.INFO, "First SAM Slot");
								TxData_CardRW[0] = 0x30;
							} else if (SAMSlotId == 2) {
								WriteLog(Level.INFO, "Second SAM Slot");
								TxData_CardRW[0] = 0x31;
							} else if (SAMSlotId == 3) {
								WriteLog(Level.INFO, "Third SAM Slot");
								TxData_CardRW[0] = 0x32;
							} else {
								WriteLog(Level.INFO, "Forth SAM Slot");
								TxData_CardRW[0] = 0x33;
							}

							i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
									St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
							if (i == 0) {
								if (ReType_CardRW[0] == 0x50) {
									WriteLog(Level.WARNING, "Ack response received");
									java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
									java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
									java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
									java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
									java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
									java.util.Arrays.fill(RxDataLen_CardRW, 0x00);
									Cm_CardRW = 0x49;
									Pm_CardRW = 0x49;
									TxDataLen_CardRW = (byte) CommandAPDU.length;
									int n = 0;
									for (n = 0; n < TxDataLen_CardRW; n++)
										TxData_CardRW[n] = CommandAPDU[n];

									St0_CardRW[0] = St1_CardRW[0] = 0;
									i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW,
											ReType_CardRW, St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW,
											Timeout);
									if (i == 0) {
										if (ReType_CardRW[0] == 0x50) {
											byReturnVal[0] = 0;// Device connected successfully
											for (int responseLen = 0; responseLen < RxDataLen_CardRW[0]; responseLen++) {
												byReturnVal[responseLen + 1] = RxData_CardRW[responseLen];
											}
											// getATR

										} else {
											byReturnVal[0] = 28;// communication failure
										}
									} else {
										byReturnVal[0] = (byte) i;// other error
									}
								} else {
									WriteLog(Level.WARNING, "Ack response not received");
									byReturnVal[0] = 28;// communication failure
								}
							} else {
								WriteLog(Level.WARNING, "Unknown error");
								byReturnVal[0] = (byte) i;// other error
							}
						} else// Contact less card
						{
							WriteLog(Level.INFO, "Contact less Card");

							int retValue = IsCardInChannel(1000);

							if (retValue == 0) {
								byte[] ret = new byte[1];
								ret[0] = 10;
								return ret;
							}

							java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
							java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
							java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
							java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
							java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
							java.util.Arrays.fill(RxDataLen_CardRW, 0x00);
							Cm_CardRW = 0x5A;
							Pm_CardRW = 0x34;
							TxDataLen_CardRW = (byte) CommandAPDU.length;
							int n = 0;
							for (n = 0; n < TxDataLen_CardRW; n++)
								TxData_CardRW[n] = CommandAPDU[n];

							St0_CardRW[0] = St1_CardRW[0] = 0;
							i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
									St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
							if (i == 0) {
								if (ReType_CardRW[0] == 0x50) {
									WriteLog(Level.WARNING, "Ack response received");
									byReturnVal[0] = 0;// Device connected successfully
									for (int responseLen = 0; responseLen < RxDataLen_CardRW[0]; responseLen++) {
										byReturnVal[responseLen + 1] = RxData_CardRW[responseLen];
									}
									// getATR

								} else {
									WriteLog(Level.WARNING, "Ack response not received");
									byReturnVal[0] = 28;// communication failure
								}
							} else {
								WriteLog(Level.WARNING, "Unknown error");
								byReturnVal[0] = (byte) i;// other error
							}
						}
					} else {
						byReturnVal[0] = 28;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
				byResNew = new byte[RxDataLen_CardRW[0] + 1];
				for (int j = 0; j < byResNew.length; j++) {
					byResNew[j] = byReturnVal[j];
				}
			}

		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				byReturnVal[0] = 18;
			} else {
				byReturnVal[0] = 31;
			}

			WriteLog(Level.WARNING, e2.getMessage());

			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());

		}
		WriteLog(Level.FINEST, "Return");

		return byResNew;
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	// Purpose of the APT is to read 16 bytes data starting from Ultralight page
	// address of MIFARE Ultralight
	public byte[] ReadUltralightBlock(int Addr, int Timeout) {
		byte[] byReturnVal = new byte[255];
		boolean bProceed = true;

		try {
			MyFormatter.strSourceMethodName = "readUltralightBlock";
			WriteLog(Level.INFO, "Addr: " + Addr + "; Timeout: " + Timeout);

			if (bConnected == false) {
				WriteLog(Level.WARNING, "Device not yet connected");
				byReturnVal[0] = 20;// Device not yet connected
				bProceed = false;
			}

			if (objDeviceType == Device.CardDispenser) {
				if (bProceed) {
					if (serialPort != null) {
						byte[] byResponse = null;
						byte bySectorNo = 0x00;
						byte byBlockNo = 0x00;
						byte byNoOfBlocks = 0x00;

						byte[] byCmdSamSlotSelect = new byte[] { STX, 0x00, 0x00, 0x07, CMT, 0x60, 0x33, 0x00,
								(byte) 0xB0, bySectorNo, byBlockNo, byNoOfBlocks, ETX, CRC };
						byCmdSamSlotSelect[byCmdSamSlotSelect.length - 1] = (byte) GetBCC(byCmdSamSlotSelect,
								byCmdSamSlotSelect.length - 1);

						if (serialPort.writeBytes(byCmdSamSlotSelect)) {
							WriteLog(Level.FINEST, "bytes written on port");

							byte[] byRes1 = serialPort.readBytes(5, Timeout);
							WriteLog(Level.FINEST, "5 bytes read");

							int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
							WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

							byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
							WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

							byResponse = new byte[byRes1.length + byRes2.length];
							System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
							System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);

							// Thread.sleep(500);
							// byResponse = serialPort.readBytes();

							serialPort.writeByte(ACK);

							if (byResponse[0] == ACK) {
								WriteLog(Level.WARNING, "Ack response received");
								
								if (byResponse[ResType_Pos] == PosRes) {
									WriteLog(Level.FINEST, "Positive response received");
									switch (byResponse[st0_Pos]) // channel status
									{
									case '0':
										WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
										break;
									case '2':
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
										break;
									}
									switch (byResponse[st1_Pos]) // stacker status
									{
									case '0':
										WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
										break;
									case '2':
										WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
										break;
									}
									switch (byResponse[st2_Pos]) // rejection bin status
									{
									case '0':
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
										break;
									}
	
									if (byResponse.length > 12) {
										// option 1 to copy R-APDU
										// System.arraycopy(byResponse, 11, byReturnVal, 1, byResponse.length - 13);
	
										// option 2 to copy R-APDU
										for (int iReturnValIndex = 0, iResIndex = 11; iResIndex < byResponse.length
												- 2; iReturnValIndex++, iResIndex++)
											byReturnVal[1 + iReturnValIndex] = byResponse[iResIndex];
									}
								} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
									WriteLog(Level.FINEST, "Negative response received");
									byReturnVal[0] = 28;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									WriteLog(Level.FINEST, "Other response received");
									byReturnVal[0] = 28;
								}

							} else if (byResponse[0] == NACK && byResponse.length >= 10) {
								WriteLog(Level.WARNING, "Nack response received");
								byReturnVal[0] = 31;
								bProceed = false;
								WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
							} else {
								byReturnVal[0] = 31;
								bProceed = false;
								WriteLog(Level.WARNING, "Unknown error");
							}
						} else {
							byReturnVal[0] = 28;
							bProceed = false;
							WriteLog(Level.WARNING, "Command send fail on serial port");
						}
					}
				}
			} else if (objDeviceType == Device.CardReaderWriter) {
				if (bProceed) {
					if (serialPort_CardRW != null) {

						// Step1 Send CMD for Select SAM
						// Step2 Send CMD for Power On/Activate, Power Off/Deactivate

						int i = 0;

						java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

						Cm_CardRW = 0x5A;
						Pm_CardRW = 0x33;

						TxDataLen_CardRW = 2;
						TxData_CardRW[0] = 0x52;// for Read
						TxData_CardRW[1] = (byte) Addr;// address

						St0_CardRW[0] = St1_CardRW[0] = 0;
						i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
								St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
						if (i == 0) {
							if (ReType_CardRW[0] == 0x50) {
								WriteLog(Level.WARNING, "Ack response received");
								byReturnVal[0] = 0;// Device connected successfully
								for (int responseLen = 1; responseLen < RxDataLen_CardRW[0]; responseLen++) {
									byReturnVal[responseLen] = RxData_CardRW[responseLen];
								}
								// getATR

							} else {
								WriteLog(Level.WARNING, "Ack response not received");
								byReturnVal[0] = 28;// communication failure
							}
						} else {
							WriteLog(Level.WARNING, "Unknown error");
							byReturnVal[0] = (byte) i;// other error
						}
					} else {
						byReturnVal[0] = 28;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			}
		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				byReturnVal[0] = 18;
			} else {
				byReturnVal[0] = 31;
			}

			WriteLog(Level.WARNING, e2.getMessage());

			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}
		WriteLog(Level.FINEST, "Return");

		return byReturnVal;
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	// Purpose of the APT is to write 4 bytes data to a specific Ultralight page
	// address of MIFARE Ultralight
	public int WriteUltralightPage(int Addr, byte[] Data, int Timeout) {
		int nReturnVal = 0;
		boolean bProceed = true;

		try {
			MyFormatter.strSourceMethodName = "writeUltralightBlock";
			WriteLog(Level.INFO, "Data len: " + Data.length + "; Timeout: " + Timeout);

			if (bConnected == false) {
				WriteLog(Level.WARNING, "Device not yet connected");
				nReturnVal = 20;// Device not yet connected
				bProceed = false;
			}

			if (objDeviceType == Device.CardDispenser) {
				if (bProceed) {
					if (serialPort != null) {
						byte[] byResponse = null;
						byte bySectorNo = 0x00;
						byte byBlockNo = 0x00;
						byte byNoOfBlocks = 0x00;

						byte[] byPreCmdAPDU = new byte[] { STX, 0x00, 0x00, 0x08, CMT, 0x60, 0x33, 0x00, (byte) 0xD1,
								bySectorNo, byBlockNo, byNoOfBlocks };
						byte[] byPostCmdAPDU = new byte[] { ETX, CRC };

						byte[] byCmdWriteUltraLightData = new byte[14 + Data.length];// { STX, 0x00, 0x00, 0x04,
																						// CMT, 0x52, 0x39, 0x33,
																						// ETX, CRC };

						// copy pre APDU in final cmd
						System.arraycopy(byPreCmdAPDU, 0, byCmdWriteUltraLightData, 0, 7);

						// set length according to received APDU in cmd
						byCmdWriteUltraLightData[3] = (byte) (8 + Data.length);

						// copy received APDU
						System.arraycopy(Data, 0, byCmdWriteUltraLightData, 7, Data.length);

						// copy post APDU in final cmd
						System.arraycopy(byPostCmdAPDU, 0, byCmdWriteUltraLightData, 7 + Data.length, 2);

						byCmdWriteUltraLightData[byCmdWriteUltraLightData.length
								- 1] = (byte) GetBCC(byCmdWriteUltraLightData, byCmdWriteUltraLightData.length - 1);

						if (serialPort.writeBytes(byCmdWriteUltraLightData)) {
							WriteLog(Level.FINEST, "bytes written on port");

							byte[] byRes1 = serialPort.readBytes(5, Timeout);
							WriteLog(Level.FINEST, "5 bytes read");

							int iPendingDataLen = (byRes1[3] * 256) + byRes1[4];
							WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

							byte[] byRes2 = serialPort.readBytes(iPendingDataLen + 2, Timeout);
							WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

							byResponse = new byte[byRes1.length + byRes2.length];
							System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
							System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);

							// Thread.sleep(500);
							// byResponse = serialPort.readBytes();

							serialPort.writeByte(ACK);

							if (byResponse[0] == ACK) {
								WriteLog(Level.WARNING, "Ack response received");
								
								if (byResponse[ResType_Pos] == PosRes) {
									WriteLog(Level.FINEST, "Positive response received");
									switch (byResponse[st0_Pos]) // channel status
									{
									case '0':
										WriteLog(Level.INFO, "CardPos- " + ST0.No_card_in_CRT_571.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_in_gate.toString());
										break;
									case '2':
										WriteLog(Level.INFO, "CardPos- " + ST0.One_card_on_RF_IC_position.toString());
										break;
									}
									switch (byResponse[st1_Pos]) // stacker status
									{
									case '0':
										WriteLog(Level.INFO, "Stacker- " + ST1.No_card_in_stacker.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "Stacker- " + ST1.Few_card_in_stacker.toString());
										break;
									case '2':
										WriteLog(Level.INFO, "Stacker- " + ST1.Enough_cards_in_card_box.toString());
										break;
									}
									switch (byResponse[st2_Pos]) // rejection bin status
									{
									case '0':
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_not_full.toString());
										break;
									case '1':
										WriteLog(Level.INFO, "ErrBin- " + ST2.Error_card_bin_full.toString());
										break;
									}
	
									if (byResponse[11] == 0x00 && byResponse[12] == 0x00) {
										nReturnVal = 0;
									} else {
										nReturnVal = 1;
									}
								} else if (byResponse[ResType_Pos] == NegRes && byResponse.length >= 10) {
									WriteLog(Level.FINEST, "Negative response received");
									nReturnVal = 28;
									WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
								} else {
									WriteLog(Level.FINEST, "Other response received");
									nReturnVal = 28;
								}

							} else if (byResponse[0] == NACK && byResponse.length >= 10) {
								WriteLog(Level.WARNING, "Nack response received");
								nReturnVal = 31;
								bProceed = false;
								WriteLog(Level.FINEST, GetErrorDesc(byResponse[e1_Pos], byResponse[e0_Pos]));
							} else {
								nReturnVal = 31;
								bProceed = false;
								WriteLog(Level.WARNING, "Unknown error");
							}
						} else {
							nReturnVal = 28;
							bProceed = false;
							WriteLog(Level.WARNING, "Command send fail on serial port");
						}
					}
				}
			} else if (objDeviceType == Device.CardReaderWriter) {
				if (bProceed) {
					if (serialPort_CardRW != null) {

						// Step1 Send CMD for Select SAM
						// Step2 Send CMD for Power On/Activate, Power Off/Deactivate

						int i = 0;

						java.util.Arrays.fill(TxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxData_CardRW, (byte) 0x00);
						java.util.Arrays.fill(ReType_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St0_CardRW, (byte) 0x00);
						java.util.Arrays.fill(St1_CardRW, (byte) 0x00);
						java.util.Arrays.fill(RxDataLen_CardRW, 0x00);

						Cm_CardRW = 0x5A;
						Pm_CardRW = 0x33;

						TxDataLen_CardRW = 2;
						TxData_CardRW[0] = 0x57;// for Write
						TxData_CardRW[1] = (byte) Addr;// address

						for (int n = 0; n < Data.length; n++) {
							TxData_CardRW[2 + n] = Data[n];
							TxDataLen_CardRW++;
						}

						St0_CardRW[0] = St1_CardRW[0] = 0;
						i = RS232_ExeCommand(Cm_CardRW, Pm_CardRW, TxDataLen_CardRW, TxData_CardRW, ReType_CardRW,
								St0_CardRW, St1_CardRW, RxDataLen_CardRW, RxData_CardRW, Timeout);
						if (i == 0) {
							if (ReType_CardRW[0] == 0x50) {
								WriteLog(Level.WARNING, "Ack response received");
								nReturnVal = 0;// successfully

							} else {
								WriteLog(Level.WARNING, "Ack response not received");
								nReturnVal = 28;// communication failure
							}
						} else {
							WriteLog(Level.WARNING, "Unknown error");
							nReturnVal = (byte) i;// other error
						}
					} else {
						nReturnVal = 28;
						WriteLog(Level.WARNING, "Serial class object not created");
					}
				}
			}
		} catch (Exception e2) {
			e2.printStackTrace();
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				nReturnVal = 18;
			} else {
				nReturnVal = 31;
			}

			WriteLog(Level.WARNING, e2.getMessage());
			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}
		WriteLog(Level.FINEST, "Return value- " + nReturnVal);

		return nReturnVal;
	}

	/////////////////////////////////////////////////////////////////////////////////////////

	/**********************
	 * 
	 * @param TxCmCode
	 *            : command code
	 * @param TxPmCode
	 *            : parameter code
	 * @param TxDataLen
	 *            : additional data packet length
	 * @param TxData
	 *            : additional data packet
	 * @param RxReplyType
	 *            0x50 : positive 0x4E : negative 0x10 : ICRW cancels communication
	 *            (NAK ) 0x20 : communication error 0x30 : Host cancels command
	 *            (DLE, EOT)
	 * @param RxStCode1
	 *            : return status code 1
	 * @param RxStCode0
	 *            : return status code 0
	 * @param RxDataLen
	 *            : returned data packet length
	 * @param RxData
	 *            : returned data packet
	 * @return: =0 positive <>0 negative
	 */
	private int RS232_ExeCommand(byte TxCmCode, byte TxPmCode, int TxDataLen, byte TxData[], byte RxReplyType[],
			byte RxStCode1[], byte RxStCode0[], int RxDataLen[], byte RxData[], int Timeout) {

		/*
		 * Transmission Format: byte 0 : STX (0xF2) byte 1 : LEN (Upper byte) byte 2 :
		 * LEN (Lower byte) byte 3 to LEN : Command last 2 byte CRCC (Upper byte + Lower
		 * Byte)
		 */
		int returnValue = 31;// Other Error

		try {

			// MyFormatter.strSourceMethodName = "RS232_ExeCommand";
			WriteLog(Level.INFO, "TxCmCode: " + String.format("%02X", TxCmCode) + "; TxPmCode: "
					+ String.format("%02X ", TxPmCode) + "; Timeout: " + Timeout);

			// Step 1 : Command formation
			int nCmdbyteIndex = 0;
			byte[] byResponse = null;
			java.util.Arrays.fill(byGlobalCommand_CardRW, (char) 0x00);
			byGlobalCommand_CardRW[nCmdbyteIndex++] = (char) STX_CardRW;// index 0;

			if (TxDataLen <= 255) {
				byGlobalCommand_CardRW[nCmdbyteIndex++] = 0x00;// index 1 ... Len upper byte

				byGlobalCommand_CardRW[nCmdbyteIndex++] = (char) (TxDataLen + 3);// index 2 ... Len lower byte

			} else// if len greater than 255 byte
			{

			}

			byGlobalCommand_CardRW[nCmdbyteIndex++] = (char) 0x43;// index 3 ... hard core value
			byGlobalCommand_CardRW[nCmdbyteIndex++] = (char) TxCmCode;// index 4 ... command Code
			byGlobalCommand_CardRW[nCmdbyteIndex++] = (char) TxPmCode;// index 5 ... hard parameter code

			for (int i = 0; i < TxDataLen; i++) {
				byGlobalCommand_CardRW[nCmdbyteIndex] = (char) TxData[i];// start index location from 6
				nCmdbyteIndex++;
			}

			//////////////////////////
			// byte []temp = new byte[] {(byte)0xF2 ,0x00 ,0x10 ,0x43 ,0x30 ,0x31 ,0x33
			////////////////////////// ,0x32 ,0x34 ,0x31 ,0x30 ,0x30 ,0x30 ,0x30 ,0x31 ,0x30
			////////////////////////// ,0x30 ,0x30 ,0x30 ,0 ,0};

			// char []temp = new char[] {(char)0xF2 ,0x00 ,0x08 ,0x43 ,0x49 ,0x39 ,0x00
			// ,(char)0x84 ,0x00 ,0x00 ,0x08 ,0x00,0x00};//(char)0x82 ,0x5E};
			// byte []temp1 = new byte[] {(byte)0xF2 ,0x00 ,0x08 ,0x43 ,0x49 ,0x39 ,0x00
			// ,(byte)0x84 ,0x00 ,0x00 ,0x08 ,(byte)0x82 ,0x5E};

			// short crc;
			char crc;

			// String s1 = new String(temp1);
			// String s1 = new String(byGlobalCommand);
			// String s1 = new String(temp1);
			// char c1[] = s1.toCharArray();
			// crc = GetCRC(temp,(short)11);
			crc = GetCRC_RFID_RW(byGlobalCommand_CardRW, (short) nCmdbyteIndex);
			// crc = (short) GetCRC(c1,(short)nCmdbyteIndex);

			// c1[nCmdbyteIndex++] = (char) ((crc >> 8) & 0xFF);
			// c1[nCmdbyteIndex++] = (char) (crc & 0xFF);

			// temp1 = (byte) (c1[19]);
			// temp2 = (byte) (c1[20]);

			byGlobalCommand_CardRW[nCmdbyteIndex++] = (char) ((crc >> 8) & 0xFF);
			byGlobalCommand_CardRW[nCmdbyteIndex] = (char) (crc & 0xFF);

			String s2 = new String(byGlobalCommand_CardRW);
			byte b1[] = stringToBytesASCII(s2);

			// byGlobalCommand[nCmdbyteIndex++] = (byte) ((crc >> 8) & 0xFF);
			// byGlobalCommand[nCmdbyteIndex] = (byte) (crc & 0xFF);
			//////////////////////

			// byGlobalCommand[nCmdbyteIndex++] = (byte) (0x59);
			// byGlobalCommand[nCmdbyteIndex] = (byte) (0xB1);

			// Step 2 : Send Command
			if (serialPort_CardRW != null) {
				// Step 2 : Retrive response from device
				WriteReqResLog("RS232_ExeCommand CMD ", b1);
				if (serialPort_CardRW.writeBytes(b1)) {
					WriteLog(Level.FINEST, "bytes written on port");
					byte[] byRes1 = serialPort_CardRW.readBytes(4, Timeout);// at 0th location ACK , STX (index 1), Len
																			// Upper
					// (index 2), Len Lower (index 3), RxReplyType
					// (index 4), RxCmCode (index 5), RxPmCode
					// (index 6), RxStCode0 (index 7), RxStCode1
					// (index 8), from Index 9 data and last 2 char
					// CRC
					WriteLog(Level.FINEST, "4 bytes read");

					WriteLog(Level.INFO, "ACK- " + String.format("%02X ", byRes1[0]));

					int iPendingDataLen = (byRes1[2] * 256) + byRes1[3];
					WriteLog(Level.FINEST, "Data length received- " + iPendingDataLen);

					byte[] byRes2 = serialPort_CardRW.readBytes(iPendingDataLen + 2, 5000);
					WriteLog(Level.FINEST, iPendingDataLen + " bytes read");

					byResponse = new byte[byRes1.length + byRes2.length];
					System.arraycopy(byRes1, 0, byResponse, 0, byRes1.length);
					System.arraycopy(byRes2, 0, byResponse, byRes1.length, byRes2.length);
					WriteReqResLog("RS232_ExeCommand RES ", byResponse);
					// Step 4 : Send ACK
					serialPort_CardRW.writeByte(ACK_CardRW);
					WriteLog(Level.WARNING, "Ack response send");

					RxReplyType[0] = byResponse[4];
					WriteLog(Level.INFO, "RxReplyType- " + String.format("%02X ", RxReplyType[0]));
					if (byResponse.length > 7) {
						RxStCode1[0] = byResponse[7];
						WriteLog(Level.INFO, "RxStCode1- " + String.format("%02X ", RxStCode1[0]));

						RxStCode0[0] = byResponse[8];
						WriteLog(Level.INFO, "RxStCode0- " + String.format("%02X ", RxStCode0[0]));
					}
					int responseLen = 0;
					String strByteResponse = "";

					// (last 2 byte is CRC... so ignore it)
					// minus 9 because first 9 is ignorable...
					for (responseLen = 0; responseLen < ((byResponse.length - 2) - 9); responseLen++) {
						RxData[responseLen] = byResponse[9 + responseLen];
						strByteResponse += String.format("%02X ", byResponse[9 + responseLen]);
					}
					WriteLog(Level.FINEST, "Other data- " + strByteResponse);
					WriteLog(Level.INFO, "Other data- " + strByteResponse);

					RxDataLen[0] = responseLen;
					returnValue = 0;// OK
				} else {
					WriteLog(Level.WARNING, "Unknown error");
					returnValue = 28; // communication failure
				}
			} else {
				WriteLog(Level.WARNING, "Serial class object not created");
				returnValue = 28; // communication failure
			}

		} catch (Exception e2) {
			if (e2.getMessage().toLowerCase().contains("timeout")) {
				returnValue = 18;
				WriteLog(Level.WARNING, "TimeOut " + e2.getMessage());
			} else {
				returnValue = 31;
				WriteLog(Level.WARNING, e2.getMessage());
			}

			WriteLog(Level.WARNING, e2.getMessage());
			StringWriter errors = new StringWriter();
			e2.printStackTrace(new PrintWriter(errors));
			WriteLog(Level.FINEST, errors.toString());
		}
		WriteLog(Level.FINEST, "Return value- " + returnValue);
		return returnValue;

	}

	/*
	 * stringToBytesASCII: function is used to convert string in byte array Used for
	 * internal purpose.
	 */
	private static byte[] stringToBytesASCII(String str) {
		try {
			byte[] b = new byte[str.length()];
			for (int i = 0; i < b.length; i++) {
				b[i] = (byte) str.charAt(i);
			}
			return b;
		} catch (Exception ex) {
			return null;
		}
	}

	/*
	 * Generate calc_crc_RFID_RW Internal use for Smart card RW device
	 */
	private char calc_crc_RFID_RW(char crc, char ch) {
		char i;
		ch <<= 8;
		for (i = 8; i > 0; i--) {
			if (((ch ^ crc) & 0x8000) > 0) {
				crc = (char) ((crc << 1) ^ 0x1021); /* ;//POLYNOMIAL; */
			} else {
				crc <<= 1;
			}
			ch <<= 1;
		}
		return crc;
	}

	/*
	 * Generate GetCRC_RFID_RW Internal use for Smart card RW device
	 */
	private char GetCRC_RFID_RW(char[] p, short n) {
		char ch;
		short i;
		char crc = 0x0000;
		for (i = 0; i < n; i++) {
			ch = (char) (p[i] & 0xFF);
			crc = calc_crc_RFID_RW(crc, (char) (ch & 0xFF));
		}
		return crc;
	}
}