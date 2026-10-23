namespace Game1
{
    public class Message
    {
    	public sbyte command;
    
    	private myReader dis;
    
    	private myWriter dos;
    
    	public Message(int command)
    	{
    		this.command = (sbyte)command;
    		dos = new myWriter();
    	}
    
    	public Message()
    	{
    		dos = new myWriter();
    	}
    
    	public Message(sbyte command)
    	{
    		this.command = command;
    		dos = new myWriter();
    	}
    
    	public Message(sbyte command, sbyte[] data)
    	{
    		this.command = command;
    		dis = new myReader(data);
    	}
    
    	public sbyte[] getData()
    	{
    		return dos.getData();
    	}
    
    	public myReader reader()
    	{
    		return dis;
    	}
    
    	public myWriter writer()
    	{
    		return dos;
    	}
    
    	/// <summary>
    	/// Đọc giá trị số nguyên 4 bytes (int).
    	/// Ghi chú kiến trúc: Đặt tên 'readInt3Byte' theo chuẩn protocol cũ của NRO để giữ tương thích gọi hàm,
    	/// nhưng triển khai thực tế gọi dis.readInt() (4 bytes) hỗ trợ dải giá trị HP/dame lớn.
    	/// </summary>
    	public int readInt3Byte()
    	{
    		return dis.readInt();
    	}
    
    	public void cleanup()
    	{
    	}
    }
}
