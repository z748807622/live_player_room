package com.zjy.entity;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "permission")
public class Permission {

    private Integer permission_id;
    @Id
    private Integer user_id;
    private Integer canSpeak;
    private Integer canLogin;

    public Integer getPermission_id() {
        return permission_id;
    }

    public void setPermission_id(Integer permission_id) {
        this.permission_id = permission_id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Integer getCanSpeak() {
        return canSpeak;
    }

    public void setCanSpeak(Integer canSpeak) {
        this.canSpeak = canSpeak;
    }

    public Integer getCanLogin() {
        return canLogin;
    }

    public void setCanLogin(Integer canLogin) {
        this.canLogin = canLogin;
    }
}
