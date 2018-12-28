
package cse.uta.edu.model;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RDDInfo {

    @SerializedName("RDD ID")
    @Expose
    private Long rDDID;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Scope")
    @Expose
    private String scope;
    @SerializedName("Callsite")
    @Expose
    private String callsite;
    @SerializedName("Parent IDs")
    @Expose
    private List<Long> parentIDs = new ArrayList<Long>();
    @SerializedName("Storage Level")
    @Expose
    private StorageLevel storageLevel;
    @SerializedName("Number of Partitions")
    @Expose
    private Long numberOfPartitions;
    @SerializedName("Number of Cached Partitions")
    @Expose
    private Long numberOfCachedPartitions;
    @SerializedName("Memory Size")
    @Expose
    private Long memorySize;
    @SerializedName("Disk Size")
    @Expose
    private Long diskSize;

    public Long getRDDID() {
        return rDDID;
    }

    public void setRDDID(Long rDDID) {
        this.rDDID = rDDID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getCallsite() {
        return callsite;
    }

    public void setCallsite(String callsite) {
        this.callsite = callsite;
    }

    public List<Long> getParentIDs() {
        return parentIDs;
    }

    public void setParentIDs(List<Long> parentIDs) {
        this.parentIDs = parentIDs;
    }

    public StorageLevel getStorageLevel() {
        return storageLevel;
    }

    public void setStorageLevel(StorageLevel storageLevel) {
        this.storageLevel = storageLevel;
    }

    public Long getNumberOfPartitions() {
        return numberOfPartitions;
    }

    public void setNumberOfPartitions(Long numberOfPartitions) {
        this.numberOfPartitions = numberOfPartitions;
    }

    public Long getNumberOfCachedPartitions() {
        return numberOfCachedPartitions;
    }

    public void setNumberOfCachedPartitions(Long numberOfCachedPartitions) {
        this.numberOfCachedPartitions = numberOfCachedPartitions;
    }

    public Long getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(Long memorySize) {
        this.memorySize = memorySize;
    }

    public Long getDiskSize() {
        return diskSize;
    }

    public void setDiskSize(Long diskSize) {
        this.diskSize = diskSize;
    }

}
