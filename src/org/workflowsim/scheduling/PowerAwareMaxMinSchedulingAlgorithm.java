package org.workflowsim.scheduling;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.power.PowerHost;
import org.workflowsim.CondorVM;
import org.workflowsim.WorkflowSimTags;

/**
 * 
 * Modifies PowerAwareSequentialSchedulingAlgorithm to be an adjustment
 * of the MaxMin classic algorithm but being power-aware. As MaxMin, this
 * algorithm first find the longest Cloudlet and schedule it to the VM
 * that achieves the lowest energy estimation, instead of lowest time.
 * 
 */

public class PowerAwareMaxMinSchedulingAlgorithm extends BaseSchedulingAlgorithm
{
	public PowerAwareMaxMinSchedulingAlgorithm()
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
        	int maxIndex = 0;
            Cloudlet maxCloudlet = null;
            //Maximum initialization
            for (int j = 0; j < cloudletSize; j++)
            {
                Cloudlet cloudlet = (Cloudlet) getCloudletList().get(j);
                if (!hasChecked.get(j))
                {
                    maxCloudlet = cloudlet;
                    maxIndex = j;
                    break;
                }
            }
            //Check errors
            if (maxCloudlet == null)
            {
                break;
            }
            //Selects the longest Cloudlet
            for (int j = 0; j < cloudletSize; j++)
            {
                Cloudlet cloudlet = (Cloudlet) getCloudletList().get(j);
                if (hasChecked.get(j))
                {
                    continue;
                }
                long length = cloudlet.getCloudletLength();
                if (length > maxCloudlet.getCloudletLength())
                {
                    maxCloudlet = cloudlet;
                    maxIndex = j;
                }
            }
            hasChecked.set(maxIndex, true);
            //Find the lowest power consumption for the maximum Cloudlet
        	for (int v = 0; v < vmSize; v++)
        	{
        		CondorVM vm = (CondorVM) getVmList().get(v);
        		if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE)
        		{
        			estimatedEnergy = getEnergyEstimation(maxCloudlet, vm);
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
            maxCloudlet.setVmId(selectedVm.getId());
            getScheduledList().add(maxCloudlet);
        }
    }
    
    private double getEnergyEstimation(Cloudlet cloudlet, CondorVM vm)
    {
    	double energy = 0;
    	//double mips = vm.getMips();
    	double mips = vm.getCurrentRequestedTotalMips();
    	PowerHost host = (PowerHost)vm.getHost();
    	//double utilization = host.getUtilizationOfCpu();
    	/*
    	 * The power will depend on the utilization
    	 * and the current frequency index selected in the DVFS.
    	 */
    	double power = host.getPower();
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