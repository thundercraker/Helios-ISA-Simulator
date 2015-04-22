package yumashish.helios.hword;

import java.util.HashMap;
import java.util.Map;

public class HInterpreter {
	
	public Map<String, HRegister> Registers;
	private HOperations operation;
	
	public HInterpreter(int width, int registers)
	{
		Registers = new HashMap<String, HRegister>();
		operation = new HOperations((short) width);
		
		for(int i = 0; i < registers; i++)
		{
			Registers.put(Name(i), new HRegister((short) operation.SYSTEM_BITS));
		}
	}
	
	public boolean getCarry() { return operation.CARRY; }
	public boolean getNegative() { return operation.NEGATIVE; }
	
	public String Name(int number) { return "R" + number; }
	public void put(String name, HRegister in) { Registers.put(name, in); }
	public HRegister get(String name){ return Registers.get(name); }
	
	public void INPUT(int register, String data)
	{
		HRegister R1 = Registers.get(Name(register));
		R1.H_IN(data);
		Registers.put(Name(register), R1);
	}
	
	public int toDecimal(String in)
	{
		return operation.toDecimal(new HRegister(operation.SYSTEM_BITS, in));
	}
	
	public String toBinaryString(int in)
	{
		return operation.toBinary(in);
	}
	
	public int getInstructionCount()
	{
		return operation.ICNT;
	}
	
	public void newInstruction()
	{
		operation.ICNT = 0;
	}
	
	public HRegister[] interpret(String line) throws Exception
	{
		if(line.substring(0, 1).equalsIgnoreCase("0") || line.substring(0, 1).equalsIgnoreCase("1"))
		{
			return this.tranlateFullBits(line);
		}
		else
		{
			return this.translate(line);
		}
	}
	
	public HRegister[] tranlateFullBits(String line) throws Exception
	{
		//translate a full binary command
		//0000 | 0000 | 0001 | 0000 | Extra bits
		//ADD | R0 | R1 | R2
		//Special: for LOAD and STORE
		//all bytes are the first 2 are little endian
		//they need to be reversed
		String temp = "";
		String[] bytes = new String[line.length()/4];
		String commands = "";
		for(int i = 0;  ((i * 4) + 4) <= line.length(); i++)
		{
			temp = line.substring(i * 4, (i * 4) + 4);
			bytes[i] = temp;
			//System.out.println(temp);
		}
		
		int opcode = this.toDecimal(bytes[0]);
		int multibyte = 0;
		int cutoff = -1;
		
		switch(opcode)
		{
		case 0:
			//ADD
			commands = "ADD ";
			break;
		case 1:
			commands = "SUB ";
			break;
		case 2:
			commands = "MUL ";
			break;
		case 3:
			commands = "DIV ";
			break;
		case 4:
			//LOAD FROM MEMORY
			commands = "MLD ";
			break;
		case 5:
			//STORE INTO MEMORY
			commands = "MST ";
			break;
		case 6:
			//LOAD FROM DATA
			commands = "LOAD ";
			multibyte = 1;
			break;
		case 7:
			//twos complement
			commands = "TWOC ";
			break;
		case 8:
			//LOAD FROM DATA
			commands = "SHTL ";
			break;
		case 9:
			//LOAD FROM DATA
			commands = "SHTR ";
			break;
		case 10:
			//LOAD FROM DATA
			commands = "ASHTL ";
			break;
		case 11:
			//LOAD FROM DATA
			commands = "ASHTR ";
			break;
		case 12:
			//LOAD TO REG HIGHEST BITS
			commands = "LOADH ";
			multibyte = 1;
			cutoff = operation.SYSTEM_BITS/2;
			break;
		case 13:
			//LOAD TO REG LOWEST BITS
			commands = "LOADL ";
			multibyte = 1;
			cutoff = operation.SYSTEM_BITS/2;
			break;
		case 14:
			//MOVE
			commands = "LOADL ";
			break;
		default:
			commands = bytes[0];
			break;
		}
		
		for(int i=1; i < bytes.length; i++)
		{
			if(bytes[i] == null)
			{
				continue;
			} else if(bytes[i].length() != 4) 
			{
				throw new HeliosISA.ControlException("Corrupted byte (" + bytes[i] + ")");
			}
			int num = this.toDecimal(bytes[i]);
			commands += Name(num) + " ";
			multibyte--;
			if(multibyte == 0)
			{
				//all bytes after this are one single entity
				String torev = "";
				for(int j = i + 1; j < bytes.length; j++)
				{
					torev += bytes[j];
				}
				if(cutoff > -1)
				{
					commands += new StringBuilder(torev).reverse().toString().substring(0, Math.min(cutoff + 1,torev.length()));
					//fill in the rest with 0s
					if(commands.length() < operation.SYSTEM_BITS)
					{
						for(int j = commands.length() - 1; j < operation.SYSTEM_BITS; j++)
							commands += "0";
					}
				} else {
					commands += new StringBuilder(torev).reverse().toString();
				}
				
				break;
			}
		}
		System.out.println("Raw command:" + line);
		System.out.println("Translated Command: " + commands);
		
		return translate(commands.trim());
	}
	
