package com.cybonix.hellohelp.Fragment;

import com.cybonix.hellohelp.Notifications.MyResponse;
import com.cybonix.hellohelp.Notifications.Sender;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAecNodqI:APA91bGM5FFt8t2dBIhZC0h7WyoP1bC8rGBcOlYvCjTK8nSediqaPfnL8v7esHaRAXNH_jz_Fh9rG0Mmi5qLqqdEe4OPk0htkecT17YVnCvWMN7RVGP-N69CHEjHb5fBzohaBMqmpgVf"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
