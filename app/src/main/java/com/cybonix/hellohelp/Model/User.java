package com.cybonix.hellohelp.Model;

public class User {

    private String id;
    private String username;
    private String imageURL;
    private String status;
    private String quartier;
    private String pret_outil;
    private String pret_alimentaire;
    private String description;
    private String covoiturage;
    boolean isBlocked = false;
    private boolean isAdmin;
    private boolean isVerified;

    public User(String id, String username, String imageURL, Boolean isBlocked, Boolean verified, Boolean admin) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.isBlocked = isBlocked;
        this.isAdmin = admin;
        this.isVerified = verified;

    }

    public User() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String getQuartier() { return quartier; }

    public void setQuartier(String quartier) { this.quartier = quartier; }

    public String getPret_outil() { return pret_outil; }

    public void setPret_outil(String pret_outil) { this.pret_outil = pret_outil; }

    public String getPret_alimentaire() { return pret_alimentaire; }

    public void setPret_alimentaire(String pret_alimentaire) { this.pret_alimentaire = pret_alimentaire; }

    public String getCovoiturage() { return covoiturage; }

    public void setCovoiturage(String covoiturage) { this.covoiturage = covoiturage; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public boolean isBlocked() { return isBlocked; }

    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }
}
