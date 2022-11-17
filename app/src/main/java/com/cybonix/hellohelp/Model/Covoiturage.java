package com.cybonix.hellohelp.Model;

public class Covoiturage {
    private String postid;
    private String lieu_depart;
    private String lieu_arrive;
    private String heure;
    private String date;
    private String publisher;
    private String nbr_places;

    public Covoiturage(String lieu_depart, String lieu_arrive, String heure, String date, String postid, String publisher, String nbr_places) {
        this.lieu_depart = lieu_depart;
        this.lieu_arrive = lieu_arrive;
        this.heure = heure;
        this.date = date;
        this.postid = postid;
        this.publisher = publisher;
        this.nbr_places = nbr_places;

    }

    public Covoiturage() {
    }

    public String getLieu_depart() {
        return lieu_depart;
    }

    public void setLieu_depart(String lieu_départ) {
        this.lieu_depart = lieu_départ;
    }

    public String getLieu_arrive() {
        return lieu_arrive;
    }

    public void setLieu_arrive(String lieu_arrivé) {
        this.lieu_arrive = lieu_arrivé;
    }

    public String getHeure() {
        return heure;
    }

    public void setHeure(String heure) {
        this.heure = heure;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getNbr_places() {
        return nbr_places;
    }

    public void setNbr_places(String nbr_places) {
        this.nbr_places = nbr_places;
    }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }
}
