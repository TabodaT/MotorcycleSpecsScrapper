package com.example.scrapping.model;


public class User {
    private int id;
    private String fullname;
    private String email;
    private String password;

    public User(int id, String fullname, String email, String password) {
        this.id = id;
        this.fullname = fullname;
        this.email = email;
        this.password = password;
    }

    public User() {}

    @Override
    public String toString() {
        return this.id +" "+this.fullname+" "+this.email+" "+this.password;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
