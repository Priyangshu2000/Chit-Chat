package com.example.wechat.FirebaseModelClass;

public class user {
    String phone,profilePic,lastSeen,name;
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

    public user(String name,String phone, String profilePic, String lastSeen, Boolean isOnline) {
        this.name=name;
        this.phone = phone;
        this.profilePic = profilePic;
        this.lastSeen = lastSeen;
        this.online = isOnline;
    }
}


