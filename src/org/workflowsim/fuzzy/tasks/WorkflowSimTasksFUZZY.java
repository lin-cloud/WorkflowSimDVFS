package org.workflowsim.fuzzy.tasks;



import net.sourceforge.jFuzzyLogic.FunctionBlock;

public final class WorkflowSimTasksFUZZY
{
	private static FunctionBlock funcBlock = null;
	//private static FuzzyEntity fuzzyEntity;
	private static String fclPath;
	
	private WorkflowSimTasksFUZZY(){}
	
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
		WorkflowSimTasksFUZZY.funcBlock = funcBlock;
	}
	
	
}