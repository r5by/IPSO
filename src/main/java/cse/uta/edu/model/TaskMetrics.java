
package cse.uta.edu.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TaskMetrics {

    @SerializedName("Executor Deserialize Time")
    @Expose
    private long executorDeserializeTime;
    @SerializedName("Executor Deserialize CPU Time")
    @Expose
    private long executorDeserializeCPUTime;
    @SerializedName("Executor Run Time")
    @Expose
    private long executorRunTime;
    @SerializedName("Executor CPU Time")
    @Expose
    private long executorCPUTime;
    @SerializedName("Result Size")
    @Expose
    private long resultSize;
    @SerializedName("JVM GC Time")
    @Expose
    private long jVMGCTime;
    @SerializedName("Result Serialization Time")
    @Expose
    private long resultSerializationTime;
    @SerializedName("Memory Bytes Spilled")
    @Expose
    private long memoryBytesSpilled;
    @SerializedName("Disk Bytes Spilled")
    @Expose
    private long diskBytesSpilled;
    @SerializedName("Shuffle Read Metrics")
    @Expose
    private ShuffleReadMetrics shuffleReadMetrics;
    @SerializedName("Shuffle Write Metrics")
    @Expose
    private ShuffleWriteMetrics shuffleWriteMetrics;
    @SerializedName("Input Metrics")
    @Expose
    private InputMetrics inputMetrics;
    @SerializedName("Output Metrics")
    @Expose
    private OutputMetrics outputMetrics;
    @SerializedName("Updated Blocks")
    @Expose
    private List<UpdatedBlock> updatedBlocks = null;

    public long getExecutorDeserializeTime() {
        return executorDeserializeTime;
    }

    public void setExecutorDeserializeTime(long executorDeserializeTime) {
        this.executorDeserializeTime = executorDeserializeTime;
    }

    public long getExecutorDeserializeCPUTime() {
        return executorDeserializeCPUTime;
    }

    public void setExecutorDeserializeCPUTime(long executorDeserializeCPUTime) {
        this.executorDeserializeCPUTime = executorDeserializeCPUTime;
    }

    public long getExecutorRunTime() {
        return executorRunTime;
    }

    public void setExecutorRunTime(long executorRunTime) {
        this.executorRunTime = executorRunTime;
    }

    public long getExecutorCPUTime() {
        return executorCPUTime;
    }

    public void setExecutorCPUTime(long executorCPUTime) {
        this.executorCPUTime = executorCPUTime;
    }

    public long getResultSize() {
        return resultSize;
    }

    public void setResultSize(long resultSize) {
        this.resultSize = resultSize;
    }

    public long getJVMGCTime() {
        return jVMGCTime;
    }

    public void setJVMGCTime(long jVMGCTime) {
        this.jVMGCTime = jVMGCTime;
    }

    public long getResultSerializationTime() {
        return resultSerializationTime;
    }

    public void setResultSerializationTime(long resultSerializationTime) {
        this.resultSerializationTime = resultSerializationTime;
    }

    public long getMemoryBytesSpilled() {
        return memoryBytesSpilled;
    }

    public void setMemoryBytesSpilled(long memoryBytesSpilled) {
        this.memoryBytesSpilled = memoryBytesSpilled;
    }

    public long getDiskBytesSpilled() {
        return diskBytesSpilled;
    }

    public void setDiskBytesSpilled(long diskBytesSpilled) {
        this.diskBytesSpilled = diskBytesSpilled;
    }

    public ShuffleReadMetrics getShuffleReadMetrics() {
        return shuffleReadMetrics;
    }

    public void setShuffleReadMetrics(ShuffleReadMetrics shuffleReadMetrics) {
        this.shuffleReadMetrics = shuffleReadMetrics;
    }

    public ShuffleWriteMetrics getShuffleWriteMetrics() {
        return shuffleWriteMetrics;
    }

    public void setShuffleWriteMetrics(ShuffleWriteMetrics shuffleWriteMetrics) {
        this.shuffleWriteMetrics = shuffleWriteMetrics;
    }

    public InputMetrics getInputMetrics() {
        return inputMetrics;
    }

    public void setInputMetrics(InputMetrics inputMetrics) {
        this.inputMetrics = inputMetrics;
    }

    public OutputMetrics getOutputMetrics() {
        return outputMetrics;
    }

    public void setOutputMetrics(OutputMetrics outputMetrics) {
        this.outputMetrics = outputMetrics;
    }

    public List<UpdatedBlock> getUpdatedBlocks() {
        return updatedBlocks;
    }

    public void setUpdatedBlocks(List<UpdatedBlock> updatedBlocks) {
        this.updatedBlocks = updatedBlocks;
    }

}
