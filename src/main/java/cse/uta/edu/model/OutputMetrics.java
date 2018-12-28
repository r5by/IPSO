
package cse.uta.edu.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OutputMetrics {

    @SerializedName("Bytes Written")
    @Expose
    private long bytesWritten;
    @SerializedName("Records Written")
    @Expose
    private long recordsWritten;

    public long getBytesWritten() {
        return bytesWritten;
    }

    public void setBytesWritten(long bytesWritten) {
        this.bytesWritten = bytesWritten;
    }

    public long getRecordsWritten() {
        return recordsWritten;
    }

    public void setRecordsWritten(long recordsWritten) {
        this.recordsWritten = recordsWritten;
    }

}
