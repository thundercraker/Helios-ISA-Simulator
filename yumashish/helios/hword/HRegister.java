package yumashish.helios.hword;

public class HRegister implements CharSequence {
	
	private String data;
	private short bits;
	
	public HRegister(short bits)
	{
		this.bits = bits;
		this.H_IN("");
	}
	
	public HRegister(HRegister reg)
	{
		this.bits = reg.size();
		this.H_IN(reg);
	}
	
	public HRegister(short bits, String in)
	{
		this.bits = bits;
		this.H_IN(in);
	}
	
	public HRegister(short bits, int in)
	{
		this.bits = bits;
		this.H_IN(Integer.toBinaryString(in));
	}
	
	public short size()
	{
		return bits;
	}
	
	/*private String reverse(String in)
	{
		if(in == null) return null;
		String t = "";
		for(int i = in.length() - 1; i >= 0; i--)
			t = t + String.valueOf(in.charAt(i));
		return t;
	}*/
	
	public String H_IN(HRegister reg)
	{
		String in = reg.getData();
		//System.out.println("H_IN SIZE: " + in.length());
		
		if(in.length() > bits)
		{
			data = in.substring(in.length() - bits);
			return in.substring(0, in.length() - bits);
		}
		else {
			//System.out.println(bits - in.length());
			for(int f = bits - in.length(); f > 0; f--)
			{
				in = "0" + in;
			}
		}
		data = in;
		//System.out.println(data);
		return null;
	}
	
	public String H_IN(String in)
	{
		if(in.length() > bits)
		{
			data = in.substring(in.length() - bits);
			return in.substring(0, in.length() - bits);
		} else {
			//System.out.println(bits - in.length());
			for(int f = bits - in.length(); f > 0; f--)
			{
				in = "0" + in;
			}
		}
		data = in;
		return null;
	}
	
	public void L_SHIFT(int i, boolean one)
	{
		String temp = "";
		String fill = (one) ? "1" : "0";
		if(i > 0)
		{
			temp = data;
			for(int j = 0; j < i; j++)
				temp = temp + fill;
		}
		this.H_IN(temp);
	}
	
	public void L_ARTH_SHIFT(int i)
	{
		L_SHIFT(1, this.BIT_AT(0));
	}
	
	public void R_SHIFT(int i, boolean one)
	{
		String fill = (one) ? "1" : "0";
		if(i > 0)
		{
			String temp = data;
			String t2 = "";
			for(int j = 0; j < i; j++)
			{
				temp = fill + temp;
				t2 = String.valueOf(temp.charAt(temp.length()-1)) + t2;
				temp = temp.substring(0, temp.length() - 1);
			}
			data = temp;
		}
	}
	
	public void R_ARTH_SHIFT(int i)
	{
		R_SHIFT(1, this.BIT_AT(0));
	}
	
	public boolean BIT_AT(int i)
	{
		return (String.valueOf(data.charAt(i)).equalsIgnoreCase("1")) ? true : false;
	}
	
	public void SET_AT(boolean bit, int at)
	{
		char[] temp = data.toCharArray();
		temp[at] = (bit) ? '1' : '0';
		data = String.valueOf(temp);
	}
	
	public String toString()
	{
		String temp = "";
		for(int i = 0; i < data.length(); i++)
		{
			if(i % 4 == 0 && i != 0)
			{
				temp += " ";
			}
			temp += String.valueOf(data.charAt(i));
				
		}
		return temp;
	}
	
	public String getData()
	{
		return data;
	}

	@Override
	public char charAt(int arg0) {
		return data.charAt(arg0);
	}

	@Override
	public int length() {
		return data.length();
	}

	@Override
	public CharSequence subSequence(int arg0, int arg1) {
		return data.subSequence(arg0, arg1);
	}
}

