package rs.ac.bg.etf.contacttracing.rest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;

import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CentralServiceApi {
    @POST("infected/register")
    Call<Void> register(
            @Query("keys") String []keys,
            @Query("dates") Long []dates
            );

    @POST("infected/getInfected")
    Call<List<RegisteredInfectedKey>> getInfected(
            @Query("period") Long period
    );
}
