namespace Game1
{
    using System;
    using System.Collections.Generic;
    using System.IO;
    using System.Net.Security;
    using System.Net.Sockets;
    using System.Security.Authentication;
    using System.Security.Cryptography;
    using System.Security.Cryptography.X509Certificates;
    using System.Text;
    using System.Threading;
    using UnityEngine;
    
    public class Session_ME : ISession
    {
    	public static readonly object sendLock = new object();

    	public class Sender
    	{
    		public List<Message> sendingMessage;
    
    		public Sender()
    		{
    			sendingMessage = new List<Message>();
    		}
    
    		public void AddMessage(Message message)
    		{
    			lock (sendLock)
    			{
    				sendingMessage.Add(message);
    				Monitor.Pulse(sendLock);
    			}
    		}
    
    		public void Clear()
    		{
    			lock (sendLock)
    			{
    				sendingMessage.Clear();
    			}
    		}
    
    		public void run()
    		{
    			while (connected)
    			{
    				try
    				{
    					if (!getKeyComplete)
    					{
    						Thread.Sleep(5);
    						continue;
    					}
    					Message m = null;
    					lock (sendLock)
    					{
    						if (sendingMessage.Count > 0)
    						{
    							m = sendingMessage[0];
    							sendingMessage.RemoveAt(0);
    						}
    						else
    						{
    							Monitor.Wait(sendLock, 50);
    						}
    					}
    					if (m != null)
    					{
    						doSendMessage(m);
    					}
    				}
    				catch (ThreadAbortException)
    				{
    					// Expected on disconnect
    					break;
    				}
    				catch (Exception ex)
    				{
    					Cout.LogError(ex.ToString());
    				}
    			}
    		}
    	}
    
    	private class MessageCollector
    	{
    		public void run()
    		{
    			try
    			{
    				while (connected)
    				{
    					Message message = readMessage();
    					if (message == null)
    					{
    						break;
    					}
    					try
    					{
    						if (message.command == -27)
    						{
    							getKey(message);
    						}
    						else
    						{
    							onRecieveMsg(message);
    						}
    					}
    					catch (Exception)
    					{
    						Cout.println("LOI NHAN  MESS THU 1");
    					}
    
    				}
    			}
    			catch (ThreadAbortException)
    			{
    				Thread.ResetAbort();
    				return;
    			}
    			catch (Exception ex3)
    			{
    				Debug.Log("error read message!");
    				Debug.Log(ex3.Message.ToString());
    			}
    			if (!connected)
    			{
    				return;
    			}
    			if (messageHandler != null)
    			{
    				if (currentTimeMillis() - timeConnected > 500)
    				{
    					messageHandler.onDisconnected(isMainSession);
    				}
    				else
    				{
    					messageHandler.onConnectionFail(isMainSession);
    				}
    			}
    			if (sc != null)
    			{
    				cleanNetwork();
    			}
    		}
    
    		private void getKey(Message message)
    		{
    			try
    			{
    				sbyte b = message.reader().readSByte();
    				key = new sbyte[b];
    				for (int i = 0; i < b; i++)
    				{
    					key[i] = message.reader().readSByte();
    				}
    				for (int j = 0; j < key.Length - 1; j++)
    				{
    					ref sbyte reference = ref key[j + 1];
    					reference ^= key[j];
    				}
    				curR = 0;
    				curW = 0;
    				getKeyComplete = true;
    				ServerListScreen.testConnect = 2;
    				ServerListScreen.isAutoConect = false;
    				Debug.Log("[NET-2] [CLIENT Handshake -27] Nhận Session Key thành công (len=" + key.Length + ")");
    				Res.onScreenDebug("SESSION_KEY_OK (len=" + key.Length + ")");
    				lock (sendLock)
    				{
    					Monitor.PulseAll(sendLock);
    				}
    				GameMidlet.IP2 = message.reader().readUTF();
    				GameMidlet.PORT2 = message.reader().readInt();
    				GameMidlet.isConnect2 = ((message.reader().readByte() != 0) ? true : false);
    				if (isMainSession && GameMidlet.isConnect2)
    				{
    					GameCanvas.connect2();
    				}
    			}
    			catch (Exception exKey)
    			{
    				Res.debugLastError = "getKey ex: " + exKey.Message;
    				Res.onScreenDebug("GET_KEY_ERR: " + exKey.Message);
    			}
    		}
    
