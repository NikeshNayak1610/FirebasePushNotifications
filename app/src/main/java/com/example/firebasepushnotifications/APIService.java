package com.example.firebasepushnotifications;

import com.example.firebasepushnotifications.Notification.MyResponse;
import com.example.firebasepushnotifications.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
     @Headers(
             {
                     "Content-Type:application/json",
                     "Authorization:key=AAAAx9sR_p8:APA91bFKH1tE2_zs3FJBC-IP2Aa-JlbFRATIgPqS4tJB-oEHQTm_ID0lbkEbHP9UuVYrqzQ7xxiT8-WUIhIXjOO2lS7SRQh_ZSSqW7fc3Uc4SStpcXa_0e16HCXkmkIbMMrQrSOC4hPF"
             }
     )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
