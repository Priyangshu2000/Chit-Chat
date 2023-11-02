package com.example.wechat.FirebaseModelClass;

public class message {

    String phone,name,lastMsg,lastTime,hasSeen;

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public String getHasSeen() {
        return hasSeen;
    }

    public void setHasSeen(String hasSeen) {
        this.hasSeen = hasSeen;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public message() {
    }

    public message(String phone, String name, String lastMsg, String lastTime, String hasSeen) {
        this.phone = phone;
        this.name = name;
        this.lastMsg = lastMsg;
        this.lastTime = lastTime;
        this.hasSeen = hasSeen;
    }
}
