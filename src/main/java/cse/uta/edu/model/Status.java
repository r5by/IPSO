
package cse.uta.edu.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Status {

    @SerializedName("Storage Level")
    @Expose
    private StorageLevel storageLevel;
    @SerializedName("Memory Size")
    @Expose
    private long memorySize;
    @SerializedName("Disk Size")
    @Expose
    private long diskSize;

    public StorageLevel getStorageLevel() {
        return storageLevel;
    }

    public void setStorageLevel(StorageLevel storageLevel) {
        this.storageLevel = storageLevel;
    }

    public long getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(long memorySize) {
        this.memorySize = memorySize;
    }

    public long getDiskSize() {
        return diskSize;
    }

    public void setDiskSize(long diskSize) {
        this.diskSize = diskSize;
    }

}
