
package cse.uta.edu.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StorageLevel {

    @SerializedName("Use Disk")
    @Expose
    private Boolean useDisk;
    @SerializedName("Use Memory")
    @Expose
    private Boolean useMemory;
    @SerializedName("Deserialized")
    @Expose
    private Boolean deserialized;
    @SerializedName("Replication")
    @Expose
    private Long replication;

    public Boolean getUseDisk() {
        return useDisk;
    }

    public void setUseDisk(Boolean useDisk) {
        this.useDisk = useDisk;
    }

    public Boolean getUseMemory() {
        return useMemory;
    }

    public void setUseMemory(Boolean useMemory) {
        this.useMemory = useMemory;
    }

    public Boolean getDeserialized() {
        return deserialized;
    }

    public void setDeserialized(Boolean deserialized) {
        this.deserialized = deserialized;
    }

    public Long getReplication() {
        return replication;
    }

    public void setReplication(Long replication) {
        this.replication = replication;
    }

}
