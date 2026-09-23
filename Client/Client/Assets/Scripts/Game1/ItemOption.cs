namespace Game1
{
    public class ItemOption
    {
    	public int param;

    	public sbyte active;

    	public sbyte activeCard;

    	public int optionTemplateId = -1;

    	private ItemOptionTemplate _optionTemplate;

    	public ItemOptionTemplate optionTemplate
    	{
    		get
    		{
    			if (_optionTemplate == null || string.IsNullOrEmpty(_optionTemplate.name))
    			{
    				if (optionTemplateId >= 0)
    				{
    					ItemOptionTemplate t = GameScr.getItemOptionTemplate(optionTemplateId);
    					if (t != null && !string.IsNullOrEmpty(t.name))
    					{
    						_optionTemplate = t;
    					}
    					else
    					{
    						string defaultName = DefaultItemOptions.GetOptionName(optionTemplateId);
    						if (!string.IsNullOrEmpty(defaultName))
    						{
    							_optionTemplate = new ItemOptionTemplate
    							{
    								id = optionTemplateId,
    								name = defaultName
    							};
    						}
    					}
    				}
    			}
    			return _optionTemplate;
    		}
    		set
    		{
    			_optionTemplate = value;
    			if (value != null)
    			{
    				optionTemplateId = value.id;
    			}
    		}
    	}

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
    		this.optionTemplateId = optionTemplateId;
    		_optionTemplate = GameScr.getItemOptionTemplate(optionTemplateId);
    		if (_optionTemplate == null || string.IsNullOrEmpty(_optionTemplate.name))
    		{
    			string optName = string.Empty;
    			if (optionTemplateId == 66)
    			{
    				optName = "Chưa có";
    			}
    			else if (optionTemplateId == 63)
    			{
    				optName = "Còn # ngày";
    			}
    			else if (optionTemplateId == 64)
    			{
    				optName = "Còn # giờ";
    			}
    			else if (optionTemplateId == 65)
    			{
    				optName = "Còn # phút";
    			}
    			else
    			{
    				optName = DefaultItemOptions.GetOptionName(optionTemplateId);
    			}
    			_optionTemplate = new ItemOptionTemplate
    			{
    				id = optionTemplateId,
    				name = optName
    			};
    		}
    	}

    	public string getOptionString()
    	{
    		if (optionTemplateId == 73 || optionTemplateId == 206)
    		{
    			return string.Empty;
    		}
    		ItemOptionTemplate opt = optionTemplate;
    		string templateName = (opt != null) ? opt.name : null;
    		if (string.IsNullOrEmpty(templateName) && optionTemplateId >= 0)
    		{
    			templateName = DefaultItemOptions.GetOptionName(optionTemplateId);
    		}
    		if (!string.IsNullOrEmpty(templateName))
    		{
    			if (templateName.StartsWith("Option", System.StringComparison.OrdinalIgnoreCase))
    			{
    				return string.Empty;
    			}
    			return NinjaUtil.replace(templateName, "#", param + string.Empty);
    		}
    		return string.Empty;
    	}

    	public string getOptionName()
    	{
    		if (optionTemplateId == 73 || optionTemplateId == 206)
    		{
    			return string.Empty;
    		}
    		ItemOptionTemplate opt = optionTemplate;
    		string templateName = (opt != null) ? opt.name : null;
    		if (string.IsNullOrEmpty(templateName) && optionTemplateId >= 0)
    		{
    			templateName = DefaultItemOptions.GetOptionName(optionTemplateId);
    		}
    		if (!string.IsNullOrEmpty(templateName))
    		{
    			if (templateName.StartsWith("Option", System.StringComparison.OrdinalIgnoreCase))
    			{
    				return string.Empty;
    			}
    			string name = NinjaUtil.replace(templateName, "+#", string.Empty);
    			name = NinjaUtil.replace(name, "#", string.Empty);
    			name = NinjaUtil.replace(name, "$", string.Empty);
    			return name;
    		}
    		return string.Empty;
    	}

    	public string getOptiongColor()
    	{
    		if (optionTemplateId == 73 || optionTemplateId == 206)
    		{
    			return string.Empty;
    		}
    		ItemOptionTemplate opt = optionTemplate;
    		string templateName = (opt != null) ? opt.name : null;
    		if (string.IsNullOrEmpty(templateName) && optionTemplateId >= 0)
    		{
    			templateName = DefaultItemOptions.GetOptionName(optionTemplateId);
    		}
    		if (string.IsNullOrEmpty(templateName) || templateName.StartsWith("Option", System.StringComparison.OrdinalIgnoreCase))
    		{
    			return string.Empty;
    		}
    		return NinjaUtil.replace(templateName, "$", string.Empty);
    	}
    }
}
