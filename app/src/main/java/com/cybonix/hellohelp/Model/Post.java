package com.cybonix.hellohelp.Model;

public class Post {
    private String postid;
    private String postimage;
    private String titre;
    private String description;
    private String publisher;
    private String lien;

    public Post(String postid, String postimage, String description, String publisher, String titre, String lien) {
        this.postid = postid;
        this.titre = titre;
        this.postimage = postimage;
        this.description = description;
        this.publisher = publisher;
        this.lien = lien;
    }

    public Post() {
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getLien() { return lien; }

    public void setLien(String lien) { this.lien = lien; }

    public String getTitre() { return titre; }

    public void setTitre(String titre) { this.titre = titre; }
}
