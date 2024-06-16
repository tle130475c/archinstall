package com.tle130475c.archinstall.systeminfo;

import java.util.List;

public class UserAccount {
    private String realName;
    private String username;
    private String password;
    private List<String> groups;

    public UserAccount(String realName, String username, String password) {
        this.realName = realName;
        this.username = username;
        this.password = password;
        this.groups = List.of("wheel", "audio", "lp", "optical", "storage", "disk", "video", "power");
    }

    public UserAccount(String realName, String username, String password, List<String> groups) {
        this.realName = realName;
        this.username = username;
        this.password = password;
        this.groups = groups;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }
}
