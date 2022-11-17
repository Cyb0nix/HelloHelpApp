package com.cybonix.hellohelp.Model;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Query;

public interface ElasticSearch2API {

    @GET("_search")
    Call<HitsObject2> search(
            @HeaderMap Map<String, String> headers,
            @Query("default_operator") String operator,
            @Query("q") String query
    );
}
