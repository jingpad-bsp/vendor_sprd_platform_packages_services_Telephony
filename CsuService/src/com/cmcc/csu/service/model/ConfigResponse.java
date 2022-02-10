package com.cmcc.csu.service.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ConfigResponse implements Parcelable {
    public static final Creator<ConfigResponse> CREATOR = new Creator<ConfigResponse>() {
        @Override
        public ConfigResponse createFromParcel(Parcel in) {
            return new ConfigResponse(in);
        }

        @Override
        public ConfigResponse[] newArray(int size) {
            return new ConfigResponse[size];
        }
    };
    // return value, 0:success； 1:fail;
    private int result;
    // configueration information
    private String configInfo;

    @Override
    public int describeContents() {
        return 0;
    }

    public ConfigResponse() {
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getConfigInfo() {
        return configInfo;
    }

    public void setConfigInfo(String configInfo) {
        this.configInfo = configInfo;
    }

    protected ConfigResponse(Parcel in) {
        result = in.readInt();
        configInfo = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(result);
        dest.writeString(configInfo);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConfigResponse{");
        sb.append("result=").append(result);
        sb.append(", configInfo='").append(configInfo).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
