namespace Game1
{
    using UnityEngine;
    
    public class MyAudioClip
    {
    	public string name;
    
    	public AudioClip clip;
    
    	public long timeStart;
    
    	public MyAudioClip(string filename)
    	{
    		clip = (AudioClip)Resources.Load(filename);
    		name = filename;
    	}
    
    	private static AudioSource cachedAudioSource;

    	public void Play()
    	{
    		if (cachedAudioSource == null && Main.main != null)
    		{
    			cachedAudioSource = Main.main.GetComponent<AudioSource>();
    		}
    		if (cachedAudioSource != null && clip != null)
    		{
    			cachedAudioSource.PlayOneShot(clip);
    		}
    		timeStart = mSystem.currentTimeMillis();
    	}
    
    	public bool isPlaying()
    	{
    		return false;
    	}
    }
}
