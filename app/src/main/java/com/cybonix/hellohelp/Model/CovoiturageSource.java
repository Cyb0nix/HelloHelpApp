package com.cybonix.hellohelp.Model;


import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@IgnoreExtraProperties
public class CovoiturageSource {

    @SerializedName("_source")
    @Expose

    private Covoiturage covoiturage;

    public CovoiturageSource(Covoiturage covoiturage) {
        this.covoiturage = covoiturage;
    }

    public Covoiturage getCovoiturage() {
        return covoiturage;
    }

    public void setCovoiturage(Covoiturage covoiturage) {
        this.covoiturage = covoiturage;
    }
}
