package yumashish.helios.hword;

import java.util.Scanner;

public class HeliosISA {
	
	static HRegister R0, R1, R2, R3, R4, R5, R6, R7;
	static boolean CARRY;
	static HInterpreter HI;
	
	public static void main(String[] args)
	{
		try {
			System.out.println("[ADD SUB MUL DIV MEM MEMB TWOC SHTL SHTR ASHTL ASHTR MLD(memory load) MST(memory store) LOAD LOADD LOADH LOADL]");
			System.out.println("You can access help with OPERATION HELP (ADD HELP)");
			String line = "";
			Scanner scan = new Scanner(System.in);
			boolean first = true;
			while (!line.equalsIgnoreCase("exit"))
			{
				line = scan.nextLine();
				
				String[] tok = line.split(" ");
				if(tok[0].equalsIgnoreCase("SET"))
				{
					int s = Integer.parseInt(tok[1]);
					int r = Integer.parseInt(tok[2]);
					HI = new HInterpreter((short) s, r);
					first = false;
					continue;
				} else if (first){
					HI = new HInterpreter((short) 16, 8);
					first = false;
				}
				try{
					long time = System.nanoTime();
					HI.newInstruction();
					HRegister[] RESULT = HI.interpret(line);
					long elapsed = System.nanoTime() - time;
					if(RESULT != null)
					{
						if(RESULT[1] == null && RESULT[0] != null)
						{
							System.out.println(RESULT[0] + "[" + HI.toDecimal(RESULT[0].getData()) + "]");
						}
						else if(RESULT[0]==null && RESULT[1]==null)
						{
							//nothing
						}
						else
						{
							System.out.println(RESULT[0] + "[" + HI.toDecimal(RESULT[0].getData()) 
									+ "], " + RESULT[1] + "[" + HI.toDecimal(RESULT[1].getData()) + "]");
						}
					}
					System.out.println("---------------Disgnostics--------------");
					System.out.println("Instructions: " + HI.getInstructionCount());
					//System.out.println("MTime: " + melapsed);
					System.out.println("Time: " + elapsed + " nanoseconds. (" + elapsed * 0.000000001 + " seconds)");
					
					//seconds per clock tick
					double tickTime = (1.0f/23000000.0f);
					System.out.println("Time for 1 tick: " + tickTime + " seconds.");
					
					if(HI.getInstructionCount() != 0)
					{
						double CPI = (elapsed/100) / HI.getInstructionCount();//numberOfTicksInElapsed/HI.getInstructionCount();
						System.out.println("CPI: " + Math.floor(CPI));
					} else {
						System.out.println("Instruction count 0: Probably due to a memory print operation.");
					}
					System.out.println("-------------Disgnostics End------------");
					System.out.println();
				}catch(ControlException ce)
				{
					System.out.println(ce.getMessage());
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static class ControlException extends Exception
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ControlException(String s)
		{
			super(s);
		}
	}
}
