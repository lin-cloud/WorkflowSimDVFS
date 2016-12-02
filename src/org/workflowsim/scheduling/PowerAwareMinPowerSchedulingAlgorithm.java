package org.workflowsim.scheduling;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPower_BAZAR_CAC;
import org.cloudbus.cloudsim.util.Variables;
import org.workflowsim.CondorVM;
import org.workflowsim.WorkflowSimTags;

/**
 * 
 * Modifies PowerAwareSequentialSchedulingAlgorithm to be an adjustment
 * of the MinMin classic algorithm but being power-aware. As MinMin, this
 * algorithm first find the shortest Cloudlet and schedule it to the VM
 * that achieves the lowest energy estimation, instead of lowest time.
 * 
 */

public class PowerAwareMinPowerSchedulingAlgorithm extends BaseSchedulingAlgorithm
{
	static Variables variables;
	
	public PowerAwareMinPowerSchedulingAlgorithm()
	{
        super();
    }
	private final List<Boolean> hasChecked = new ArrayList<>();
	
    @Override
    public void run()
    {
    	int cloudletSize = getCloudletList().size();
    	hasChecked.clear();
        for (int t = 0; t < cloudletSize; t++)
        {
            hasChecked.add(false);
        }
    	int vmSize = getVmList().size();
    	
        for (int c = 0; c < cloudletSize; c++)
        {
        	CondorVM selectedVm = (CondorVM) null;
        	double minimumEnergy = Double.MAX_VALUE;
        	double estimatedEnergy = 0;
        	//Cloudlet cloudlet = (Cloudlet) getCloudletList().get(c);
        	int minIndex = 0;
            Cloudlet minCloudlet = null;
            
            //Minimum initialization
            /*
            for (int j = 0; j < cloudletSize; j++)
            {
                Cloudlet cloudlet = (Cloudlet) getCloudletList().get(j);
                if (!hasChecked.get(j))
                {
                    minCloudlet = cloudlet;
                    minIndex = j;
                    break;
                }
            }
            
                       
            //Check errors
            if (minCloudlet == null)
            {
                break;
            }
            //Selects the shortest Cloudlet
            for (int j = 0; j < cloudletSize; j++)
            {
                Cloudlet cloudlet = (Cloudlet) getCloudletList().get(j);
                if (hasChecked.get(j))
                {
                    continue;
                }
                long length = cloudlet.getCloudletLength();
                if (length < minCloudlet.getCloudletLength())
                {
                    minCloudlet = cloudlet;
                    minIndex = j;
                }
            }
            */
            
            //Select cloudlet
            minCloudlet=(Cloudlet) getCloudletList().get(c);
            minIndex=c;     
            hasChecked.set(minIndex, true);minCloudlet.getCloudletTotalLength();
            
            
        	long length = minCloudlet.getCloudletLength();
        	variables.cloudletsTime = variables.cloudletsTime + length / 15000.0;
            
        	//Find the lowest power consumption for the Cloudlet
        	for (int v = 0; v < vmSize; v++)
        	{
        		CondorVM vm = (CondorVM) getVmList().get(v);
        		if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE)
        		{
        			estimatedEnergy = getEnergyEstimation(minCloudlet, vm);
        			if (estimatedEnergy < minimumEnergy)
        			{
        				minimumEnergy = estimatedEnergy;
        				selectedVm = vm;
        			}
        		}
        	}
        	if (selectedVm == null)
        	{
                break;
        	}
        	selectedVm.setState(WorkflowSimTags.VM_STATUS_BUSY);
            minCloudlet.setVmId(selectedVm.getId());
            getScheduledList().add(minCloudlet);
        }
    }
    
    private double getEnergyEstimation(Cloudlet cloudlet, CondorVM vm)
    {
    	double energy = 0;
    	double mips = vm.getMips();

    	PowerHost host = (PowerHost)vm.getHost();
    	double utilization = host.getUtilizationOfCpu();
    	List<Pe> PeList=host.getPeList();
    	
    	/*
    	 * The power will depend on the utilization
    	 * and the current frequency index selected in the DVFS.
    	 */
    	//double power = host.getPower();
    	PowerModelSpecPower_BAZAR_CAC bazar_cac=new  PowerModelSpecPower_BAZAR_CAC(PeList);
    	double power=bazar_cac.getPower(utilization);
    	    	
    	//Length is expressed in MI.
    	long length = cloudlet.getCloudletLength();
    	//Execution time of the Cloudlet in this VM.
    	double time = length / mips;
    	/*
    	 * The real energy consumption will depend on the length of the Cloudlet,
    	 * and so on the time spent in its execution.
    	 */
    	energy = power * time;
    	return energy;
    }
}