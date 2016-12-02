package org.workflowsim.utils;

public final class Verbose
{
	private static boolean verbose;

	private Verbose()
	{
		verbose = true;
	}
	
	public static boolean isVerbose() {
		return verbose;
	}

	public static void setVerbose(boolean verbose) {
		Verbose.verbose = verbose;
	}
	
	public static void toPrint(Object text)
	{
		if(Verbose.isVerbose())
		{
			System.out.println(text);
		}
	}
	
	public static void toPrint()
	{
		if(Verbose.isVerbose())
		{
			System.out.println();
		}
	}
}