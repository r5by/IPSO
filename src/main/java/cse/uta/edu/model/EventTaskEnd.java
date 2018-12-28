
package cse.uta.edu.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EventTaskEnd {

    @SerializedName("Event")
    @Expose
    private String event;
    @SerializedName("Stage ID")
    @Expose
    private long stageID;
    @SerializedName("Stage Attempt ID")
    @Expose
    private long stageAttemptID;
    @SerializedName("Task Type")
    @Expose
    private String taskType;
    @SerializedName("Task End Reason")
    @Expose
    private TaskEndReason taskEndReason;
    @SerializedName("Task Info")
    @Expose
    private TaskInfo taskInfo;
    @SerializedName("Task Metrics")
    @Expose
    private TaskMetrics taskMetrics;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public long getStageID() {
        return stageID;
    }

    public void setStageID(long stageID) {
        this.stageID = stageID;
    }

    public long getStageAttemptID() {
        return stageAttemptID;
    }

    public void setStageAttemptID(long stageAttemptID) {
        this.stageAttemptID = stageAttemptID;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public TaskEndReason getTaskEndReason() {
        return taskEndReason;
    }

    public void setTaskEndReason(TaskEndReason taskEndReason) {
        this.taskEndReason = taskEndReason;
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    public TaskMetrics getTaskMetrics() {
        return taskMetrics;
    }

    public void setTaskMetrics(TaskMetrics taskMetrics) {
        this.taskMetrics = taskMetrics;
    }

}
