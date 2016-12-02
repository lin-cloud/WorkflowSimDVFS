/**
 * Copyright 2012-2013 University Of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.workflowsim.examples.dvfs;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.HarddriveStorage;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySimpleWattPerMipsMetric;
import org.cloudbus.cloudsim.power.models.PowerModelAnalytical;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPower_BAZAR_CAC;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.util.Variables;
import org.cloudbus.cloudsim.xml.DvfsDatas;
import org.workflowsim.CondorVM;
import org.workflowsim.Job;
import org.workflowsim.Task;
import org.workflowsim.WorkflowEngine;
import org.workflowsim.WorkflowPlanner;
import org.workflowsim.dvfs.WorkflowDVFSDatacenter;
import org.workflowsim.fuzzy.tasks.GeneralParametersTasksFuzzy;
import org.workflowsim.fuzzy.vms.GeneralParametersVmsFuzzy;
import org.workflowsim.utils.ClusteringParameters;
import org.workflowsim.utils.ExportCsv;
import org.workflowsim.utils.OverheadParameters;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.Parameters.ClassType;
import org.workflowsim.utils.ReplicaCatalog;


/**
 * This WorkflowSimExample creates a workflow planner, a workflow engine,
 * one scheduler, one datacenter and 20 vms. You should change daxPath at
 * least. You may change other parameters as well.
 *
 * @author Weiwei Chen
 * @since WorkflowSim Toolkit 1.0
 * @date Apr 9, 2013
 */
public class WorkflowDVFSBasicNoBRITE
{
	static Variables variables=new Variables();
    ////////////////////////// STATIC METHODS ///////////////////////
    /**
     * Creates main() to run this example. This example has only one datacenter
     * and one storage
     */
	
	private static DvfsDatas ConfigDvfs;
	private static int num_hosts = 20;
	private static boolean variable_machines = false;
	private static boolean variable_vms = true;
	
