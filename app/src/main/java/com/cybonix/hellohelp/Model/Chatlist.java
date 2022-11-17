package com.cybonix.hellohelp.Model;

public class Chatlist {
    public String id;
    public String datesend;

    public Chatlist(String id,String datesend) {
        this.id = id;
        this.datesend = datesend;
    }


    public Chatlist() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDatesend() {
        return datesend;
    }

    public void setDatesend(String datesend) {
        this.datesend = datesend;
    }
}
