package rs.ac.bg.etf.contacttracing.rest;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RegisteredInfectedKey {
    public String dailyKey;
    public Long date;

    public RegisteredInfectedKey(String key, Long date) {
        this.dailyKey = key;
        this.date = date;
    }

    public RegisteredInfectedKey() { }

    @NonNull
    @Override
    public String toString() {
        GsonBuilder gsonBuilder = new GsonBuilder();
//        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        return gson.toJson(this);
    }
}
