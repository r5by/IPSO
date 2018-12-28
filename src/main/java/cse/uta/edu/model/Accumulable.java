
package cse.uta.edu.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 
 * TODO: The "value" filed is not properly deserialized which contains memory/storage info
 * "Value" filed from original json is commentted out because the 
 * feedback of this value depends on the execution: it's a long value of a list of values
 * 
 * @author ruby
 *
 */
public class Accumulable {

    @SerializedName("ID")
    @Expose
    private Long iD;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Value")
    @Expose
    private Long value;
    @SerializedName("Internal")
    @Expose
    private Boolean internal;
    @SerializedName("Count Failed Values")
    @Expose
    private Boolean countFailedValues;

    public Long getID() {
        return iD;
    }

    public void setID(Long iD) {
        this.iD = iD;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Boolean getInternal() {
        return internal;
    }

    public void setInternal(Boolean internal) {
        this.internal = internal;
    }

    public Boolean getCountFailedValues() {
        return countFailedValues;
    }

    public void setCountFailedValues(Boolean countFailedValues) {
        this.countFailedValues = countFailedValues;
    }

}
