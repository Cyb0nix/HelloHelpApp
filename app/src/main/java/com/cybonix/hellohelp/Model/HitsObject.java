package com.cybonix.hellohelp.Model;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@IgnoreExtraProperties
public class HitsObject {
    @SerializedName("hits")
    @Expose
    private HitsList hits;

    public HitsList getHits() {
        return hits;
    }

}
