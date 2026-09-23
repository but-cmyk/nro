namespace Game1
{
    public class ItemTemplates
    {
    	public static MyHashTable itemTemplates = new MyHashTable();
    
    	public static void add(ItemTemplate it)
    	{
    		itemTemplates.put(it.id, it);
    	}
    
    	public static ItemTemplate get(short id)
    	{
    		ItemTemplate it = (ItemTemplate)itemTemplates.get(id);
    		if (it == null)
    		{
    			it = new ItemTemplate(id, 0, 0, "Item " + id, string.Empty, 0, 0, 0, 0, false);
    			itemTemplates.put(id, it);
    		}
    		return it;
    	}

    	public static short getPart(short itemTemplateID)
    	{
    		ItemTemplate it = get(itemTemplateID);
    		return (it != null) ? it.part : (short)0;
    	}

    	public static short getIcon(short itemTemplateID)
    	{
    		ItemTemplate it = get(itemTemplateID);
    		return (it != null) ? it.iconID : (short)0;
    	}
    }
}
