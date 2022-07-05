package rs.ac.bg.etf.contacttracing.rest;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rs.ac.bg.etf.contacttracing.BluetoothService;
import rs.ac.bg.etf.contacttracing.MainActivity;
import rs.ac.bg.etf.contacttracing.db.DailyKey;

public class RestService {
    private static final String BASE_URL = "http://192.168.1.7:4000/";

    private CentralServiceApi centralService;

    public RestService() {
        //za ovo isto treba dependancy - dodatne informacije o greskama
        //ako javlja gresku CLEARTEXT -> treba dodati xml resurs config
        //dodati i android:networkSecurityConfig="@xml/config" u manifest
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.addInterceptor(loggingInterceptor);

        OkHttpClient okHttpClient = okHttpClientBuilder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        //napraviti implementaciju u skladu sa opisom onog interfejsa
        centralService = retrofit.create(CentralServiceApi.class);
    }

    public void registerInfected(Context service, List<DailyKey> keys){
        String[]dailies=keys.stream().map(DailyKey::getDailyKey).collect(Collectors.toList()).toArray(new String[keys.size()]);
        Long[]dates=keys.stream().map(key->key.getDate().getTime()).collect(Collectors.toList()).toArray(new Long[keys.size()]);
        Log.d("jana",dailies.toString());
        Call<Void> call=centralService.register(dailies,dates);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(service, "call succeeded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(service, "call failed", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    public LiveData<List<RegisteredInfectedKey>> getInfected(Context service, long period){
        MutableLiveData<List<RegisteredInfectedKey>> infected=new MutableLiveData<>();
        Call<List<RegisteredInfectedKey>> call=centralService.getInfected(period);
        call.enqueue(new Callback<List<RegisteredInfectedKey>>() {
            @Override
            public void onResponse(Call<List<RegisteredInfectedKey>> call, Response<List<RegisteredInfectedKey>> response) {
                Toast.makeText(service, "call succeeded", Toast.LENGTH_SHORT).show();
                if (response.isSuccessful()) {
                    List<RegisteredInfectedKey> keys = response.body();
                    infected.postValue(keys);
                    Log.d("life-cycle", keys.toString());
                }
            }

            @Override
            public void onFailure(Call<List<RegisteredInfectedKey>> call, Throwable t) {
                Toast.makeText(service, "call failed", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
        return infected;
    }



    public void getCurrentWeather(double latitude, double longitude) {
//        Call<DiagnosedPositive> call = centralService
//                .getCurrentWeather(API_KEY, latitude, longitude, "metric");
//
//        //ovo pozivamo i ocekujemo podatak
//        call.enqueue(new Callback<DiagnosedPositive>() {
//            @Override
//            public void onResponse(
//                    @NonNull Call<DiagnosedPositive> call,
//                    @NonNull Response<DiagnosedPositive> response) {
//                if (response.isSuccessful()) {
//                    DiagnosedPositive currentWeatherModel = response.body();
//                    Log.d("life-cycle", currentWeatherModel.toString());
//                }
//            }
//
//            @Override
//            public void onFailure(
//                    @NonNull Call<DiagnosedPositive> call,
//                    @NonNull Throwable t) {
//
//            }
//        });
    }
}
