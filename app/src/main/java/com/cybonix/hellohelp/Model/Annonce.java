package com.cybonix.hellohelp.Model;

public class Annonce {

    private String annonceid;
    private String publisher;
    private String type;
    private String contenu;
    private String quartier;

    public Annonce(String annonceid, String publisher, String contenu,String type, String quartier) {
        this.annonceid = annonceid;
        this.publisher = publisher;
        this.quartier = quartier;
        this.type = type;
        this.contenu = contenu;
    }

    public Annonce() {
    }

    public String getAnnonceid() {
        return annonceid;
    }

    public void setAnnonceid(String annonceid) {
        this.annonceid = annonceid;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuartier() {
        return quartier;
    }

    public void setQuartier(String quartier) {
        this.quartier = quartier;
    }
}
