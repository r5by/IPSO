package cse.uta.edu.IPSO;

import cse.uta.edu.Utils.Util;
import org.apache.log4j.Logger;

import java.util.ArrayList;

import javax.naming.directory.InvalidAttributesException;

/**
 * IPSO identified stages, with Wp, Ws and Wo properties
 * @author ruby
 *
 */
public class IPSOStage {
	private static final Logger LOG = Logger.getLogger(IPSOStage.class);
	//===============================
	//      Fileds
	//================================
	/* Newly assigned stage ID in Spark application's longest execution flow */
	private long id;

	/* stage metrics */
	private StageMetrics stageInfo;
	
	/* List of tasks in the stage */
	private ArrayList<TaskMetricsInStage> taskInfo = new ArrayList<TaskMetricsInStage>();
	
	/* Stage type */
	private IPSOStageTypes type = IPSOStageTypes.UNSET;
	
	/* Lazy initialization of IPSO */
	private IPSO ipso = null;
	
	//===============================
	//       Constructors	
	//================================
	public IPSOStage(long id) {
		this.id = id;
	} //NOTE: id for a stage is only used locally
	
	//===============================
	//       Interfaces
	//================================
	/**
	 * Return stage name
	 * @return
	 */
	public String getStageName() {
		return stageInfo.getStageName();
	}

	/** Return a unique key to retrieve the current stage from cache */
	public String getStageKey() { return Util.formStageKey(getStageName()); }
	
	/**
	 * Return duration of stage
	 * @return
	 */
	public long getDurationInMS() {
		return stageInfo.durationMS();
	}
	
	/**
	 * Return the stage's executorRunTime as the accumulation of all tasks' executorRunTime in millionSeconds
	 * @return
	 */
	public long getExecutorRunTimeInMS() {
		return stageInfo.getComputingTime();
	}
	
	/**
	 * Return number of tasks for this stage
	 * @return
	 */
	public long numOfTasks(){
		return stageInfo.getNumbOfTasks();
	}
	
	/**
	 * Return the IPSO type of this stage
	 * @return
	 */
	public IPSOStageTypes stageType() {
		long numOfTasks = numOfTasks();
		long NP = IPSOExprConfig.getInstance().NP();
		long MP = IPSOExprConfig.getInstance().MP();
		
		if( NP == 1 || NP == MP)
			type = IPSOStageTypes.UNSET;
		else {
			if(numOfTasks == NP )
				type = IPSOStageTypes.NTYPE;
			else if(numOfTasks == 1)
				type = IPSOStageTypes.STYPE;
			else
				type = IPSOStageTypes.MTYPE;
		}
		
		return type;
	}

	/**
	 * Return the ipso-related calculation results
	 * @return
	 */
	public synchronized IPSO IPSO() {
		if(ipso == null) {
			ipso = new IPSO();
			ipso.init();
		}
		return ipso;
	}
	
	//===============================
	//       Privates
	//================================
	protected class IPSO {
		
		/* IPSO scaling factors, in unit of milliseconds */
		long wp = 0;
		long ws = 0;
		long wo = 0;

		/**
		 * Called only after all stages are properly tagged
		 */
		void init() {
			if (AppStageTag.getInstance().isWoStage(getStageKey())) {
				wo += stageInfo.durationMS();
			} else {
				LOG.debug("Getting IPSO stage type informatino for stage: " + getStageKey());
				switch (AppStageTag.getInstance().get(getStageKey())) {
					case NTYPE:
						/*when number of tasks in stage is equal to N*/
						wp = stageInfo.durationMS();
						LOG.debug("The stage: " + getStageKey() + " is IPSO_N Type: Wp +=" + wp + " (ms)");

						//TODO: Improve to the task-level granularity implementation in future release
//				//hold the first task in executor ID n (count from 1) in the array index of n-1
//				long[] firstTaskInExecutorRunTimeArry = new long[IPSOExprConfig.getInstance().MP()];
//				for(TaskMetricsInStage task : taskInfo) {
//					int executorID = (int) task.getExecutorID();
//					long taskExecutorRunTime = task.getExecutorDeserializeTime();
//
//					//If the array isnot filled or it's not the largest, we will fill it with the task's executorRunTime (ms)
//					//NOTE: Here we assume the longest task is always the executor's first task
//					if(firstTaskInExecutorRunTimeArry[executorID - 1] == 0
//							|| firstTaskInExecutorRunTimeArry[executorID -1] < taskExecutorRunTime )
//						firstTaskInExecutorRunTimeArry[executorID - 1] = task.getExecutorRunTime();
//
//					long sumOfFirstTaskInExecutorRunTime = 0;
//					for(int i = 0; i < firstTaskInExecutorRunTimeArry.length; i++)
//						sumOfFirstTaskInExecutorRunTime += firstTaskInExecutorRunTimeArry[i];
//
//					//Formula: WP(N,m) = Sigma.ExecRunTime / (N- m) * N
//					wp = ((double) (getExecutorRunTimeInMS() - sumOfFirstTaskInExecutorRunTime))  /
//							((double) (IPSOExprConfig.getInstance().NP() - IPSOExprConfig.getInstance().MP())) *
//							((double) IPSOExprConfig.getInstance().NP());
//
//					//Formula: Ws(N,m) = Ws(N, 1) = stageDuration - Wp(N,1)
//					//NOTE: Only applicable if m = 1
//					if(IPSOExprConfig.getInstance().MP() == 1)
//						ws = (double) getDurationInMS() - wp;
//					else
//						ws = -1;
//
//					//Formula: Wo(N,m) = stageDuration(N,m) - Ws(N,1) - Wp(N,1)/m
//					//NOTE: Wo can only be calculated when m =1 (it's 0) otherwise it has to be based on previous experiments
//					if(IPSOExprConfig.getInstance().MP() == 1)
//						wo = 0;
//					else
//						wo = -1;
//				}

						break;
					case MTYPE:
						//When number of tasks are related to available executors
						if(IPSOExprConfig.getInstance().MP() == 1) { //If m=1, Wo is always 0.
							wo = 0;
							wp = stageInfo.durationMS();
						} else {
							wp = stageInfo.getComputingTime();
							wo = stageInfo.durationMS() - wp/IPSOExprConfig.getInstance().MP();
						}
						LOG.debug("The stage: " + getStageKey() + " is IPSO_M type: Wo += " + wo + "(ms); Wp += " + wp + "(ms)");

						break;
					case STYPE:
						// when number of tasks in stage is equal to 1
						ws = stageInfo.durationMS();
						LOG.debug("The stage: " + getStageKey() + " is IPSO_S Type: Ws += " + ws + "(ms)");

						break;
					case UNSET:
						LOG.error("Unexpected IPSO type unset error...");
						break;
				}
			}
		}
		
		public double wp() {
			return wp;
		}
		
		public double ws() {
			return ws;
		}
		
		public double wo() {
			return wo;
		}
	}

	//===============================
	//       Getters & Setters
	//================================
	
	public long getId() {
		return id;
	}

	public void addTaskInfo(TaskMetricsInStage pTask) throws InvalidAttributesException {
		if(pTask.getStageID() != id)
			throw new InvalidAttributesException("Task doesn't belong to the stage.");
		
		taskInfo.add(pTask);
	}
	
	public void addStageInfo(StageMetrics pInfo) throws InvalidAttributesException {
		if(pInfo.getID() != id)
			throw new InvalidAttributesException("Stage info doesn't match the stage.");
		
		this.stageInfo = pInfo;
	}
}