    		private static byte[] ReadFully(Stream stream, int count)
    		{
    			if (count <= 0)
    			{
    				return new byte[0];
    			}
    			byte[] buffer = new byte[count];
    			int offset = 0;
    			while (offset < count)
    			{
    				int read = stream.Read(buffer, offset, count - offset);
    				if (read <= 0)
    				{
    					throw new IOException("Stream closed unexpectedly: expected " + count + " bytes, read " + offset);
    				}
    				offset += read;
    			}
    			return buffer;
    		}

    		private Message readMessage2(sbyte cmd)
    		{
    			int num = readKey(dis.ReadSByte()) + 128;
    			int num2 = readKey(dis.ReadSByte()) + 128;
    			int num3 = readKey(dis.ReadSByte()) + 128;
    			int num4 = (num3 * 256 + num2) * 256 + num;
    			if (num4 < 0 || num4 > 2097152)
    			{
    				throw new IOException("Message2 frame size vượt ngưỡng cho phép (2MB): " + num4);
    			}
    			sbyte[] array = ArrayCast.SafeAllocSByte(num4, 2097152);
    			byte[] src = ReadFully(dataStream, num4);
    			array = ArrayCast.cast(src);
    			recvByteCount += 5 + num4;
    			int num6 = recvByteCount + sendByteCount;
    			strRecvByteCount = num6 / 1024 + "." + num6 % 1024 / 102 + "Kb";
    			if (getKeyComplete)
    			{
    				for (int i = 0; i < array.Length; i++)
    				{
    					array[i] = readKey(array[i]);
    				}
    			}
    			return new Message(cmd, array);
    		}
    
    		private Message readMessage()
    		{
    			try
    			{
    				sbyte b = dis.ReadSByte();
    				if (getKeyComplete)
    				{
    					b = readKey(b);
    				}
    				if (b == -32 || b == -66 || b == 11 || b == -67 || b == -74 || b == -87 || b == 66)
    				{
    					return readMessage2(b);
    				}
    				int num;
    				if (getKeyComplete)
    				{
    					sbyte b2 = dis.ReadSByte();
    					sbyte b3 = dis.ReadSByte();
    					num = ((readKey(b2) & 0xFF) << 8) | (readKey(b3) & 0xFF);
    				}
    				else
    				{
    					sbyte b4 = dis.ReadSByte();
    					sbyte b5 = dis.ReadSByte();
    					num = ((b4 & 0xFF) << 8) | (b5 & 0xFF);
    				}
    				if (num < 0 || num > 65536)
    				{
    					throw new IOException("Frame size vượt ngưỡng cho phép (64KB): " + num);
    				}
    				sbyte[] array = ArrayCast.SafeAllocSByte(num, 65536);
    				byte[] src = ReadFully(dataStream, num);
                    array = ArrayCast.cast(src);
                    recvByteCount += 5 + num;
    				int num4 = recvByteCount + sendByteCount;
    				strRecvByteCount = num4 / 1024 + "." + num4 % 1024 / 102 + "Kb";
    				if (getKeyComplete)
    				{
    					for (int i = 0; i < array.Length; i++)
    					{
    						array[i] = readKey(array[i]);
    					}
    				}
    				return new Message(b, array);
    			}
    			catch (Exception)
    			{
    			}
    			return null;
    		}
    	}
    
    	protected static Session_ME instance = new Session_ME();
    
    	private static Stream dataStream;
    
    	private static BinaryReader dis;
    
    	private static BinaryWriter dos;
    
    	public static IMessageHandler messageHandler;
    
    	public static bool isMainSession = true;
    
    	private static TcpClient sc;
    
    	public static bool connected;
    
    	public static bool connecting;
    
    	private static Sender sender = new Sender();
    
    	public static Thread initThread;
    
    	public static Thread collectorThread;
    
    	public static Thread sendThread;
    
    	public static int sendByteCount;
    
    	public static int recvByteCount;
    
    	public static bool getKeyComplete;
    
    	public static sbyte[] key = null;
    
    	private static sbyte curR;
    
    	private static sbyte curW;
    
    	private static int timeConnected;
    
    	private long lastTimeConn;
    
    	public static string strRecvByteCount = string.Empty;
    
    	public static bool isCancel;
    
    	private string host;
    
    	private int port;
    
    	private long timeWaitConnect;
    
    	public static int count;
    
