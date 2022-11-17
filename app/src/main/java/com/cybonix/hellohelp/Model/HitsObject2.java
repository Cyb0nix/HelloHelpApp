package com.cybonix.hellohelp.Model;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
@IgnoreExtraProperties
public class HitsObject2 {
    @SerializedName("hits")
    @Expose
    private HitsList2 hits;

        public HitsList2 getHits2() {
            return hits;
        }


}
