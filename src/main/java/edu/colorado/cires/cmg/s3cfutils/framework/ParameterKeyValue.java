package edu.colorado.cires.cmg.s3cfutils.framework;

import com.amazonaws.services.cloudformation.model.Parameter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CloudFormation stack parameter key/value pair
 */
public class ParameterKeyValue {
    @JsonProperty("ParameterKey")
    private String parameterKey;
    @JsonProperty("ParameterValue")
    private String parameterValue;

    public ParameterKeyValue() {
    }

    public ParameterKeyValue(String parameterKey, String parameterValue) {
        this.parameterKey = parameterKey;
        this.parameterValue = parameterValue;
    }

    public String getParameterKey() {
        return this.parameterKey;
    }

    public void setParameterKey(String parameterKey) {
        this.parameterKey = parameterKey;
    }

    public String getParameterValue() {
        return this.parameterValue;
    }

    public ParameterKeyValue setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
        return this;
    }

    /**
     * Converts key/value to {@link Parameter}
     * @return {@link Parameter} generated from parameter key and parameter value
     */
    public Parameter toParameter() {
        return (new Parameter()).withParameterKey(this.parameterKey).withParameterValue(this.parameterValue);
    }

    public String toString() {
        return "ParameterKeyValue{parameterKey='" + this.parameterKey + "', parameterValue='" + this.parameterValue + "'}";
    }
}
