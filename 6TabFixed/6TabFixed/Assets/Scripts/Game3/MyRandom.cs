using System;

namespace Game3
{
    
    public class MyRandom
    {
    	public Random r;
    
    	public MyRandom()
    	{
    		r = new Random();
    	}
    
    	public int nextInt()
    	{
    		return r.Next();
    	}
    
    	public int nextInt(int a)
    	{
    		if (a <= 0) return 0;
    		return r.Next(a);
    	}
    
    	public int nextInt(int a, int b)
    	{
    		if (a >= b) return a;
    		return r.Next(a, b);
    	}
    }
}
