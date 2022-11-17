package com.cybonix.hellohelp.Model;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@IgnoreExtraProperties
public class HitsList2 {

    @SerializedName("hits")
    @Expose
    private List<AnnonceSource> annonceIndex;


    public List<AnnonceSource> getAnnonceIndex() {
        return annonceIndex;
    }

}
