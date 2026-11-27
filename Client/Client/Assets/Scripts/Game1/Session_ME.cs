namespace Game1
{
    using System;
    using System.Collections.Concurrent;
    using System.Collections.Generic;
    using System.IO;
    using System.Net.Security;
    using System.Net.Sockets;
    using System.Security.Cryptography.X509Certificates;
    using System.Text;
    using System.Threading;
    using UnityEngine;
    
    public class Session_ME : ISession
    {
    	public class Sender
    	{
    		public Queue<Message> sendingMessage;
    		private readonly object sendLock = new object();
    		private byte[] sendBuffer = new byte[4096];

    		public Sender()
    		{
    			sendingMessage = new Queue<Message>();
    		}

    		public void AddMessage(Message message)
    		{
    			lock (sendLock)
    			{
    				sendingMessage.Enqueue(message);
    				Monitor.Pulse(sendLock);
    			}
    		}

    		public void Clear()
    		{
    			lock (sendLock)
    			{
    				sendingMessage.Clear();
    				Monitor.PulseAll(sendLock);
    			}
    		}

    		public void run()
    		{
    			int myGen = sessionGeneration;
    			while (connected && myGen == sessionGeneration)
    			{
    				try
    				{
    					Message m = null;
    					if (getKeyComplete)
    					{
    						lock (sendLock)
    						{
    							if (sendingMessage.Count > 0)
    							{
    								m = sendingMessage.Dequeue();
    							}
    							else
    							{
    								Monitor.Wait(sendLock, 50);
    							}
    						}
    						if (m != null)
    						{
    							doSendMessage(m, ref sendBuffer);
    							continue;
    						}
    					}
    					else
    					{
    						Thread.Sleep(10);
    					}
    				}
    				catch (Exception ex)
    				{
    					if (connected && myGen == sessionGeneration)
    					{
    						Cout.LogError(ex.ToString());
    					}
    					break;
    				}
    			}
    		}
    	}
    
    	private class MessageCollector
    	{
    		public void run()
    		{
    			int myGen = sessionGeneration;
    			try
    			{
    				while (connected && myGen == sessionGeneration)
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
    			catch (Exception ex3)
    			{
    				if (connected && myGen == sessionGeneration)
    				{
    					Debug.Log("error read message!");
    					Debug.Log(ex3.Message.ToString());
    				}
    			}
    			if (!connected || myGen != sessionGeneration)
    			{
    				return;
    			}
    			IMessageHandler handler = messageHandler;
    			if (handler != null)
    			{
    				bool disconnected = (currentTimeMillis() - timeConnected > 500);
    				bool mainFlag = isMainSession;
    				mainThreadCallbacks.Enqueue(() =>
    				{
    					if (disconnected)
    					{
    						handler.onDisconnected(mainFlag);
    					}
    					else
    					{
    						handler.onConnectionFail(mainFlag);
    					}
    				});
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
    				int b = message.reader().readUnsignedByte();
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
    				getKeyComplete = true;
    				GameMidlet.IP2 = message.reader().readUTF();
    				GameMidlet.PORT2 = message.reader().readInt();
    				GameMidlet.isConnect2 = ((message.reader().readByte() != 0) ? true : false);
    			}
    			catch (Exception)
    			{
    			}
    		}
    
    		private static byte[] ReadExactBytes(BinaryReader reader, int count)
    		{
    			if (count < 0 || count > 2000000)
    			{
    				throw new IOException("Invalid packet length: " + count);
    			}
    			if (count == 0)
    			{
    				return new byte[0];
    			}
    			byte[] buffer = new byte[count];
    			int offset = 0;
    			while (offset < count)
    			{
    				int read = reader.Read(buffer, offset, count - offset);
    				if (read <= 0)
    				{
    					throw new EndOfStreamException("Unexpected EOF while reading packet payload");
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
    			byte[] src = ReadExactBytes(dis, num4);
    			sbyte[] array = ArrayCast.cast(src);
    			recvByteCount += 5 + num4;
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
    				byte[] src = ReadExactBytes(dis, num);
    				sbyte[] array = ArrayCast.cast(src);
                    recvByteCount += 5 + num;
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

    	public static volatile bool connected;

    	public static volatile bool connecting;

    	private static volatile int sessionGeneration;

    	private static readonly ConcurrentQueue<Action> mainThreadCallbacks = new ConcurrentQueue<Action>();

    	private static Sender sender = new Sender();

    	public static Thread initThread;

    	public static Thread collectorThread;

    	public static Thread sendThread;

    	public static long sendByteCount;

    	public static long recvByteCount;

    	private static volatile bool getKeyComplete;

    	public static sbyte[] key = null;

    	private static int curR;

    	private static int curW;

    	private static long timeConnected;
    
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
    			initThread = new Thread(NetworkInit);
    			initThread.Start();
    		}
    	}
    
    	private void NetworkInit()
    	{
    		isCancel = false;
    		connecting = true;
    		Thread.CurrentThread.Priority = System.Threading.ThreadPriority.Highest;
    		connected = true;
    		int myGen = sessionGeneration;
    		try
    		{
    			doConnect(host, port);
    			IMessageHandler handler = messageHandler;
    			if (handler != null && myGen == sessionGeneration)
    			{
    				bool mainFlag = isMainSession;
    				mainThreadCallbacks.Enqueue(() => handler.onConnectOK(mainFlag));
    			}
    		}
    		catch (Exception)
    		{
    			IMessageHandler handler = messageHandler;
    			if (handler != null)
    			{
    				close();
    				bool mainFlag = isMainSession;
    				mainThreadCallbacks.Enqueue(() => handler.onConnectionFail(mainFlag));
    			}
    		}
    	}

    	private static bool ValidateServerCertificate(object senderObj, X509Certificate certificate, X509Chain chain, SslPolicyErrors sslPolicyErrors, string targetHost)
    	{
    		if (sslPolicyErrors == SslPolicyErrors.None)
    		{
    			return true;
    		}
    		if (targetHost == "127.0.0.1" || targetHost == "localhost")
    		{
    			return true;
    		}
    		return false;
    	}

    	public void doConnect(string host, int port)
    	{
    		sc = new TcpClient();
    		sc.NoDelay = true;
    		sc.SendTimeout = 10000;
    		try
    		{
    			sc.Client.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.KeepAlive, true);
    		}
    		catch (Exception)
    		{
    		}
    		sc.Connect(host, port);
    		Stream rawStream = sc.GetStream();
    		if (port == 14446)
    		{
    			SslStream sslStream = new SslStream(rawStream, false, (s, cert, chain, err) => ValidateServerCertificate(s, cert, chain, err, host));
    			sslStream.AuthenticateAsClient(host);
    			dataStream = sslStream;
    		}
    		else
    		{
    			dataStream = rawStream;
    		}
    		dis = new BinaryReader(dataStream, new UTF8Encoding());
    		dos = new BinaryWriter(dataStream, new UTF8Encoding());
    		sendThread = new Thread(sender.run);
    		sendThread.IsBackground = true;
    		sendThread.Start();
    		MessageCollector @object = new MessageCollector();
    		Cout.Log("new -----");
    		collectorThread = new Thread(@object.run);
    		collectorThread.IsBackground = true;
    		collectorThread.Start();
    		timeConnected = currentTimeMillis();
    		connecting = false;
    		byte[] handshakeBuf = new byte[16];
    		doSendMessage(new Message(-27), ref handshakeBuf);
    		key = null;
    	}
    
    	public void sendMessage(Message message)
    	{
    		count++;
    		sender.AddMessage(message);
    	}
    
    	private static void doSendMessage(Message m, ref byte[] buffer)
    	{
    		sbyte[] data = m.getData();
    		try
    		{
    			int dataLen = (data != null) ? data.Length : 0;
    			int totalLen = 3 + dataLen;
    			if (buffer == null || buffer.Length < totalLen)
    			{
    				buffer = new byte[totalLen];
    			}
    			if (getKeyComplete)
    			{
    				buffer[0] = (byte)writeKey(m.command);
    				buffer[1] = (byte)writeKey((sbyte)(dataLen >> 8));
    				buffer[2] = (byte)writeKey((sbyte)(dataLen & 0xFF));
    				for (int i = 0; i < dataLen; i++)
    				{
    					buffer[3 + i] = (byte)writeKey(data[i]);
    				}
    			}
    			else
    			{
    				buffer[0] = (byte)m.command;
    				buffer[1] = (byte)(dataLen >> 8);
    				buffer[2] = (byte)(dataLen & 0xFF);
    				for (int i = 0; i < dataLen; i++)
    				{
    					buffer[3 + i] = (byte)data[i];
    				}
    			}
    			dos.Write(buffer, 0, totalLen);
    			dos.Flush();
    			sendByteCount += 5 + dataLen;
    		}
    		catch (Exception ex)
    		{
    			Debug.Log(ex.StackTrace);
    		}
    	}

    	public static sbyte readKey(sbyte b)
	{
		sbyte[] array = key;
		int num = curR++;
		sbyte result = (sbyte)((array[num] & 0xFF) ^ (b & 0xFF));
		if (curR >= key.Length)
		{
			curR %= key.Length;
		}
		return result;
	}
    
    	public static sbyte writeKey(sbyte b)
	{
		sbyte[] array = key;
		int num = curW++;
		sbyte result = (sbyte)((array[num] & 0xFF) ^ (b & 0xFF));
		if (curW >= key.Length)
		{
			curW %= key.Length;
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
    		while (mainThreadCallbacks.TryDequeue(out Action cb))
    		{
    			try
    			{
    				cb?.Invoke();
    			}
    			catch (Exception ex)
    			{
    				Cout.LogError("Lỗi callback mạng: " + ex.Message);
    			}
    		}
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
    		Interlocked.Increment(ref sessionGeneration);
    		key = null;
    		curR = 0;
    		curW = 0;
    		try
    		{
    			connected = false;
    			connecting = false;
    			sender.Clear();
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
    			initThread = null;
    			collectorThread = null;
    			if (isMainSession)
    			{
    				ServerListScreen.testConnect = 0;
    			}
    		}
    		catch (Exception)
    		{
    		}
    	}

    	public static long currentTimeMillis()
    	{
    		return mSystem.currentTimeMillis();
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
