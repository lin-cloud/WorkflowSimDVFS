package org.workflowsim.dvfs;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
import org.workflowsim.ClusteringEngine;
import org.workflowsim.Task;
import org.workflowsim.WorkflowEngine;
import org.workflowsim.WorkflowParser;
import org.workflowsim.WorkflowSimTags;
import org.workflowsim.planning.BasePlanningAlgorithm;
import org.workflowsim.planning.DHEFTPlanningAlgorithm;
import org.workflowsim.planning.HEFTPlanningAlgorithm;
import org.workflowsim.planning.RandomPlanningAlgorithm;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.Parameters.PlanningAlgorithm;
import org.workflowsim.utils.Verbose;

public class WorkflowPlannerDVFS extends PowerDatacenterBroker{
	/**
	 * WorkflowPlanner supports dynamic planning. In the future we will have global
	 * and static algorithm here. The WorkflowSim starts from WorkflowPlanner. It
	 * picks up a planning algorithm based on the configuration
	 *
	 * @author Weiwei Chen
	 * @since WorkflowSim Toolkit 1.0
	 * @date Apr 9, 2013
	 *
	 */
    /**
     * The task list.
     */
    protected List< Task> taskList;
    /**
     * The workflow parser.
     */
    protected WorkflowParser parser;
    /**
     * The associated clustering engine.
     */
    private int clusteringEngineId;
    private ClusteringEngine clusteringEngine;

    /**
     * Created a new WorkflowPlanner object.
     *
     * @param name name to be associated with this entity (as required by
     * Sim_entity class from simjava package)
     * @throws Exception the exception
     * @pre name != null
     * @post $none
     */
    public WorkflowPlannerDVFS(String name) throws Exception {
        this(name, 1);
    }

    public WorkflowPlannerDVFS(String name, int schedulers) throws Exception {
        super(name);

        setTaskList(new ArrayList<>());
        this.clusteringEngine = new ClusteringEngine(name + "_Merger_", schedulers);
        this.clusteringEngineId = this.clusteringEngine.getId();
        this.parser = new WorkflowParser(getClusteringEngine().getWorkflowEngine().getSchedulerId(0));

    }

    /**
     * Gets the clustering engine id
     *
     * @return clustering engine id
     */
    public int getClusteringEngineId() {
        return this.clusteringEngineId;
    }

    /**
     * Gets the clustering engine
     *
     * @return the clustering engine
     */
    public ClusteringEngine getClusteringEngine() {
        return this.clusteringEngine;
    }

    /**
     * Gets the workflow parser
     *
     * @return the workflow parser
     */
    public WorkflowParser getWorkflowParser() {
        return this.parser;
    }

    /**
     * Gets the workflow engine id
     *
     * @return the workflow engine id
     */
    public int getWorkflowEngineId() {
        return getClusteringEngine().getWorkflowEngineId();
    }

    /**
     * Gets the workflow engine
     *
     * @return the workflow engine
     */
    public WorkflowEngine getWorkflowEngine() {
        return getClusteringEngine().getWorkflowEngine();
    }

    /**
     * Processes events available for this Broker.
     *
     * @param ev a SimEvent object
     * @pre ev != null
     * @post $none
     */
    @Override
    public void processEvent(SimEvent ev) {
        switch (ev.getTag()) {
            case WorkflowSimTags.START_SIMULATION:
                getWorkflowParser().parse();
                setTaskList(getWorkflowParser().getTaskList());
                processPlanning();
                processImpactFactors(getTaskList());
                sendNow(getClusteringEngineId(), WorkflowSimTags.JOB_SUBMIT, getTaskList());
                break;
            case CloudSimTags.END_OF_SIMULATION:
                shutdownEntity();
                break;
            // other unknown tags are processed by this method
            default:
                processOtherEvent(ev);
                break;
        }
    }

