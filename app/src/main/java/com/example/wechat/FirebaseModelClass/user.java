package com.example.wechat.FirebaseModelClass;

public class user {
    String phone,profilePic,lastSeen,name;
    String fcmToken;

    public user(String phone, String profilePic, String lastSeen, String name, String fcmToken, Boolean online) {
        this.phone = phone;
        this.profilePic = profilePic;
        this.lastSeen = lastSeen;
        this.name = name;
        this.fcmToken = fcmToken;
        this.online = online;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    Boolean online;

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

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }



    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public user() {
    }

}


