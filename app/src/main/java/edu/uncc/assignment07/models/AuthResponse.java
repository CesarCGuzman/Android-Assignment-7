package edu.uncc.assignment07.models;

import java.io.Serializable;

public class AuthResponse implements Serializable {
    String user_id, user_fullname, token;

    public AuthResponse() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_fullname() {
        return user_fullname;
    }

    public void setUser_fullname(String user_fullname) {
        this.user_fullname = user_fullname;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    /*

    {
    "status": "ok",
    "token": "wcUJRckNsODlsbkgyZUJS",
    "user_id": 1,
    "user_fullname": "Alice Smith"
}
     */
}
