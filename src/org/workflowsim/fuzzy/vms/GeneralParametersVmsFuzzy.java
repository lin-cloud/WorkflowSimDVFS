package org.workflowsim.fuzzy.vms;

public final class GeneralParametersVmsFuzzy
{
	private static double minVmMips;	//Minimum MIPS of all VMs
	private static double maxVmMips;	//Maximum MIPS of all VMs
	private static double minHostMips;	//Minimum MIPS of all Hosts. Main file, createVm method
	private static double maxHostMips;	//Maximum MIPS of all Hosts. Main file, createVm method
	private static double minUtil;		//Minimum possible utilization
	private static double maxUtil;		//Maximum possible utilization
	private static double minPower;		//PowerModel, 0 index in idle power array
	private static double maxPower;		//PowerModel, max index in full power array
	
	
	private GeneralParametersVmsFuzzy(){}
	
	public static void initialise()
	{
		minVmMips = Double.MAX_VALUE;
		maxVmMips = 0;
		minHostMips = Double.MAX_VALUE;
		maxHostMips = 0;
		minUtil = 0;
		maxUtil = 1;
		minPower = Double.MAX_VALUE;
		maxPower = 0;
	}
	
	//MinVmMIPS
	public static double getMinVmMips()
	{
		return minVmMips;
	}
	
	public static void setMinVmMips(double mips)
	{
		minVmMips = mips;
	}
	
	//MaxVmMIPS
	public static double getMaxVmMips()
	{
		return maxVmMips;
	}
	
	public static void setMaxVmMips(double mips)
	{
		maxVmMips = mips;
	}
	
	//MinHostMIPS
	public static double getMinHostMips()
	{
		return minHostMips;
	}
	
	public static void setMinHostMips(double mips)
	{
		minHostMips = mips;
	}
	
	//MaxHostMIPS
	public static double getMaxHostMips()
	{
		return maxHostMips;
	}
	
	public static void setMaxHostMips(double mips)
	{
		maxHostMips = mips;
	}
	
	//MinUtil
	public static double getMinUtil()
	{
		return minUtil;
	}
	

	
	//MaxUtil
	public static double getMaxUtil()
	{
		return maxUtil;
	}
	

	
	//MinPower
	public static double getMinPower()
	{
		return minPower;
	}
	
	public static void setMinPower(double power)
	{
		minPower = power;
	}
	
	//MaxPower
	public static double getMaxPower()
	{
		return maxPower;
	}
	
	public static void setMaxPower(double power)
	{
		maxPower = power;
	}
}