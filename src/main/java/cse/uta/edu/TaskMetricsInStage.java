package cse.uta.edu;

public class TaskMetricsInStage {
	// ================================
	// Fields
	// ================================
		private long taskID;
		private long stageID;
		private long executorID;
		
		private String taskType;

		/* Task launch & finish time in milliseconds */
		private long submissionTime;
		private long completionTime;
		
		private long executorRunTime;
		private long jvmGCTime;
		private long executorDeserializeCpuTime;
		private long shuffleWriteTime;
		private long executorCpuTime;
		private long resultSerializationTime;
		private long executorDeserializeTime;
		
		//-===============================
		//       Constructor
		//================================
		public TaskMetricsInStage(long taskID, long stageID, long executorID, String taskType, long submissionTime,
				long completionTime, long executorRunTime, long jvmGCTime, long executorDeserializeCpuTime,
				long shuffleWriteTime, long executorCpuTime, long resultSerializationTime,
				long executorDeserializeTime) {
			super();
			this.taskID = taskID;
			this.stageID = stageID;
			this.executorID = executorID;
			this.taskType = taskType;
			this.submissionTime = submissionTime;
			this.completionTime = completionTime;
			this.executorRunTime = executorRunTime;
			this.jvmGCTime = jvmGCTime;
			this.executorDeserializeCpuTime = executorDeserializeCpuTime;
			this.shuffleWriteTime = shuffleWriteTime;
			this.executorCpuTime = executorCpuTime;
			this.resultSerializationTime = resultSerializationTime;
			this.executorDeserializeTime = executorDeserializeTime;
		}
		
		//-===============================
		//       Public 
		//================================
		public void taskInfo() {
			System.out.println(this.taskID + " | "
					+ this.stageID + " | "
					+ this.executorID + " | "
					+ this.taskType + " | "
					+ this.submissionTime + " | "
					+ this.completionTime + " | "
					+ this.executorRunTime + " | "
					+ this.jvmGCTime + " | "
					+ this.executorDeserializeCpuTime + " | "
					+ this.shuffleWriteTime + " | "
					+ this.executorCpuTime + " | "
					+ this.resultSerializationTime + " | "
					+ this.executorDeserializeTime);
		}
		
		
		//-===============================
		//       Getters
		//================================
		public long getTaskID() {
			return taskID;
		}
	
		public long getStageID() {
			return stageID;
		}
		public long getExecutorID() {
			return executorID;
		}
		public String getTaskType() {
			return taskType;
		}
		public long getSubmissionTime() {
			return submissionTime;
		}
		public long getCompletionTime() {
			return completionTime;
		}
		public long getExecutorRunTime() {
			return executorRunTime;
		}
		public long getJvmGCTime() {
			return jvmGCTime;
		}
		public long getExecutorDeserializeCpuTime() {
			return executorDeserializeCpuTime;
		}
		public long getShuffleWriteTime() {
			return shuffleWriteTime;
		}
		public long getExecutorCpuTime() {
			return executorCpuTime;
		}
		public long getResultSerializationTime() {
			return resultSerializationTime;
		}
		public long getExecutorDeserializeTime() {
			return executorDeserializeTime;
		}
}