    	public static Queue<Message> recieveMsg = new Queue<Message>();
    
    	public Session_ME()
    	{
    		Debug.Log("init Session_ME");
    	}
    
    	public void clearSendingMessage()
    	{
    		sender.Clear();
    	}
    
    	public static Session_ME gI()
    	{
    		if (instance == null)
    		{
    			instance = new Session_ME();
    		}
    		return instance;
    	}
    
    	public bool isConnected()
    	{
    		return connected && sc != null && dis != null;
    	}
    
    	public void setHandler(IMessageHandler msgHandler)
    	{
    		messageHandler = msgHandler;
    	}
    
    	public void connect(string host, int port)
    	{
    		if (!connected && !connecting && mSystem.currentTimeMillis() >= timeWaitConnect)
    		{
    			timeWaitConnect = mSystem.currentTimeMillis() + 50;
    			if (isMainSession)
    			{
    				ServerListScreen.testConnect = -1;
    			}
    			this.host = host;
    			this.port = port;
    			getKeyComplete = false;
    			close();
    			Debug.Log("connecting...!");
    			Debug.Log("host: " + host);
    			Debug.Log("port: " + port);
    			Res.onScreenDebug("NET_CONNECTING: " + host + ":" + port);
    			connecting = true;
    			initThread = new Thread(NetworkInit);
    			initThread.Start();
    		}
    	}
    
    	private void NetworkInit()
    	{
    		isCancel = false;
    		connecting = true;
    		connected = false;
    		Thread.CurrentThread.Priority = System.Threading.ThreadPriority.Highest;
    		try
    		{
    			doConnect(host, port);
    			messageHandler.onConnectOK(isMainSession);
    		}
    		catch (ThreadAbortException)
    		{
    			connected = false;
    			connecting = false;
    			Thread.ResetAbort();
    		}
    		catch (Exception ex)
    		{
    			connected = false;
    			connecting = false;
    			Debug.LogError("[NET-1] [CLIENT Connect] Kết nối thất bại tới " + host + ":" + port + " - Lý do: " + ex.Message);
    			Res.debugLastError = "NetInit: " + ex.Message;
    			Res.onScreenDebug("CONNECT_FAIL: " + ex.Message);
    			if (messageHandler != null)
    			{
    				close();
    				messageHandler.onConnectionFail(isMainSession);
    			}
    		}
    	}
    
    	public void doConnect(string host, int port)
    	{
    		Debug.Log("[NET-1] [CLIENT Connect] Đang mở socket tới " + host + ":" + port + (TlsConfig.ShouldUseTls(port) ? " (TLS)" : " (Plaintext)"));
    		sc = new TcpClient();
    		sc.NoDelay = true;
    		sc.Connect(host, port);
    		Stream stream = sc.GetStream();
    		if (TlsConfig.ShouldUseTls(port))
    		{
    			SslStream ssl = new SslStream(stream, false, new RemoteCertificateValidationCallback(ValidateServerCertificate));
    			ssl.ReadTimeout = 30000;
    			ssl.WriteTimeout = 30000;
    			string targetHost = !string.IsNullOrEmpty(TlsConfig.ServerName) ? TlsConfig.ServerName : host;
    			ssl.AuthenticateAsClient(targetHost, null, SslProtocols.Tls12, false);
    			stream = ssl;
    			Debug.Log("[NET-1] [CLIENT Connect] TLS Handshake THÀNH CÔNG với host=" + targetHost + " trên port=" + port);
    		}
    		else
    		{
    			Debug.Log("[NET-1] [CLIENT Connect] TCP Plaintext kết nối THÀNH CÔNG tới port=" + port);
    		}
    		dataStream = stream;
    		dis = new BinaryReader(dataStream, new UTF8Encoding());
    		dos = new BinaryWriter(dataStream, new UTF8Encoding());
    		key = null;
    		getKeyComplete = false;
    		curR = 0;
    		curW = 0;

    		// [NET-2] Gửi Handshake -27 đầu tiên ngay sau khi mở Stream thành công
    		Debug.Log("[NET-2] [CLIENT Handshake -27] Gửi gói xin Session Key (-27) tới Server...");
    		doSendMessage(new Message(-27));

    		sendThread = new Thread(sender.run);
    		sendThread.Start();
    		MessageCollector @object = new MessageCollector();
    		Cout.Log("new -----");
    		collectorThread = new Thread(@object.run);
    		collectorThread.Start();
    		timeConnected = currentTimeMillis();
    		connecting = false;
    		connected = true;
    		Res.onScreenDebug("TCP_OK: " + host + ":" + port);
    	}

