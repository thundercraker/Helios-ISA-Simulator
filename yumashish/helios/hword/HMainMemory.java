package yumashish.helios.hword;

import yumashish.helios.hword.HeliosISA.ControlException;

public class HMainMemory {
	//Word addressable main memory
	short SYSTEM_BITS;
	short BLOCK_SIZE;
	private HRegister[] memory;
	
	public HMainMemory(int SIZE, short SYSTEM_BITS, short BLOCK_SIZE)
	//How many words in memory
	//How many bits in word
	//How many words in one block
	{
		memory = new HRegister[SIZE];
		this.SYSTEM_BITS = SYSTEM_BITS;
		this.BLOCK_SIZE = BLOCK_SIZE;
	}
	
	public String fetchWord(int offset) throws ControlException
	{
		if(offset > memory.length)
			throw new HeliosISA.ControlException("Main memory: Offset beyond memory index bounds");
		return (memory[offset] != null) ? memory[offset].getData() : 
			(new HRegister(SYSTEM_BITS, "")).getData();
	}
	
	public String getBlock(int offset) throws ControlException
	{
		if(offset > memory.length)
			throw new HeliosISA.ControlException("Main memory: Offset beyond memory index bounds");
		String block = "";
		for(int i = 0; i < BLOCK_SIZE; i++)
		{
			block += (memory[offset + i] != null) ? memory[offset].getData() : 
				(new HRegister(SYSTEM_BITS, "")).getData();
			block += "\n";
		}
		return block;
	}
	
	public void putWord(String word, int offset) throws Exception
	{
		if(offset > memory.length)
			throw new HeliosISA.ControlException("Main memory: Offset beyond memory index bounds");
		if(word.length() != SYSTEM_BITS) 
			throw new HeliosISA.ControlException("Main Memory: Attempted to store bad word. (" + word.length() + " bits) (System word: + " + SYSTEM_BITS + ")");
		memory[offset] = new HRegister(SYSTEM_BITS, word);
	}
	
	public void putBlock(String block, int offset) throws Exception
	{
		if(offset > memory.length)
			throw new HeliosISA.ControlException("Main memory: Offset beyond memory index bounds");
		if(block.length() % SYSTEM_BITS != 0)
		{
			throw new HeliosISA.ControlException("Main Memory: Attempted to store bad block.");
		}
		int words = block.length() / SYSTEM_BITS;
		for(int i = 0; i < words; i++)
		{
			String word = block.substring(i * SYSTEM_BITS,(i * SYSTEM_BITS) + SYSTEM_BITS);
			memory[offset + i] = new HRegister(SYSTEM_BITS, word);
		}
	}
}
