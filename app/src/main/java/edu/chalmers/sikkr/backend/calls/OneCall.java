package edu.chalmers.sikkr.backend.calls;

/**
 * Created by Mia on 02/10/14.
 */
public class OneCall {
    String callNumber, callName, callDate, isCallNew, callType;

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    public void setCallName(String callName) {
        this.callName = callName;
    }

    public void setCallDate(String callDate) {
        this.callDate = callDate;
    }

    public void setIsCallNew(String isCallNew) {
        this.isCallNew = isCallNew;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getCallNumber() {
        return callNumber;
    }

    public String getCallName() {
        return callName;
    }

    public String getCallDate() {
        return callDate;
    }

    public String getIsCallNew() {
        return isCallNew;
    }

    public String getCallType() {
        return callType;
    }
}
