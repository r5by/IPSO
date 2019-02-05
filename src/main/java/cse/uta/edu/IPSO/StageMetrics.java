package cse.uta.edu.IPSO;

import org.apache.log4j.Logger;

public class StageMetrics {
	private static final Logger LOG = Logger.getLogger(StageMetrics.class);

	// ================================
	// Fields
	// ================================
	private long id;
	private long numbOfTasks;
	
	private String stageName;


	/* Stage launch & finish time in milliseconds */
	private long launchTime;
	private long finishTime;

	/* Stage computing time in ms */
	private long computingTime;
	/* Stage deserialization time in ms */
	private long deserializationTime;
	/* Stage JVM GC time in ms */
	private long jvmGCTime;

	// ================================
	// Constructor
	// ================================
	public StageMetrics(long id, String stageName, long numbOfTasks, long launchTime, long finishTime,long deserializationTime, long computingTime, long jvmGCTime) {
		super();
		this.id = id;
		this.stageName = stageName;
		this.numbOfTasks = numbOfTasks;
		this.launchTime = launchTime;
		this.finishTime = finishTime;
		this.computingTime = computingTime;
		this.deserializationTime = deserializationTime;
		this.jvmGCTime = jvmGCTime;
	}

	// ================================
	// Publics
	// ================================
	/**
	 * Duration of stage in seconds.
	 * 
	 * @return
	 */
	public double duration() {
		return this.durationMS() / 1000;
	}
	
	public long durationMS() {
		return (this.finishTime - this.launchTime);
	}
	
	/**
	 * Print out metrics in this stage as following format:
	 * 
	 * StageID | numberOfTasks | Duration(s) | Deserialization Time(ms) | Computing Time(ms) | GC Time (ms)
	 * 
	 * copy and past this out to excel for later processing.
	 * @return
	 */
	public void stageInfo() {
		LOG.info(this.id + " | "
				+ this.getStageName() + " | "
				+ this.getNumbOfTasks() + " | "
				+ this.getSubmissionTime() + " | "
				+ this.getCompletionTime() + " | "
				+ this.durationMS() + " | "
				+ this.getDeserializationTime() + " | "
				+ this.getComputingTime() + " | "
				+ this.getJvmGCTime());
	}
	
	/**
	 * Print only significant stages:
	 *   Duration of stage is greater > 500ms
	 * @return
	 */
	public void stageInfoSignificant() {
		if(this.duration() > 0.5 )
			LOG.info(this.id + " | "
					+ this.getNumbOfTasks() + " | "
					+ this.getSubmissionTime() + " | "
					+ this.getCompletionTime() + " | "
					+ this.duration() + " | "
					+ this.getDeserializationTime() + " | "
					+ this.getComputingTime());
	}

	// ================================
	// Getters & Setters
	// ================================
	public long getID() {
		return this.id;
	}

	public long getSubmissionTime() {
		return this.launchTime;
	}
	
	public long getCompletionTime() {
		return this.finishTime;
	}
	
	public String getStageName() {
		return stageName;
	}
	
	public long getNumbOfTasks() {
		return numbOfTasks;
	}

	public long getLaunchTime() {
		return launchTime;
	}

	public long getFinishTime() {
		return finishTime;
	}

	public long getComputingTime() {
		return computingTime;
	}

	public long getDeserializationTime() {
		return deserializationTime;
	}

	public long getJvmGCTime() {
		return jvmGCTime;
	}

	public void setJvmGCTime(long jvmGCTime) {
		this.jvmGCTime = jvmGCTime;
	}

}
