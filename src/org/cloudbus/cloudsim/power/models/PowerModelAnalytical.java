package org.cloudbus.cloudsim.power.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.workflowsim.fuzzy.tasks.GeneralParametersTasksFuzzy;
import org.workflowsim.fuzzy.vms.GeneralParametersVmsFuzzy;

public class PowerModelAnalytical extends PowerModelSpecPowerDVFS
{
	/* Input parameters */
	private double maxFrequency;						//Maximum frequency of the processor
	private double maxVoltage;							//Maximum voltage of the processor
	private double dynamicConstant;						//Number of active gates [0 ~ 1]
	private double staticConstant;						//Percentage of static power [0 ~ 1]
	private double capacitance;							//Capacitance of the processor
	double[] percentages;								//Array of percentages of the multiplier [0 ~ 100]
	double IPC;								 			//Instructions per Cycle
	
	/* Calculated parameters */
	List<Double> frequencies = new ArrayList<Double>();	//Array of all frequencies. Apply percentages array to maxFrequency
	List<Double> voltages = new ArrayList<Double>();	//Array of all voltages. Apply percentages array to maxVoltage
	List<Double> dynamicPower = new ArrayList<Double>();//The dynamic component of the total power. calculate() method
	double maxPower;									//The total power at maximum frequency. calculate() method
	List<Double> fullPower = new ArrayList<Double>();	//Array of all frequencies. Apply percentages array to maxFrequency
	double staticPower;									//The static component of the total power. calculate() method
	List<Double> idlePower = new ArrayList<Double>();	//Array of all frequencies. Apply percentages array to maxFrequency
	double maxMips;										//The MIPS performance at the maximum frequency. calculate() method
	List<Double> Mips = new ArrayList<Double>();		//Array of all frequencies. Apply percentages array to maxFrequency
	
	Pe tmp_pe;
	
	public PowerModelAnalytical(double maxFreq, double maxVolt, double dynConst,
			double statConst, double cap, double[] perc, double IPC, List<Pe> peList)
	{
        Iterator it = peList.iterator();
        Object o = it.next();
        tmp_pe = (Pe)o;
		
		maxFrequency = maxFreq;
		maxVoltage = maxVolt;
		dynamicConstant = dynConst;
		staticConstant = statConst;
		capacitance = cap;
		percentages = perc;
		this.IPC = IPC;
		calculate();
		double maxPower = fullPower.get(idlePower.size() - 1);
		double minPower = idlePower.get(0);
		if(maxPower > GeneralParametersTasksFuzzy.getMaxPower() || maxPower > GeneralParametersVmsFuzzy.getMaxPower())
		{
			GeneralParametersTasksFuzzy.setMaxPower(maxPower);
			GeneralParametersVmsFuzzy.setMaxPower(maxPower);
		}
		if(minPower < GeneralParametersTasksFuzzy.getMinPower() || minPower < GeneralParametersVmsFuzzy.getMinPower())
		{
			GeneralParametersTasksFuzzy.setMinPower(minPower);
			GeneralParametersVmsFuzzy.setMinPower(minPower);
		}
	}
	
	private void calculate()
	{
		/* Frequencies and voltages arrays */
		for (int i = 0; i < percentages.length; i++)
		{
			frequencies.add(maxFrequency * percentages[i] / 100);
			voltages.add(maxVoltage * percentages[i] / 100);
		}
		
		/* Dynamic power => Pd = Cd * C * f * V^2 */
		for (int i = 0; i < percentages.length; i++)
		{
			dynamicPower.add(dynamicConstant * capacitance * frequencies.get(i) * voltages.get(i) * voltages.get(i));
		}
		
		/* Max power => Pt = Pd + Ps */
		maxPower = dynamicPower.get(dynamicPower.size() - 1) / (1 - staticConstant);
		
		/* Full power array */
		for (int i = 0; i < percentages.length; i++)
		{
			fullPower.add(dynamicPower.get(i) / (1 - staticConstant));
		}
		
		/* Static power => Ps = Pt * Cs */
		staticPower = maxPower * staticConstant;
		
		/* Idle power array */
		for (int i = 0; i < percentages.length; i++)
		{
			idlePower.add(fullPower.get(i) * staticConstant);
		}
		
		/* Max MIPS => f * IPC / 1E6 */
		maxMips = maxFrequency * IPC / 1E6;
		
		/* MIPS array */
		for (int i = 0; i < percentages.length; i++)
		{
			Mips.add(maxMips * percentages[i] / 100);
		}
		
	}
	
	@Override
	public double getPower(double utilization) throws IllegalArgumentException
	{
		int index = tmp_pe.getIndexFreq();
		
        double power = (1 - utilization) * idlePower.get(index) + utilization * fullPower.get(index);
        
        Log.printLine("Power computation : index current freq = " + index + " / associated value = " + idlePower.get(index) + "/" + fullPower.get(index));
        Log.printLine("(1 - " + utilization + ")*" + idlePower.get(index) + " + " + utilization + " * " + fullPower.get(index));
        Log.printLine("Power = " + power);
        
        return power;
	}
	
	public double getPMin(int frequency)
    {
        return idlePower.get(frequency);
    }
    
    public double getPMax(int frequency)
    {
        return fullPower.get(frequency);
    }
}