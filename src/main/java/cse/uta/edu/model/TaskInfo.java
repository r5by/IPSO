
package cse.uta.edu.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TaskInfo {

    @SerializedName("Task ID")
    @Expose
    private long taskID;
    @SerializedName("Index")
    @Expose
    private long index;
    @SerializedName("Attempt")
    @Expose
    private long attempt;
    @SerializedName("Launch Time")
    @Expose
    private long launchTime;
    @SerializedName("Executor ID")
    @Expose
    private long executorID;
    @SerializedName("Host")
    @Expose
    private String host;
    @SerializedName("Locality")
    @Expose
    private String locality;
    @SerializedName("Speculative")
    @Expose
    private boolean speculative;
    @SerializedName("Getting Result Time")
    @Expose
    private long gettingResultTime;
    @SerializedName("Finish Time")
    @Expose
    private long finishTime;
    @SerializedName("Failed")
    @Expose
    private boolean failed;
    @SerializedName("Killed")
    @Expose
    private boolean killed;
    @SerializedName("Accumulables")
    @Expose
    private List<Accumulable> accumulables = null;

    public long getTaskID() {
        return taskID;
    }

    public void setTaskID(long taskID) {
        this.taskID = taskID;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getAttempt() {
        return attempt;
    }

    public void setAttempt(long attempt) {
        this.attempt = attempt;
    }

    public long getLaunchTime() {
        return launchTime;
    }

    public void setLaunchTime(long launchTime) {
        this.launchTime = launchTime;
    }

    public long getExecutorID() {
        return executorID;
    }

    public void setExecutorID(long executorID) {
        this.executorID = executorID;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public boolean isSpeculative() {
        return speculative;
    }

    public void setSpeculative(boolean speculative) {
        this.speculative = speculative;
    }

    public long getGettingResultTime() {
        return gettingResultTime;
    }

    public void setGettingResultTime(long gettingResultTime) {
        this.gettingResultTime = gettingResultTime;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public boolean isKilled() {
        return killed;
    }

    public void setKilled(boolean killed) {
        this.killed = killed;
    }

    public List<Accumulable> getAccumulables() {
        return accumulables;
    }

    public void setAccumulables(List<Accumulable> accumulables) {
        this.accumulables = accumulables;
    }

}
