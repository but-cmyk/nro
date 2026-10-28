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
    
    public class Session_ME2 : ISession
    {
    	public class Sender
    	{
    		public List<Message> sendingMessage;
    
    		public Sender()
    		{
    			sendingMessage = new List<Message>();
    		}
    
    		public void AddMessage(Message message)
    		{
    			sendingMessage.Add(message);
    		}
    
    		public void Clear()
    		{
    			sendingMessage.Clear();
    		}
    
    		public void run()
    		{
    			while (connected)
    			{
    				try
    				{
    					if (getKeyComplete)
    					{
    						while (sendingMessage.Count > 0)
    						{
    							Message m = sendingMessage[0];
    							doSendMessage(m);
    							sendingMessage.RemoveAt(0);
    						}
    					}
    					try
    					{
    						Thread.Sleep(5);
    					}
    					catch (ThreadAbortException)
    					{
    						// Expected on disconnect
    					}
    					catch (Exception ex)
    					{
    						Cout.LogError(ex.ToString());
    					}
    				}
    				catch (Exception)
    				{
    					Res.outz("error send message! ");
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
    					try
    					{
    						Thread.Sleep(5);
    					}
    					catch (Exception)
    					{
    						Cout.println("LOI NHAN  MESS THU 2");
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
    				GameMidlet.IP2 = message.reader().readUTF();
    				GameMidlet.PORT2 = message.reader().readInt();
    				GameMidlet.isConnect2 = ((message.reader().readByte() != 0) ? true : false);
    				if (isMainSession && GameMidlet.isConnect2)
    				{
    					GameCanvas.connect2();
    				}
    			}
    			catch (Exception)
    			{
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
    				throw new IOException("Message2 frame size vuot nguong cho phep (2MB): " + num4);
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
    				if (b == -32 || b == -66 || b == 11 || b == -67 || b == -74 || b == -87)
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
    					throw new IOException("Frame size vuot nguong cho phep (64KB): " + num);
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
    			catch (Exception ex)
    			{
    				Debug.Log(ex.StackTrace.ToString());
    			}
    			return null;
    		}
    	}
    
    	protected static Session_ME2 instance = new Session_ME2();
    
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
    
    	private static bool getKeyComplete;
    
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
    
    	public static MyVector recieveMsg = new MyVector();
    
    	public Session_ME2()
    	{
    		Debug.Log("init Session_ME");
    	}
    
    	public void clearSendingMessage()
    	{
    		sender.sendingMessage.Clear();
    	}
    
    	public static Session_ME2 gI()
    	{
    		if (instance == null)
    		{
    			instance = new Session_ME2();
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
    			this.host = host;
    			this.port = port;
    			getKeyComplete = false;
    			close();
    			Debug.Log("connecting...!");
    			Debug.Log("host: " + host);
    			Debug.Log("port: " + port);
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
    			Debug.LogError("Connect failed (ME2): " + ex.Message);
    			if (messageHandler != null)
    			{
    				close();
    				messageHandler.onConnectionFail(isMainSession);
    			}
    		}
    	}
    
    	public void doConnect(string host, int port)
    	{
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
    			Debug.Log("TLS connected (ME2): " + targetHost);
    		}
    		dataStream = stream;
    		dis = new BinaryReader(dataStream, new UTF8Encoding());
    		dos = new BinaryWriter(dataStream, new UTF8Encoding());
    		sendThread = new Thread(sender.run);
    		sendThread.Start();
    		MessageCollector @object = new MessageCollector();
    		Cout.Log("new -----");
    		collectorThread = new Thread(@object.run);
    		collectorThread.Start();
    		timeConnected = currentTimeMillis();
    		connecting = false;
    		connected = true;
    		doSendMessage(new Message(-27));
    	}

    	private static bool ValidateServerCertificate(object sender, X509Certificate certificate,
    	        X509Chain chain, SslPolicyErrors errors)
    	{
    		if (certificate == null)
    		{
    			return false;
    		}
    		if (TlsConfig.AllowUntrustedForDev)
    		{
    			Debug.LogWarning("TLS (ME2): AllowUntrustedForDev=true, certificate is NOT pinned!");
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
    							return true;
    						}
    					}
    				}
    			}
    		}
    		catch (Exception ex)
    		{
    			Debug.LogError("TLS pin check failed (ME2): " + ex.Message);
    		}
    		Debug.LogError("TLS certificate pin mismatch (ME2). Check TlsConfig.PinnedSha256.");
    		return false;
    	}
    
    	public void sendMessage(Message message)
    	{
    		sender.AddMessage(message);
    	}
    
    	private static void doSendMessage(Message m)
    	{
    		sbyte[] data = m.getData();
    		try
    		{
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
    					dos.Write((ushort)num);
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
    		if (Thread.CurrentThread.Name == Main.mainThreadName)
    		{
    			messageHandler.onMessage(msg);
    		}
    		else
    		{
    			recieveMsg.addElement(msg);
    		}
    	}
    
    	public static void update()
    	{
    		while (recieveMsg.size() > 0)
    		{
    			Message message = (Message)recieveMsg.elementAt(0);
    			if (Controller.isStopReadMessage)
    			{
    				break;
    			}
    			if (message == null)
    			{
    				recieveMsg.removeElementAt(0);
    				break;
    			}
    			sbyte cmd = message.command;
    			try
    			{
    				messageHandler.onMessage(message);
    			}
    			catch (Exception ex)
    			{
    				Cout.LogError("Lỗi xử lý message2 cmd=" + cmd + ": " + ex.Message + "\n" + ex.StackTrace);
    			}
    			finally
    			{
    				message.cleanup();
    				recieveMsg.removeElementAt(0);
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
    			if (recieveMsg != null)
    			{
    				recieveMsg.removeAllElements();
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
    			sendThread = null;
    			collectorThread = null;
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
    }
}
