package rs.ac.bg.etf.contacttracing.rest;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CentralServiceApi {

    //oo iza baznog url
    @POST("/")
    Call<Void> proba();


    @GET("weather")
    Call<DiagnosedPositive> getCurrentWeather(
            @Query("appid") String apiKey,
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("units") String units
    );
}
