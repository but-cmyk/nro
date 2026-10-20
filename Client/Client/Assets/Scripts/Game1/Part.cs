namespace Game1
{
    public class Part
    {
    	public int type;
    
    	public PartImage[] pi;
    
    	public Part(int type)
    	{
    		this.type = type;
    		if (type == 0)
    		{
    			pi = new PartImage[3];
    		}
    		else if (type == 1)
    		{
    			pi = new PartImage[17];
    		}
    		else if (type == 2)
    		{
    			pi = new PartImage[14];
    		}
    		else if (type == 3)
    		{
    			pi = new PartImage[2];
    		}
    		else
    		{
    			pi = new PartImage[0];
    		}
    	}
    }
}
