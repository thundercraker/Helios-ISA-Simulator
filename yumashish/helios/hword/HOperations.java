package yumashish.helios.hword;

import yumashish.helios.hword.HeliosISA.ControlException;

public class HOperations {
	
	public HOperations(short SYSTEM_BITS)
	//main memory created with default size of 128 words
	//and block size of 8 words
	{
		this.SYSTEM_BITS = SYSTEM_BITS;
		AU1 = new HRegister(SYSTEM_BITS);
		AU2 = new HRegister(SYSTEM_BITS);
		AU3 = new HRegister(SYSTEM_BITS);
		memory = new HMainMemory(128, SYSTEM_BITS, (short)8);
	}
	
	public HOperations(short SYSTEM_BITS, int MEMORY_SIZE, short BLOCK_SIZE)
	{
		this.SYSTEM_BITS = SYSTEM_BITS;
		AU1 = new HRegister(SYSTEM_BITS);
		AU2 = new HRegister(SYSTEM_BITS);
		AU3 = new HRegister(SYSTEM_BITS);
		memory = new HMainMemory(MEMORY_SIZE, SYSTEM_BITS, BLOCK_SIZE);
	}
	
	public short SYSTEM_BITS;
	public boolean CARRY, NEGATIVE;
	public HRegister AU1, AU2, AU3;
	public HMainMemory memory;
	public int ICNT;
	
	//utility methods
	public String toBinary(int data)
	//turn an integer to twos complement binary string
	{
		ICNT += SYSTEM_BITS * SYSTEM_BITS; 
		//this operation involved power * multiplication for every unit
		//power,multiplication are bounded by O(n^2)
		return Integer.toBinaryString(data);
	}
	
	public int toDecimal(HRegister R)
	{
		if(R.BIT_AT(0))
		//negative
		{
			R = TWOS_COMPLEMENT(R);
			ICNT += SYSTEM_BITS * SYSTEM_BITS; 
			//this operation involved power * multiplication for every unit
			//power,multiplication are bounded by O(n^2)
			return -1 * Integer.parseInt(R.getData(), 2);
		} else {
			ICNT += SYSTEM_BITS * SYSTEM_BITS; 
			//this operation involved power * multiplication for every unit
			//power,multiplication are bounded by O(n^2)
			return Integer.parseInt(R.getData(), 2);
		}
	}
	
	public int toDecimal(String data)
	{
		HRegister R = new HRegister(SYSTEM_BITS, data);
		if(R.BIT_AT(0))
		//negative
		{
			R = TWOS_COMPLEMENT(R);
			ICNT += SYSTEM_BITS * SYSTEM_BITS; 
			//this operation involved power * multiplication for every unit
			//power,multiplication are bounded by O(n^2)
			return -1 * Integer.parseInt(R.getData(), 2);
		} else {
			ICNT += SYSTEM_BITS * SYSTEM_BITS; 
			//this operation involved power * multiplication for every unit
			//power,multiplication are bounded by O(n^2)
			return Integer.parseInt(R.getData(), 2);
		}
	}
	
	public HRegister ADD(HRegister R1, HRegister R2)
	{	
		//this operation is bounded by O(n)
		ICNT += SYSTEM_BITS * 2;
		
		CARRY = false;
		HRegister R = new HRegister(SYSTEM_BITS); ICNT+=SYSTEM_BITS;
		
		for(int i = SYSTEM_BITS - 1; i >= 0; i--)
		{
			boolean B1 = R1.BIT_AT(i), B2 = R2.BIT_AT(i);
			if(B1 && B2)
			{
				if(CARRY)
				{
					CARRY = true;
					R.SET_AT(true, i);
				} else {
					CARRY = true;
					R.SET_AT(false, i);
				}
			} else if(B1 ^ B2)
			{
				if(CARRY)
				{
					CARRY = true;
					R.SET_AT(false, i);
				} else {
					R.SET_AT(true, i);
				}
			} else {
				if(CARRY)
				{
					CARRY = false;
					R.SET_AT(true, i);
				} else {
					//System.out.println(i);
					R.SET_AT(false, i);
				}
			}
		}
		
		return R;
	}
	
	public HRegister INCREMENT(HRegister R1)
	{
		return ADD(R1, new HRegister(SYSTEM_BITS,"1"));
	}
	
	public HRegister NOT(HRegister R1)
	{
		//function bounded by O(n)
		ICNT += SYSTEM_BITS;
		
		for(int i = 0; i < R1.size(); i++)
			R1.SET_AT(((R1.BIT_AT(i)) ? false : true), i);
		
		return R1;
	}
	
	
	public boolean EQUALS(HRegister R1, HRegister R2)
	{
		//function bounded by O(n)
		ICNT += SYSTEM_BITS;
				
		if(R1.getData().equalsIgnoreCase(R2.getData()))
			return true;
		return false;
	}
	