    private void processPlanning() {
        if (Parameters.getPlanningAlgorithm().equals(PlanningAlgorithm.INVALID)) {
            return;
        }
        BasePlanningAlgorithm planner = getPlanningAlgorithm(Parameters.getPlanningAlgorithm());
        
        planner.setTaskList(getTaskList());
        planner.setVmList(getWorkflowEngine().getAllVmList());
        try {
            planner.run();
        } catch (Exception e) {
            Verbose.toPrint("Error in configuring scheduler_method");
            e.printStackTrace();
        }
    }

    /**
     * Switch between multiple planners. Based on planner.method
     *
     * @param name the SCHMethod name
     * @return the scheduler that extends BaseScheduler
     */
    private BasePlanningAlgorithm getPlanningAlgorithm(PlanningAlgorithm name) {
        BasePlanningAlgorithm planner;

        // choose which scheduler to use. Make sure you have add related enum in
        //Parameters.java
        switch (name) {
            //by default it is FCFS_SCH
            case INVALID:
                planner = null;
                break;
            case RANDOM:
                planner = new RandomPlanningAlgorithm();
                break;
            case HEFT:
                planner = new HEFTPlanningAlgorithm();
                break;
            case DHEFT:
                planner = new DHEFTPlanningAlgorithm();
                break;
            default:
                planner = null;
                break;
        }
        return planner;
    }

    /**
     * Add impact factor for each task. This is useful in task balanced
     * clustering algorithm It is for research purpose and thus it is optional.
     *
     * @param taskList all the tasks
     */
    private void processImpactFactors(List<Task> taskList) {
        List<Task> exits = new ArrayList<>();
        for (Task task : taskList) {
            if (task.getChildList().isEmpty()) {
                exits.add(task);
            }
        }
        double avg = 1.0 / exits.size();
        for (Task task : exits) {
            addImpact(task, avg);
        }
    }

    /**
     * Add impact factor for one particular task
     *
     * @param task, the task
     * @param impact , the impact factor
     */
    private void addImpact(Task task, double impact) {

        task.setImpact(task.getImpact() + impact);
        int size = task.getParentList().size();
        if (size > 0) {
            double avg = impact / size;
            for (Task parent : task.getParentList()) {
                addImpact(parent, avg);
            }
        }
    }

    /**
     * Overrides this method when making a new and different type of Broker.
     * This method is called by {@link #body()} for incoming unknown tags.
     *
     * @param ev a SimEvent object
     * @pre ev != null
     * @post $none
     */
    protected void processOtherEvent(SimEvent ev) {
        if (ev == null) {
            Verbose.toPrint(getName() + ".processOtherEvent(): " + "Error - an event is null.");
            return;
        }

        Verbose.toPrint(getName() + ".processOtherEvent(): "
                + "Error - event unknown by this DatacenterBroker.");
    }

    /**
     * Send an internal event communicating the end of the simulation.
     *
     * @pre $none
     * @post $none
     */
    protected void finishExecution() {
        //sendNow(getId(), CloudSimTags.END_OF_SIMULATION);
    }

    /*
     * (non-Javadoc)
     * @see cloudsim.core.SimEntity#shutdownEntity()
     */
    @Override
    public void shutdownEntity() {
        Verbose.toPrint(getName() + " is shutting down...");
    }

    /*
     * (non-Javadoc)
     * @see cloudsim.core.SimEntity#startEntity()
     */
    @Override
    public void startEntity() {
        Verbose.toPrint("Starting WorkflowSim " + Parameters.getVersion());
        Verbose.toPrint(getName() + " is starting...");
        schedule(getId(), 0, WorkflowSimTags.START_SIMULATION);
    }

    /**
     * Gets the task list.
     *
     * @return the task list
     */
    @SuppressWarnings("unchecked")
    public List<Task> getTaskList() {
        return (List<Task>) taskList;
    }

    /**
     * Sets the task list.
     *
     * @param taskList
     */
    protected void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }
}