	public HRegister[] translate(String line) throws Exception
	{
		boolean help = line.toLowerCase().contains("help");
		String[] tok = line.split(" ");
		HRegister[] RES = new HRegister[2];
		//ADD COMMAND
		//OP CODE: 0000
		if(tok[0].equalsIgnoreCase("ADD"))
		{
			if(help)
			{
				System.out.println("Addition: Opcode 0[0]");
				System.out.println("You can use ADD with 2 or 3 arguments");
				System.out.println("ADD R1 R2 (R1 + R2 and store in R1");
				System.out.println("ADD R1 R2 R3 (R2 + R3 and store in R1)");
				throw new HeliosISA.ControlException("-Exit help-");
			}
			//ADD R1, R2 (R1 = R1 + R2)
			else if(tok.length == 3)
			{
				HRegister R1 = get(tok[1]);
				HRegister R2 = get(tok[2]);
				
				HRegister R0 = operation.ADD(R1, R2);
				put(tok[1], R0);
				RES[0] = R0;
				return RES;
			}
			//ADD R1, R2, R3 (R1 = R2 + R3)
			else if(tok.length == 4)
			{
				HRegister R2 = get(tok[2]);
				HRegister R3 = get(tok[3]);
				
				HRegister R0 = operation.ADD(R2, R3);
				put(tok[1], R0);
				RES[0] = R0;
				return RES;
			}
		}
		
		//SUB COMMAND
		//OP CODE: 0001
		else if(tok[0].equalsIgnoreCase("SUB"))
		{
			if(help)
			{
				System.out.println("Subtraction: Opcode 1[1]");
				System.out.println("You can use SUB with 2 or 3 arguments");
				System.out.println("SUB R1 R2 (R1 - R2 and store in R1");
				System.out.println("SUB R1 R2 R3 (R2 - R3 and store in R1)");
				throw new HeliosISA.ControlException("-Exit help-");
			}
			//SUB R1, R2 (R1 = R1 + R2)
			else if(tok.length == 3)
			{
				HRegister R1 = get(tok[1]);
				HRegister R2 = get(tok[2]);
				
				HRegister R0 = operation.SUB(R1, R2);
				put(tok[1], R0);
				RES[0] = R0;
				return RES;
			}
			//SUB R1, R2, R3 (R1 = R2 + R3)
			if(tok.length == 4)
			{
				HRegister R2 = get(tok[2]);
				HRegister R3 = get(tok[3]);
				
				HRegister R0 = operation.SUB(R2, R3);
				put(tok[1], R0);
				RES[0] = R0;
				return RES;
			}
		}
		
		//MUL COMMAND
		//OP CODE: 0010
		else if(tok[0].equalsIgnoreCase("MUL"))
		{
			if(help)
			{
				System.out.println("Multiplication: Opcode 2[10]");
				System.out.println("You can use MUL with 2 to 4 arguments");
				System.out.println("MUL R1 R2 (R1 * R2 and store in R1,R2 [Big Endian]");
				System.out.println("MUL R1 R2 R3 (R2 * R3 and store in R1,R2 [Big Endian]");
				System.out.println("MUL R1 R2 R3 R4 (R3 * R4 and store in R1,R2 [Big Endian]");
				throw new HeliosISA.ControlException("-Exit help-");
			}
			//MUL R1, R2 (R1,R2 = R1 * R2)
			else if(tok.length == 3)
			{
				HRegister R1 = get(tok[1]);
				HRegister R2 = get(tok[2]);
						
				RES = operation.MUL(R1, R2);
				put(tok[1], RES[0]);
				put(tok[2], RES[1]);
				return RES;
			}
			//MUL R1, R2, R3 (R1,R2 = R2 * R3)
			else if(tok.length == 4)
			{
				HRegister R2 = get(tok[2]);
				HRegister R3 = get(tok[3]);
				
				RES = operation.MUL(R2, R3);
				put(tok[1], RES[0]);
				put(tok[2], RES[1]);
				return RES;
			}
			//MUL R1, R2, R3, R4 (R1,R2 = R3 * R4)
			else if(tok.length == 5)
			{
				HRegister R3 = get(tok[3]);
				HRegister R4 = get(tok[4]);
				
				RES = operation.MUL(R3, R4);
				put(tok[1], RES[0]);
				put(tok[2], RES[1]);
				return RES;
			}
		}
		
		//DIV COMMAND
		//OP CODE 0011
		else if(tok[0].equalsIgnoreCase("DIV"))
		{
			if(help)
			{
				System.out.println("Division: Opcode 3[11]");
				System.out.println("You can use DIV with 2 to 4 arguments");
				System.out.println("DIV R1 R2 (R1 / R2 and store Remainder in R2 and Quotient in R1");
				System.out.println("DIV R1 R2 R3 (R2 / R3 and store Remainder in R2 and Quotient in R1");
				System.out.println("DIV R1 R2 R3 R4 (R3 / R4 and store Remainder in R2 and Quotient in R1");
				throw new HeliosISA.ControlException("-Exit help-");
			}
			//DIV R1, R2 (R1(QUO),R2(REM) = R1 / R2)
			else if(tok.length == 3)
			{
				HRegister R1 = get(tok[1]);
				HRegister R2 = get(tok[2]);
								
				RES = operation.DIV(R1, R2);
				put(tok[1], RES[1]);
				put(tok[2], RES[0]);
				return RES;
			}
			//DIV R1, R2, R3 (R1,R2 = R2 / R3)
			else if(tok.length == 4)
			{
				HRegister R2 = get(tok[2]);
				HRegister R3 = get(tok[3]);
						
				RES = operation.DIV(R2, R3);
				put(tok[1], RES[1]);
				put(tok[2], RES[0]);
				return RES;
			}
			//DIV R1, R2, R3 (R1,R2 = R3 / R4)
			else if(tok.length == 5)
			{
				HRegister R3 = get(tok[3]);
				HRegister R4 = get(tok[4]);
						
				RES = operation.DIV(R3, R4);
				put(tok[1], RES[1]);
				put(tok[2], RES[0]);
				return RES;
			}
		}
		
		else if(tok[0].equalsIgnoreCase("MEM"))
		{
			if(help)
			{
				System.out.println("Retreives a word from the main memory.");
				System.out.println("You can use MEM with 1 argument: the word offset.");
				throw new HeliosISA.ControlException("-Exit help-");
			}
			int offset = Integer.parseInt(tok[1]);
			System.out.println("Word at (0X" + offset + ") " + operation.memory.fetchWord(offset));
			return null;
		}
		else if(tok[0].equalsIgnoreCase("MEMB"))
		{
			if(help)
			{
				System.out.println("Retreives a block from the main memory.");
				System.out.println("You can use MEMB with 1 argument: the word offset (of the block).");
				throw new HeliosISA.ControlException("-Exit help-");
			}
			int offset = Integer.parseInt(tok[1]);
			System.out.println("Block at (0X" + offset + ") \n" + operation.memory.getBlock(offset));
			return null;
		}
		else if(tok[0].equalsIgnoreCase("TWOC"))
		{
			if(help)
			{
				System.out.println("Twos complement: Opcode 7[111]");
				System.out.println("Returns the Twos complement of a register");
				System.out.println("You can use TWOC with 1 argument: the register.");
				throw new HeliosISA.ControlException("-Exit help-");
			}
			HRegister R = get(tok[1]);
			RES[0] = operation.TWOS_COMPLEMENT(R);
			return RES;
		}
		else if(tok[0].equalsIgnoreCase("SHTL"))
		{
			if(help)
			{
				System.out.println("Shift Left: Opcode 8[1000]");
				System.out.println("Shift a register left. Arguments: (register)");
				throw new HeliosISA.ControlException("-Exit help-");
			}
			if(tok.length < 3)
			{
				throw new HeliosISA.ControlException("This command needs 3 arguments.");
			}
			HRegister R = get(tok[1]);
			int i = Integer.parseInt(tok[2]);
			R.L_SHIFT(i, false);
			RES[0] = R;
			return RES;
		}
		else if(tok[0].equalsIgnoreCase("SHTR"))
		{
			if(help)
			{
				System.out.println("Shift Right: Opcode 9[1001]");
				System.out.println("Shift a register right. Arguments: (register)");
				throw new HeliosISA.ControlException("-Exit help-");
			}
			if(tok.length < 3)
			{
				throw new HeliosISA.ControlException("This command needs 3 arguments.");
			}
			HRegister R = get(tok[1]);
			int i = Integer.parseInt(tok[2]);
			R.R_SHIFT(i, false);
			RES[0] = R;
			return RES;
		}
		else if(tok[0].equalsIgnoreCase("ASHTL"))
		{
			if(help)
			{
				System.out.println("Arithmetic shift left: Opcode 10[1010]");
				System.out.println("Arithmetic Shift a register left. Arguments: (register)");
				throw new HeliosISA.ControlException("-Exit help-");
			}
			if(tok.length < 3)
			{
				throw new HeliosISA.ControlException("This command needs 3 arguments.");
			}
			HRegister R = get(tok[1]);
			int i = Integer.parseInt(tok[2]);
			R.L_ARTH_SHIFT(i);
			RES[0] = R;
			return RES;
		}
		else if(tok[0].equalsIgnoreCase("ASHTR"))
		{
			if(help)
			{
				System.out.println("Arithmethic shift right: Opcode 11[1011]");
				System.out.println("Arthmetic Shift a register right. Arguments: (register)");
				throw new HeliosISA.ControlException("-Exit help-");
			}
			if(tok.length < 3)
			{
				throw new HeliosISA.ControlException("This command needs 3 arguments.");
			}
			HRegister R = get(tok[1]);
			int i = Integer.parseInt(tok[2]);
			R.R_ARTH_SHIFT(i);
			RES[0] = R;
			return RES;
		}
		
		else if(tok[0].equalsIgnoreCase("MLD"))
		{
			if(help)
			{
				System.out.println("Memory Load: Opcode 4[100]");
				System.out.println("Load a word from the main memory into the register");
				System.out.println("MLD R1 1 (Load into register R1 from memory offset 1)");
				throw new HeliosISA.ControlException("-Exit help-");
			}
			if(tok.length < 3)
			{
				throw new HeliosISA.ControlException("This command needs 3 arguments.");
			}
			HRegister R = get(tok[1]);
			int offset = this.toDecimal(tok[2]);
			R.H_IN(operation.memory.fetchWord(offset));
			RES[0] = R;
			return RES;
		}
		
		else if(tok[0].equalsIgnoreCase("MST"))
		{
			if(help)
			{
				System.out.println("Memory Store: Opcode 5[101]");
				System.out.println("Store a word from the register into the main memory");
				System.out.println("MST R1 1 (Store from register R1 into memory offset 1)");
				throw new HeliosISA.ControlException("-Exit help-");
			}
			if(tok.length < 3)
			{
				throw new HeliosISA.ControlException("This command needs 3 arguments.");
			}
			HRegister R = get(tok[1]);
			int offset = this.toDecimal(tok[2]);
			operation.memory.putWord(R.getData(), offset);
			RES[0] = R;
			return RES;
		}
		
		else if(tok[0].equalsIgnoreCase("LOAD"))
		//LOAD R1 1111
		{
			if(help)
			{
				System.out.println("Load into register: Opcode 6[110]");
				System.out.println("Load a binary string into the register");
				System.out.println("Example: LOAD R1 1100");
				throw new HeliosISA.ControlException("-Exit help-");
			}
			if(tok.length < 3)
			{
				throw new HeliosISA.ControlException("This command needs 3 arguments.");
			}
			try
			{
				operation.toDecimal(tok[2]);
			} catch(NumberFormatException e) {
				throw new HeliosISA.ControlException("There was a format exception. If you are trying to LOAD a decimal number please use LOADD.");
			}
			put(tok[1], new HRegister(operation.SYSTEM_BITS,tok[2]));
			RES[0] = get(tok[1]);
			return RES;
		}
		
		else if(tok[0].equalsIgnoreCase("LOADH"))
			//LOAD given binary data into largest SYSTEM_BITS/2 bits of Register
		{
			if(help)
			{
				System.out.println("Load into upper half of register: Opcode 12[1100]");
				System.out.println("Load a binary string into the upper half of register");
				System.out.println("Example: LOADU R1 1100");
				throw new HeliosISA.ControlException("-Exit help-");
			}
			if(tok.length < 3)
			{
				throw new HeliosISA.ControlException("This command needs 3 arguments.");
			}
			try
			{
				operation.toDecimal(tok[2]);
			} catch(NumberFormatException e) {
				throw new HeliosISA.ControlException("There was a format exception. If you are trying to LOAD a decimal number please use LOADD.");
			}
			String upper = "", filler = "";
			if(tok[2].length() < (operation.SYSTEM_BITS/2))
			{
				for(int i = 0; i < (operation.SYSTEM_BITS/2) - tok[2].length(); i++) filler += "0";
				upper = tok[2] + filler;
			} else if(tok[2].length() > (operation.SYSTEM_BITS/2)) {
				upper = tok[2].substring(0, (operation.SYSTEM_BITS/2) + 1);
			} else {
				upper = tok[2];
			}
			
			for(int j = 0; j < (operation.SYSTEM_BITS/2); j++)
			{
				upper += "0";
			}
			
			put(tok[1], new HRegister(operation.SYSTEM_BITS,upper));
			RES[0] = get(tok[1]);
			return RES;
		}
		
		else if(tok[0].equalsIgnoreCase("LOADL"))
			//LOAD given binary data into largest SYSTEM_BITS/2 bits of Register
		{
			if(help)
			{
				System.out.println("Load into lower half of register: Opcode 13[1101]");
				System.out.println("Load a binary string into the lower half of register");
				System.out.println("Example: LOADL R1 1100");
				throw new HeliosISA.ControlException("-Exit help-");
			}
			if(tok.length < 3)
			{
				throw new HeliosISA.ControlException("This command needs 3 arguments.");
			}
			try
			{
				operation.toDecimal(tok[2]);
			} catch(NumberFormatException e) {
				throw new HeliosISA.ControlException("There was a format exception. If you are trying to LOAD a decimal number please use LOADD.");
			}
			String lower = "", filler = "";
			if(tok[2].length() < (operation.SYSTEM_BITS/2))
			{
				for(int i = 0; i < (operation.SYSTEM_BITS/2) - tok[2].length(); i++) filler += "0";
				lower = filler + tok[2];
			} else if(tok[2].length() > (operation.SYSTEM_BITS/2)) {
				lower = tok[2].substring(0, (operation.SYSTEM_BITS/2) + 1);
			} else {
				lower = tok[2];
			}

			put(tok[1], new HRegister(operation.SYSTEM_BITS,lower));
			RES[0] = get(tok[1]);
			return RES;
		}
		
		else if(tok[0].equalsIgnoreCase("LOADD"))
		{
			if(help)
			{
				System.out.println("Load a signed decimal into the register");
				System.out.println("Example: LOAD R1 -87");
				throw new HeliosISA.ControlException("-Exit help-");
			}
			if(tok.length < 3)
			{
				throw new HeliosISA.ControlException("This command needs 3 arguments.");
			}
			//LOADD CALLED
			put(tok[1], new HRegister(operation.SYSTEM_BITS,
					operation.toBinary(Integer.parseInt(tok[2]))));
			RES[0] = get(tok[1]);
			return RES;
		}
		
		else if(tok[0].equalsIgnoreCase("MOV"))
			//LOAD R1 1111
			{
				if(help)
				{
					System.out.println("Move: Opcode 14[1110]");
					System.out.println("Move data from one register to another");
					System.out.println("Example: MOV R1 R2");
					throw new HeliosISA.ControlException("-Exit help-");
				}
				if(tok.length < 3)
				{
					throw new HeliosISA.ControlException("This command needs 3 arguments.");
				}
				HRegister R = get(tok[1]);
				HRegister R1 = get(tok[2]);
				R.H_IN(R1);
				put(tok[1], R);
				RES[0] = R;
				RES[1] = R1;
				return RES;
			}
		
		else 
		{
			throw new HeliosISA.ControlException("HInterpreter: Unrecognized opcode (" + tok[0] + ")");
		}
		
		return null;
	}
	
	public interface Command {
		public HRegister Execute(HRegister R1);
		public HRegister Execute(HRegister R1, HRegister R2);
		public HRegister Execute(HRegister R1, HRegister R2, HRegister R3);
	}
}
