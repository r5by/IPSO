
package cse.uta.edu.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ShuffleWriteMetrics {

    @SerializedName("Shuffle Bytes Written")
    @Expose
    private long shuffleBytesWritten;
    @SerializedName("Shuffle Write Time")
    @Expose
    private long shuffleWriteTime;
    @SerializedName("Shuffle Records Written")
    @Expose
    private long shuffleRecordsWritten;

    public long getShuffleBytesWritten() {
        return shuffleBytesWritten;
    }

    public void setShuffleBytesWritten(long shuffleBytesWritten) {
        this.shuffleBytesWritten = shuffleBytesWritten;
    }

    public long getShuffleWriteTime() {
        return shuffleWriteTime;
    }

    public void setShuffleWriteTime(long shuffleWriteTime) {
        this.shuffleWriteTime = shuffleWriteTime;
    }

    public long getShuffleRecordsWritten() {
        return shuffleRecordsWritten;
    }

    public void setShuffleRecordsWritten(long shuffleRecordsWritten) {
        this.shuffleRecordsWritten = shuffleRecordsWritten;
    }

}
