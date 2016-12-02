package org.workflowsim.fuzzy.tasks;

import java.util.ArrayList;

public final class GeneralParametersTasksFuzzy
{
	private static double maxVmMips;			//Maximum MIPS of all VMs
	private static double minVmMips;			//Minimum MIPS of all VMs
	private static double maxPower;				//PowerModelSpecPower_BAZAR, max index in Tab_Power_full
	private static double minPower;				//PowerModelSpecPower_BAZAR, 0 index in Tab_Power_idle
	private static double maxDaxLength; 		//WorkflowParser file, runtime
	private static double minDaxLength; 		//WorkflowParser file, runtime
	private static double maxTime;				//Highest task length / lowest MIPS of all VM
	private static double minTime;				//Lowest task length / highest MIPS of all VM
	private static double maxEnergy;			//Highest possible power at highest frequency index and highest utilization * maxTime
	private static double minEnergy;			//Lowest possible power at lowest frequency index and lowest utilization * minTime
	
	private static double maxHostMips;			//Maximum MIPS of all Hosts. Main file, createVm method
	private static ArrayList<Double> mipsArray;	//MIPS of each physical machine
	
	private GeneralParametersTasksFuzzy(){}
	
	public static void initialise()
	{
		maxVmMips = 0;
		minVmMips = 0;
		maxPower = 0;
		minPower = Double.MAX_VALUE;
		maxDaxLength = 0;
		minDaxLength = Double.MAX_VALUE;
		maxTime = 0;
		minTime = 0;
		maxEnergy = 0;
		minEnergy = Double.MAX_VALUE;
		
		maxHostMips = 0;
		mipsArray = new ArrayList<Double>();
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
	
	//MinVmMIPS
	public static double getMinVmMips()
	{
		return minVmMips;
	}
	
	public static void setMinVmMips(double mips)
	{
		minVmMips = mips;
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
	
	//MinPower
	public static double getMinPower()
	{
		return minPower;
	}
	
	public static void setMinPower(double power)
	{
		minPower = power;
	}
	
	//MaxLength
	public static double getMaxLength()
	{
		return maxDaxLength;
	}
	
	public static void setMaxLength(double length)
	{
		maxDaxLength = length;
	}
	
	//MinLength
	public static double getMinLength()
	{
		return minDaxLength;
	}
	
	public static void setMinLength(double length)
	{
		minDaxLength = length;
	}
	
	//MaxTime
	public static double getMaxTime()
	{
		return maxTime;
	}
	
	public static void setMaxTime(double time)
	{
		maxTime = time;
	}
	
	//MinTime
	public static double getMinTime()
	{
		return minTime;
	}
	
	public static void setMinTime(double time)
	{
		minTime = time;
	}
	
	//MaxEnergy
	public static double getMaxEnergy()
	{
		return maxEnergy;
	}
	
	public static void setMaxEnergy(double energy)
	{
		maxEnergy = energy;
	}
	
	//MinEnergy
	public static double getMinEnergy()
	{
		return minEnergy;
	}
	
	public static void setMinEnergy(double energy)
	{
		minEnergy = energy;
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
	
	//MIPS array
	public static ArrayList<Double> getMipsArray()
	{
		return mipsArray;
	}
	
	public static void setMipsArray(ArrayList<Double> array)
	{
		mipsArray = array;
	}
}