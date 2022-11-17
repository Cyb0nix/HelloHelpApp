package com.cybonix.hellohelp.Model;

public class Shop {

    private String adresse;
    private String nom;
    private String numero;
    private String site;
    private String image;
    private String type;
    private String distance;

    public Shop(String adresse, String nom, String numero, String site, String image, String type) {
        this.adresse = adresse;
        this.nom = nom;
        this.numero = numero;
        this.site = site;
        this.image = image;
        this.type = type;
    }

    public Shop() {

    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numéro) {
        this.numero = numéro;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
