package edu.chalmers.sikkr.backend.calls;

/**
 * Created by Mia on 02/10/14.
 */
public class OneCall implements Comparable <OneCall> {
    String callNumber, callDate, isCallNew, contactID;
    int callType;

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    public void setCallDate(String callDate) {
        this.callDate = callDate;
    }

    public void setIsCallNew(String isCallNew) {
        this.isCallNew = isCallNew;
    }

    public void setCallType(int callType) { this.callType = callType; }

    public void setContactID(String contactID) { this.contactID = contactID; }

    public String getCallNumber() {
        return callNumber;
    }

    public String getCallDate() {
        return callDate;
    }

    public String getIsCallNew() {
        return isCallNew;
    }

    public int getCallType() {
        return callType;
    }

    public String getContactID() { return contactID; }


    @Override
    public int compareTo(OneCall anotherCall) {
        if (anotherCall == null) {
            throw new NullPointerException("another call is null");
        } else {
            long timeInMillis = Long.parseLong(this.getCallDate());
            long anotherTimeInMillis = Long.parseLong(anotherCall.getCallDate());

            if (timeInMillis > anotherTimeInMillis) {
                return -1;
            }
            if (timeInMillis == anotherTimeInMillis) {

                return 0;
            }

            return 1;
        }
    }
}