    	/// <summary>
    	/// Certificate pinning: accepts a certificate whose DER SHA-256 OR
    	/// Public Key SHA-256 matches one of TlsConfig.PinnedSha256.
    	/// AllowUntrustedForDev is for local development only.
    	/// </summary>
    	private static bool ValidateServerCertificate(object sender, X509Certificate certificate,
    	        X509Chain chain, SslPolicyErrors errors)
    	{
    		if (certificate == null)
    		{
    			Debug.LogError("[NET-1] [CLIENT Pinning] Thất bại: Chứng chỉ máy chủ rỗng (null)!");
    			return false;
    		}
    		if (TlsConfig.AllowUntrustedForDev)
    		{
    			Debug.LogWarning("[NET-1] [CLIENT Pinning] AllowUntrustedForDev=true, bỏ qua kiểm tra Pinning cho Dev!");
    			return true;
    		}
    		try
    		{
    			using (SHA256 sha = SHA256.Create())
    			{
    				byte[] rawCert = certificate.GetRawCertData();
    				string certPin = Convert.ToBase64String(sha.ComputeHash(rawCert));

    				byte[] pubKey = certificate.GetPublicKey();
    				string pubKeyPin = pubKey != null ? Convert.ToBase64String(sha.ComputeHash(pubKey)) : null;

    				for (int i = 0; i < TlsConfig.PinnedSha256.Length; i++)
    				{
    					string expected = TlsConfig.PinnedSha256[i];
    					if (!string.IsNullOrEmpty(expected))
    					{
    						if (expected == certPin || (pubKeyPin != null && expected == pubKeyPin))
    						{
    							Debug.Log("[NET-1] [CLIENT Pinning] Xác thực Public Key / Cert Pinning THÀNH CÔNG: " + expected);
    							return true;
    						}
    					}
    				}
    			}
    		}
    		catch (Exception ex)
    		{
    			Debug.LogError("[NET-1] [CLIENT Pinning] Lỗi kiểm tra TLS pin: " + ex.Message);
    		}
    		Debug.LogError("[NET-1] [CLIENT Pinning] Thất bại: Mã pin không khớp! Vui lòng cập nhật TlsConfig.PinnedSha256.");
    		return false;
    	}
    
    	public void sendMessage(Message message)
    	{
    		count++;
    		sender.AddMessage(message);
    	}
    
    	private static void doSendMessage(Message m)
    	{
    		sbyte[] data = m.getData();
    		try
    		{
    			lock (sendLock)
    			{
    				if (dos == null)
    				{
    					return;
    				}
    				Debug.Log("[NET-SEND] Gửi gói cmd=" + m.command + ", len=" + ((data != null) ? data.Length : 0));
    			if (getKeyComplete)
    			{
    				sbyte value = writeKey(m.command);
    				dos.Write(value);
    			}
    			else
    			{
    				dos.Write(m.command);
    			}
    			if (data != null)
    			{
    				int num = data.Length;
    				if (getKeyComplete)
    				{
    					int num2 = writeKey((sbyte)(num >> 8));
    					dos.Write((sbyte)num2);
    					int num3 = writeKey((sbyte)(num & 0xFF));
    					dos.Write((sbyte)num3);
    				}
    				else
    				{
    					dos.Write((sbyte)(num >> 8));
    					dos.Write((sbyte)(num & 0xFF));
    				}
    				if (getKeyComplete)
    				{
    					for (int i = 0; i < data.Length; i++)
    					{
    						sbyte value2 = writeKey(data[i]);
    						dos.Write(value2);
    					}
    				}
    				sendByteCount += 5 + data.Length;
    			}
    			else
    			{
    				if (getKeyComplete)
    				{
    					int num4 = 0;
    					int num5 = writeKey((sbyte)(num4 >> 8));
    					dos.Write((sbyte)num5);
    					int num6 = writeKey((sbyte)(num4 & 0xFF));
    					dos.Write((sbyte)num6);
    				}
    				else
    				{
    					dos.Write((ushort)0);
    				}
    				sendByteCount += 5;
    			}
    				dos.Flush();
    			}
    		}
    		catch (Exception ex)
    		{
    			Debug.Log(ex.StackTrace);
    		}
    	}
    
