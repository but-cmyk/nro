namespace Game1
{
    using System;
    using Game1.Assets.src.g;
    using Game1.God;
    
    public partial class Service
    {
    
    	public void loadRMS(string key)
    	{
    		Cout.println("REQUEST RMS");
    		Message message = null;
    		try
    		{
    			message = messageSubCommand(61);
    			message.writer().writeUTF(key);
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
    
    	public void clearTask()
    	{
    		Message message = null;
    		try
    		{
    			message = messageNotMap(17);
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
    
    	public void changeName(string name, int id)
    	{
    		Message message = null;
    		try
    		{
    			message = messageNotMap(18);
    			message.writer().writeInt(id);
    			message.writer().writeUTF(name);
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
    
    	public void requestIcon(int id)
    	{
    		//GameCanvas.connect();
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-67));
    			message.writer().writeInt(id);
    			session = Session_ME.gI();
    			session.sendMessage(message);
    			session = Session_ME.gI();
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
    
    	public void doConvertUpgrade(int index1, int index2, int index3)
    	{
    		Message message = null;
    		try
    		{
    			message = messageNotMap(33);
    			message.writer().writeByte(index1);
    			message.writer().writeByte(index2);
    			message.writer().writeByte(index3);
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
    
    	public void inviteClanDun(string name)
    	{
    		Message message = null;
    		try
    		{
    			message = messageNotMap(34);
    			message.writer().writeUTF(name);
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
    
    	public void inputNumSplit(int indexItem, int numSplit)
    	{
    		Message message = null;
    		try
    		{
    			message = messageNotMap(40);
    			message.writer().writeByte(indexItem);
    			message.writer().writeInt(numSplit);
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
    
    	public void activeAccProtect(int pass)
    	{
    		Message message = null;
    		try
    		{
    			message = messageNotMap(37);
    			message.writer().writeInt(pass);
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
    
    	public void clearAccProtect(int pass)
    	{
    		Message message = null;
    		try
    		{
    			message = messageNotMap(41);
    			message.writer().writeInt(pass);
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
    
    	public void updateActive(int passOld, int passNew)
    	{
    		Message message = null;
    		try
    		{
    			message = messageNotMap(38);
    			message.writer().writeInt(passOld);
    			message.writer().writeInt(passNew);
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
    
    	public void openLockAccProtect(int pass2)
    	{
    		Message message = null;
    		try
    		{
    			message = messageNotMap(39);
    			message.writer().writeInt(pass2);
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
    
    	public void getBgTemplate(short id)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-32));
    			message.writer().writeShort(id);
    			session = Session_ME.gI();
    			session.sendMessage(message);
    			session = Session_ME.gI();
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
    
    	public void getMapOffline()
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-33));
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
    
    	public void finishUpdate()
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-38));
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
    
    	public void finishUpdate(int playerID)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-38));
    			message.writer().writeInt(playerID);
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_1)
    		{
    		    Cout.LogWarning("[Service.Part2.cs] " + catchErr_1.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void finishLoadMap()
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-39));
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
    
    	public void getChest(sbyte action)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-35));
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
    
    	public void requestBagImage(sbyte ID)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-63));
    			message.writer().writeByte(ID);
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
    
    	public void getBag(sbyte action)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-36));
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
    
    	public void getBody(sbyte action)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-37));
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
    
    	public void login2(string user)
    	{
    		Res.outz("Login 2");
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-101));
    			message.writer().writeUTF(user);
    			message.writer().writeByte(1);
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_2)
    		{
    		    Cout.LogWarning("[Service.Part2.cs] " + catchErr_2.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void getMagicTree(sbyte action)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-34));
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
        public void upPotentialPet(int typePotential, int num)
        {
            Message message = null;
            try
            {
                message = messageSubCommand(-99);
                message.writer().writeByte(typePotential);
                message.writer().writeShort(num);
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
    
        public void upPotential(int typePotential, int num)
    	{
    		Message message = null;
    		try
    		{
    			message = messageSubCommand(16);
    			message.writer().writeByte(typePotential);
    			message.writer().writeShort(num);
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
    
    	public void getResource(sbyte action, MyVector vResourceIndex)
    	{
    		Res.outz("request resource action= " + action);
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-74));
    			message.writer().writeByte(action);
    			if (action == 2 && vResourceIndex != null)
    			{
    				message.writer().writeShort(vResourceIndex.size());
    				for (int i = 0; i < vResourceIndex.size(); i++)
    				{
    					message.writer().writeShort(short.Parse((string)vResourceIndex.elementAt(i)));
    				}
    			}
    			else
    			{
    				reciveFromMainSession = true;
    				session = Session_ME.gI();
    			}
    			session.sendMessage(message);
    			session = Session_ME.gI();
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
    
    	public void requestMapSelect(int selected)
    	{
    		Res.outz("request magic tree");
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-91));
    			message.writer().writeByte(selected);
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_3)
    		{
    		    Cout.LogWarning("[Service.Part2.cs] " + catchErr_3.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void petInfo()
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-107));
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_4)
    		{
    		    Cout.LogWarning("[Service.Part2.cs] " + catchErr_4.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void sendTop(string topName, sbyte selected)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-96));
    			message.writer().writeUTF(topName);
    			message.writer().writeByte(selected);
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_5)
    		{
    		    Cout.LogWarning("[Service.Part2.cs] " + catchErr_5.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void enemy(sbyte b, int charID)
    	{
    		Message message = null;
    		Res.outz("add enemy");
    		try
    		{
    			message = new Message((sbyte)(-99));
    			message.writer().writeByte(b);
    			if (b == 1 || b == 2)
    			{
    				message.writer().writeInt(charID);
    			}
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_6)
    		{
    		    Cout.LogWarning("[Service.Part2.cs] " + catchErr_6.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void kigui(sbyte action, int itemId, sbyte moneyType, int money, int quaintly)
    	{
    		Message message = null;
    		try
    		{
    			Res.outz("ki gui action= " + action);
    			message = new Message((sbyte)(-100));
    			message.writer().writeByte(action);
    			if (action == 0)
    			{
    				message.writer().writeShort(itemId);
    				message.writer().writeByte(moneyType);
    				message.writer().writeInt(money);
    				message.writer().writeInt(quaintly);
    			}
    			if (action == 1 || action == 2)
    			{
    				message.writer().writeShort(itemId);
    			}
    			if (action == 3)
    			{
    				message.writer().writeShort(itemId);
    				message.writer().writeByte(moneyType);
    				message.writer().writeInt(money);
    			}
    			if (action == 4)
    			{
    				message.writer().writeByte(moneyType);
    				message.writer().writeByte(money);
    				Res.outz("currTab= " + moneyType + " page= " + money);
    			}
    			if (action == 5)
    			{
    				message.writer().writeShort(itemId);
    			}
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_7)
    		{
    		    Cout.LogWarning("[Service.Part2.cs] " + catchErr_7.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void getFlag(sbyte action, sbyte flagType)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-103));
    			message.writer().writeByte(action);
    			Res.outz("------------service--  " + action + "   " + flagType);
    			if (action != 0)
    			{
    				message.writer().writeByte(flagType);
    			}
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_8)
    		{
    		    Cout.LogWarning("[Service.Part2.cs] " + catchErr_8.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void setLockInventory(int pass)
    	{
    		Message message = null;
    		try
    		{
    			Res.outz("------------setLockInventory:     " + pass);
    			message = new Message((sbyte)(-104));
    			message.writer().writeInt(pass);
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_9)
    		{
    		    Cout.LogWarning("[Service.Part2.cs] " + catchErr_9.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void petStatus(sbyte status)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-108));
    			message.writer().writeByte(status);
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_10)
    		{
    		    Cout.LogWarning("[Service.Part2.cs] " + catchErr_10.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void transportNow()
    	{
    		Message message = null;
    		try
    		{
    			Res.outz("------------transportNow  ");
    			message = new Message((sbyte)(-105));
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_11)
    		{
    		    Cout.LogWarning("[Service.Part2.cs] " + catchErr_11.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void funsion(sbyte type)
    	{
    		Message message = null;
    		try
    		{
    			Res.outz("FUNSION");
    			message = new Message((sbyte)125);
    			message.writer().writeByte(type);
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
    
    	public void imageSource(MyVector vID)
    	{
    		Message message = null;
    		try
    		{
    			Res.outz("IMAGE SOURCE size= " + vID.size());
    			message = new Message((sbyte)(-111));
    			message.writer().writeShort(vID.size());
    			if (vID.size() > 0)
    			{
    				for (int i = 0; i < vID.size(); i++)
    				{
    					Res.outz("gui len str " + ((ImageSource)vID.elementAt(i)).id);
    					message.writer().writeUTF(((ImageSource)vID.elementAt(i)).id);
    				}
    			}
    			else
    			{
    				session = Session_ME.gI();
    				reciveFromMainSession = true;
    			}
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
    
    	public void getQuayso()
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-126));
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
    
    	public void sendServerData(sbyte action, int id, sbyte[] data)
    	{
    		Message message = null;
    		try
    		{
    			Res.outz("SERVER DATA");
    			message = new Message((sbyte)(-110));
    			message.writer().writeByte(action);
    			if (action == 1)
    			{
    				message.writer().writeInt(id);
    				if (data != null)
    				{
    					int num = data.Length;
    					message.writer().writeShort(num);
    					message.writer().write(ref data, 0, num);
    				}
    			}
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_12)
    		{
    		    Cout.LogWarning("[Service.Part2.cs] " + catchErr_12.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void changeOnKeyScr(sbyte[] skill)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-113));
    			for (int i = 0; i < GameScr.onScreenSkill.Length; i++)
    			{
    				message.writer().writeByte(skill[i]);
    			}
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
    
    	public void requestPean()
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-114));
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
    
    	public void sendThachDau(int id)
    	{
    		Res.outz("GUI THACH DAU");
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-118));
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
    
    	public void messagePlayerMenu(int charId)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-30));
    			message.writer().writeByte((sbyte)63);
    			message.writer().writeInt(charId);
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
    
    	public void playerMenuAction(int charId, short select)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-30));
    			message.writer().writeByte((sbyte)64);
    			message.writer().writeInt(charId);
    			message.writer().writeShort(select);
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
    
    	public void getImgByName(string nameImg)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)66);
    			message.writer().writeUTF(nameImg);
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_13)
    		{
    		    Cout.LogWarning("[Service.Part2.cs] " + catchErr_13.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void SendCrackBall(byte type, byte soluong)
    	{
    		Message message = new Message((sbyte)(-127));
    		try
    		{
    			message.writer().writeByte(type);
    			if (soluong > 0)
    			{
    				message.writer().writeByte(soluong);
    			}
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_14)
    		{
    		    Cout.LogWarning("[Service.Part2.cs] " + catchErr_14.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void SendRada(int i, int id)
    	{
    		Message message = new Message(sbyte.MaxValue);
    		try
    		{
    			message.writer().writeByte(i);
    			if (id != -1)
    			{
    				message.writer().writeShort(id);
    			}
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_15)
    		{
    		    Cout.LogWarning("[Service.Part2.cs] " + catchErr_15.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void sendOptHat(sbyte action)
    	{
    		Message message = new Message((sbyte)24);
    		try
    		{
    			if (action == 1)
    			{
    				sbyte[] array = Res.TakeSnapShot();
    				message.writer().writeByte(1);
    				message.writer().writeShort(array.Length);
    				message.writer().write(array);
    			}
    			else
    			{
    				message.writer().writeByte((Char.myCharz().idHat != -1) ? (-1) : 0);
    			}
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_16)
    		{
    		    Cout.LogWarning("[Service.Part2.cs] " + catchErr_16.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void sendDelAcc()
    	{
    		Message message = new Message((sbyte)69);
    		try
    		{
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_17)
    		{
    		    Cout.LogWarning("[Service.Part2.cs] " + catchErr_17.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    
    	public void new_skill_not_focus(sbyte idTemplateSkill, sbyte dir, short x, short y)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-45));
    			message.writer().writeSByte(20);
    			message.writer().writeSByte(idTemplateSkill);
    			message.writer().writeShort(Char.myCharz().cx);
    			message.writer().writeShort(Char.myCharz().cy);
    			message.writer().writeSByte(dir);
    			message.writer().writeShort(x);
    			message.writer().writeShort(y);
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
    
    	public void sendCT_ready(sbyte sub, sbyte sub_sub)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)24);
    			message.writer().writeByte(sub);
    			message.writer().writeByte(sub_sub);
    			Res.err(" =====> SEND OPTION_HAT " + sub + "_" + sub_sub);
    			session.sendMessage(message);
    		}
    		catch (Exception catchErr_18)
    		{
    		    Cout.LogWarning("[Service.Part2.cs] " + catchErr_18.Message);
    		}
    		finally
    		{
    			message.cleanup();
    		}
    	}
    }
}