	public boolean GREATER_THAN(HRegister R1, HRegister R2)
	{
		//function bounded by O(n)
		ICNT += SYSTEM_BITS;
				
		if(EQUALS(R1, R2)) return false;
		
		for(int i = 0; i < R1.size(); i++)
		{
			boolean B1 = R1.BIT_AT(i), B2 = R2.BIT_AT(i);
			if(B1 == B2)
				continue;
			
			if(B1 && !B2)
				return true;
			
			if(!B1 && B2)
				return false;
		}
		return false;
	}
	
	public boolean LESS_THAN(HRegister R1, HRegister R2)
	{
		//function instruction counter is updated by inner functions
				
		if(EQUALS(R1, R2)) return false;
		return !GREATER_THAN(R1, R2);
	}
	
	public HRegister TWOS_COMPLEMENT(HRegister R1)
	{
		//function instruction counter is updated by inner functions
		
		HRegister FIN = NOT(R1);
		FIN = INCREMENT(FIN);
		return FIN;
	}
	
	
	public HRegister SUB(HRegister R1, HRegister R2)
	{
		return ADD(R1, TWOS_COMPLEMENT(R2));
	}
	
	public HRegister[] MUL(HRegister R1, HRegister R2)
	{
		
		//if either is negative, turn to positive and remember
		int C1 = 1, C2 = 1;
				
		if(R1.BIT_AT(0))
		{
			ICNT++;
			C1 = -1;
			R1 = TWOS_COMPLEMENT(R1);
		}
		if(R2.BIT_AT(0))
		{
			ICNT++;
			C2 = -1;
			R2 = TWOS_COMPLEMENT(R2);
		}
		
		HRegister[] RESULT = new HRegister[2];
		HRegister O = new HRegister(SYSTEM_BITS);
		HRegister T = new HRegister(SYSTEM_BITS);
		HRegister R;
		int repeat;
		
		if(GREATER_THAN(R1, R2) || EQUALS(R1, R2))
		{
			R = new HRegister(R1);
			repeat = toDecimal(R2);
		} else {
			R = new HRegister(R2);
			repeat = toDecimal(R1);
		}
		
		
		for(int i = 0; i < repeat; i++)
		{
			//if there was a carry from the operation
			//it needs to be added to the overflow Register
			T = ADD(T, R);
			if(CARRY)
			{
				O = INCREMENT(O);
			}
		}
		
		if((C1 * C2) < 0)
		{
			T = TWOS_COMPLEMENT(T);
			O = (O.getData().contains("1")) ? TWOS_COMPLEMENT(O) : NOT(O);
		}
		RESULT[0] = O;
		RESULT[1] = T;
		return RESULT;
	}
	
	public HRegister[] DIV(HRegister R1, HRegister R2) throws ControlException
	//0 -  Remainder
	//1 - Quotient
	{
		if(!R1.getData().contains("1") || !R2.getData().contains("1"))
			throw new HeliosISA.ControlException("Divide by zero operation.");
		
		HRegister[] RESULT = new HRegister[2];
		//if either is negative, turn to positive and remember
		int C1 = 1, C2 = 1;
						
		if(R1.BIT_AT(0))
		{
			ICNT++;
			C1 = -1;
			R1 = TWOS_COMPLEMENT(R1);
		}
		if(R2.BIT_AT(0))
		{
			ICNT++;
			C2 = -1;
			R2 = TWOS_COMPLEMENT(R2);
		}
		
		HRegister Zero = new HRegister(SYSTEM_BITS);
		int ac = 0;
		while(GREATER_THAN(R1, R2) || EQUALS(R1, R2))
		{
			R1 = SUB(new HRegister(R1), new HRegister(R2));
			
			if(GREATER_THAN(new HRegister(R1), Zero) || 
					EQUALS(R1, Zero)) ac++;
		}
		
		RESULT[0] = R1;
		RESULT[1]= new HRegister(SYSTEM_BITS, toBinary(C1 * C2 * ac));
		System.out.println("Remainder: " + R1);
		return RESULT;
	}
	
	//memory operations
	public HRegister LOAD_MEM(HRegister R, int offset) throws ControlException
	{
		ICNT++;
		R.H_IN(memory.fetchWord(offset));
		return R;
	}
	
	public void STORE_MEM(HRegister R, int offset) throws Exception
	{
		ICNT++;
		memory.putWord(R.getData(), offset);
	}
}
