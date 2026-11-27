namespace Game1
{
    using System;
    using Game1.Assets.src.g;
    using Game1.God;
    
    public partial class Service
    {
    
    	public void requestItemInfo(int typeUI, int indexUI)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)35);
    			message.writer().writeByte(typeUI);
    			message.writer().writeByte(indexUI);
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
    
    	public void requestItemPlayer(int charId, int indexUI)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)90);
    			message.writer().writeInt(charId);
    			message.writer().writeByte(indexUI);
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
    
    	public void upSkill(int skillTemplateId, int point)
    	{
    		Message message = null;
    		try
    		{
    			message = messageSubCommand(17);
    			message.writer().writeShort(skillTemplateId);
    			message.writer().writeByte(point);
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
    
    	public void saleItem(sbyte action, sbyte type, short id)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)7);
    			message.writer().writeByte(action);
    			message.writer().writeByte(type);
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
    
    	public void buyItem(sbyte type, int id, int quantity)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)6);
    			message.writer().writeByte(type);
    			message.writer().writeShort(id);
    			if (quantity > 1)
    			{
    				message.writer().writeShort(quantity);
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
    
    	public void selectSkill(int skillTemplateId)
    	{
    		Cout.println(Char.myCharz().cName + " SELECT SKILL " + skillTemplateId);
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)34);
    			message.writer().writeShort(skillTemplateId);
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
    
    	public void getEffData(short id)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-66));
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
    
    	public void openUIZone()
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)29);
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
    
    	public void confirmMenu(short npcID, sbyte select)
    	{
    		Res.outz("confirme menu" + select);
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)32);
    			message.writer().writeShort(npcID);
    			message.writer().writeByte(select);
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
    
    	public void openMenu(int npcId)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)33);
    			message.writer().writeShort(npcId);
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
    
    	public void menu(int npcId, int menuId, int optionId)
    	{
    		Cout.println("menuid: " + menuId);
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)22);
    			message.writer().writeByte(npcId);
    			message.writer().writeByte(menuId);
    			message.writer().writeByte(optionId);
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
    
    	public void menuId(short menuId)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)27);
    			message.writer().writeShort(menuId);
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
    
    	public void textBoxId(short menuId, string str)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)88);
    			message.writer().writeShort(menuId);
    			message.writer().writeUTF(str);
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
    
    	public void requestItem(int typeUI)
    	{
    		Message message = null;
    		try
    		{
    			message = messageSubCommand(22);
    			message.writer().writeByte(typeUI);
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
    
    	public void boxSort()
    	{
    		Message message = null;
    		try
    		{
    			message = messageSubCommand(19);
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
    
    	public void boxCoinOut(int coinOut)
    	{
    		Message message = null;
    		try
    		{
    			message = messageSubCommand(21);
    			message.writer().writeInt(coinOut);
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
    
    	public void upgradeItem(Item item, Item[] items, bool isGold)
    	{
    		GameCanvas.msgdlg.pleasewait();
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)14);
    			message.writer().writeBoolean(isGold);
    			message.writer().writeByte(item.indexUI);
    			for (int i = 0; i < items.Length; i++)
    			{
    				if (items[i] != null)
    				{
    					message.writer().writeByte(items[i].indexUI);
    				}
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
    
    	public void crystalCollectLock(Item[] items)
    	{
    		GameCanvas.msgdlg.pleasewait();
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)13);
    			for (int i = 0; i < items.Length; i++)
    			{
    				if (items[i] != null)
    				{
    					message.writer().writeByte(items[i].indexUI);
    				}
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
    
    	public void acceptInviteTrade(int playerMapId)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)37);
    			message.writer().writeInt(playerMapId);
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
    
    	public void cancelInviteTrade()
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)50);
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
    
    	public void tradeAccept()
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)39);
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
    
    	public void tradeItemLock(int coin, Item[] items)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)38);
    			message.writer().writeInt(coin);
    			int num = 0;
    			for (int i = 0; i < items.Length; i++)
    			{
    				if (items[i] != null)
    				{
    					num++;
    				}
    			}
    			message.writer().writeByte(num);
    			for (int j = 0; j < items.Length; j++)
    			{
    				if (items[j] != null)
    				{
    					message.writer().writeByte(items[j].indexUI);
    				}
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
    
    	public void sendPlayerAttack(MyVector vMob, MyVector vChar, int type)
    	{
    		try
    		{
    			Message message = null;
    			if (type == 0)
    			{
    				return;
    			}
    			if (vMob.size() > 0 && vChar.size() > 0)
    			{
    				switch (type)
    				{
    				case 1:
    					message = new Message((sbyte)(-4));
    					break;
    				case 2:
    					message = new Message((sbyte)67);
    					break;
    				}
    				message.writer().writeByte(vMob.size());
    				for (int i = 0; i < vMob.size(); i++)
    				{
    					Mob mob = (Mob)vMob.elementAt(i);
    					message.writer().writeByte(mob.mobId);
    				}
    				for (int j = 0; j < vChar.size(); j++)
    				{
    					Char @char = (Char)vChar.elementAt(j);
    					if (@char != null)
    					{
    						message.writer().writeInt(@char.charID);
    					}
    					else
    					{
    						message.writer().writeInt(-1);
    					}
    				}
    			}
    			else if (vMob.size() > 0)
    			{
    				message = new Message((sbyte)54);
    				for (int k = 0; k < vMob.size(); k++)
    				{
    					Mob mob2 = (Mob)vMob.elementAt(k);
    					if (!mob2.isMobMe)
    					{
    						message.writer().writeByte(mob2.mobId);
    						continue;
    					}
    					message.writer().writeByte((sbyte)(-1));
    					message.writer().writeInt(mob2.mobId);
    				}
    			}
    			else if (vChar.size() > 0)
    			{
    				message = new Message((sbyte)(-60));
    				for (int l = 0; l < vChar.size(); l++)
    				{
    					Char char2 = (Char)vChar.elementAt(l);
    					message.writer().writeInt(char2.charID);
    				}
    			}
    			message.writer().writeSByte((sbyte)Char.myCharz().cdir);
    			if (message != null)
    			{
    				session.sendMessage(message);
    			}
    		}
    		catch (Exception)
    		{
    			Res.err(">>err ATTACT  vMob=" + vMob.size() + "  vChar=" + vChar.size());
    		}
    	}
    
    	public void pickItem(int itemMapId)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-20));
    			message.writer().writeShort(itemMapId);
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
    
    	public void throwItem(int index)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-18));
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
    
    	public void returnTownFromDead()
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-15));
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
    
    	public void wakeUpFromDead()
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-16));
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
    
    	public void chat(string text)
        {
            if (ClientManager.getInstance().Chat(text)) return;
            Message message = null;
    		try
    		{
    			message = new Message((sbyte)44);
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
    
    	public void updateData()
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-87));
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
    
    	public void updateMap()
    	{
    		Message message = null;
    		try
    		{
    			message = messageNotMap(6);
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
    
    	public void updateSkill()
    	{
    		Message message = null;
    		try
    		{
    			message = messageNotMap(7);
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
    
    	public void updateItem()
    	{
    		Message message = null;
    		try
    		{
    			message = messageNotMap(8);
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
    
    	public void clientOk()
    	{
    		Message message = null;
    		try
    		{
    			message = messageNotMap(13);
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
    
    	public void tradeInvite(int charId)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)36);
    			message.writer().writeInt(charId);
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
    
    	public void addFriend(string name)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)53);
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
    
    	public void addPartyAccept(int charId)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)76);
    			message.writer().writeInt(charId);
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
    
    	public void addPartyCancel(int charId)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)77);
    			message.writer().writeInt(charId);
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
    
    	public void testInvite(int charId)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)59);
    			message.writer().writeInt(charId);
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
    
    	public void addCuuSat(int charId)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)62);
    			message.writer().writeInt(charId);
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
    
    	public void addParty(string name)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)75);
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
    
    	public void player_vs_player(sbyte action, sbyte type, int playerId)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-59));
    			message.writer().writeByte(action);
    			message.writer().writeByte(type);
    			message.writer().writeInt(playerId);
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
    
    	public void requestMaptemplate(int maptemplateId)
    	{
    		Message message = null;
    		try
    		{
    			message = messageNotMap(10);
    			message.writer().writeByte(maptemplateId);
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
    
    	public void outParty()
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)79);
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
    
    	public void requestPlayerInfo(MyVector chars)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)18);
    			message.writer().writeByte(chars.size());
    			for (int i = 0; i < chars.size(); i++)
    			{
    				Char @char = (Char)chars.elementAt(i);
    				message.writer().writeInt(@char.charID);
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
    
    	public void pleaseInputParty(string str)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)16);
    			message.writer().writeUTF(str);
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
    
    	public void acceptPleaseParty(string str)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)17);
    			message.writer().writeUTF(str);
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
    
    	public void chatPlayer(string text, int id)
    	{
    		Res.outz("chat player text = " + text);
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-72));
    			message.writer().writeInt(id);
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
    
    	public void chatGlobal(string text)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)(-71));
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
    
    	public void chatPrivate(string to, string text)
    	{
    		Message message = null;
    		try
    		{
    			message = new Message((sbyte)91);
    			message.writer().writeUTF(to);
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
    
    	public void sendCardInfo(string NAP, string PIN)
    	{
    		Message message = null;
    		try
    		{
    			message = messageNotMap(16);
    			message.writer().writeUTF(NAP);
    			message.writer().writeUTF(PIN);
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
    
    	public void saveRms(string key, sbyte[] data)
    	{
    		Message message = null;
    		try
    		{
    			message = messageSubCommand(60);
    			message.writer().writeUTF(key);
    			message.writer().writeInt(data.Length);
    			message.writer().write(data);
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
