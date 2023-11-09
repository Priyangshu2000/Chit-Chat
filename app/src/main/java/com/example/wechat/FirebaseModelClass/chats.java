package com.example.wechat.FirebaseModelClass;

public class chats {

    String msgId,msgReceiver,msgSender,msgTime,msgType,msgValue;
    String hasSeen;


    public chats() {
    }

    public String getHasSeen() {
        return hasSeen;
    }

    public void setHasSeen(String hasSeen) {
        this.hasSeen = hasSeen;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getMsgReceiver() {
        return msgReceiver;
    }

    public void setMsgReceiver(String msgReceiver) {
        this.msgReceiver = msgReceiver;
    }

    public String getMsgSender() {
        return msgSender;
    }

    public void setMsgSender(String msgSender) {
        this.msgSender = msgSender;
    }

    public String getMsgTime() {
        return msgTime;
    }

    public void setMsgTime(String msgTime) {
        this.msgTime = msgTime;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getMsgValue() {
        return msgValue;
    }

    public void setMsgValue(String msgValue) {
        this.msgValue = msgValue;
    }

    public chats(String msgId, String msgReceiver, String msgSender, String msgTime, String msgType, String msgValue, String hasSeen) {
        this.msgId = msgId;
        this.msgReceiver = msgReceiver;
        this.msgSender = msgSender;
        this.msgTime = msgTime;
        this.msgType = msgType;
        this.msgValue = msgValue;
        this.hasSeen=hasSeen;
    }
}
