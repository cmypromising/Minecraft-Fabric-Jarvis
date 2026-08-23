package com.promising.jarvis.llm.deepseek;

import com.google.gson.annotations.SerializedName;

public class ContentResponseBody {
    @SerializedName("capability")
    private String capability;

    @SerializedName("type")
    private Integer type;
    @SerializedName("command")
    private String command;
    @SerializedName("additional_info")
    private String additionalInfo;

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public String getCapability() { return capability; }
    public void setCapability(String capability) { this.capability = capability; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    @Override
    public String toString() {
        return "ContentResponseBody{" +
                "type=" + type +
                ", command='" + command + '\'' +
                ", additionalInfo='" + additionalInfo + '\'' +
                '}';
    }
}
