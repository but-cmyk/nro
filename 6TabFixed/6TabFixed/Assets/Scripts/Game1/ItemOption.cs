namespace Game1
{
    public class ItemOption
    {
    	public int param;
    
    	public sbyte active;
    
    	public sbyte activeCard;
    
    	public ItemOptionTemplate optionTemplate;
    
    	public ItemOption()
    	{
    	}
    
    	public ItemOption(int optionTemplateId, int param)
    	{
    		if (optionTemplateId == 22)
    		{
    			optionTemplateId = 6;
    			param *= 1000;
    		}
    		if (optionTemplateId == 23)
    		{
    			optionTemplateId = 7;
    			param *= 1000;
    		}
    		this.param = param;
    		if (GameScr.gI().iOptionTemplates != null && optionTemplateId >= 0 && optionTemplateId < GameScr.gI().iOptionTemplates.Length)
    		{
    			optionTemplate = GameScr.gI().iOptionTemplates[optionTemplateId];
    		}
    	}
    
    	public string getOptionString()
    	{
    		return optionTemplate != null ? NinjaUtil.replace(optionTemplate.name, "#", param + string.Empty) : "";
    	}
    
    	public string getOptionName()
    	{
    		return optionTemplate != null ? NinjaUtil.replace(optionTemplate.name, "+#", string.Empty) : "";
    	}
    
    	public string getOptiongColor()
    	{
    		return optionTemplate != null ? NinjaUtil.replace(optionTemplate.name, "$", string.Empty) : "";
    	}
    }
}
