
package cse.uta.edu.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EventApplicationStart {

    @SerializedName("Event")
    @Expose
    private String event;
    @SerializedName("App Name")
    @Expose
    private String appName;
    @SerializedName("App ID")
    @Expose
    private String appID;
    @SerializedName("Timestamp")
    @Expose
    private Long timestamp;
    @SerializedName("User")
    @Expose
    private String user;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

}
