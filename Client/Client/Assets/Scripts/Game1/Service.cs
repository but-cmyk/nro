namespace Game1
{
    using System;
    using Game1.Assets.src.g;
    using Game1.God;
    
    public partial class Service
    {
    	private ISession session = Session_ME.gI();
    
    	protected static Service instance;
    
    	public static long curCheckController;
    
    	public static long curCheckMap;
    
    	public static long logController;
    
    	public static long logMap;
    
    	public int demGui;
    
    	public static bool reciveFromMainSession;
    
    	public static Service gI()
    	{
    		if (instance == null)
    		{
    			instance = new Service();
    		}
    		return instance;
    	}
    
    	public void gotoPlayer(int id)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)18);
    			message.writer().writeInt(id);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			ex.StackTrace.ToString();
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void androidPack()
    	{
    		if (mSystem.android_pack == null)
    		{
    			return;
    		}
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)126);
    			message.writer().writeUTF(mSystem.android_pack);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			ex.StackTrace.ToString();
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void charInfo(string user, string pass)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)42);
    			message.writer().writeUTF(user);
    			message.writer().writeUTF(pass);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			if (message != null)
    			{
    				message.cleanup();
    			}
    		}
    	}
    
    	public void androidPack2()
    	{
    		if (mSystem.android_pack == null)
    		{
    			return;
    		}
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)126);
    			message.writer().writeUTF(mSystem.android_pack);
    			session = Session_ME.gI();
    			session.sendMessage(message);
    			session = Session_ME.gI();
    		}
    		catch (Exception ex)
    		{
    			ex.StackTrace.ToString();
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void checkAd(sbyte status)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-44));
    			message.writer().writeByte(status);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			ex.StackTrace.ToString();
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void combine(sbyte action, MyVector id)
    	{
    		Res.outz("combine");
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-81));
    			message.writer().writeByte(action);
    			if (action == 1)
    			{
    				message.writer().writeByte(id.size());
    				for (int i = 0; i < id.size(); i++)
    				{
    					message.writer().writeByte(((Item)id.elementAt(i)).indexUI);
    					Res.outz("gui id " + ((Item)id.elementAt(i)).indexUI);
    				}
    			}
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_1)
    		{
    		    Cout.LogWarning("[Service.cs] " + catchErr_1.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void giaodich(sbyte action, int playerID, sbyte index, int num)
    	{
    		Res.outz2("giao dich action = " + action);
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-86));
    			message.writer().writeByte(action);
    			if (action == 0 || action == 1)
    			{
    				Res.outz2(">>>> len playerID =" + playerID);
    				message.writer().writeInt(playerID);
    			}
    			if (action == 2)
    			{
    				Res.outz2("gui len index =" + index + " num= " + num);
    				message.writer().writeByte(index);
    				message.writer().writeInt(num);
    			}
    			if (action == 4)
    			{
    				Res.outz2(">>>> len index =" + index);
    				message.writer().writeByte(index);
    			}
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_2)
    		{
    		    Cout.LogWarning("[Service.cs] " + catchErr_2.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void sendClientInput(TField[] t)
    	{
    		Message message = null;
    		try
    		{
    			Res.outz(" gui input ");
    			message = new Message((sbyte)(-125));
    			Res.outz("byte lent = " + t.Length);
    			message.writer().writeByte(t.Length);
    			for (int i = 0; i < t.Length; i++)
    			{
    				message.writer().writeUTF(t[i].getText());
    			}
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_3)
    		{
    		    Cout.LogWarning("[Service.cs] " + catchErr_3.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void speacialSkill(sbyte index)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)112);
    			message.writer().writeByte(index);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			ex.StackTrace.ToString();
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void test(short x, short y)
    	{
    		Res.outz("gui x= " + x + " y= " + y);
    		Message message = null;
    		try
    		{
    			message = new Message(0);
    			message.writer().writeShort(x);
    			message.writer().writeShort(y);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			ex.StackTrace.ToString();
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void test2()
    	{
    		Res.outz("gui test1");
    		Message message = null;
    		try
    		{
    			message = new Message(1);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			ex.StackTrace.ToString();
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void testJoint()
    	{
    	}
    
    	public void mobCapcha(char ch)
    	{
    		Res.outz("cap char c= " + ch);
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-85));
    			message.writer().writeChar(ch);
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_4)
    		{
    		    Cout.LogWarning("[Service.cs] " + catchErr_4.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void friend(sbyte action, int playerId)
    	{
    		Res.outz("add friend");
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-80));
    			message.writer().writeByte(action);
    			if (playerId != -1)
    			{
    				message.writer().writeInt(playerId);
    			}
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void getArchivemnt(int index)
    	{
    		Res.outz("get ngoc");
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-76));
    			message.writer().writeByte(index);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void getPlayerMenu(int playerID)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-79));
    			message.writer().writeInt(playerID);
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_5)
    		{
    		    Cout.LogWarning("[Service.cs] " + catchErr_5.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void clanImage(sbyte id)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-62));
    			message.writer().writeByte(id);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void skill_not_focus(sbyte status)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-45));
    			message.writer().writeByte(status);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void clanDonate(int id)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-54));
    			message.writer().writeInt(id);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void clanMessage(int type, string text, int clanID)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-51));
    			message.writer().writeByte(type);
    			if (type == 0)
    			{
    				message.writer().writeUTF(text);
    			}
    			if (type == 2)
    			{
    				message.writer().writeInt(clanID);
    			}
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void useItem(sbyte type, sbyte where, sbyte index, short template)
    	{
    		Cout.println("USE ITEM! " + type);
    		if (Char.myCharz().statusMe == 14)
    		{
    			return;
    		}
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-43));
    			message.writer().writeByte(type);
    			message.writer().writeByte(where);
    			message.writer().writeByte(index);
    			if (index == -1)
    			{
    				message.writer().writeShort(template);
    			}
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_6)
    		{
    		    Cout.LogWarning("[Service.cs] " + catchErr_6.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void joinClan(int id, sbyte action)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-49));
    			message.writer().writeInt(id);
    			message.writer().writeByte(action);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void clanMember(int id)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-50));
    			message.writer().writeInt(id);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void searchClan(string text)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-47));
    			message.writer().writeUTF(text);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void requestClan(short id)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-53));
    			message.writer().writeShort(id);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void clanRemote(int id, sbyte role)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-56));
    			message.writer().writeInt(id);
    			message.writer().writeByte(role);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void leaveClan()
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-55));
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void clanInvite(sbyte action, int playerID, int clanID, int code)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-57));
    			message.writer().writeByte(action);
    			if (action == 0)
    			{
    				message.writer().writeInt(playerID);
    			}
    			if (action == 1 || action == 2)
    			{
    				message.writer().writeInt(clanID);
    				message.writer().writeInt(code);
    			}
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void getClan(sbyte action, sbyte id, string text)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-46));
    			message.writer().writeByte(action);
    			if (action == 2 || action == 4)
    			{
    				message.writer().writeByte(id);
    				message.writer().writeUTF(text);
    			}
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void updateCaption(sbyte gender)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-41));
    			message.writer().writeByte(gender);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void getItem(sbyte type, sbyte id)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-40));
    			message.writer().writeByte(type);
    			message.writer().writeByte(id);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void getTask(int npcTemplateId, int menuId, int optionId)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)40);
    			message.writer().writeByte(npcTemplateId);
    			message.writer().writeByte(menuId);
    			if (optionId >= 0)
    			{
    				message.writer().writeByte(optionId);
    			}
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public Message messageNotLogin(sbyte command)
    	{
    		Message message = new Message((sbyte)(-29));
    		message.writer().writeByte(command);
    		return message;
    	}
    
    	public Message messageNotMap(sbyte command)
    	{
    		Message message = new Message((sbyte)(-28));
    		message.writer().writeByte(command);
    		return message;
    	}
    
    	public static Message messageSubCommand(sbyte command)
    	{
    		Message message = new Message((sbyte)(-30));
    		message.writer().writeByte(command);
    		return message;
    	}
    
    	public void setClientType()
    	{
    		if (Rms.loadRMSInt("clienttype") != -1)
    		{
    			Main.typeClient = Rms.loadRMSInt("clienttype");
    		}
    		try
    		{
    			Message message = messageNotLogin(2);
    			message.writer().writeByte(Main.typeClient);
    			message.writer().writeByte(mGraphics.zoomLevel);
    			message.writer().writeBoolean(false);
    			message.writer().writeInt(GameCanvas.w);
    			message.writer().writeInt(GameCanvas.h);
    			message.writer().writeBoolean(TField.isQwerty);
    			message.writer().writeBoolean(GameCanvas.isTouch);
    			message.writer().writeUTF(GameCanvas.getPlatformName() + "|" + GameMidlet.VERSION);
    			DataInputStream dataInputStream = MyStream.readFile("/info");
    			if (dataInputStream != null)
    			{
    				sbyte[] data = new sbyte[dataInputStream.r.buffer.Length];
    				dataInputStream.read(ref data);
    				if (data != null)
    				{
    					message.writer().writeShort(data.Length);
    					message.writer().write(data);
    				}
    			}
    			session.sendMessage(message);
    			message.cleanup();
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    	}
    
    	public void setClientType2()
    	{
    		Res.outz("SET CLIENT TYPE");
    		if (Rms.loadRMSInt("clienttype") != -1)
    		{
    			mSystem.clientType = Rms.loadRMSInt("clienttype");
    		}
    		try
    		{
    			Res.outz("setType");
    			Message message = messageNotLogin(2);
    			message.writer().writeByte(mSystem.clientType);
    			message.writer().writeByte(mGraphics.zoomLevel);
    			Res.outz("gui zoomlevel = " + mGraphics.zoomLevel);
    			message.writer().writeBoolean(false);
    			message.writer().writeInt(GameCanvas.w);
    			message.writer().writeInt(GameCanvas.h);
    			message.writer().writeBoolean(TField.isQwerty);
    			message.writer().writeBoolean(GameCanvas.isTouch);
    			message.writer().writeUTF(GameCanvas.getPlatformName() + "|" + GameMidlet.VERSION);
    			DataInputStream dataInputStream = MyStream.readFile("/info");
    			if (dataInputStream != null)
    			{
    				sbyte[] data = new sbyte[dataInputStream.r.buffer.Length];
    				dataInputStream.read(ref data);
    				if (data != null)
    				{
    					message.writer().writeShort(data.Length);
    					message.writer().write(data);
    				}
    			}
    			session = Session_ME.gI();
    			session.sendMessage(message);
    			session = Session_ME.gI();
    			message.cleanup();
    		}
    		catch (Exception ex)
    		{
    			ex.StackTrace.ToString();
    		}
    	}
    
    	public void sendCheckController()
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-120));
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_7)
    		{
    		    Cout.LogWarning("[Service.cs] " + catchErr_7.Message);
    		}
    		finally
    		{
    			curCheckController = mSystem.currentTimeMillis();
    			message.cleanup();
    		}
    	}
    
    	public void sendCheckMap()
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-121));
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_8)
    		{
    		    Cout.LogWarning("[Service.cs] " + catchErr_8.Message);
    		}
    		finally
    		{
    			curCheckMap = mSystem.currentTimeMillis();
    			message.cleanup();
    		}
    	}
    
    	public void login(string username, string pass, string version, sbyte type)
    	{
    		try
    		{
    			Message message = messageNotLogin(0);
    			message.writer().writeUTF(username);
    			message.writer().writeUTF(pass);
    			message.writer().writeUTF(version);
    			message.writer().writeByte(type);
    			session.sendMessage(message);
    			message.cleanup();
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    	}
    
    	public void requestRegister(string username, string pass, string usernameAo, string passAo, string version)
    	{
    		try
    		{
    			Message message = messageNotLogin(1);
    			message.writer().writeUTF(username);
    			message.writer().writeUTF(pass);
    			if (usernameAo != null && !usernameAo.Equals(string.Empty))
    			{
    				message.writer().writeUTF(usernameAo);
    				message.writer().writeUTF("a");
    			}
    			session.sendMessage(message);
    			message.cleanup();
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    	}
    
    	public void requestChangeMap()
    	{
    		Message message = new Message((sbyte)(-23));
    		session.sendMessage(message);
    		message.cleanup();
    	}
    
    	public void magicTree(sbyte type)
    	{
    		Message message = new Message((sbyte)(-34));
    		try
    		{
    			message.writer().writeByte(type);
    			session.sendMessage(message);
    			message.cleanup();
    		}
    		catch (Exception catchErr_9)
    		{
    		    Cout.LogWarning("[Service.cs] " + catchErr_9.Message);
    		}
    	}
    
    	public void requestChangeZone(int zoneId, int indexUI)
    	{
    		Message message = new Message((sbyte)21);
    		try
    		{
    			message.writer().writeByte(zoneId);
    			session.sendMessage(message);
    			message.cleanup();
    		}
    		catch (Exception catchErr_10)
    		{
    		    Cout.LogWarning("[Service.cs] " + catchErr_10.Message);
    		}
    	}
    
    	public void checkMMove(int second)
    	{
    		Message message = new Message((sbyte)(-78));
    		try
    		{
    			message.writer().writeInt(second);
    			session.sendMessage(message);
    			message.cleanup();
    		}
    		catch (Exception catchErr_11)
    		{
    		    Cout.LogWarning("[Service.cs] " + catchErr_11.Message);
    		}
    	}

    	public static long lastTimeSendMove;

    	public void charMove()
    	{
    		int num = Char.myCharz().cx - Char.myCharz().cxSend;
    		int num2 = Char.myCharz().cy - Char.myCharz().cySend;
    		if (Char.ischangingMap || (num == 0 && num2 == 0) || Controller.isStopReadMessage || Char.myCharz().isTeleport || Char.myCharz().cy <= 0 || Char.myCharz().telePortSkill)
    		{
    			return;
    		}
    		long now = mSystem.currentTimeMillis();
    		bool isUrgent = (Char.myCharz().statusMe == 1 || Char.myCharz().statusMe == 5 || Char.myCharz().statusMe == 14 || Res.abs(num) >= 80 || Res.abs(num2) >= 80);
    		if (!isUrgent && now - lastTimeSendMove < 120)
    		{
    			return;
    		}
    		try
    		{
    			lastTimeSendMove = now;
    			Message message = new Message((sbyte)(-7));
    			Char.myCharz().cxSend = Char.myCharz().cx;
    			Char.myCharz().cySend = Char.myCharz().cy;
    			Char.myCharz().cdirSend = Char.myCharz().cdir;
    			Char.myCharz().cactFirst = Char.myCharz().statusMe;
    			if (TileMap.tileTypeAt(Char.myCharz().cx / TileMap.size, Char.myCharz().cy / TileMap.size) == 0)
    			{
    				message.writer().writeByte((sbyte)1);
    				if (Char.myCharz().canFly)
    				{
    					if (!Char.myCharz().isHaveMount)
    					{
    						Char.myCharz().cMP -= Char.myCharz().cMPGoc / 100 * ((Char.myCharz().isMonkey != 1) ? 1 : 2);
    					}
    					if (Char.myCharz().cMP < 0)
    					{
    						Char.myCharz().cMP = 0;
    					}
    					GameScr.gI().isInjureMp = true;
    					GameScr.gI().twMp = 0;
    				}
    			}
    			else
    			{
    				message.writer().writeByte((sbyte)0);
    			}
    			message.writer().writeShort(Char.myCharz().cx);
    			if (num2 != 0)
    			{
    				message.writer().writeShort(Char.myCharz().cy);
    			}
    			session.sendMessage(message);
    			GameScr.tickMove++;
    			message.cleanup();
    		}
    		catch (Exception ex)
    		{
    			Cout.LogError("LOI CHAR MOVE " + ex.ToString());
    		}
    	}
    
    	public void selectCharToPlay(string charname)
    	{
    		Message message = new Message((sbyte)(-28));
    		try
    		{
    			message.writer().writeByte((sbyte)1);
    			message.writer().writeUTF(charname);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		session.sendMessage(message);
    	}
    
    	public void selectZone(sbyte sub, int value)
    	{
    	}
    
    	public void createChar(string name, int gender, int hair)
    	{
    		Message message = new Message((sbyte)(-28));
    		try
    		{
    			message.writer().writeByte((sbyte)2);
    			message.writer().writeUTF(name);
    			message.writer().writeByte(gender);
    			message.writer().writeByte(hair);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		session.sendMessage(message);
    	}
    
    	public void requestModTemplate(int modTemplateId)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)11);
    			message.writer().writeByte(modTemplateId);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void requestNpcTemplate(int npcTemplateId)
    	{
    		Message message = null;
    		try
    		{
    			message = messageNotMap(12);
    			message.writer().writeByte(npcTemplateId);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void requestSkill(int skillId)
    	{
    		Message message = null;
    		try
    		{
    			message = messageNotMap(9);
    			message.writer().writeShort(skillId);
    			session.sendMessage(message);
    		}
    		catch (Exception ex)
    		{
    			Cout.println(ex.Message + ex.StackTrace);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    }
}
