package org.workflowsim.fuzzy.vms;



import net.sourceforge.jFuzzyLogic.FunctionBlock;


public final class WorkflowSimVmsFUZZY
{
	private static FunctionBlock funcBlock = null;
	private static String fclPath;
	
	private WorkflowSimVmsFUZZY(){}
	

	public static void setPath(String fcl)
	{
		fclPath = fcl;
	}
	
	public static String getPath()
	{
		return fclPath;
	}
	
	public static FunctionBlock getFunctionBlock()
	{
		return funcBlock;
	}
	
	public static void setFunctionBlock(FunctionBlock funcBlock)
	{
		WorkflowSimVmsFUZZY.funcBlock = funcBlock;
	}
	

}