    	public static sbyte readKey(sbyte b)
    	{
    		if (key == null || key.Length == 0)
    		{
    			return b;
    		}
    		sbyte[] array = key;
    		sbyte num = curR;
    		curR = (sbyte)(num + 1);
    		sbyte result = (sbyte)((array[num] & 0xFF) ^ (b & 0xFF));
    		if (curR >= key.Length)
    		{
    			curR %= (sbyte)key.Length;
    		}
    		return result;
    	}
    
    	public static sbyte writeKey(sbyte b)
    	{
    		if (key == null || key.Length == 0)
    		{
    			return b;
    		}
    		sbyte[] array = key;
    		sbyte num = curW;
    		curW = (sbyte)(num + 1);
    		sbyte result = (sbyte)((array[num] & 0xFF) ^ (b & 0xFF));
    		if (curW >= key.Length)
    		{
    			curW %= (sbyte)key.Length;
    		}
    		return result;
    	}
    
    	public static void onRecieveMsg(Message msg)
    	{
    		if (Thread.CurrentThread.Name == Main.mainThreadName && !Controller.isStopReadMessage)
    		{
    			messageHandler.onMessage(msg);
    		}
    		else
    		{
    			lock (recieveMsg)
    			{
    				recieveMsg.Enqueue(msg);
    			}
    		}
    	}
    
    	public static void update()
    	{
    		while (true)
    		{
    			if (Controller.isStopReadMessage)
    			{
    				break;
    			}
    			Message message = null;
    			lock (recieveMsg)
    			{
    				if (recieveMsg.Count > 0)
    				{
    					message = recieveMsg.Dequeue();
    				}
    			}
    			if (message == null)
    			{
    				break;
    			}
    			sbyte cmd = message.command;
    			try
    			{
    				messageHandler.onMessage(message);
    			}
    			catch (Exception ex)
    			{
    				Cout.LogError("Lỗi xử lý message cmd=" + cmd + ": " + ex.Message + "\n" + ex.StackTrace);
    			}
    			finally
    			{
    				message.cleanup();
    			}
    		}
    	}
    
    	public void close()
    	{
    		cleanNetwork();
    	}
    
    	private static void cleanNetwork()
    	{
    		key = null;
    		curR = 0;
    		curW = 0;
    		getKeyComplete = false;
    		try
    		{
    			connected = false;
    			connecting = false;
    			if (sender != null)
    			{
    				sender.Clear();
    			}
    			lock (recieveMsg)
    			{
    				recieveMsg.Clear();
    			}
    			if (sc != null)
    			{
    				sc.Close();
    				sc = null;
    			}
    			if (dataStream != null)
    			{
    				dataStream.Close();
    				dataStream = null;
    			}
    			if (dos != null)
    			{
    				dos.Close();
    				dos = null;
    			}
    			if (dis != null)
    			{
    				dis.Close();
    				dis = null;
    			}
    			if (Thread.CurrentThread.Name == Main.mainThreadName)
    			{
    				if (sendThread != null)
    				{
    					sendThread.Abort();
    				}
    				sendThread = null;
    				if (initThread != null)
    				{
    					initThread.Abort();
    				}
    				initThread = null;
    				if (collectorThread != null)
    				{
    					collectorThread.Abort();
    				}
    				collectorThread = null;
    			}
    			else
    			{
    				sendThread = null;
    				initThread = null;
    				collectorThread = null;
    			}
    			if (isMainSession)
    			{
    				ServerListScreen.testConnect = 0;
    			}
    		}
    		catch (Exception)
    		{
    		}
    	}
    
    	public static int currentTimeMillis()
    	{
    		return Environment.TickCount;
    	}
    
    	public static byte convertSbyteToByte(sbyte var)
    	{
    		if (var > 0)
    		{
    			return (byte)var;
    		}
    		return (byte)(var + 256);
    	}
    
    	public static byte[] convertSbyteToByte(sbyte[] var)
    	{
    		byte[] array = new byte[var.Length];
    		for (int i = 0; i < var.Length; i++)
    		{
    			if (var[i] > 0)
    			{
    				array[i] = (byte)var[i];
    			}
    			else
    			{
    				array[i] = (byte)(var[i] + 256);
    			}
    		}
    		return array;
    	}
    
    	public bool isCompareIPConnect()
    	{
    		return true;
    	}
    }
}
