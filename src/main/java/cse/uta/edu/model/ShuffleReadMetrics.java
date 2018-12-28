
package cse.uta.edu.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ShuffleReadMetrics {

    @SerializedName("Remote Blocks Fetched")
    @Expose
    private long remoteBlocksFetched;
    @SerializedName("Local Blocks Fetched")
    @Expose
    private long localBlocksFetched;
    @SerializedName("Fetch Wait Time")
    @Expose
    private long fetchWaitTime;
    @SerializedName("Remote Bytes Read")
    @Expose
    private long remoteBytesRead;
    @SerializedName("Local Bytes Read")
    @Expose
    private long localBytesRead;
    @SerializedName("Total Records Read")
    @Expose
    private long totalRecordsRead;

    public long getRemoteBlocksFetched() {
        return remoteBlocksFetched;
    }

    public void setRemoteBlocksFetched(long remoteBlocksFetched) {
        this.remoteBlocksFetched = remoteBlocksFetched;
    }

    public long getLocalBlocksFetched() {
        return localBlocksFetched;
    }

    public void setLocalBlocksFetched(long localBlocksFetched) {
        this.localBlocksFetched = localBlocksFetched;
    }

    public long getFetchWaitTime() {
        return fetchWaitTime;
    }

    public void setFetchWaitTime(long fetchWaitTime) {
        this.fetchWaitTime = fetchWaitTime;
    }

    public long getRemoteBytesRead() {
        return remoteBytesRead;
    }

    public void setRemoteBytesRead(long remoteBytesRead) {
        this.remoteBytesRead = remoteBytesRead;
    }

    public long getLocalBytesRead() {
        return localBytesRead;
    }

    public void setLocalBytesRead(long localBytesRead) {
        this.localBytesRead = localBytesRead;
    }

    public long getTotalRecordsRead() {
        return totalRecordsRead;
    }

    public void setTotalRecordsRead(long totalRecordsRead) {
        this.totalRecordsRead = totalRecordsRead;
    }

}
