
package cse.uta.edu.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EventStageEnd {

    @SerializedName("Event")
    @Expose
    private String event;
    @SerializedName("Stage Info")
    @Expose
    private StageInfo stageInfo;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public StageInfo getStageInfo() {
        return stageInfo;
    }

    public void setStageInfo(StageInfo stageInfo) {
        this.stageInfo = stageInfo;
    }

}
