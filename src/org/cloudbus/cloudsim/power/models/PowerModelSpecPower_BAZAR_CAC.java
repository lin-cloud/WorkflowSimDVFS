/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power.models;

import java.util.Iterator;
import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.workflowsim.fuzzy.tasks.GeneralParametersTasksFuzzy;




/**
 * 
 * PowerModel for REIMS GRID 5000 SITE.
 * 
 * 
 * CPU Intel(R) Core(TM)2 Quad CPU Q6700 @ 2.66GHz and 4GB Ram running with Ubuntu TLS 10.4 (Linux kernel 2.6.32).
 * All energy consumption measures have been done on this host with Plogg wireless electricity meter that allow to see
 * and save the live power consumption.
 * 
 * Values measured by changing frequency of 1 core (of 4)
 * 
 * @author Guérout Tom, Monteil Thierry, Da Costa Georges  |  LAAS - IRIT , Toulouse, France
 * 
 * *  Please cite:
 * T. Guérout et al., Energy-aware simulation with DVFS, Simulat. Modell. Pract. Theory (2013), http://dx.doi.org/10.1016/j.simpat.2013.04.007
 */
public class PowerModelSpecPower_BAZAR_CAC extends PowerModelSpecPowerDVFS
{
	
	/** Tables power, in relation with CPU Frequency !
     * tabIdle[f1][f2]....[fn]
     * tabFull[f1][f2]....[fn]
     */
	
	double Tab_Power_idle[]={82.75, 82.85, 85.95, 83.10, 83.25};
	double Tab_Power_full[]={88.77, 92.00, 95.5, 99.45, 103.0};
	double Tab_freq[]={1.60, 1.867, 2.133, 2.40, 2.67};
	List<Pe> peList;
	
	Pe tmp_pe;
    
	public PowerModelSpecPower_BAZAR_CAC ( List<Pe> PeList_) 
	{              
	    peList = PeList_;
	    Iterator it = peList.iterator();
	    Object o = it.next();
	    tmp_pe = (Pe)o;
	    GeneralParametersTasksFuzzy.setMaxPower(Tab_Power_full[Tab_Power_idle.length - 1]);
        GeneralParametersTasksFuzzy.setMinPower(Tab_Power_idle[0]);
	}

    /**
     * 
     * The power model use here is the classical linear power model
     * 
     * Cmin + UtilizationPe [Cmax - Cmin]
     * 
     * 
     * @param utilization
     * @return
     * @throws IllegalArgumentException 
     */
    @Override
    public double getPower(double utilization) throws IllegalArgumentException
    {
    	double conso ;
    	int index = tmp_pe.getIndexFreq();
    	int indexLast = tmp_pe.getIndexFreqLast();
    	double incrfreq=Math.abs(Tab_freq[index]-Tab_freq[indexLast]);
    	double pcpu=(1-utilization)*Tab_Power_idle[index] + utilization*Tab_Power_full[index];
    	double preconf=Math.pow(incrfreq, 2);
        conso = pcpu+preconf;
        /*
        Log.printLine("Power computation : index current freq = "  + index + " / associated value = "  +  Tab_Power_idle[index] +"/"+ Tab_Power_full[index]);
        Log.printLine("(1 - " + utilization + ")*"+Tab_Power_idle[index]+" + " + utilization + " * " + Tab_Power_full[index]);
        Log.printLine("Power = " + conso);
        Log.printLine("Frecuency = " + Tab_freq[index]);
        */
       // Log.printLine("incFrecuency = " + incrfreq);
         
        
        return conso;
    }
    
    public double getPMin(int frequency)
    {
        return Tab_Power_idle[frequency];
    }
    
    public double getPMax(int frequency)
    {
        return Tab_Power_full[frequency];
    }
}