    public static void main(String[] args)
    {
        try
        {
        	//Log.disable();
        	
        	Log.printLine("Verbose: " + Log.isDisabled());
        	ExportCsv.setExport(false, System.getProperty("user.dir")+"/export666.csv");
        	
        	// First step: Initialize the WorkflowSim package.
            /**
             * However, the exact number of vms may not necessarily be vmNum If
             * the data center or the host doesn't have sufficient resources the
             * exact vmNum would be smaller than that. Take care.
             */
            int vmNum = 20; //number of vms;
            /**
             * Should change this based on real physical path
             */
            Log.disable();
            String daxPath = "";
            if (args.length == 0 || args[0].equals("default"))
            {
            	daxPath = System.getProperty("user.dir")+"/config/dax/Inspiral_30.xml";
            }
            else
            {
            	daxPath = args[0];
            }
            
            
            File daxFile = new File(daxPath);
            if (!daxFile.exists())
            {
                System.out.println("Warning: Please replace daxPath with the physical path in your working environment!");
                return;
            }
            
            /**
             * Since we are using MINMIN scheduling algorithm, the planning
             * algorithm should be INVALID such that the planner would not
             * override the result of the scheduler.
             */
            Parameters.SchedulingAlgorithm sch_method;
            if (args.length > 1 && !args[1].equals("default"))
            {
            	sch_method = Parameters.SchedulingAlgorithm.STATIC;
            }
            else
            {
            	sch_method = Parameters.SchedulingAlgorithm.POWERMIN;
            }
            Parameters.PlanningAlgorithm pln_method = Parameters.PlanningAlgorithm.INVALID;
            ReplicaCatalog.FileSystem file_system = ReplicaCatalog.FileSystem.SHARED;
                      
        	
            /**
             * No overheads
             */
            OverheadParameters op = new OverheadParameters(0, null, null, null, null, 0);
            
            /**
             * No Clustering
             */
            ClusteringParameters.ClusteringMethod method = ClusteringParameters.ClusteringMethod.NONE;
            ClusteringParameters cp = new ClusteringParameters(0, 0, method, null);
            
            /**
             * Initialize static parameters
             */
            Parameters.init(vmNum, daxPath, null, null, op, cp, sch_method, pln_method, null, 0);

            
            
            ReplicaCatalog.init(file_system);
            
            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events
            
            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);
            
            WorkflowDVFSDatacenter datacenter0 = createDatacenter("Datacenter_0");
            datacenter0.setDisableMigrations(true);
            Log.printLine("WorkflowDatacenter Entity's ID: " + datacenter0.getId());
            
            /**
             * Create a WorkflowPlanner with one schedulers.
             */
            WorkflowPlanner wfPlanner = new WorkflowPlanner("planner_0", 1);
            Log.printLine("WorkflowPlanner Entity's ID: " + wfPlanner.getId());
            Log.printLine("ClusteringEngine Entity's ID: " + wfPlanner.getClusteringEngineId());
            Log.printLine("WorkflowEngine Entity's ID: " + wfPlanner.getWorkflowEngineId());
            Log.printLine("WorkflowScheduler Entity's ID: " + wfPlanner.getWorkflowEngine().getSchedulerId(0));
            
            /**
             * Create a WorkflowEngine.
             */
            WorkflowEngine wfEngine = wfPlanner.getWorkflowEngine();
            
            /**
             * Create a list of VMs. The userId of a vm is basically the id of
             * the scheduler that controls this vm.
             */
            List<CondorVM> vmlist0 = createVM(wfEngine.getSchedulerId(0), Parameters.getVmNum());
            
            /**
             * Submits this list of vms to this WorkflowEngine.
             */
            wfEngine.submitVmList(vmlist0, 0);
            
            /**
             * Binds the data centers with the scheduler.
             */
            wfEngine.bindSchedulerDatacenter(datacenter0.getId(), 0);
            Log.printLine("Total number of entities: " + CloudSim.getNumEntities());
            
            double lastClock = CloudSim.startSimulation();
            List<Job> outputList0 = wfEngine.getJobsReceivedList();
            CloudSim.stopSimulation();
            Log.enable();
            
            Log.printLine();
			
            double time = lastClock;
            double sumPower = datacenter0.getPower();
            double avgPower = datacenter0.getPower() / (lastClock * 100);      // Pcpu + Preconf     
            double netPower= 0.5 * (Math.pow(2, 0.6) - 1); // Shannon Hartley R=15Mbps, W=25Mhz
            double energy = (datacenter0.getPower() / (lastClock*100)) * (lastClock*100 / 3600) + netPower * variables.getTime();

            Log.printLine(String.format("Total simulation time: %.2f sec", time));
            Log.printLine(String.format("Power Sum: %.8f W", sumPower));
            Log.printLine(String.format("Power Average: %.8f W", avgPower));
            Log.printLine(String.format("Energy consumption: %.8f Wh", energy));
			if (Log.isDisabled() && args.length == 0)
			{
				System.out.println(String.format("%.4f", time));
				System.out.println(String.format("%.4f", sumPower));
				System.out.println(String.format("%.4f", avgPower));
				System.out.println(String.format("%.4f", energy));
			}
			
            if (args.length > 0)
            {
            	/*System.out.println
            	(
            		Double.toString(time) + ';' +
            		Double.toString(sumPower) + ';' +
            		Double.toString(avgPower) + ';' +
            		Double.toString(energy)
            	);*/
            	System.out.println(Double.toString(time) + ';' + Double.toString(energy));
            }
            
			Log.printLine();
			
            printJobList(outputList0);
        }
        catch (Exception e)
        {
            System.out.println("The simulation has been terminated due to an unexpected error: " + e);
        }
    }
    
    protected static WorkflowDVFSDatacenter createDatacenter(String name)
    {
        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store one or more
        //    Machines
        List<PowerHost> hostList = new ArrayList<>();
        
        // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
        //    create a list to store these PEs before creating
        //    a Machine.
        
        //boolean variable_machines = false;
        
        double[] machine_mips = new double[num_hosts];
        double maxMips = 0;
        double minMips = Double.MAX_VALUE;
        //int mips_first = 3000;
        //int mips_last = 1000;
        //int increasal = (mips_last - mips_first) / num_hosts;
        
    	/* Variable parameters calculation */
    	
    	double[] percentages = {59.925, 69.93, 79.89, 89.89, 100.0};
    	
    	double[] maxFrequencies = new double[num_hosts];
    	double firstFrequency = 3E9;
    	double lastFrequency = 1E9;
    	double frequencySlope = (lastFrequency - firstFrequency) / num_hosts;
    	
    	double[] maxVoltages = new double[num_hosts];
    	double firstVoltage = 3;
    	double lastVoltage = 2;
    	double voltageSlope = (lastVoltage - firstVoltage) / num_hosts;
    	
    	double dynamicConstant = 0.5;
    	
    	double staticConstant = 0.7;
    	
    	double[] capacitances = new double[num_hosts];
    	double firstCapacitance = 0.5 * 1E-8;
    	double lastCapacitance = 3E-8;
    	double capacitanceSlope = (lastCapacitance - firstCapacitance) / num_hosts;
    	
    	double[] IPCs = new double[num_hosts];
    	double firstIPC = 0.5;
    	double lastIPC = 2;
    	double IPCSlope = (lastIPC - firstIPC) / num_hosts;
    	
    	for (int i = 0; i < num_hosts; i++)
    	{
    		maxFrequencies[i] = firstFrequency + frequencySlope * i;
    		maxVoltages[i] = firstVoltage + voltageSlope * i;
    		capacitances[i] = firstCapacitance + capacitanceSlope * i;
    		IPCs[i] = firstIPC + IPCSlope * i;
    	}
    	
    	/* END Variable parameters */
        
        int mips = 1500;
        
        for (int i = 0; i < num_hosts; i++)
        {
            //machine_mips[i] = mips_first + i * increase;
        	machine_mips[i] = maxFrequencies[i] * IPCs[i] / 1E6;
        	if (machine_mips[i] > maxMips)
        	{
        		maxMips = machine_mips[i];
        	}
        	if ((machine_mips[i] * percentages[0] / 100) < minMips)
        	{
        		minMips = (int) (machine_mips[i] * percentages[0] / 100);
        	}
        }
        
        if(variable_machines)
        {
        	GeneralParametersTasksFuzzy.setMaxHostMips(maxMips);
        	GeneralParametersVmsFuzzy.setMaxHostMips(maxMips);
        	GeneralParametersVmsFuzzy.setMinHostMips(minMips);
        }
        else
        {
        	GeneralParametersTasksFuzzy.setMaxHostMips(mips);
        	GeneralParametersVmsFuzzy.setMaxHostMips(mips);
        	GeneralParametersVmsFuzzy.setMinHostMips(mips);
        }
        
        for (int i = 1; i <= num_hosts; i++)
        {
            List<Pe> peList1 = new ArrayList<>();
            //double maxPower = 250; // 250W
    		//double staticPowerPercent = 0.7; // 70%
            
            boolean enableDVFS = true; // is the Dvfs enable on the host
    		ArrayList<Double> freqs = new ArrayList<>(); // frequencies available by the CPU
    		freqs.add(59.925); // frequencies are defined in %, it makes free to use Host MIPS like we want.
    		freqs.add(69.93);  // frequencies must be in increase order !
    		freqs.add(79.89);
    		freqs.add(89.89);
    		freqs.add(100.0);
    		// Define wich governor is used by each CPU
    		HashMap<Integer,String> govs = new HashMap<Integer,String>();
    		//govs.put(0, "performance"); // Select one governor for the simulation
    		//govs.put(0, "powersave");
    		govs.put(0, "ondemand");
    		//govs.put(0, "conservative");
    		
    		ConfigDvfs = new DvfsDatas();
			HashMap<String,Integer> tmp_HM_OnDemand = new HashMap<>();
			tmp_HM_OnDemand.put("up_threshold", 95);
			tmp_HM_OnDemand.put("sampling_down_factor", 100);
			HashMap<String,Integer> tmp_HM_Conservative = new HashMap<>();
			tmp_HM_Conservative.put("up_threshold", 80);
			tmp_HM_Conservative.put("down_threshold", 20);
			tmp_HM_Conservative.put("enablefreqstep", 0);
			tmp_HM_Conservative.put("freqstep", 5);
			HashMap<String,Integer> tmp_HM_UserSpace = new HashMap<>();
			tmp_HM_UserSpace.put("frequency", 3);
			ConfigDvfs.setHashMapOnDemand(tmp_HM_OnDemand);
			ConfigDvfs.setHashMapConservative(tmp_HM_Conservative);
			ConfigDvfs.setHashMapUserSpace(tmp_HM_UserSpace);
    		
            // 3. Create PEs and add these into the list.
            //for a quad-core machine, a list of 4 PEs is required:
			// need to store Pe id and MIPS Rating
			if(variable_machines)
			{
				peList1.add(new Pe(0, new PeProvisionerSimple(machine_mips[i - 1]), freqs, govs.get(0), ConfigDvfs));
			}
			else
			{
				peList1.add(new Pe(0, new PeProvisionerSimple(mips), freqs, govs.get(0), ConfigDvfs));
			}
            //peList1.add(new Pe(1, new PeProvisionerSimple(mips), freqs, govs.get(0), ConfigDvfs));//
			
            int hostId = i;
            int ram = 2048; //host memory (MB)
            long storage = 1000000; //host storage
            int bw = 10000;
            if(variable_machines)
            {
            	hostList.add
            	(
    	            new PowerHost
    	            (
    	                hostId,
    	                new RamProvisionerSimple(ram),
    	               	new BwProvisionerSimple(bw),
    	               	storage,
    	               	peList1,
    	               	new VmSchedulerTimeShared(peList1),							//<==
    	               	new PowerModelAnalytical
    	               	(
    	               		maxFrequencies[i - 1],
    	               		maxVoltages[i - 1],
    	               		dynamicConstant,
    	               		staticConstant,
    	               		capacitances[i - 1],
    	               		percentages,
    	               		IPCs[i - 1],
    	               		peList1
    	               	),
    					false,
    					enableDVFS
    	            )
    	        );
            }
            else
            {
	            hostList.add
	            (
	            	new PowerHost
	            	(
	                	hostId,
	                	new RamProvisionerSimple(ram),
	                	new BwProvisionerSimple(bw),
	                	storage,
	                	peList1,
	                	new VmSchedulerTimeShared(peList1),							//<==
	                	new PowerModelSpecPower_BAZAR_CAC(peList1),
						false,
						enableDVFS
	                )
	            );
            }
        }
        
        
        
        ArrayList<Double> mipsArray = new ArrayList<Double>();
        for (int i = 0; i < hostList.size(); i++)
        {
        	mipsArray.add(hostList.get(i).getPeList().get(0).getPeProvisioner().getMips() * percentages[0] / 100 * 0.975);
        }
   
        
        
        // 4. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      		// system architecture
        String os = "Linux";          	// operating system
        String vmm = "Xen";
        double time_zone = 10.0;        // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.1;	// the cost of using storage in this resource
        double costPerBw = 0.1;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<>();	//we are not adding SAN devices by now
        WorkflowDVFSDatacenter datacenter = null;
        
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);
        
        // 5. Finally, we need to create a storage object.
        /**
         * The bandwidth within a data center in MB/s.
         */
        int maxTransferRate = 15;// the number comes from the futuregrid site, you can specify your bw
        
        try
        {
            // Here we set the bandwidth to be 15MB/s
            HarddriveStorage s1 = new HarddriveStorage(name, 1e12);
            s1.setMaxTransferRate(maxTransferRate);
            storageList.add(s1);

            	datacenter = new WorkflowDVFSDatacenter
	            (
	            	name,
	            	characteristics,
	            	new PowerVmAllocationPolicySimpleWattPerMipsMetric(hostList),
	            	storageList,
	            	0.01
	            );

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return datacenter;
    }
    
    protected static List<CondorVM> createVM(int userId, int vms)
    {
        //Creates a container to store VMs. This list is passed to the broker later
        LinkedList<CondorVM> list = new LinkedList<>();
        //boolean random = true;
        //boolean variable_vms = true;
        //VM Parameters
        long size = 10000; //image size (MB)
        int ram = 512; //vm memory (MB)
        int mips = 1000;
        long bw = 1000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name
        double[] VMs = new double[vms];
        ArrayList<Double> mipsArray = new ArrayList<Double>();
        double max = 0;
        double min = Double.MAX_VALUE;
        int indexMax = 1;
        
        if(variable_vms && variable_machines)
        {
        
        	
        	for (int i = 0; i < vms; i++)
            {
            	if (mipsArray.get(i) > max)
            	{
            		max = mipsArray.get(i);
            		indexMax = i + 1;
            	}
            	if (mipsArray.get(i) < min)
            	{
            		min = mipsArray.get(i);
            	}
            }
        	GeneralParametersTasksFuzzy.setMaxVmMips(max);
        	GeneralParametersVmsFuzzy.setMaxVmMips(max);
        	GeneralParametersTasksFuzzy.setMinVmMips(min);
        	GeneralParametersVmsFuzzy.setMinVmMips(min);
        }
        
        if(variable_vms && !variable_machines)
        {
            double mipsPerVm = GeneralParametersTasksFuzzy.getMaxHostMips();
            GeneralParametersTasksFuzzy.setMaxVmMips(mipsPerVm);
            GeneralParametersVmsFuzzy.setMaxVmMips(mipsPerVm);
            //double totalVmMips = vms * mipsPerVm;
            double downScale = (mipsPerVm - (mipsPerVm / 3)) / vms;
            GeneralParametersTasksFuzzy.setMinVmMips(mipsPerVm - vms * downScale);
            GeneralParametersVmsFuzzy.setMinVmMips(mipsPerVm - vms * downScale);
            double totalMips = 0;
            
            for(int i = 0; i < vms; i++)
            {
            	VMs[i] = mipsPerVm - i * downScale;
            	totalMips = totalMips + VMs[i];
            }
            
            for (int i = 0; i < vms; i++)
            {
            	if (VMs[i] > max)
            	{
            		max = VMs[i];
            		indexMax = i;
            	}
            }
        }
        
        if (ExportCsv.doExport())
        {
        	ExportCsv.setIndex(indexMax);
        }
        
        //create VMs
        CondorVM[] vm = new CondorVM[vms];
        for (int i = 0; i < vms; i++)
        {
            double ratio = 1.0;
            /*if(random)
            {
            	vm[i] = new CondorVM(i, userId, VMs[i] * ratio, pesNumber, ram, bw, size, vmm,
	            		new CloudletSchedulerSpaceShared());
            }*/
            if(variable_vms && variable_machines)
            {
            	vm[i] = new CondorVM(i, userId, mipsArray.get(i) * ratio, pesNumber, ram, bw, size, vmm,
	            		new CloudletSchedulerSpaceShared());
            }
            if(variable_vms && !variable_machines)
            {
            	vm[i] = new CondorVM(i, userId, VMs[i] * ratio, pesNumber, ram, bw, size, vmm,
	            		new CloudletSchedulerSpaceShared());
            }
            if(!variable_vms)
            {
	            vm[i] = new CondorVM(i, userId, mips * ratio, pesNumber, ram, bw, size, vmm,
	            		new CloudletSchedulerSpaceShared());
            }
	        list.add(vm[i]);
        }
        return list;
    }
    
    /**
     * Prints the job objects
     *
     * @param list list of jobs
     */
    protected static void printJobList(List<Job> list)
    {
        //String indent = "    ";
        String indent = "\t";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Job ID" + indent + indent + "Task ID" + indent + indent + "STATUS" + indent + indent
        	+ "Data center ID" + indent + "VM ID" + indent + indent
        	+ "Time" + indent + indent + "Start Time" + indent + "Finish Time" + indent + "Depth");
        DecimalFormat dft = new DecimalFormat("###.##");
        for (Job job : list)
        {
        	Log.print(job.getCloudletId() + indent + indent);
            if (job.getClassType() == ClassType.STAGE_IN.value)
            {
            	Log.print("Stage-in");
                if (job.getCloudletStatus() == Cloudlet.SUCCESS)
                {
                	Log.print(indent + "SUCCESS");
                	Log.printLine(indent + indent + job.getResourceId() + indent + indent + job.getVmId()
                    	+ indent + indent + dft.format(job.getActualCPUTime())
                    	+ indent + indent + dft.format(job.getExecStartTime()) + indent + indent
                    	+ dft.format(job.getFinishTime()) + indent + indent + job.getDepth());
                }
                else if (job.getCloudletStatus() == Cloudlet.FAILED)
                {
                	Log.print("FAILED");
                	Log.printLine(indent + indent + job.getResourceId() + indent + indent + job.getVmId()
                    	+ indent + indent + dft.format(job.getActualCPUTime())
                    	+ indent + indent + dft.format(job.getExecStartTime()) + indent + indent
                    	+ dft.format(job.getFinishTime()) + indent + indent + job.getDepth());
                }
            }
            for (Task task : job.getTaskList())
            {
            	Log.print(task.getCloudletId());
                if (job.getCloudletStatus() == Cloudlet.SUCCESS)
                {
                	Log.print(indent + indent + "SUCCESS");
                	Log.printLine(indent + indent + job.getResourceId() + indent + indent + job.getVmId()
                    	+ indent + indent + dft.format(job.getActualCPUTime())
                    	+ indent + indent + dft.format(job.getExecStartTime()) + indent + indent
                    	+ dft.format(job.getFinishTime()) + indent + indent + job.getDepth());
                }
                else if (job.getCloudletStatus() == Cloudlet.FAILED)
                {
                	Log.print("FAILED");
                	Log.printLine(indent + indent + job.getResourceId() + indent + indent + job.getVmId()
                    	+ indent + indent + dft.format(job.getActualCPUTime())
                    	+ indent + indent + dft.format(job.getExecStartTime()) + indent + indent
                    	+ dft.format(job.getFinishTime()) + indent + indent + job.getDepth());
                }
            }
        }
    }
}