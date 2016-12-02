package org.workflowsim.scheduling;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.power.PowerHost;
import org.workflowsim.CondorVM;
import org.workflowsim.WorkflowSimTags;

/**
 *
 * Attempts to define Buyya et al "Power Aware Scheduling of Bag-of-Tasks Applications with
 * Deadline Constraints on DVS-enabled Clusters" paper's algorithm in WorkflowSim. The Cloudlets
 * order is followed sequentially, just as the paper shows.
 *
 */

public class PowerAwareSeqSchedulingAlgorithm extends BaseSchedulingAlgorithm
{
	public PowerAwareSeqSchedulingAlgorithm()
	{
        super();
    }
	
    @Override
    public void run()
    {
    	int cloudletSize = getCloudletList().size();
    	int vmSize = getVmList().size();
        for (int c = 0; c < cloudletSize; c++)
        {
        	CondorVM selectedVm = (CondorVM) null;
        	double minimumEnergy = Double.MAX_VALUE;
        	double estimatedEnergy = 0;
        	Cloudlet cloudlet = (Cloudlet) getCloudletList().get(c);
        	for (int v = 0; v < vmSize; v++)
        	{
        		CondorVM vm = (CondorVM) getVmList().get(v);
        		if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE)
        		{
        			estimatedEnergy = getEnergyEstimation(cloudlet, vm);
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
            cloudlet.setVmId(selectedVm.getId());
            getScheduledList().add(cloudlet);
        }
    }
    
    private double getEnergyEstimation(Cloudlet cloudlet, CondorVM vm)
    {
    	double energy = 0;
    	double mips = vm.getMips();
    	PowerHost host = (PowerHost)vm.getHost();
    	double utilization = host.getUtilizationOfCpu();
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