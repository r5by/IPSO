
package cse.uta.edu.model;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StageInfo {

    @SerializedName("Stage ID")
    @Expose
    private Long stageID;
    @SerializedName("Stage Attempt ID")
    @Expose
    private Long stageAttemptID;
    @SerializedName("Stage Name")
    @Expose
    private String stageName;
    @SerializedName("Number of Tasks")
    @Expose
    private Long numberOfTasks;
    @SerializedName("RDD Info")
    @Expose
    private List<RDDInfo> rDDInfo = new ArrayList<RDDInfo>();
    @SerializedName("Parent IDs")
    @Expose
    private List<Object> parentIDs = new ArrayList<Object>();
    @SerializedName("Details")
    @Expose
    private String details;
    @SerializedName("Submission Time")
    @Expose
    private Long submissionTime;
    @SerializedName("Completion Time")
    @Expose
    private Long completionTime;
    @SerializedName("Accumulables")
    @Expose
    private List<Accumulable> accumulables = new ArrayList<Accumulable>();

    public Long getStageID() {
        return stageID;
    }

    public void setStageID(Long stageID) {
        this.stageID = stageID;
    }

    public Long getStageAttemptID() {
        return stageAttemptID;
    }

    public void setStageAttemptID(Long stageAttemptID) {
        this.stageAttemptID = stageAttemptID;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public Long getNumberOfTasks() {
        return numberOfTasks;
    }

    public void setNumberOfTasks(Long numberOfTasks) {
        this.numberOfTasks = numberOfTasks;
    }

    public List<RDDInfo> getRDDInfo() {
        return rDDInfo;
    }

    public void setRDDInfo(List<RDDInfo> rDDInfo) {
        this.rDDInfo = rDDInfo;
    }

    public List<Object> getParentIDs() {
        return parentIDs;
    }

    public void setParentIDs(List<Object> parentIDs) {
        this.parentIDs = parentIDs;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Long getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(Long submissionTime) {
        this.submissionTime = submissionTime;
    }

    public Long getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(Long completionTime) {
        this.completionTime = completionTime;
    }

    public List<Accumulable> getAccumulables() {
        return accumulables;
    }

    public void setAccumulables(List<Accumulable> accumulables) {
        this.accumulables = accumulables;
    }

}
