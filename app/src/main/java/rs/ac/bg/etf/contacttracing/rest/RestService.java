package rs.ac.bg.etf.contacttracing.rest;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rs.ac.bg.etf.contacttracing.MainActivity;

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

    public void doSth(MainActivity activity){
        Call<Void> call=centralService.proba();
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(activity, "call succeeded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(activity, "call failed", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
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
